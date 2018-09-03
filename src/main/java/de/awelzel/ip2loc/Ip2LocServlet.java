package de.awelzel.ip2loc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;


public class Ip2LocServlet extends HttpServlet {

    private static Logger logger = LogManager.getLogger(Ip2LocServlet.class);

    private Database db;

    @Override
    public void init() throws ServletException {
        logger.info("Init Servlet");
        ServletContext ctx = getServletContext();
        Enumeration<String> ctxParams = ctx.getInitParameterNames();
        for (String s : Collections.list(ctxParams)) {
            logger.info(String.format("%s: %s", s, ctx.getInitParameter(s)));
        }
        String ip2locDbDriver = ctx.getInitParameter("ip2loc_db_driver");
        String ip2locDbUrl = ctx.getInitParameter("ip2loc_db_url");
        init(ip2locDbDriver, ip2locDbUrl);
    }

    public void init(String ip2locDbDriver, String ip2locDbUrl) throws ServletException {
        db = new Database(ip2locDbDriver, ip2locDbUrl);
        try {
            db.init();
        } catch (DatabaseError e) {
            throw new ServletException(e);
        }
    }

    @Override
    public void destroy() {
        db.destroy();
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        long s = System.currentTimeMillis();
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> error = new HashMap<>();
        List<Map<String, Object>> errors = new ArrayList<>();
        resp.setContentType("application/json");
        resp.setCharacterEncoding("utf-8");
        try {
            long ipInt = ipv4ToLong(req.getParameter("ip"));
            result.put("data", db.lookupIp(ipInt));
            result.put("success", true);
            resp.setStatus(200);
        } catch (IllegalArgumentException e) {
            resp.setStatus(400);

            Map<String, Object> source = new HashMap<>();
            source.put("parameter", "ip");
            error.put("source", source);
            error.put("status", "400");
            error.put("message", e.getMessage());
            errors.add(error);
        } catch (DatabaseError e) {
            resp.setStatus(500);

            error.put("message", e.getMessage());
            error.put("status", "500");
            errors.add(error);
        } finally {
            long e = System.currentTimeMillis();
            Map<String, Object> meta = new HashMap<>();
            meta.put("took_ms", (int)(e - s));
            result.put("_meta", meta);
        }
        if (errors.size() > 0)
            result.put("errors", errors);
        if (!result.containsKey("success"))
            result.put("success", false);

        String content = new JSONObject(result).toString(4);
        PrintWriter out = resp.getWriter();
        out.write(content);
        out.flush();
    }

    /**
     * Check that ip is a valid IP Address and convert to its integer value.
     *
     * @param ip
     * @return
     */
    public long ipv4ToLong(String ip) {
        if (ip == null || ip.length() == 0)
            throw new IllegalArgumentException("missing ip");
        String[] split = ip.split("\\.", 4);
        if (split.length != 4) {
            throw new IllegalArgumentException("invalid ip");
        }

        long ipValue = 0;
        for (int i = 0; i < 4; i++) {
            String s = split[i];
            long value = 0;
            try {
                value = Long.parseLong(s);
                if (value < 0 || value > 255)
                    throw new NumberFormatException();
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("invalid ip");
            }
            ipValue |= (value << (3 - i) * 8);
        }
        return ipValue;
    }
}
