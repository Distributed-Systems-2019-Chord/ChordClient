package org.distributed.systems.chord.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.util.Timeout;
import com.typesafe.config.Config;
import org.distributed.systems.ChordStart;
import scala.concurrent.Await;
import scala.concurrent.Future;
import sun.awt.X11.XSystemTrayPeer;

import java.io.Serializable;
import java.time.Duration;
import java.util.Random;

class Statistics extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    private Config config = getContext().getSystem().settings().config();
    private String centralNodeAddress;
    private ActorRef centralNode;
    public static final int m = 3; // Number of bits in key id's
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
                .matchEquals("kill", putValueMessage -> {
                    long envVal;
                    String hostName = config.getString("akka.remote.artery.canonical.hostname");
                    String port = config.getString("akka.remote.artery.canonical.port");

                    Random rd = new Random(); // creating Random object
                    envVal = Math.floorMod(rd.nextLong(), AMOUNT_OF_KEYS);

                    long start_time = System.currentTimeMillis();
                    ActorRef nodeToKill = null;
//                    TODO find successor of rnaodm key
                    nodeToKill.tell("kill", getSelf());
//                    TODO ask network for generated key, see if it has foudn a new successor (stabilized)
                    ActorRef newNode = null;
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
