package com.mycompany.ltmproject.util;

import java.sql.*;

public class DBHealth {
  public static void main(String[] args) {
    try (Connection con = DB.get();
         PreparedStatement ps = con.prepareStatement("SELECT 1");
         ResultSet rs = ps.executeQuery()) {
      rs.next();
      System.out.println("DB OK  URL=" + con.getMetaData().getURL() +
          " | Product=" + con.getMetaData().getDatabaseProductName() +
          " " + con.getMetaData().getDatabaseProductVersion());
    } catch (Exception e) {
      System.err.println("DB FAIL");
      e.printStackTrace();
    }
  }
}
