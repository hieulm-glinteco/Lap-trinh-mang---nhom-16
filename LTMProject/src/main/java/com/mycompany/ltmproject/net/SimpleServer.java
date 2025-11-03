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
    // Writer của các kết nối listener (realtime)
    private static Map<Integer, PrintWriter> listenerStreams = Collections.synchronizedMap(new HashMap<>());
    // Tạm quản lý điểm và vòng theo session
    private static Map<Integer, Integer> sessionScoreP1 = Collections.synchronizedMap(new HashMap<>());
    private static Map<Integer, Integer> sessionScoreP2 = Collections.synchronizedMap(new HashMap<>());
    private static Map<Integer, Integer> sessionHostUserId = Collections.synchronizedMap(new HashMap<>());
    private static Map<Integer, Integer> sessionGuestUserId = Collections.synchronizedMap(new HashMap<>());

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
                } else if (line.contains("\"action\":\"startListening\"")) {
                    try {
                        JSONObject req = new JSONObject(line);
                        int userId = req.getInt("userId");
                        // Đăng ký writer của kết nối hiện tại làm listener của user này
                        listenerStreams.put(userId, out);
                        isListening.put(userId, true);
                        JSONObject resp = new JSONObject();
                        resp.put("status", "ok");
                        out.println(resp.toString());
                        out.flush();
                        System.out.println("👂 Now listening: userId=" + userId);
                    } catch (Exception ex) {
                        System.err.println("⚠️ Error startListening: " + ex.getMessage());
                    }
                } else if (line.contains("\"action\":\"acceptInvite\"")) {
                    handleAcceptInvite(line, out);
                } else if (line.contains("\"action\":\"requestRound\"")) {
                    handleRequestRound(line, out);
                } else if (line.contains("\"action\":\"submitAnswers\"")) {
                    handleSubmitAnswers(line);
                } else if (line.contains("\"action\":\"endGame\"")) {
                    handleEndGame(line);
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

            // Ưu tiên gửi qua listener stream để client listener nhận được
            PrintWriter targetOut = listenerStreams.get(toUserId);
            if (targetOut == null) {
                targetOut = onlinePlayers.get(toUserId);
            }

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

    private static void handleAcceptInvite(String line, PrintWriter acceptorOut) {
        try {
            JSONObject request = new JSONObject(line);
            int fromUserId = request.getInt("fromUserId"); // the acceptor
            int toUserId = request.getInt("toUserId"); // the inviter

            String fromUsername = onlineUsernames.getOrDefault(fromUserId, "");
            String toUsername = onlineUsernames.getOrDefault(toUserId, "");

            // Optionally create a session in DB here and get sessionId
            int sessionId = (int) (System.currentTimeMillis() % Integer.MAX_VALUE);

            // Track who is host/guest
            sessionHostUserId.put(sessionId, toUserId);
            sessionGuestUserId.put(sessionId, fromUserId);
            sessionScoreP1.put(sessionId, 0);
            sessionScoreP2.put(sessionId, 0);

            JSONObject startGameForAcceptor = new JSONObject();
            startGameForAcceptor.put("type", "start_game");
            startGameForAcceptor.put("sessionId", sessionId);
            startGameForAcceptor.put("opponentId", toUserId);
            startGameForAcceptor.put("opponentUsername", toUsername);
            startGameForAcceptor.put("isHost", false);

            JSONObject startGameForInviter = new JSONObject();
            startGameForInviter.put("type", "start_game");
            startGameForInviter.put("sessionId", sessionId);
            startGameForInviter.put("opponentId", fromUserId);
            startGameForInviter.put("opponentUsername", fromUsername);
            startGameForInviter.put("isHost", true);

            // Send to acceptor (ưu tiên listener stream)
            PrintWriter acceptorStream = listenerStreams.get(fromUserId);
            if (acceptorStream == null) {
                acceptorStream = acceptorOut;
            }
            acceptorStream.println(startGameForAcceptor.toString());
            acceptorStream.flush();

            // Send to inviter (ưu tiên listener stream)
            PrintWriter inviterOut = listenerStreams.get(toUserId);
            if (inviterOut == null) {
                inviterOut = onlinePlayers.get(toUserId);
            }
            if (inviterOut != null) {
                inviterOut.println(startGameForInviter.toString());
                inviterOut.flush();
            }

            System.out.println("🎮 Start game between " + fromUsername + " and " + toUsername + ", sessionId=" + sessionId);
        } catch (Exception e) {
            System.err.println("⚠️ Error handling acceptInvite: " + e.getMessage());
        }
    }

    private static Map<Integer, Integer> sessionRoundCount = Collections.synchronizedMap(new HashMap<>());

    private static void handleRequestRound(String line, PrintWriter requesterOut) {
        try {
            JSONObject req = new JSONObject(line);
            int sessionId = req.getInt("sessionId");

            // tăng đếm vòng
            int current = sessionRoundCount.getOrDefault(sessionId, 0) + 1;
            sessionRoundCount.put(sessionId, current);

            if (current > 5) {
                handleEndGame(line);
                sessionRoundCount.remove(sessionId);
                return;
            }

            // Lấy ảnh ngẫu nhiên từ DB
            String imageUrl = null;
            int n1 = 0, n2 = 0, n3 = 0;
            try (var conn = com.mycompany.ltmproject.util.DB.get(); var ps = conn.prepareStatement("SELECT number1, number2, number3, filepath FROM Images ORDER BY RAND() LIMIT 1"); var rs = ps.executeQuery()) {
                if (rs.next()) {
                    n1 = rs.getInt("number1");
                    n2 = rs.getInt("number2");
                    n3 = rs.getInt("number3");
                    imageUrl = rs.getString("filepath");
                }
            } catch (Exception e) {
                System.err.println("⚠️ DB error requestRound: " + e.getMessage());
            }

            JSONObject round = new JSONObject();
            round.put("type", "round_data");
            round.put("sessionId", sessionId);
            round.put("imageUrl", imageUrl);
            round.put("n1", n1);
            round.put("n2", n2);
            round.put("n3", n3);

            // gửi cho cả 2 người chơi
            PrintWriter host = listenerStreams.get(sessionHostUserId.get(sessionId));
            PrintWriter guest = listenerStreams.get(sessionGuestUserId.get(sessionId));
            if (host != null) {
                host.println(round.toString());
                host.flush();
            }
            if (guest != null) {
                guest.println(round.toString());
                guest.flush();
            }

        } catch (Exception e) {
            System.err.println("⚠️ Error handleRequestRound: " + e.getMessage());
        }
    }

    // track number of submitted players per round
    private static Map<Integer, Integer> sessionRoundSubmits = Collections.synchronizedMap(new HashMap<>());

    private static void handleSubmitAnswers(String line) {
        try {
            JSONObject req = new JSONObject(line);
            int sessionId = req.getInt("sessionId");
            int userId = req.getInt("userId");
            int e = req.getInt("e");
            int s = req.getInt("s");
            int f = req.getInt("f");
            int n1 = req.getInt("n1");
            int n2 = req.getInt("n2");
            int n3 = req.getInt("n3");

            int points = 0;
            if (e == n1) {
                points++;
            }
            if (s == n2) {
                points++;
            }
            if (f == n3) {
                points++;
            }

            Integer hostId = sessionHostUserId.get(sessionId);
            if (hostId != null && hostId == userId) {
                sessionScoreP1.put(sessionId, sessionScoreP1.getOrDefault(sessionId, 0) + points);
            } else {
                sessionScoreP2.put(sessionId, sessionScoreP2.getOrDefault(sessionId, 0) + points);
            }

            // send updated score to both players
            JSONObject update = new JSONObject();
            update.put("type", "score_update");
            update.put("sessionId", sessionId);
            update.put("scoreP1", sessionScoreP1.getOrDefault(sessionId, 0));
            update.put("scoreP2", sessionScoreP2.getOrDefault(sessionId, 0));

            PrintWriter host = listenerStreams.get(sessionHostUserId.get(sessionId));
            PrintWriter guest = listenerStreams.get(sessionGuestUserId.get(sessionId));
            if (host != null) {
                host.println(update.toString());
                host.flush();
            }
            if (guest != null) {
                guest.println(update.toString());
                guest.flush();
            }

            // ✅ increase submit count
            int count = sessionRoundSubmits.getOrDefault(sessionId, 0) + 1;
            sessionRoundSubmits.put(sessionId, count);

            // ✅ if both players submitted => trigger next round
            if (count >= 2) {
                sessionRoundSubmits.put(sessionId, 0); // reset for next round

                JSONObject nextReq = new JSONObject();
                nextReq.put("sessionId", sessionId);
                handleRequestRound(nextReq.toString(), null); // next round or end game
            }

        } catch (Exception e) {
            System.err.println("⚠️ Error handleSubmitAnswers: " + e.getMessage());
        }
    }

    private static void handleEndGame(String line) {
        try {
            JSONObject req = new JSONObject(line);
            int sessionId = req.getInt("sessionId");

            int p1 = sessionScoreP1.getOrDefault(sessionId, 0);
            int p2 = sessionScoreP2.getOrDefault(sessionId, 0);
            int hostId = sessionHostUserId.getOrDefault(sessionId, -1);
            int guestId = sessionGuestUserId.getOrDefault(sessionId, -1);
            int winner = 0;
            if (p1 > p2) {
                winner = hostId;
            } else if (p2 > p1) {
                winner = guestId;
            }

            // persist into DB
            try (var conn = com.mycompany.ltmproject.util.DB.get(); var ps = conn.prepareStatement(
                    "INSERT INTO GameSession (Playerid1, Playerid2, start_time, end_time, playerscore1, playerscore2, winner) VALUES (?, ?, NOW(), NOW(), ?, ?, ?)")) {
                ps.setInt(1, hostId);
                ps.setInt(2, guestId);
                ps.setInt(3, p1);
                ps.setInt(4, p2);
                ps.setInt(5, winner);
                ps.executeUpdate();
            } catch (Exception e) {
                System.err.println("⚠️ DB save GameSession failed: " + e.getMessage());
            }

            JSONObject end = new JSONObject();
            end.put("type", "game_end");
            end.put("sessionId", sessionId);
            end.put("scoreP1", p1);
            end.put("scoreP2", p2);
            end.put("winner", winner);

            PrintWriter host = listenerStreams.get(hostId);
            PrintWriter guest = listenerStreams.get(guestId);
            if (host != null) {
                host.println(end.toString());
                host.flush();
            }
            if (guest != null) {
                guest.println(end.toString());
                guest.flush();
            }

            // cleanup
            sessionScoreP1.remove(sessionId);
            sessionScoreP2.remove(sessionId);
            sessionHostUserId.remove(sessionId);
            sessionGuestUserId.remove(sessionId);
        } catch (Exception e) {
            System.err.println("⚠️ Error handleEndGame: " + e.getMessage());
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
        for (Map.Entry<Integer, PrintWriter> entry : listenerStreams.entrySet()) {
            entry.getValue().println(notification.toString());
            entry.getValue().flush();
        }
    }
}
