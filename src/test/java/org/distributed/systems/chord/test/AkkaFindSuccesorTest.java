package org.distributed.systems.chord.test;

import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.testkit.javadsl.TestKit;
import org.distributed.systems.chord.actors.Node;
import org.distributed.systems.chord.messaging.NodeJoinMessage;
import org.distributed.systems.chord.model.ChordNode;
import org.distributed.systems.chord.model.finger.Finger;
import org.distributed.systems.chord.model.finger.FingerInterval;
import org.distributed.systems.chord.model.finger.FingerTable;
import org.junit.*;

import java.util.ArrayList;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.AbstractActor;
import java.time.Duration;

public class AkkaFindSuccesorTest {
    @Test
    public void closestPrecedingFinger(){
        ChordNode centralNode_0 = new ChordNode(0L, "localhost", 8080);
        ChordNode node_3 = new ChordNode(3L, "localhost", 8082);
        FingerTable table0 = new FingerTable(new ArrayList<>(3), 0);
        table0.addFinger(new Finger(1L, new FingerInterval(1L,2L), node_3));
        table0.addFinger(new Finger(2L, new FingerInterval(2L,4L), node_3));
        table0.addFinger(new Finger(4L, new FingerInterval(4L,0L), centralNode_0));

//        FingerTable table1 = new FingerTable(new ArrayList<>(3), 0);
//        table1.addFinger(new Finger(2L, new FingerInterval(2L,3L), node_3));
//        table1.addFinger(new Finger(3L, new FingerInterval(3L,5L), node_3));
//        table1.addFinger(new Finger(5L, new FingerInterval(5L,1L), centralNode_0));

//        ChordNode node_1 = new ChordNode(1L, "localhost", 8081);



    }
}
