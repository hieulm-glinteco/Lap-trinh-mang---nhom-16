package com.mycompany.ltmproject.controller;

import com.mycompany.ltmproject.dao.GameSessionDAO;
import com.mycompany.ltmproject.dao.UserDAO;
import com.mycompany.ltmproject.model.GameSession;
import com.mycompany.ltmproject.model.User;
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

    @FXML
    public void initialize() {
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

    private void loadHistory() {
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            System.out.println("⚠️ Không có user trong session — không thể tải lịch sử!");
            return;
        }

        int currentUserId = currentUser.getId();
        List<GameSession> sessions = gameSessionDAO.getGameSessionById(currentUserId);
        ObservableList<HistoryRecord> historyList = FXCollections.observableArrayList();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        for (GameSession gs : sessions) {
            String date = (gs.getStart() != null) ? dateFormat.format(gs.getStart()) : "";
            String opponent = "Ẩn danh";
            String status;
            String score = gs.getPlayerscore1() + " - " + gs.getPlayerscore2();
            
            System.out.println(gs.getPlayerid1() + " " + gs.getPlayerid2());
            try {
                // ✅ Xác định đối thủ dựa theo ID
                if (gs.getPlayerid1() == currentUserId) {
                    if (gs.getPlayerid2() != 0) {
                        User opponentUser = userDAO.getUserById(gs.getPlayerid2());
//                        System.out.println(opponentUser.getName());
                        if (opponentUser != null && opponentUser.getUsername() != null) {
                            opponent = opponentUser.getUsername();
                        }
                    }
                } else if (gs.getPlayerid2() == currentUserId) {
                    if (gs.getPlayerid1() != 0) {
                        System.out.println();
                        User opponentUser = userDAO.getUserById(gs.getPlayerid1());
//                        System.out.println(opponentUser.getName());
                        if (opponentUser != null && opponentUser.getUsername() != null) {
                            opponent = opponentUser.getUsername();
                        }
                    }
                }
            } catch (Exception e) {
                opponent = "Không xác định";
                e.printStackTrace();
            }

            // ✅ Xác định kết quả
            if (gs.getWinner() == 0) {
                status = "Hòa";
            } else if (gs.getWinner() == currentUserId) {
                status = "Thắng";
            } else {
                status = "Thua";
            }

            historyList.add(new HistoryRecord(date, opponent, status, score));
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
