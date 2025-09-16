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
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            PrintWriter out = new PrintWriter(s.getOutputStream(), true)
        ) {
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
                }
                // Sau khi login thành công, nhận các lệnh khác của client
                else if (isLoggedIn && line.contains("\"action\":\"logout\"")) {
                    out.println("{\"status\":\"logged_out\"}");
                    break; // Thoát khỏi vòng lặp, sẽ đóng socket
                }
                // Ví dụ: lấy danh sách user online (tùy bạn tự hiện thực thêm)
                else if (isLoggedIn && line.contains("\"action\":\"get_online\"")) {
                    // Gửi danh sách online, ví dụ demo:
                    out.println("{\"users\":[{\"name\":\"cuong\"},{\"name\":\"quang\"}]}");
                }
                // ...các action khác
            }
        } catch (Exception e) {
            System.out.println("Client disconnected unexpectedly: " + e.getMessage());
        } finally {
            try { s.close(); } catch (Exception ex) {}
            System.out.println("Client disconnected!");
        }
    }

    private static String extractValue(String line, String key) {
        String pattern = "\"" + key + "\":\"";
        int idx = line.indexOf(pattern);
        if (idx == -1) return "";
        int start = idx + pattern.length();
        int end = line.indexOf("\"", start);
        return line.substring(start, end);
    }
}
