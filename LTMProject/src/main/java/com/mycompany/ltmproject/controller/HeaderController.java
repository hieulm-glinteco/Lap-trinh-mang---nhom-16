package com.mycompany.ltmproject.controller;

import com.mycompany.ltmproject.session.SessionManager;
import com.mycompany.ltmproject.model.User;
import java.io.IOException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Node;

public class HeaderController {

    @FXML
    private Label lblUsername;

    @FXML
    public void initialize() {
        // Lấy username từ SessionManager (trì hoãn 1 chút để chắc chắn đã được set)
        Platform.runLater(() -> {
            User user = SessionManager.getCurrentUser();
            if (user != null) {
                lblUsername.setText("Xin chào, " + user.getUsername());
            } else {
                lblUsername.setText("Người chơi");
            }
        });
    }

    @FXML
    private void handleHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/home.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 1000, 650);
            scene.getStylesheets().add(getClass().getResource("/css/home.css").toExternalForm());
            Stage stage = (Stage) lblUsername.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
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
}
