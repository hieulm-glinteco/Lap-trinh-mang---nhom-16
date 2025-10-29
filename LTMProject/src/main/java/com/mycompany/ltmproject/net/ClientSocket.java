package com.mycompany.ltmproject.net;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.cloudinary.json.JSONArray;

public class ClientSocket {

    private static ClientSocket instance;

    // Socket chính để gửi/nhận request
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
<<<<<<< HEAD

    // Socket riêng để lắng nghe realtime updates (tạo khi cần)
    private Socket listenerSocket;
    private BufferedReader listenerIn;
    private PrintWriter listenerOut;
    private boolean listenerConnected = false;

    public static ClientSocket getInstance() {
        if (instance == null) {
            instance = new ClientSocket();
        }
        return instance;
    }

    // Kết nối chính - gọi lần đầu (login)
    public void connect(String host, int port) throws IOException {
=======
    private final List<String> onlineUsers = new ArrayList<>();
    private Thread listenerThread;

    public static ClientSocket getInstance(){
        if(instance == null) instance = new ClientSocket();
        return instance;
    }

    public void connect(String host, int port) throws IOException{
>>>>>>> origin/HoangND
        socket = new Socket(host, port);
        try {
            socket.setTcpNoDelay(true);
        } catch (Exception ignored) {}
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
    }

<<<<<<< HEAD
    // Kết nối riêng cho listener - gọi khi vào online players
    public synchronized void connectListener(String host, int port) throws IOException {
        // Kiểm tra xem đã kết nối chưa
        if (listenerConnected) {
            return;
        }

        try {
            listenerSocket = new Socket(host, port);
            listenerIn = new BufferedReader(new InputStreamReader(listenerSocket.getInputStream()));
            listenerOut = new PrintWriter(listenerSocket.getOutputStream(), true);
            listenerConnected = true;
            System.out.println("✅ Listener socket connected");

            // Gửi handshake để server biết user này là ai và sẽ nhận realtime
            try {
                org.cloudinary.json.JSONObject hello = new org.cloudinary.json.JSONObject();
                hello.put("action", "startListening");
                try {
                    com.mycompany.ltmproject.model.User u = com.mycompany.ltmproject.session.SessionManager.getCurrentUser();
                    if (u != null) {
                        hello.put("userId", u.getId());
                    }
                } catch (Exception ignored) {}
                listenerOut.println(hello.toString());
                listenerOut.flush();
            } catch (Exception ignored) {}
        } catch (IOException e) {
            System.err.println("❌ Failed to connect listener socket: " + e.getMessage());
            throw e;
        }
    }

    // Đóng listener socket - gọi khi back khỏi online players
    public synchronized void disconnectListener() {
        if (listenerSocket != null && listenerConnected) {
            try {
                listenerSocket.close();
                listenerIn = null;
                listenerOut = null;
                listenerConnected = false;
                System.out.println("✅ Listener socket disconnected");
            } catch (IOException e) {
                System.err.println("❌ Error closing listener socket: " + e.getMessage());
            }
        }
    }

    public synchronized void send(String message) {
        if (out != null) {
            out.println(message);
        }
    }

    public synchronized String receive() throws IOException {
        if (in != null) {
            return in.readLine();
        }
        return null;
    }

    // Dùng cho listener thread
    public BufferedReader getListenerReader() {
        return listenerIn;
    }

    // Gửi message qua listener socket (nếu cần)
    public synchronized void sendListener(String message) {
        if (listenerOut != null) {
            listenerOut.println(message);
        }
    }

    public boolean isListenerConnected() {
        return listenerConnected;
    }

    public void close() throws IOException {
        if (socket != null) {
            socket.close();
        }
        disconnectListener();
    }

    // Kiểm tra xem socket đã sẵn sàng chưa
    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

// Đợi socket sẵn sàng (tối đa 5 lần x 100ms = 500ms)
    public void waitForReady() {
        int attempts = 0;
        while (!isConnected() && attempts < 5) {
            try {
                Thread.sleep(100);
                attempts++;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
=======
    public void send(String message) {
        if (out != null) {
            out.println(message);
        }
    }

    public String receive() throws IOException {
        return in.readLine();
    }

    /** 🔸 Chỉ start listener 1 lần duy nhất */
    public void startListening(Consumer<String> onMessage) {
        if (listenerThread != null && listenerThread.isAlive()) return;

        listenerThread = new Thread(() -> {
            try {
                String line;
                while ((line = in.readLine()) != null) {
                    onMessage.accept(line);
                }
            } catch (IOException e) {
                System.out.println("⚠️ Mất kết nối tới server: " + e.getMessage());
            }
        });
        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    /** ✅ Cập nhật danh sách online */
    public void updateOnlineUsers(JSONArray arr) {
        onlineUsers.clear();
        for (int i = 0; i < arr.length(); i++) {
            onlineUsers.add(arr.getString(i));
        }
    }

    public List<String> getOnlineUsers() {
        return onlineUsers;
    }

    /** ✅ Đóng kết nối */
    public void close() {
        try {
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException ignored) {}
>>>>>>> origin/HoangND
    }
}
