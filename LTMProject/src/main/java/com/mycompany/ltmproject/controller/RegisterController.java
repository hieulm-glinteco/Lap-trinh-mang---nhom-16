package com.mycompany.ltmproject.controller;

import com.mycompany.ltmproject.net.ClientSocket;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import javafx.animation.PauseTransition;
import javafx.scene.Node;

public class RegisterController {

    @FXML
    private TextField fullNameField;
    @FXML
    private DatePicker dobPicker;
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField phoneField;
    @FXML
    private Label statusLabel;

    private ClientSocket clientSocket = ClientSocket.getInstance();

    @FXML
    public void handleRegister(ActionEvent event) {
        String fullName = fullNameField.getText().trim();
        LocalDate dob = dobPicker.getValue();
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();

        // Kiểm tra input cơ bản
        if (fullName.isEmpty() || dob == null || username.isEmpty()
                || password.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            statusLabel.setText("Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        if (password.length() < 6) {
            statusLabel.setText("Mật khẩu phải có ít nhất 6 ký tự!");
            return;
        }

        // Gửi request đăng ký tới server
        String registerRequest = String.format(
                "{\"action\":\"register\",\"fullname\":\"%s\",\"dob\":\"%s\",\"username\":\"%s\",\"password\":\"%s\",\"email\":\"%s\",\"phone\":\"%s\"}",
                fullName, dob.toString(), username, password, email, phone
        );
        clientSocket.send(registerRequest);

        new Thread(() -> {
            try {
                String response = clientSocket.receive();
                Platform.runLater(() -> {
                    if (response.contains("\"status\":\"success\"")) {
                        statusLabel.setStyle("-fx-text-fill:green;");
                        statusLabel.setText("Đăng ký thành công! Đang quay lại đăng nhập...");
                        PauseTransition pause = new PauseTransition(javafx.util.Duration.seconds(3));
                        pause.setOnFinished(e -> {
                            try {
                                Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
                                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                                stage.setScene(new Scene(root));
                                stage.show();
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        });
                        pause.play();
                    } else {
                        statusLabel.setStyle("-fx-text-fill:red;");
                        statusLabel.setText("Đăng ký thất bại! Tên đăng nhập đã tồn tại?");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> statusLabel.setText("Lỗi kết nối tới server"));
            }
        }).start();
    }

    @FXML
    public void goLogin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);

            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Đăng nhập");
            stage.setResizable(false);
            stage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
            statusLabel.setText("Không thể quay lại màn hình đăng nhập!");
        }
    }

}
