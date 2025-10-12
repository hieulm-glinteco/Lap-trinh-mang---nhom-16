/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.ltmproject.controller;

import com.mycompany.ltmproject.model.User;
import com.mycompany.ltmproject.net.ClientSocket;
import com.mycompany.ltmproject.session.SessionManager;
import java.sql.Date;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.cloudinary.json.JSONObject;

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
            return; // chặn spam nút
        }
        String username = usernameField.getText() == null ? "" : usernameField.getText().trim();
        String password = passwordField.getText() == null ? "" : passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setStyle("-fx-text-fill:red;");
            statusLabel.setText("Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        // Khóa UI khi đang đăng nhập
        setBusy(true, "Đang đăng nhập...");

        // Gửi JSON login request
        JSONObject loginRequest = new JSONObject();
        loginRequest.put("action", "login");
        loginRequest.put("username", username);
        loginRequest.put("password", password);

        new Thread(() -> {
            try {
                clientSocket.send(loginRequest.toString());
                String response = clientSocket.receive();

                Platform.runLater(() -> {
                    try {
                        if (response == null || response.isEmpty()) {
                            statusLabel.setStyle("-fx-text-fill:red;");
                            statusLabel.setText("Không nhận được phản hồi từ server!");
                            return;
                        }

                        System.out.println("Server response: " + response);
                        JSONObject json = new JSONObject(response);

                        String status = json.optString("status", "fail");
                        if (!status.equals("success")) {
                            statusLabel.setStyle("-fx-text-fill:red;");
                            statusLabel.setText("Sai tên đăng nhập hoặc mật khẩu!");
                            return;
                        }

                        // ✅ Parse thông tin user
                        JSONObject userJson = json.optJSONObject("user");
                        if (userJson == null) {
                            statusLabel.setStyle("-fx-text-fill:red;");
                            statusLabel.setText("Phản hồi từ server bị thiếu user!");
                            return;
                        }

                        User user = new User();
                        user.setId(userJson.optInt("id", -1));
                        user.setUsername(userJson.optString("username", ""));
                        user.setName(userJson.optString("name", ""));
                        user.setEmail(userJson.optString("email", ""));
                        user.setPhone(userJson.optString("phone", ""));
                        user.setTotalRankScore(userJson.optInt("totalRankScore", 0));

                        if (userJson.has("dob") && !userJson.isNull("dob")) {
                            try {
                                user.setDob(Date.valueOf(userJson.getString("dob")));
                            } catch (IllegalArgumentException e) {
                                System.err.println("DOB format invalid: " + userJson.getString("dob"));
                            }
                        }

                        // ✅ Mở Home.fxml và truyền user vào controller
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/home.fxml"));
                        Parent root = loader.load();

                        HomeController homeController = loader.getController();
                        if (homeController == null) {
                            System.err.println("⚠️ Không thể lấy controller từ home.fxml!");
                            statusLabel.setText("Không thể mở màn hình Trang chủ!");
                            return;
                        }
                        
                        SessionManager.setCurrentUser(user);
                        System.out.println("Đăng nhập thành công, user ID = " + user.getId());

                        Scene scene = new Scene(root, 800, 520);
                        Stage stage = (Stage) loginButton.getScene().getWindow();
                        stage.setScene(scene);
                        stage.setTitle("Trang chủ");
                        stage.setResizable(false);
                        stage.show();

                        statusLabel.setStyle("-fx-text-fill:green;");
                        statusLabel.setText("Đăng nhập thành công!");

                    } catch (Exception ex) {
                        ex.printStackTrace();
                        statusLabel.setStyle("-fx-text-fill:red;");
                        statusLabel.setText("Phản hồi không hợp lệ từ server!");
                    } finally {
                        setBusy(false, null);
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    e.printStackTrace();
                    statusLabel.setStyle("-fx-text-fill:red;");
                    statusLabel.setText("Lỗi kết nối tới server!");
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
