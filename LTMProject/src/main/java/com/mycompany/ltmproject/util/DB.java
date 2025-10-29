package com.mycompany.ltmproject.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.InputStream;
import java.sql.Connection;
import java.util.Properties;

public class DB {
  private static HikariDataSource dataSource;

  static {
    try (InputStream in = DB.class.getResourceAsStream("/db.properties")) {
      Properties p = new Properties();
      p.load(in);

      String url = p.getProperty("db.url");
      String user = p.getProperty("db.user");
      String pass = p.getProperty("db.pass");

      HikariConfig cfg = new HikariConfig();
      cfg.setJdbcUrl(url);
      cfg.setUsername(user);
      cfg.setPassword(pass);
      cfg.setMaximumPoolSize(10);
      cfg.setMinimumIdle(2);
      cfg.setPoolName("LTMProjectPool");
      cfg.setConnectionTimeout(10000);
      cfg.setIdleTimeout(60000);
      cfg.setMaxLifetime(300000);

      dataSource = new HikariDataSource(cfg);
    } catch (Exception e) {
      throw new RuntimeException("Cannot initialize DB pool", e);
    }
  }

  public static Connection get() throws Exception {
    return dataSource.getConnection();
  }
}
