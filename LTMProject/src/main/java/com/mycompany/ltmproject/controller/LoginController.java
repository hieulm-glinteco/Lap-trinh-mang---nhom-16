/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.ltmproject.controller;

import com.mycompany.ltmproject.net.ClientSocket;
import com.sun.javafx.util.TempState;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
/**
 *
 * @author admin
 */
public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label statusLabel;
    
    private ClientSocket clientSocket = ClientSocket.getInstance();
    
    @FXML
    public void handleLogin(ActionEvent event){
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        if(username.isEmpty()||password.isEmpty()){
            statusLabel.setText("Vui lòng nhập đầy đủ thông tin!");
            return;
        }
        String loginRequest = "{\"action\":\"login\",\"username\":\"" + username + "\",\"password\":\"" + password + "\"}";
        clientSocket.send(loginRequest);
        
        // doi server tra ve ket qua
        new Thread(()->{
            try {
                String response = clientSocket.receive();
                Platform.runLater(()->{
                    if(response.contains("\"status\":\"success\"")){
                        statusLabel.setStyle("-fx-text-fill:green;");
                        statusLabel.setText("Đăng nhập thành công");
                        //chuyen sang man hinh choi game
                    }
                    else{
                        statusLabel.setStyle("-fx-text-fill:red;");
                        statusLabel.setText("Sai tên đăng nhập hoặc mật khẩu");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(()->{
                    statusLabel.setText("Lỗi kết nối tới server");
                });
            }
        }).start();
    }
    
    @FXML
    public void handleRegister(ActionEvent event) {}
}
