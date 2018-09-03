package de.awelzel;

import de.awelzel.ip2loc.Ip2LocServlet;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;

public class Ipv4ToLongTest {

    private Ip2LocServlet servlet;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() {
        servlet = new Ip2LocServlet();
    }

    @Test
    public void testIpv4ToInt() {
        long result = servlet.ipv4ToLong("127.0.0.1");
        assertEquals(2130706433, result);
    }

    @Test
    public void testIpv4ToInt2() {
        long result = servlet.ipv4ToLong("95.90.207.146");
        assertEquals(1599786898, result);
    }
    @Test
    public void testIpv4ToInt3() {
        long result = servlet.ipv4ToLong("195.201.148.209");
        assertEquals(3284767953L, result);
    }

    @Test
    public void testIpv4ToIntIllegal1() {
        thrown.expect(IllegalArgumentException.class);
        servlet.ipv4ToLong("127.0.0");
    }
    @Test
    public void testIpv4ToIntIllegal2() {
        thrown.expect(IllegalArgumentException.class);
        servlet.ipv4ToLong("127.0.0.1.1");
    }
    @Test
    public void testIpv4ToIntIllegal3() {
        thrown.expect(IllegalArgumentException.class);
        servlet.ipv4ToLong("256.0.0.0");
    }
    @Test
    public void testIpv4ToIntIllegal4() {
        thrown.expect(IllegalArgumentException.class);
        servlet.ipv4ToLong("a.b.c.d");
    }
    @Test
    public void testIpv4ToIntIllegal5() {
        thrown.expect(IllegalArgumentException.class);
        servlet.ipv4ToLong(null);
    }
}
