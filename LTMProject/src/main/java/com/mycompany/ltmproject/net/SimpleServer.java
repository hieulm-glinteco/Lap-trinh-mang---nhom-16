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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.cloudinary.json.JSONArray;
import org.cloudinary.json.JSONObject;

public class SimpleServer {

    private static Map<Integer, PrintWriter> onlinePlayers = Collections.synchronizedMap(new HashMap<>());
    private static Map<Integer, String> onlineUsernames = Collections.synchronizedMap(new HashMap<>());

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
        Map<Integer, PrintWriter> clientOutputStreams = new HashMap<>();
            int clientUserId = -1;
            String clientUsername = "";
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
                        clientUserId = user.getId();
                        clientUsername = user.getUsername();

                        System.out.println("🟢 User " + username + " (ID: " + clientUserId + ") is now ONLINE");

                        // QUAN TRỌNG: Thêm vào map TRƯỚC khi broadcast
                        onlinePlayers.put(clientUserId, out);
                        onlineUsernames.put(clientUserId, clientUsername);

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
                        out.flush();

                        // GỬI response thành công TRƯỚC, sau đó mới broadcast
                        try {
                            Thread.sleep(100); // Chờ 100ms để client kịp setup listener
                        } catch (InterruptedException e) {
                        }

                        // Bây giờ mới broadcast
                        broadcastOnlineStatus();

                    } else {
                        System.out.println("Login failed for username: " + username);
                        out.println("{\"status\":\"fail\"}");
                    }
                } else if (isLoggedIn && line.contains("\"action\":\"logout\"")) {
                    if (clientUserId != -1) {
                        onlinePlayers.remove(clientUserId);
                        onlineUsernames.remove(clientUserId);
                        System.out.println("🔴 User " + clientUsername + " logged out");
                        broadcastOnlineStatus();
                    }
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
                } //xu ly yeu cau xem lich su
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
                } else if (line.contains("\"action\":\"getOnlinePlayers\"")) {
                    handleGetOnlinePlayers(out, clientUserId);
                } else if (line.contains("\"action\":\"sendInvite\"")) {
                    handleSendInvite(line, out);
                }

            }
        } catch (Exception e) {
            System.out.println("Client disconnected unexpectedly: " + e.getMessage());
        } finally {
            if (clientUserId != -1) {
                onlinePlayers.remove(clientUserId);
                onlineUsernames.remove(clientUserId);
                isListening.remove(clientUserId);
                System.out.println("🔴 User " + clientUsername + " (ID: " + clientUserId + ") disconnected");

                // Broadcast để thông báo cho tất cả client khác biết user này offline
                broadcastOnlineStatus();
            }
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

    private static void handleGetOnlinePlayers(PrintWriter out, int currentUserId) {
        JSONArray playersArray = new JSONArray();

        for (Map.Entry<Integer, String> entry : onlineUsernames.entrySet()) {
            int userId = entry.getKey();
            String username = entry.getValue();

            // QUAN TRỌNG: Không thêm chính người dùng hiện tại
            if (userId != currentUserId) {
                JSONObject playerObj = new JSONObject();
                playerObj.put("id", userId);
                playerObj.put("username", username);
                playersArray.put(playerObj);
            }
        }

        JSONObject response = new JSONObject();
        response.put("status", "success");
        response.put("players", playersArray);
        response.put("count", playersArray.length());

        out.println(response.toString());
        out.flush();
        System.out.println("📤 Sent online players list. Count: " + playersArray.length());
    }

    private static void handleSendInvite(String line, PrintWriter senderOut) {
        try {
            JSONObject request = new JSONObject(line);
            int toUserId = request.getInt("toUserId");
            int fromUserId = request.getInt("fromUserId");
            String fromUsername = request.getString("fromUsername");

            PrintWriter targetOut = onlinePlayers.get(toUserId);

            if (targetOut != null) {
                // Gửi thông báo cho người nhận
                JSONObject invitation = new JSONObject();
                invitation.put("type", "invitation");
                invitation.put("fromUserId", fromUserId);
                invitation.put("fromUsername", fromUsername);

                targetOut.println(invitation.toString());
                targetOut.flush();

                // Thông báo thành công cho người gửi
                JSONObject response = new JSONObject();
                response.put("status", "success");
                response.put("message", "Invitation sent");
                senderOut.println(response.toString());
                senderOut.flush();

                System.out.println("💌 Invitation sent from " + fromUsername + " to userId " + toUserId);
            } else {
                // Người nhận offline
                JSONObject response = new JSONObject();
                response.put("status", "fail");
                response.put("error", "user_offline");
                senderOut.println(response.toString());
                senderOut.flush();

                System.out.println("❌ User " + toUserId + " is offline");
            }
        } catch (Exception e) {
            System.err.println("⚠️ Error handling invite: " + e.getMessage());
        }
    }

    // Thêm 1 Map để track client nào đang lắng nghe
    private static Map<Integer, Boolean> isListening = Collections.synchronizedMap(new HashMap<>());

// Khi client kết nối listener (từ OnlineController)
// Server biết client này đang lắng nghe
// (Bạn cần thêm action "startListening" ở server)
    private static void broadcastOnlineStatus() {
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
        }

        JSONArray playersArray = new JSONArray();

        for (Map.Entry<Integer, String> entry : onlineUsernames.entrySet()) {
            JSONObject playerObj = new JSONObject();
            playerObj.put("id", entry.getKey());
            playerObj.put("username", entry.getValue());
            playersArray.put(playerObj);
        }

        JSONObject notification = new JSONObject();
        notification.put("type", "online_update");
        notification.put("players", playersArray);
        notification.put("count", playersArray.length());

        // Chỉ gửi cho client đang lắng nghe (có listener socket)
        for (Map.Entry<Integer, PrintWriter> entry : onlinePlayers.entrySet()) {
            if (isListening.getOrDefault(entry.getKey(), false)) {
                entry.getValue().println(notification.toString());
                entry.getValue().flush();
            }
        }
    }
}
