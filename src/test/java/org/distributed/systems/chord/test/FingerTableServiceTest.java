package org.distributed.systems.chord.test;

import org.distributed.systems.chord.model.ChordNode;
import org.distributed.systems.chord.model.finger.Finger;
import org.distributed.systems.chord.model.finger.FingerInterval;
import org.distributed.systems.chord.model.finger.FingerTable;
import org.distributed.systems.chord.service.FingerTableService;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    @Test
    public void calcInterval() {
        FingerInterval interval1 = new FingerInterval(2, 3);
        FingerInterval interval2 = new FingerInterval(3, 5);
        FingerInterval interval3 = new FingerInterval(5, 1);

        Finger finger1 = new Finger(2, interval1, new ChordNode(1, "", 1));
        Finger finger2 = new Finger(3, interval2, new ChordNode(2, "", 2));
        Finger finger3 = new Finger(5, interval3, new ChordNode(3, "", 3));

        List<Finger> fingers = new ArrayList<>(Arrays.asList(finger1, finger2, finger3));

        FingerTable fingerTable = new FingerTable(fingers, 1);

        FingerTable fingerTableRes = service.initFingerTable(new ChordNode(1, "", 4));

//        FingerInterval actual1 = service.calcInterval(1);
//        FingerInterval actual2 = service.calcInterval(2);
//        FingerInterval actual3 = service.calcInterval(3);

        assertEquals(interval1.getStartKey(), fingerTableRes.getFingerList().get(0).getInterval().getStartKey());
        assertEquals(interval1.getEndKey(), fingerTableRes.getFingerList().get(0).getInterval().getEndKey());
        assertEquals(interval1.getStartKey(), fingerTableRes.getFingerList().get(1).getInterval().getStartKey());
        assertEquals(interval1.getEndKey(), fingerTableRes.getFingerList().get(1).getInterval().getEndKey());
        assertEquals(interval1.getStartKey(), fingerTableRes.getFingerList().get(2).getInterval().getStartKey());
        assertEquals(interval1.getEndKey(), fingerTableRes.getFingerList().get(2).getInterval().getEndKey());

    }
}
