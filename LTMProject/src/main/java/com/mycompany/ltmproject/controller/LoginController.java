/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.ltmproject.controller;

import com.mycompany.ltmproject.net.ClientSocket;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author admin
 */
public class LoginController {

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label statusLabel;
    @FXML
    private Button loginButton;

    private ClientSocket clientSocket = ClientSocket.getInstance();
    private volatile boolean loggingIn = false;

    @FXML
    public void initialize() {
        usernameField.setOnAction(this::handleLogin);
        passwordField.setOnAction(this::handleLogin);

        if (loginButton != null) {
            loginButton.setDefaultButton(true);
        }
    }

    @FXML
    public void handleLogin(ActionEvent event) {
        if (loggingIn) {
            return; // chặn bấm liên tục
        }
        String username = usernameField.getText() == null ? "" : usernameField.getText().trim();
        String password = passwordField.getText() == null ? "" : passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setStyle("-fx-text-fill:red;");
            statusLabel.setText("Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        // Khoá UI khi đang đăng nhập
        setBusy(true, "Đang đăng nhập...");

        String loginRequest
                = "{\"action\":\"login\",\"username\":\"" + escapeJson(username)
                + "\",\"password\":\"" + escapeJson(password) + "\"}";

        new Thread(() -> {
            try {
                clientSocket.send(loginRequest);

                // TODO: nếu có thể, nên dùng receive có timeout để tránh treo vô hạn
                String response = clientSocket.receive();

                Platform.runLater(() -> {
                    if (response != null && response.contains("\"status\":\"success\"")) {
                        statusLabel.setStyle("-fx-text-fill:green;");
                        statusLabel.setText("Đăng nhập thành công");

                        try {
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/home.fxml"));
                            Parent root = loader.load();
                            // Giữ kích thước cố định như login
                            Scene scene = new Scene(root, 800, 520);

                            Stage stage = (Stage) loginButton.getScene().getWindow();
                            stage.setScene(scene);
                            stage.setTitle("Trang chủ");
                            stage.setResizable(false);
                            stage.show();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            statusLabel.setStyle("-fx-text-fill:red;");
                            statusLabel.setText("Không thể mở màn hình Trang chủ!");
                        }
                    } else {
                        statusLabel.setStyle("-fx-text-fill:red;");
                        statusLabel.setText("Sai tên đăng nhập hoặc mật khẩu");
                    }
                    setBusy(false, null);
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    statusLabel.setStyle("-fx-text-fill:red;");
                    statusLabel.setText("Lỗi kết nối tới server");
                    setBusy(false, null);
                });
            }
        }, "login-thread").start();
    }

    @FXML
    public void handleRegister(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/register.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 800, 560);

            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Đăng ký");
            stage.setResizable(false);
            stage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
            statusLabel.setStyle("-fx-text-fill:red;");
            statusLabel.setText("Không thể mở màn hình đăng ký!");
        }
    }

    // ===== Helpers =====
    private void setBusy(boolean busy, String msg) {
        loggingIn = busy;
        if (loginButton != null) {
            loginButton.setDisable(busy);
        }
        usernameField.setDisable(busy);
        passwordField.setDisable(busy);
        if (msg != null) {
            statusLabel.setStyle("-fx-text-fill:#333;");
            statusLabel.setText(msg);
        }
    }

    private static String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

}
