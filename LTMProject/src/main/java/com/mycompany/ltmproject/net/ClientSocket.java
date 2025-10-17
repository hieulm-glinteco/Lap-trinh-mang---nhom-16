package com.mycompany.ltmproject.net;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.cloudinary.json.JSONArray;

public class ClientSocket {
    private static ClientSocket instance;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private final List<String> onlineUsers = new ArrayList<>();
    private Thread listenerThread;

    public static ClientSocket getInstance(){
        if(instance == null) instance = new ClientSocket();
        return instance;
    }

    public void connect(String host, int port) throws IOException{
        socket = new Socket(host, port);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
    }

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
    }
}
