/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.ltmproject.net;

/**
 *
 * @author admin
 */
import com.mycompany.ltmproject.dao.UserDAO;
import java.net.*;
import java.io.*;
import java.sql.Date;

public class SimpleServer {

    public static void main(String[] args) throws IOException {
        ServerSocket ss = new ServerSocket(8888);
        System.out.println("Server started!");
        while (true) {
            Socket s = ss.accept();
            System.out.println("Client connected");
            new Thread(() -> handleClient(s)).start();
        }
    }

    private static void handleClient(Socket s) {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream())); PrintWriter out = new PrintWriter(s.getOutputStream(), true)) {
            boolean isLoggedIn = false;
            String currentUser = null;
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println("Received: " + line);

                // Xử lý login
                if (line.contains("\"action\":\"login\"")) {
                    String username = extractValue(line, "username");
                    String password = extractValue(line, "password");
                    if (UserDAO.checkLogin(username, password)) {
                        isLoggedIn = true;
                        currentUser = username;
                        out.println("{\"status\":\"success\"}");
                    } else {
                        out.println("{\"status\":\"fail\"}");
                    }
                } else if (isLoggedIn && line.contains("\"action\":\"logout\"")) {
                    out.println("{\"status\":\"logged_out\"}");
                    break;
                } else if (line.contains("\"action\":\"register\"")) {
                    String username = extractValue(line, "username");
                    String password = extractValue(line, "password");
                    String name = extractValue(line, "fullname");
                    String dob = extractValue(line, "dob");
                    String email = extractValue(line, "email");
                    String phone = extractValue(line, "phone");

                    if (username.isEmpty() || password.isEmpty() || name.isEmpty() || email.isEmpty() || phone.isEmpty() || dob.isEmpty()) {
                        out.println("{\"status\":\"fail\",\"error\":\"missing_field\"}");
                        continue;
                    }
                    if (UserDAO.usernameExist(username)) {
                        out.println("{\"status\":\"fail\",\"error\":\"username_exists\"}");
                        continue;
                    }
                    boolean ok = UserDAO.register(username, password, name, dob, email, phone);
                    out.println(ok ? "{\"status\":\"success\"}" : "{\"status\":\"fail\",\"error\":\"db_error\"}");
                } else if (line.contains("\"action\":\"ranking\"")) {
                    var users = UserDAO.getAllUsers();

                    // Sắp xếp: điểm giảm dần, nếu bằng thì theo tên
                    users.sort(java.util.Comparator.comparingInt(com.mycompany.ltmproject.model.User::getTotalRankScore).reversed()
                            .thenComparing(com.mycompany.ltmproject.model.User::getName));

                    StringBuilder sb = new StringBuilder();
                    sb.append("[");
                    for (int i = 0; i < users.size(); i++) {
                        var u = users.get(i);
                        sb.append("{");
                        sb.append("\"top\":").append(i + 1).append(",");
                        sb.append("\"username\":\"").append(u.getUsername()).append("\",");
                        sb.append("\"score\":").append(u.getTotalRankScore());
                        sb.append("}");
                        if (i != users.size() - 1) {
                            sb.append(",");
                        }
                    }
                    sb.append("]");

                    out.println(sb.toString());
                }

            }
        } catch (Exception e) {
            System.out.println("Client disconnected unexpectedly: " + e.getMessage());
        } finally {
            try {
                s.close();
            } catch (Exception ex) {
            }
            System.out.println("Client disconnected!");
        }
    }

    private static String extractValue(String line, String key) {
        String pattern = "\"" + key + "\":\"";
        int idx = line.indexOf(pattern);
        if (idx == -1) {
            return "";
        }
        int start = idx + pattern.length();
        int end = line.indexOf("\"", start);
        return line.substring(start, end);
    }
}
