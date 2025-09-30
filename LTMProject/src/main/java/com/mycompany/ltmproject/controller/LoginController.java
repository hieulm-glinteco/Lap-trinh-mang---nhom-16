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

    private ClientSocket clientSocket = ClientSocket.getInstance();

    @FXML
    public void handleLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Vui lòng nhập đầy đủ thông tin!");
            return;
        }
        String loginRequest = "{\"action\":\"login\",\"username\":\"" + username + "\",\"password\":\"" + password + "\"}";
        clientSocket.send(loginRequest);

        // Đợi server trả về kết quả
        new Thread(() -> {
            try {
                String response = clientSocket.receive();
                Platform.runLater(() -> {
                    if (response.contains("\"status\":\"success\"")) {
                        statusLabel.setStyle("-fx-text-fill:green;");
                        statusLabel.setText("Đăng nhập thành công");

                        // Chuyển sang màn hình Home
                        try {
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/home.fxml"));
                            Parent root = loader.load();
                            Scene scene = new Scene(root);

                            // Lấy stage hiện tại từ event
                            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
                            stage.setScene(scene);
                            stage.setTitle("Trang chủ");
                            stage.setResizable(false);
                            stage.show();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            statusLabel.setText("Không thể mở màn hình Trang chủ!");
                        }

                    } else {
                        statusLabel.setStyle("-fx-text-fill:red;");
                        statusLabel.setText("Sai tên đăng nhập hoặc mật khẩu");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    statusLabel.setText("Lỗi kết nối tới server");
                });
            }
        }).start();
    }

    @FXML
    public void handleRegister(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/register.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);

            // Lấy stage hiện tại từ nút bấm
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Đăng ký");
            stage.setResizable(false);
            stage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
            statusLabel.setText("Không thể mở màn hình đăng ký!");
        }
    }

}
