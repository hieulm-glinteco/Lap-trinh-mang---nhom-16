/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.ltmproject.net;

/**
 *
 * @author admin
 */
import com.mycompany.ltmproject.dao.GameSessionDAO;
import com.mycompany.ltmproject.dao.UserDAO;
import com.mycompany.ltmproject.model.GameSession;
import com.mycompany.ltmproject.model.User;
import java.net.*;
import java.io.*;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.List;
import org.cloudinary.json.JSONArray;
import org.cloudinary.json.JSONObject;

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

                    User user = UserDAO.getUserByUsername(username);

                    if (user != null && UserDAO.checkLogin(username, password)) {
                        isLoggedIn = true;
                        currentUser = username;

                        JSONObject response = new JSONObject();
                        response.put("status", "success");

                        JSONObject userJson = new JSONObject();
                        userJson.put("id", user.getId());
                        userJson.put("username", user.getUsername());
                        userJson.put("name", user.getName());
                        userJson.put("email", user.getEmail());
                        userJson.put("phone", user.getPhone());
                        userJson.put("totalRankScore", user.getTotalRankScore());

                        response.put("user", userJson);

                        out.println(response.toString());
                    } else {
                        System.out.println("fallllllll");
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


                    // Tạo JSON array chứa danh sách top
                    JSONArray jsonArray = new JSONArray();

                    for (int i = 0; i < users.size(); i++) {
                        var u = users.get(i);
                        JSONObject userJson = new JSONObject();
                        userJson.put("top", i + 1);
                        userJson.put("username", u.getUsername());
                        userJson.put("score", u.getTotalRankScore());
                        userJson.put("wins", UserDAO.countWins(u.getId()));
                        System.out.println(UserDAO.countWins(u.getId()) + " " + u.getId());
                        jsonArray.put(userJson);
                    }

                    // Gói trong 1 đối tượng JSON chính (tùy bạn có muốn hay không)
                    JSONObject response = new JSONObject();
                    response.put("status", "success");
                    response.put("ranking", jsonArray);

                    // Gửi JSON về client   
                    out.println(response.toString());
                    out.flush();
                    System.out.println("[SERVER] Sent ranking to client, total: " + users.size());
                }
                //xu ly yeu cau xem lich su
                else if (line.contains("\"action\":\"history\"")) {
                    JSONObject request = new JSONObject(line);
                    int userId = request.getInt("userId");

                    System.out.println("📥 Nhận yêu cầu xem lịch sử từ userId: " + userId);

                    GameSessionDAO gameSessionDAO = new GameSessionDAO();
                    UserDAO userDAO = new UserDAO();

                    List<GameSession> sessions = gameSessionDAO.getGameSessionById(userId);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

                    JSONArray historyArray = new JSONArray();

                    for (GameSession gs : sessions) {
                        JSONObject obj = new JSONObject();

                        String date = (gs.getStart() != null) ? dateFormat.format(gs.getStart()) : "";
                        String opponent = "Ẩn danh";
                        String status;
                        String score = gs.getPlayerscore1() + " - " + gs.getPlayerscore2();

                        // ✅ Xác định đối thủ
                        try {
                            if (gs.getPlayerid1() == userId) {
                                User opp = userDAO.getUserById(gs.getPlayerid2());
                                if (opp != null && opp.getUsername() != null) {
                                    opponent = opp.getUsername();
                                }
                            } else if (gs.getPlayerid2() == userId) {
                                User opp = userDAO.getUserById(gs.getPlayerid1());
                                if (opp != null && opp.getUsername() != null) {
                                    opponent = opp.getUsername();
                                }
                            }
                        } catch (Exception e) {
                            opponent = "Không xác định";
                        }

                        // ✅ Trạng thái trận đấu
                        if (gs.getWinner() == 0) {
                            status = "Draw";
                        } else if (gs.getWinner() == userId) {
                            status = "Win";
                        } else {
                            status = "Lose";
                        }

                        // ✅ Tạo object JSON
                        obj.put("date", date);
                        obj.put("opponent", opponent);
                        obj.put("status", status);
                        obj.put("score", score);

                        historyArray.put(obj);
                    }

                    // ✅ Gói kết quả trả về
                    JSONObject response = new JSONObject();
                    response.put("status", "success");
                    response.put("history", historyArray);

                    out.println(response.toString());
                    out.flush();

                    System.out.println("📤 Đã gửi lịch sử đấu cho userId " + userId);
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
