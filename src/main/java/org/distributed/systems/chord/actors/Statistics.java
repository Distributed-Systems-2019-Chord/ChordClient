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
    public static final int m = 12; // Number of bits in key id's
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
                .matchEquals("kill", msg -> {
                    System.out.println("statistics actor gonna kill");
                    Timeout timeout = Timeout.create(Duration.ofMillis(ChordStart.STANDARD_TIME_OUT));
                    long generatedKey = 1;

                    ActorRef nodeToKill = null;
                    boolean randomKeyIsCentral = true;
                    while(randomKeyIsCentral){
//                          generate random key
                        Random rd = new Random();
                        generatedKey = Math.floorMod(rd.nextLong(), AMOUNT_OF_KEYS);

                        System.out.println("generated key: " + generatedKey);

//                         find successor of random key
                        Future<Object> findSuccessorFuture = Patterns.ask(centralNode, new FindSuccessor.Request(generatedKey, 0), timeout);
                        FindSuccessor.Reply rply = (FindSuccessor.Reply) Await.result(findSuccessorFuture, timeout.duration());

                        System.out.println("returned key: " + rply.id);
                        System.out.println("returned node: " + rply.succesor.toString());
                        nodeToKill = rply.succesor;
                        randomKeyIsCentral = centralNode.equals(nodeToKill);
                        System.out.println("Generatign new key, we dotn wanna kill the cnertal node");
                        Thread.sleep(100);
                    }


//                    lets kill the node and see how long it takes to stabilize
                    nodeToKill.tell("killActor", getSelf());

//                    start timer
                    long start_time = System.currentTimeMillis();

//                    TODO ask network for generated key, see if it has foudn a new successor (stabilized)
                    Future<Object> findSuccessorFuture1 = Patterns.ask(centralNode, new FindSuccessor.Request(generatedKey, 0), timeout);
                    FindSuccessor.Reply rply1 = (FindSuccessor.Reply) Await.result(findSuccessorFuture1, timeout.duration());

                    System.out.println("returned key: " + rply1.id);
                    System.out.println("returned node: " + rply1.succesor.toString());
                    ActorRef newNode = rply1.succesor;

                    long end_time = System.currentTimeMillis();
                    long millisToComplete = end_time - start_time;
                    if (nodeToKill == newNode){
                        System.out.println("Error: het netwerk heeft geen niewue node gevonden");
                    }
                    System.out.println("time to stabilise: " + millisToComplete);
                })
                .matchEquals("killbatch", msg -> {
                    //hard coded for now TODO
                    int amountOfNodesToKill = 4;
                    System.out.println("statistics actor gonna kill in batch");
                    Timeout timeout = Timeout.create(Duration.ofMillis(ChordStart.STANDARD_TIME_OUT));
                    long[] generatedKey= new long[1000];
                    ActorRef[] nodeToKill = new ActorRef[1000];

//                    TODO find unique nodes, otherwise he will trhow an out future timed out exception
                    for (int i = 0; i < amountOfNodesToKill; i++) {
                        boolean randomKeyIsCentral = true;
                        while (randomKeyIsCentral) {
//                          generate random key
                            Random rd = new Random();
                            generatedKey[i] = Math.floorMod(rd.nextLong(), AMOUNT_OF_KEYS);

                            System.out.println("--------------------------");
                            System.out.println("generated key: " + generatedKey[i]);

//                         find successor of random key
                            Future<Object> findSuccessorFuture = Patterns.ask(centralNode, new FindSuccessor.Request(generatedKey[i], 0), timeout);
                            FindSuccessor.Reply rply = (FindSuccessor.Reply) Await.result(findSuccessorFuture, timeout.duration());

                            System.out.println("node key: " + rply.id);
                            System.out.println("node: " + rply.succesor.toString());
                            nodeToKill[i] = rply.succesor;
                            randomKeyIsCentral = centralNode.equals(nodeToKill[i]);
                            if (randomKeyIsCentral) {
                                System.out.println("Generatign new key, we dotn wanna kill the cnertal node");
                                Thread.sleep(100);
                            }else{
                                System.out.println("we will kill this node ");
                            }
                        }
                    }

//                    lets kill the node and see how long it takes to stabilize
                    for (int i = 0; i < amountOfNodesToKill; i++) {
                        nodeToKill[i].tell("killActor", getSelf());
                    }

//                    start timer
                    long start_time = System.currentTimeMillis();
                    System.out.println("---- retrieving new responsible ndoes ----");

                    for (int i = 0; i < amountOfNodesToKill; i++) {
//                    TODO ask network for generated key, see if it has foudn a new successor (stabilized)
                        Future<Object> findSuccessorFuture1 = Patterns.ask(centralNode, new FindSuccessor.Request(generatedKey[i], 0), timeout);
                        FindSuccessor.Reply rply1 = (FindSuccessor.Reply) Await.result(findSuccessorFuture1, timeout.duration());

                        System.out.println("------------------------");
                        System.out.println("findign successor of key: " + generatedKey[i]);
                        System.out.println("node key: " + rply1.id);
                        System.out.println("node: " + rply1.succesor.toString());
                        ActorRef newNode = rply1.succesor;

                        if (nodeToKill[i] == newNode) {
                            System.out.println("Error: het netwerk heeft geen niewue node gevonden");
//                            TODO wait till it has find a new node
                        }
                    }
                    long end_time = System.currentTimeMillis();
                    long millisToComplete = end_time - start_time;
                    System.out.println("time to stabilise: " + millisToComplete);
                })
                .matchEquals("fpowekfew", getValueMessage -> {

                })
                .build();
    }
}
