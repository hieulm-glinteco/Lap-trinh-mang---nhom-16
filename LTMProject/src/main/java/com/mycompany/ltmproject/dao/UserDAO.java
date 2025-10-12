/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.ltmproject.dao;

import com.mycompany.ltmproject.model.User;
import com.mycompany.ltmproject.util.DB;
import java.sql.Connection;
import java.sql.Date;
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
        String sql = "SELECT * FROM player WHERE username=? AND password=?";
        try (Connection conn = DB.get(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
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
        String sql = "SELECT * FROM player";
        try (Connection conn = DB.get(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                User u = new User(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getInt("totalRankScore"),
                        rs.getDate("dob")
                );
                users.add(u);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return users;
    }

    public static boolean usernameExist(String username) {
        String sql = "SELECT * FROM player WHERE username = ?";
        try (Connection conn = DB.get(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs != null) {
                    return false;
                } else {
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public static boolean register(String username, String password, String name, String dob, String email, String phone) {
        String sql = "INSERT INTO `player`(`name`, `username`, `password`, `email`, `phone`, `totalRankScore`, `dob`) VALUES (?, ?, ?,?,?,?,?) ";
        try (Connection conn = DB.get(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(2, username);
            ps.setString(3, password);
            ps.setString(1, name);
            ps.setString(4, email);
            ps.setString(5, phone);
            ps.setInt(6, 0);
            ps.setDate(7, Date.valueOf(dob));
            int row = ps.executeUpdate();
            return row == 1;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public static List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM player";

        try (
                Connection conn = DB.get(); PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                User user = new User(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getInt("totalRankScore"),
                        rs.getDate("dob")
                );
                users.add(user);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return users;
    }

    public static int countWins(int playerId) {
        String sql = "SELECT COUNT(*) FROM GameSession WHERE winner = ?";
        try (Connection conn = DB.get(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, playerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1); // Trả về số lượng trận thắng
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0; // Nếu không tìm thấy trận thắng nào
    }

    public static User getUserById(int id) {
        String sql = "SELECT * FROM player WHERE id = ?";
        User user = null;

        try (Connection conn = DB.get(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    user = new User();
                    user.setId(rs.getInt("id"));
                    user.setUsername(rs.getString("username"));
                    user.setPassword(rs.getString("password"));
                    user.setEmail(rs.getString("email"));
                    user.setPhone(rs.getString("phone"));
                    user.setName(rs.getString("name"));
                    user.setTotalRankScore(rs.getInt("totalRankScore"));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return user;
    }

    public static User getUserByUsername(String username) {
        try (Connection conn = DB.get()) {
            String sql = "SELECT * FROM player WHERE username = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                User u = new User();
                u.setId(rs.getInt("id"));
                u.setName(rs.getString("name"));
                u.setUsername(rs.getString("username"));
                u.setEmail(rs.getString("email"));
                u.setPhone(rs.getString("phone"));
                u.setTotalRankScore(rs.getInt("totalRankScore"));
                return u;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
