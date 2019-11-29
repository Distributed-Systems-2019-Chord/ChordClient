package org.distributed.systems.chord.test;

import org.distributed.systems.chord.util.impl.HashUtil;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HashUtilTest {

    @Test
    public void HashIpAddressTest() {
        String stringToHash = "127.0.0.1";
        Long expected = 3674185302L;

        HashUtil hashTest = new HashUtil();
        Long actual = hashTest.hash(stringToHash);
        assertEquals(expected, actual);
    }

    @Test
    public void HashIpAddressWithPortNumberTest() {
        String stringToHash = "127.0.0.1:8080";
        Long expected = 4219226621L;

        HashUtil hashTest = new HashUtil();
        Long actual = hashTest.hash(stringToHash);
        assertEquals(expected, actual);
    }
}
