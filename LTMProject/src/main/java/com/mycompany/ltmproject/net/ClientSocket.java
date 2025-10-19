package com.mycompany.ltmproject.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientSocket {

    private static ClientSocket instance;

    // Socket chính để gửi/nhận request
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    // Socket riêng để lắng nghe realtime updates (tạo khi cần)
    private Socket listenerSocket;
    private BufferedReader listenerIn;
    private boolean listenerConnected = false;

    public static ClientSocket getInstance() {
        if (instance == null) {
            instance = new ClientSocket();
        }
        return instance;
    }

    // Kết nối chính - gọi lần đầu (login)
    public void connect(String host, int port) throws IOException {
        socket = new Socket(host, port);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
    }

    // Kết nối riêng cho listener - gọi khi vào online players
    public synchronized void connectListener(String host, int port) throws IOException {
        // Kiểm tra xem đã kết nối chưa
        if (listenerConnected) {
            return;
        }

        try {
            listenerSocket = new Socket(host, port);
            listenerIn = new BufferedReader(new InputStreamReader(listenerSocket.getInputStream()));
            listenerConnected = true;
            System.out.println("✅ Listener socket connected");
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
                listenerConnected = false;
                System.out.println("✅ Listener socket disconnected");
            } catch (IOException e) {
                System.err.println("❌ Error closing listener socket: " + e.getMessage());
            }
        }
    }

    public void send(String message) {
        if (out != null) {
            out.println(message);
        }
    }

    public String receive() throws IOException {
        if (in != null) {
            return in.readLine();
        }
        return null;
    }

    // Dùng cho listener thread
    public BufferedReader getListenerReader() {
        return listenerIn;
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
    }
}
