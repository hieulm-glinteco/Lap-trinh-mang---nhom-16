/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.ltmproject.dao;

import com.mycompany.ltmproject.util.DB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author admin
 */
public class UserDAO {

    public static boolean checkLogin(String username, String password) {
        String sql = "SELECT * FROM user WHERE username=? AND password=?";
        try ( Connection conn = DB.get();  PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try ( ResultSet rs = ps.executeQuery()) {
                return rs.next();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    public static List<User> getAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM user";
        try ( Connection conn = DB.get();  PreparedStatement ps = conn.prepareStatement(sql);  ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                User u = new User(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("username"),
                        rs.getString("password")
                );
                users.add(u);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return users;
    }

    public static void main(String[] args) {
        boolean ok = checkLogin("quangcuong", "123456");
        System.out.println("Đăng nhập quangcuong/123456: " + (ok ? "Đúng" : "Sai"));
    }
}
