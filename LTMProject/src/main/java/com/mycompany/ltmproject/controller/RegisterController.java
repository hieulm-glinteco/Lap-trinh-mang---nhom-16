package com.mycompany.ltmproject.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class RegisterController {

    @FXML private Label statusLabel;
    // ... các @FXML field khác nếu FXML có fx:id tương ứng

    @FXML
    private void handleRegister(ActionEvent e) {
        // TODO: logic đăng ký
        if (statusLabel != null) statusLabel.setText("Đang xử lý đăng ký...");
    }

    @FXML
    private void goLogin(ActionEvent e) {
        // TODO: điều hướng về màn login
    }
}
