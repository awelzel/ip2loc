package de.awelzel;

import de.awelzel.ip2loc.Ip2LocServlet;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

public class Ip2LocServletTest {

    private Ip2LocServlet servlet;
    private HttpServletRequest mockReq;
    private HttpServletResponse mockResp;
    private PrintWriter respPrintWriter;
    private ByteArrayOutputStream respBaos; /* Where the response goes */

    @Before
    public void setUp() throws ServletException, IOException {
        Path curPath = Paths.get("").toAbsolutePath();
        Path dbUrl = Paths.get("jdbc:sqlite:", curPath.toString(),"data", "ip2loc_test.db");
        servlet = new Ip2LocServlet();
        servlet.init("org.sqlite.JDBC", dbUrl.toString());


        mockReq = mock(HttpServletRequest.class);
        mockResp = mock(HttpServletResponse.class);
        respBaos = new ByteArrayOutputStream();
        respPrintWriter = new PrintWriter(respBaos, true);
        when(mockResp.getWriter()).thenReturn(respPrintWriter);
    }
    @After
    public void tearDown() {
        servlet.destroy();
    }

    @Test
    public void testDoGetNoIp() throws IOException {
        servlet.doGet(mockReq, mockResp);
        respPrintWriter.flush();
        verify(mockReq).getParameter("ip");
        verify(mockResp).setStatus(400);

        JSONObject obj = new JSONObject(respBaos.toString());

        assertFalse(obj.getBoolean("success"));
    }
    @Test
    public void testDoGetEmptyIp() throws IOException {
        when(mockReq.getParameter("ip")).thenReturn("");
        servlet.doGet(mockReq, mockResp);
        respPrintWriter.flush();
        verify(mockReq).getParameter("ip");
        verify(mockResp).setStatus(400);

        JSONObject obj = new JSONObject(respBaos.toString());

        assertFalse(obj.getBoolean("success"));
    }

    @Test
    public void testDoGetInvalidIp() throws IOException {
        when(mockReq.getParameter("ip")).thenReturn("funky");
        servlet.doGet(mockReq, mockResp);
        respPrintWriter.flush();
        verify(mockReq).getParameter("ip");
        verify(mockResp).setStatus(400);

        JSONObject obj = new JSONObject(respBaos.toString());

        assertFalse(obj.getBoolean("success"));
        JSONArray errors = (obj.getJSONArray("errors"));
        assertEquals(1, errors.length());
        String message = errors.getJSONObject(0).getString("message");
        assertEquals("invalid ip", message);
        JSONObject source = errors.getJSONObject(0).getJSONObject("source");
        assertEquals("ip", source.getString("parameter"));
    }

    @Test
    public void testDoGetGoodIp() throws IOException {
        when(mockReq.getParameter("ip")).thenReturn("8.8.8.8");
        servlet.doGet(mockReq, mockResp);
        respPrintWriter.flush();
        verify(mockReq).getParameter("ip");

        JSONObject obj = new JSONObject(respBaos.toString());
        assertTrue(obj.getBoolean("success"));
        assertFalse(obj.has("error_message"));
        JSONObject data = obj.getJSONObject("data");
        assertEquals("United States", data.getString("country_name"));
        assertEquals("Mountain View", data.getString("city_name"));
    }
}
