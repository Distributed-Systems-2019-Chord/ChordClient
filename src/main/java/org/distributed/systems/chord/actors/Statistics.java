package org.distributed.systems.chord.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.dispatch.OnComplete;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.pattern.Patterns;
import akka.util.Timeout;
import com.typesafe.config.Config;
import org.distributed.systems.ChordStart;
import org.distributed.systems.chord.messaging.FindSuccessor;
import scala.concurrent.Await;
import scala.concurrent.Future;
import sun.awt.X11.XSystemTrayPeer;

import java.io.Serializable;
import java.time.Duration;
import java.util.Random;

public class Statistics extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    private Config config = getContext().getSystem().settings().config();
    private String centralNodeAddress;
    private ActorRef centralNode;
    public static final int m = 16; // Number of bits in key id's
    public static final long AMOUNT_OF_KEYS = Math.round(Math.pow(2, m));

    public Statistics() {
        String centralEntityAddress = config.getString("myapp.centralEntityAddress");
        String centralEntityAddressPort = config.getString("myapp.centralEntityPort");
        centralNodeAddress = "akka://ChordNetwork@" + centralEntityAddress + ":" + centralEntityAddressPort + "/user/ChordActor";
    }

    @Override
    public void preStart() throws Exception {
        Timeout timeout = Timeout.create(Duration.ofMillis(ChordStart.STANDARD_TIME_OUT));
        Future<ActorRef> centralNodeFuture = getContext().actorSelection(centralNodeAddress).resolveOne(timeout);
        centralNode = (ActorRef) Await.result(centralNodeFuture, timeout.duration());

        String port = config.getString("akka.remote.artery.canonical.port");
        System.out.println("my generated port:" + port);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .matchEquals("killbatch", msg -> {
                    //hard coded for now TODO
                    int amountOfNodesToKill = 3;
                    System.out.println("statistics actor gonna kill in batch");
                    Timeout timeout = Timeout.create(Duration.ofMillis(ChordStart.STANDARD_TIME_OUT));
                    long[] generatedKey= new long[1000];
                    ActorRef[] nodeToKill = new ActorRef[1000];

//                    TODO find unique nodes, otherwise he will trhow an out future timed out exception
                    for (int i = 0; i < amountOfNodesToKill; i++) {
                        boolean randomKeyIsCentral = true;
                        boolean duplicateNode = true;
                        while (randomKeyIsCentral || duplicateNode) {
//                          generate random key
                            Random rd = new Random();
                            generatedKey[i] = Math.floorMod(rd.nextLong(), AMOUNT_OF_KEYS);;

                            System.out.println("--------------------------");
                            System.out.println("generated key: " + generatedKey[i]);

//                         find successor of random key
                            Future<Object> findSuccessorFuture = Patterns.ask(centralNode, new FindSuccessor.Request(generatedKey[i], 0, getSelf(),0), timeout);
                            FindSuccessor.Reply rply = (FindSuccessor.Reply) Await.result(findSuccessorFuture, timeout.duration());

                            System.out.println("node key: " + rply.id);
                            System.out.println("node: " + rply.succesor.toString());
                            nodeToKill[i] = rply.succesor;
                            randomKeyIsCentral = centralNode.equals(nodeToKill[i]);
                            if (randomKeyIsCentral) {
                                System.out.println("Generatign new key, we dotn wanna kill the cnertal node");
                                Thread.sleep(100);
                            }else{
                                duplicateNode = false;
                                for (int j = 0; j < i; j++) {
                                    if (nodeToKill[i].equals(nodeToKill[j])){
                                        duplicateNode = true;
                                    }
                                }
                                if(duplicateNode){
                                    System.out.println("duplicate node, looking for new node");
                                    Thread.sleep(100);
                                }else {
                                    System.out.println("we will kill this node ");
                                }
                            }
                        }
                    }

//                    lets kill the node and see how long it takes to stabilize
                    for (int i = 0; i < amountOfNodesToKill; i++) {
//                        nodeToKill[i].tell("killActor", getSelf());
                        Future<Object> findSuccessorFuture = Patterns.ask(nodeToKill[i], "killActor", timeout);
                        FindSuccessor.Reply rply = (FindSuccessor.Reply) Await.result(findSuccessorFuture, timeout.duration());
                    }

//                    start timer
                    long start_time = System.currentTimeMillis();
                    System.out.println("---- retrieving new responsible ndoes ----");

                    for (int i = 0; i < amountOfNodesToKill; i++) {
                        boolean nodeIsNotStabilized = true;
                        while(nodeIsNotStabilized) {
//                         TODO ask network for generated key, see if it has foudn a new successor (stabilized)
                            Future<Object> findSuccessorFuture1 = Patterns.ask(centralNode, new FindSuccessor.Request(generatedKey[i], 0,getSelf(), 0), timeout);
                            FindSuccessor.Reply rply1 = (FindSuccessor.Reply) Await.result(findSuccessorFuture1, timeout.duration());

                            System.out.println("------------------------");
                            System.out.println("findign successor of key: " + generatedKey[i]);
                            System.out.println("node key: " + rply1.id);
                            System.out.println("node: " + rply1.succesor.toString());
                            System.out.println("old (killed) successor node: " + nodeToKill[i]);
                            ActorRef newNode = rply1.succesor;

                            if (nodeToKill[i].equals(newNode)) {
                                nodeIsNotStabilized = false;
                            }else{
                                System.out.println("Error: het netwerk heeft geen niewue node gevonden");
                                Thread.sleep(100);
                            }
                        }
                    }
                    long end_time = System.currentTimeMillis();
                    long millisToComplete = end_time - start_time;
                    System.out.println("time to stabilise: " + millisToComplete);
                })
                .matchEquals("getAverageLookupTime", msg -> {
                    //hard coded for now TODO
                    int amountOfKeys = 100;
                    System.out.println("statistics actor gonna get average lookup time in batch");
                    Timeout timeout = Timeout.create(Duration.ofMillis(ChordStart.STANDARD_TIME_OUT));
                    long[] generatedKey= new long[amountOfKeys];
                    long total_time = 0;


                    for (int i = 0; i < amountOfKeys; i++) {
//                          generate random key
                        Random rd = new Random();
                        generatedKey[i] = Math.floorMod(rd.nextLong(), AMOUNT_OF_KEYS);

                        System.out.println("--------------------------");
                        System.out.println("generated key: " + generatedKey[i]);


                        long start_time = System.currentTimeMillis();

//                         find successor of random key
                        Future<Object> findSuccessorFuture = Patterns.ask(centralNode, new FindSuccessor.Request(generatedKey[i], 0, getSelf(), 0), timeout);
                        FindSuccessor.Reply rply = (FindSuccessor.Reply) Await.result(findSuccessorFuture, timeout.duration());

                        long stop_time = System.currentTimeMillis();
                        total_time += stop_time - start_time;
                    }

                    System.out.println("average lookup time: " + total_time/amountOfKeys);
                })
                .matchEquals("getAverageHops", msg -> {
                    //hard coded for now TODO
                    int amountOfKeys = 5000;
                    System.out.println("statistics actor gonna get average lookup time in batch");
                    Timeout timeout = Timeout.create(Duration.ofMillis(ChordStart.STANDARD_TIME_OUT));
                    long[] generatedKey= new long[amountOfKeys];

                    long total_time = 0;
                    long totalAmountOfHops = 0;

                    for (int i = 0; i < amountOfKeys; i++) {
//                          generate random key
                        Random rd = new Random();
                        generatedKey[i] = Math.floorMod(rd.nextLong(), AMOUNT_OF_KEYS);

                        System.out.println("--------------------------");
                        System.out.println("generated key: " + generatedKey[i]);


                        long start_time = System.currentTimeMillis();

//                         find successor of random key
                        Future<Object> findSuccessorFuture = Patterns.ask(centralNode, new FindSuccessor.Request(generatedKey[i], 0, getSelf(), 0), timeout);
                        FindSuccessor.Reply rply = (FindSuccessor.Reply) Await.result(findSuccessorFuture, timeout.duration());

                        System.out.println("amount of hops: " + rply.amountOfHops);
                        totalAmountOfHops += rply.amountOfHops;

                        long stop_time = System.currentTimeMillis();
                        total_time += stop_time - start_time;
                    }

                    System.out.println("total hops: " + totalAmountOfHops);
                    System.out.println("average hops: " + totalAmountOfHops/(float)amountOfKeys);
                    System.out.println("average lookup time: " + total_time/(float)amountOfKeys);
                })
                .build();
    }
}
