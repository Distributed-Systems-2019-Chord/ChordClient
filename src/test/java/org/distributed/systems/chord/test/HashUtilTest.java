package org.distributed.systems.chord.test;

import org.distributed.systems.chord.util.impl.HashUtil;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class HashUtilTest {

    @Test
    public void HashIpAddressTest(){
        String stringToHash = "127.0.0.1";
        String expected = "4b84b15bff6ee5796152495a230e45e3d7e947d9";

        HashUtil hashTest = new HashUtil();
        String actual = hashTest.hash(stringToHash);
        assertEquals(expected, actual);
    }

    @Test
    public void HashIpAddressWithPortNumberTest(){
        String stringToHash = "127.0.0.1:8080";
        String expected = "56852a5456d1b09e1eb11c0ca39d8fbce6480106";

        HashUtil hashTest = new HashUtil();
        String actual = hashTest.hash(stringToHash);
        assertEquals(expected, actual);
    }
}
