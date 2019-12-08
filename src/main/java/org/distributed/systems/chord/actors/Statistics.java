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
                    long envVal;

//                    generate random key
                    Random rd = new Random();
                    envVal = Math.floorMod(rd.nextLong(), AMOUNT_OF_KEYS);
                    System.out.println("generated key: " + envVal);

//                    find successor of random key
                    Future<Object> findSuccessorFuture = Patterns.ask(centralNode, new FindSuccessor.Request(envVal, 0), timeout);
                    FindSuccessor.Reply rply = (FindSuccessor.Reply) Await.result(findSuccessorFuture, timeout.duration());

                    System.out.println("returned key: " + rply.id);
                    System.out.println("returned node: " + rply.succesor.toString());
                    ActorRef nodeToKill = rply.succesor;


//                    lets kill the node and see how long it takes to stabilize
                    nodeToKill.tell("killActor", getSelf());

//                    start timer
                    long start_time = System.currentTimeMillis();

//                    TODO ask network for generated key, see if it has foudn a new successor (stabilized)
                    Future<Object> findSuccessorFuture1 = Patterns.ask(centralNode, new FindSuccessor.Request(envVal, 0), timeout);
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
                .matchEquals("fpowekfew", getValueMessage -> {

                })
                .build();
    }
}
