package com.mycompany.ltmproject.controller;

import com.mycompany.ltmproject.model.User;
import com.mycompany.ltmproject.net.ClientSocket;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Pair;
import org.cloudinary.json.JSONArray;
import org.cloudinary.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class RankingController {

    @FXML
    private TableView<Pair<User, Integer>> rankingTable;

    @FXML
    private TableColumn<Pair<User, Integer>, String> colUsername;

    @FXML
    private TableColumn<Pair<User, Integer>, Integer> colScore;

    @FXML
    private TableColumn<Pair<User, Integer>, Integer> colWins;

    @FXML
    private Label statusLabel;

    private ClientSocket clientSocket = ClientSocket.getInstance();

    @FXML
    public void initialize() throws IOException, InterruptedException {
        setupTable();
        loadRankingData();
    }

    private void setupTable() {
        // Cột Username
        colUsername.setCellValueFactory(pair
                -> new ReadOnlyObjectWrapper<>(pair.getValue().getKey().getUsername()));

        // Cột điểm tổng
        colScore.setCellValueFactory(pair
                -> new ReadOnlyObjectWrapper<>(pair.getValue().getKey().getTotalRankScore()));

        // Cột số trận thắng
        // Nếu colWins null, tự tạo mới
        if (colWins == null) {
            colWins = new TableColumn<>("Wins");
            colWins.setPrefWidth(100);
            rankingTable.getColumns().add(colWins);
        }
        colWins.setCellValueFactory(pair -> new ReadOnlyObjectWrapper<>(pair.getValue().getValue()));

        // Cột Top
        TableColumn<Pair<User, Integer>, Number> colTop = new TableColumn<>("Top");
        colTop.setPrefWidth(50);
        colTop.setCellValueFactory(cellData
                -> new ReadOnlyObjectWrapper<>(rankingTable.getItems().indexOf(cellData.getValue()) + 1)
        );
        rankingTable.getColumns().add(0, colTop);
    }

    private void loadRankingData() throws IOException, InterruptedException {
        setStatus("Đang tải bảng xếp hạng...", "#333");

        new Thread(() -> {
            try {
                // ⭐ Ngừng listener tạm thời
                boolean wasListening = clientSocket.isListenerConnected();
                if (wasListening) {
                    clientSocket.disconnectListener();
                    Thread.sleep(100);
                }

                try {
                    clientSocket.waitForReady();

                    JSONObject request = new JSONObject();
                    request.put("action", "ranking");
                    clientSocket.send(request.toString());

                    String responseStr = clientSocket.receive();
                    System.out.println("📩 Server response: " + responseStr);

                    if (responseStr == null || responseStr.isEmpty()) {
                        Platform.runLater(() -> setStatus("Không nhận được phản hồi từ server!", "red"));
                        return;
                    }

                    JSONObject response = new JSONObject(responseStr);

                    if (!response.has("status")) {
                        System.err.println("Response không có field 'status': " + responseStr);
                        Platform.runLater(() -> setStatus("Phản hồi bị lỗi từ server!", "red"));
                        return;
                    }

                    String status = response.optString("status", "fail");
                    if (!status.equals("success")) {
                        Platform.runLater(() -> setStatus("Không thể tải bảng xếp hạng!", "red"));
                        return;
                    }

                    JSONArray rankingArray = response.optJSONArray("ranking");
                    if (rankingArray == null) {
                        Platform.runLater(() -> setStatus("Phản hồi bị thiếu dữ liệu!", "red"));
                        return;
                    }

                    List<Pair<User, Integer>> rankingList = new ArrayList<>();

                    for (int i = 0; i < rankingArray.length(); i++) {
                        JSONObject obj = rankingArray.getJSONObject(i);
                        System.out.println("🔹 Obj[" + i + "] = " + obj.toString(2));
                        User user = new User();
                        user.setUsername(obj.optString("username", ""));
                        user.setTotalRankScore(obj.optInt("score", 0));
                        int wins = obj.optInt("wins", 0);
                        rankingList.add(new Pair<>(user, wins));
                    }

                    rankingList.sort(
                            Comparator.comparingInt((Pair<User, Integer> pair) -> pair.getKey().getTotalRankScore())
                                    .reversed()
                                    .thenComparing(Comparator.comparingInt((Pair<User, Integer> pair) -> pair.getValue()).reversed())
                                    .thenComparing(pair -> pair.getKey().getUsername())
                    );

                    ObservableList<Pair<User, Integer>> data = FXCollections.observableArrayList(rankingList);

                    Platform.runLater(() -> {
                        rankingTable.setItems(data);
                        setStatus("Tải bảng xếp hạng thành công!", "green");
                    });

                    System.out.println("Loaded " + rankingList.size() + " ranking records");

                } catch (org.cloudinary.json.JSONException e) {
                    System.err.println("JSON Parse Error: " + e.getMessage());
                    Platform.runLater(() -> setStatus("Lỗi xử lý dữ liệu từ server!", "red"));
                    e.printStackTrace();
                } finally {
                    // ⭐ Khôi phục listener
                    if (wasListening) {
                        try {
                            clientSocket.connectListener("localhost", 8888);
                            Thread.sleep(100);
                        } catch (IOException e) {
                            System.err.println("Failed to reconnect listener: " + e.getMessage());
                        }
                    }
                }

            } catch (Exception e) {
                System.err.println("Error in ranking thread: " + e.getMessage());
                Platform.runLater(() -> setStatus("Lỗi khi tải bảng xếp hạng từ server!", "red"));
                e.printStackTrace();
            }
        }, "ranking-thread").start();
    }

    private void setStatus(String message, String color) {
        if (statusLabel != null) {
            statusLabel.setStyle("-fx-text-fill:" + color + ";");
            statusLabel.setText(message);
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/home.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Trang chủ");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            setStatus("Không thể quay lại Trang chủ!", "red");
        }
    }
}
