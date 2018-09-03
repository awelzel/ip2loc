package de.awelzel.ip2loc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

class DatabaseError extends Exception {

    public DatabaseError(String s) {
        super(s);
    }

    public DatabaseError(Throwable e) {
        super(e);
    }
}

public class Database {

    private static Logger logger = LogManager.getLogger(Database.class);
    private static String[] columns = {
        "country_code",
        "country_name",
        "city_name",
        "latitude",
        "longitude",
    };

    private String driver;
    private String url;
    private DataSource ip2loc_db_pool = null;


   public Database(String driver, String url) {
       this.driver = driver;
       this.url = url;
   }

   public void init() throws DatabaseError {
       try {
           Class.forName(driver);
       } catch (ClassNotFoundException e) {
           throw new DatabaseError(e);
       }
       PoolProperties properties = new PoolProperties();
       properties.setUrl(url);
       properties.setDriverClassName(driver);
       ip2loc_db_pool = new DataSource(properties);

       try {
           if (!ip2loc_db_pool.getConnection().isValid(1)) {
               throw new DatabaseError("Could not create connection for " + ip2loc_db_pool);
           }
       } catch (SQLException e) {
           throw new DatabaseError(e);
       }
   }

    public Map<String, Object> lookupIp(long ipVal) throws DatabaseError {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Map<String, Object> data = new HashMap<>();
        try {
            conn = ip2loc_db_pool.getConnection();
            stmt = conn.prepareStatement(
                    "SELECT * FROM ipv4 " +
                    "WHERE ip_from = (SELECT max(ip_from) FROM ipv4 WHERE ip_from <= ?) " +
                    "AND ? <= ip_to LIMIT 1"
            );
            stmt.setLong(1, ipVal);
            stmt.setLong(2, ipVal);
            rs = stmt.executeQuery();
            while (rs.next()) {
                for (String s : columns)
                    data.put(s, rs.getObject(s));
            }
            return data;
        } catch (SQLException e) {
            logger.error("UGH:", e);
            throw new DatabaseError(e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ignored) { }
                ;
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException ignored) { }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ignored) { }
            }
        }
    }

    void destroy() {
        logger.info("Database.destroy()");
        if (ip2loc_db_pool != null)
            ip2loc_db_pool.close();;
    }
}
