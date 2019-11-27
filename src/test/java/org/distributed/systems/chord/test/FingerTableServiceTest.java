package org.distributed.systems.chord.test;

import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import org.distributed.systems.ChordStart;
import org.distributed.systems.chord.model.ChordNode;
import org.distributed.systems.chord.model.finger.Finger;
import org.distributed.systems.chord.model.finger.FingerInterval;
import org.distributed.systems.chord.model.finger.FingerTable;
import org.distributed.systems.chord.service.FingerTableService;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;


import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class FingerTableServiceTest {

    // TODO mock ChordStart constants!
    private FingerTableService service = new FingerTableService();

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void calcFingerTableIndex() {
        long actual1 = service.startFinger(1, 1);
        long actual2 = service.startFinger(1, 2);
        long actual3 = service.startFinger(1, 3);

        long expected1 = 2;
        long expected2 = 3;
        long expected3 = 5;

        assertEquals(expected1, actual1);
        assertEquals(expected2, actual2);
        assertEquals(expected3, actual3);
    }

//    @Test
//    public void calcInterval() {
//        // BASED ON m = 160
//        FingerInterval expectedInterval1 = new FingerInterval(2, 3);
//        FingerInterval expectedInterval2 = new FingerInterval(3, 5);
//        FingerInterval expectedInterval3 = new FingerInterval(5, 9);
//
//        FingerTable fingerTableRes = service.initFingerTable(new ChordNode(1));
//
//
//        assertEquals(expectedInterval1.getStartKey(), fingerTableRes.getFingerList().get(0).getInterval().getStartKey());
//        assertEquals(expectedInterval1.getEndKey(), fingerTableRes.getFingerList().get(0).getInterval().getEndKey());
//        assertEquals(expectedInterval2.getStartKey(), fingerTableRes.getFingerList().get(1).getInterval().getStartKey());
//        assertEquals(expectedInterval2.getEndKey(), fingerTableRes.getFingerList().get(1).getInterval().getEndKey());
//        assertEquals(expectedInterval3.getStartKey(), fingerTableRes.getFingerList().get(2).getInterval().getStartKey());
//        assertEquals(expectedInterval3.getEndKey(), fingerTableRes.getFingerList().get(2).getInterval().getEndKey());
//    }
//
//    @Test
//    public void calcIntervalWithPassingFirstNode() {
//        // BASED ON m = 160
//        FingerInterval expectedInterval1 = new FingerInterval(ChordStart.AMOUNT_OF_KEYS - 1, 0);
//        FingerInterval expectedInterval2 = new FingerInterval(0, 2);
//        FingerInterval expectedInterval3 = new FingerInterval(2, 6);
//
//        FingerTable fingerTableRes = service.initFingerTable(new ChordNode(ChordStart.AMOUNT_OF_KEYS - 2));
//
//
//        assertEquals(expectedInterval1.getStartKey(), fingerTableRes.getFingerList().get(0).getInterval().getStartKey());
//        assertEquals(expectedInterval1.getEndKey(), fingerTableRes.getFingerList().get(0).getInterval().getEndKey());
//        assertEquals(expectedInterval2.getStartKey(), fingerTableRes.getFingerList().get(1).getInterval().getStartKey());
//        assertEquals(expectedInterval2.getEndKey(), fingerTableRes.getFingerList().get(1).getInterval().getEndKey());
//        assertEquals(expectedInterval3.getStartKey(), fingerTableRes.getFingerList().get(2).getInterval().getStartKey());
//        assertEquals(expectedInterval3.getEndKey(), fingerTableRes.getFingerList().get(2).getInterval().getEndKey());
//    }
}
