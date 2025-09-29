/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.ltmproject.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author admin
 */
public class previewUI extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        try {
            var root = FXMLLoader.load(getClass().getResource("/fxml/register.fxml"));
            stage.setScene(new Scene((Parent) root, 800, 520));
            stage.setTitle("Đăng nhập");
            stage.show();
        } catch (Exception ex) {
            ex.printStackTrace(); // IN RA NGUYÊN NHÂN THẬT
            // hiện thêm hộp thoại để thấy lỗi khi chạy bằng plugin
            new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR,
                    "Lỗi khởi tạo UI: " + ex.getClass().getSimpleName() + "\n" + ex.getMessage()).showAndWait();
            // đừng System.exit ở đây để đọc log
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

}
