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
        FingerTable table3 = new FingerTable(new ArrayList<>(3), 0);
        table3.addFinger(new Finger(4L, new FingerInterval(4L,5L), centralNode_0));
        table3.addFinger(new Finger(5L, new FingerInterval(5L,7L), centralNode_0));
        table3.addFinger(new Finger(7L, new FingerInterval(7L,3L), centralNode_0));

        // Suppose node 3 wants to find the successor of key 1
        // Closest_preceding_finger will lookup in fingertable of node 3 and will find that key 1 falls within
        // key interval 7 and 3. Expected result of this test is that centralNode_0 is the return value of
        // closest_preceding_finger

        // TODO: Also make a test if the id cannot be found in the finger table.

        // expected = centralNode_0.closest_preceding_finger(1);
        long expectedNodeId = 0L;

        // assertEquals(expectedNodeId, expected.getId());



    }
}
