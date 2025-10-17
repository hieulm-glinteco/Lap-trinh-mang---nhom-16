package com.mycompany.ltmproject.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import org.cloudinary.json.JSONArray;
import java.io.IOException;

public class GameController {

    @FXML
    private Label roomLabel;
    @FXML
    private Label player1Label;
    @FXML
    private Label player2Label;
    @FXML
    private Button backButton;

    private String roomId;

    public void setRoomId(String roomId) {
        this.roomId = roomId;
        roomLabel.setText("Phòng " + roomId);
    }

    public void setPlayers(JSONArray players) {
        if (players.length() >= 2) {
            player1Label.setText(players.getString(0));
            player2Label.setText(players.getString(1));
        }
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/home.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root, 800, 520));
            stage.setTitle("Trang chủ");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
