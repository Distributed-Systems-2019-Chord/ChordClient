package org.distributed.systems.chord.test;

import org.distributed.systems.chord.model.ChordNode;
import org.distributed.systems.chord.model.finger.FingerInterval;
import org.distributed.systems.chord.model.finger.FingerTable;
import org.distributed.systems.chord.service.FingerTableService;
import org.junit.Before;
import org.junit.Test;

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
        FingerInterval expectedInterval1 = new FingerInterval(2, 3);
        FingerInterval expectedInterval2 = new FingerInterval(3, 5);
        FingerInterval expectedInterval3 = new FingerInterval(5, 1);

        FingerTable fingerTableRes = service.initFingerTable(new ChordNode(1, "", 4));


        assertEquals(expectedInterval1.getStartKey(), fingerTableRes.getFingerList().get(0).getInterval().getStartKey());
        assertEquals(expectedInterval1.getEndKey(), fingerTableRes.getFingerList().get(0).getInterval().getEndKey());
        assertEquals(expectedInterval2.getStartKey(), fingerTableRes.getFingerList().get(1).getInterval().getStartKey());
        assertEquals(expectedInterval2.getEndKey(), fingerTableRes.getFingerList().get(1).getInterval().getEndKey());
        assertEquals(expectedInterval3.getStartKey(), fingerTableRes.getFingerList().get(2).getInterval().getStartKey());
        assertEquals(expectedInterval3.getEndKey(), fingerTableRes.getFingerList().get(2).getInterval().getEndKey());

    }
}
