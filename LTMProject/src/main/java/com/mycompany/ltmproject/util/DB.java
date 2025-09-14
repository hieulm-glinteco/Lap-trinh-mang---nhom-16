package com.mycompany.ltmproject.util;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class DB {
  private static String url, user, pass;
  static {
    try (InputStream in = DB.class.getResourceAsStream("/db.properties")) {
      Properties p = new Properties(); p.load(in);
      url = p.getProperty("db.url"); user = p.getProperty("db.user"); pass = p.getProperty("db.pass");
    } catch (Exception e) { throw new RuntimeException("Cannot load DB config", e); }
  }
  public static Connection get() throws Exception {
    return DriverManager.getConnection(url, user, pass);
  }
}
