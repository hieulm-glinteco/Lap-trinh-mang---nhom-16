package com.mycompany.ltmproject.controller;

import com.mycompany.ltmproject.model.User;
import com.mycompany.ltmproject.session.SessionManager;
import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class HomeController {
    // Không cần currentUser hay currentUserId nữa, vì dùng SessionManager

    public void handleLogout(ActionEvent e) {
        try {
            // ✅ Xóa user khỏi session khi logout
            SessionManager.clearSession();

            Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
            Scene scene = new Scene(root, 1000, 650);
            Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Đăng nhập");
            stage.setResizable(false);
            stage.show();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void handleViewRanking(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ranking.fxml"));
            Parent root = loader.load();

            // ✅ Không cần truyền user nữa
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 650));
            stage.setTitle("Bảng xếp hạng");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleViewHistory(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/history.fxml"));
            Parent root = loader.load();

            // ✅ Không cần gọi setCurrentUser vì HistoryController tự lấy từ SessionManager
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 650));
            stage.setTitle("Lịch sử đấu");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void handleViewOnlinePlayers(ActionEvent event) {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/online.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root, 1000, 650));
        stage.setTitle("Người chơi online");
        stage.show();
    } catch (IOException e) {
        e.printStackTrace();
    }
}
}