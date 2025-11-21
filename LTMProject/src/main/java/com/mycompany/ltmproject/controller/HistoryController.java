package com.mycompany.ltmproject.controller;

import com.mycompany.ltmproject.dao.GameSessionDAO;
import com.mycompany.ltmproject.dao.UserDAO;
import com.mycompany.ltmproject.model.GameSession;
import com.mycompany.ltmproject.model.User;
import com.mycompany.ltmproject.net.ClientSocket;
import com.mycompany.ltmproject.session.SessionManager;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.scene.Node;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.cloudinary.json.JSONArray;
import org.cloudinary.json.JSONObject;

public class HistoryController {

    @FXML
    private TableView<HistoryRecord> historyTable;

    @FXML
    private TableColumn<HistoryRecord, String> colDate;

    @FXML
    private TableColumn<HistoryRecord, String> colOpponent;

    @FXML
    private TableColumn<HistoryRecord, String> colStatus;

    @FXML
    private TableColumn<HistoryRecord, String> colScore;

    @FXML
    private Button backButton;

    private final GameSessionDAO gameSessionDAO = new GameSessionDAO();
    private final UserDAO userDAO = new UserDAO();
    private ClientSocket clientSocket = ClientSocket.getInstance();

    @FXML
    public void initialize() {
        colDate.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getDate()));
        colOpponent.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getOpponent()));
        colStatus.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getStatus()));
        colScore.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getScore()));

        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            System.out.println("Không có người dùng trong session — cần đăng nhập lại!");
            return;
        }

        loadHistoryInBackground();
    }

    private void loadHistoryInBackground() {
        new Thread(() -> {
            try {
                loadHistory();
            } catch (IOException e) {
                System.err.println("Error loading history: " + e.getMessage());
                e.printStackTrace();
            } catch (InterruptedException ex) {
                Logger.getLogger(HistoryController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }, "history-loader").start();
    }

    private void loadHistory() throws IOException, InterruptedException {
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            System.out.println("Không có user trong session — không thể tải lịch sử!");
            return;
        }

        int currentUserId = currentUser.getId();

        // ⭐ Ngừng listener tạm thời
        boolean wasListening = clientSocket.isListenerConnected();
        if (wasListening) {
            clientSocket.disconnectListener();
            Thread.sleep(100);
        }

        try {
            clientSocket.waitForReady();

            JSONObject historyRequest = new JSONObject();
            historyRequest.put("action", "history");
            historyRequest.put("userId", currentUserId);
            clientSocket.send(historyRequest.toString());

            String response = clientSocket.receive();
            System.out.println("📩 History Response: " + response);

            if (response == null || response.isEmpty()) {
                System.err.println("Empty response from server");
                return;
            }

            JSONObject res = new JSONObject(response);

            if (!res.has("status")) {
                System.err.println("Response không có field 'status': " + response);
                return;
            }

            String status = res.optString("status", "fail");

            if (!status.equals("success")) {
                System.err.println("Status không phải success: " + status);
                return;
            }

            if (!res.has("history")) {
                System.err.println("Response không có field 'history'");
                return;
            }

            JSONArray arr = res.getJSONArray("history");
            ObservableList<HistoryRecord> historyList = FXCollections.observableArrayList();

            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                historyList.add(new HistoryRecord(
                        obj.getString("date"),
                        obj.getString("opponent"),
                        obj.getString("status"),
                        obj.getString("score")
                ));
            }

            Platform.runLater(() -> {
                historyTable.setItems(historyList);
            });

            System.out.println("Loaded " + historyList.size() + " history records");

        } catch (org.cloudinary.json.JSONException e) {
            System.err.println("JSON Parse Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // ⭐ Khôi phục listener
            if (wasListening) {
                try {
                    clientSocket.connectListener("localhost", 8888);
                    Thread.sleep(100);
                } catch (IOException e) {
                    System.err.println("Failed to reconnect listener: " + e.getMessage());
                }
            }
        }
    }

    @FXML
    void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/home.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, 1000, 650);
            scene.getStylesheets().add(getClass().getResource("/css/home.css").toExternalForm());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Trang chủ");
            stage.setResizable(false);
            stage.show();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class HistoryRecord {

        private final String date;
        private final String opponent;
        private final String status;
        private final String score;

        public HistoryRecord(String date, String opponent, String status, String score) {
            this.date = date;
            this.opponent = opponent;
            this.status = status;
            this.score = score;
        }

        public String getDate() {
            return date;
        }

        public String getOpponent() {
            return opponent;
        }

        public String getStatus() {
            return status;
        }

        public String getScore() {
            return score;
        }
    }
}
