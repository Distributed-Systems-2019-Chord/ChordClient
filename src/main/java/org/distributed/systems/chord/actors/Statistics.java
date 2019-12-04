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

import javax.xml.crypto.dsig.keyinfo.KeyValue;
import java.io.Serializable;
import java.time.Duration;

class Statistics extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    private Config config = getContext().getSystem().settings().config();
    private String centralNodeAddress;
    private ActorRef centralNode;

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
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .matchEquals("kill", putValueMessage -> {
                    //genrate random hash, kill successor

                })
                .matchEquals("fpowekfew", getValueMessage -> {

                })
                .build();
    }
}
