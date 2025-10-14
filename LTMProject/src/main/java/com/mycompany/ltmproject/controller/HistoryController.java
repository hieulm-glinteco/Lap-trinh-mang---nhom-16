package com.mycompany.ltmproject.controller;

import com.mycompany.ltmproject.dao.GameSessionDAO;
import com.mycompany.ltmproject.dao.UserDAO;
import com.mycompany.ltmproject.model.GameSession;
import com.mycompany.ltmproject.model.User;
import com.mycompany.ltmproject.net.ClientSocket;
import com.mycompany.ltmproject.session.SessionManager;
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
    public void initialize() throws IOException {
        // Cấu hình các cột
        colDate.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getDate()));
        colOpponent.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getOpponent()));
        colStatus.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getStatus()));
        colScore.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getScore()));

        // Lấy user hiện tại từ Session
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            System.out.println("⚠️ Không có người dùng trong session — cần đăng nhập lại!");
            return;
        }

        // Tải lịch sử dựa trên userId
        loadHistory();
    }

    private void loadHistory() throws IOException {
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            System.out.println("⚠️ Không có user trong session — không thể tải lịch sử!");
            return;
        }

        int currentUserId = currentUser.getId();
        // Gửi yêu cầu đến server
        JSONObject historyRequest = new JSONObject();
        historyRequest.put("action", "history");
        historyRequest.put("userId", currentUserId);
        clientSocket.send(historyRequest.toString());
        String response = clientSocket.receive();

        JSONObject res = new JSONObject(response);
        ObservableList<HistoryRecord> historyList = FXCollections.observableArrayList();
        if (res.getString("status").equals("success")) {
            JSONArray arr = res.getJSONArray("history");
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                historyList.add(new HistoryRecord(
                        obj.getString("date"),
                        obj.getString("opponent"),
                        obj.getString("status"),
                        obj.getString("score")
                ));
            }
        }

        historyTable.setItems(historyList);

    }

    // ✅ Giữ lại session khi quay lại Home
    @FXML
    void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/home.fxml"));
            Parent root = loader.load();

            // Không cần setCurrentUser nữa vì đã có SessionManager
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 800, 520));
            stage.setTitle("Trang chủ");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ==================== CLASS NỘI BỘ CHO TABLEVIEW ====================
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
