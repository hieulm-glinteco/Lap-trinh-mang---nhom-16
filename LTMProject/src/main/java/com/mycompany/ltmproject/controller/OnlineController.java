package com.mycompany.ltmproject.controller;

import com.mycompany.ltmproject.model.User;
import com.mycompany.ltmproject.net.ClientSocket;
import com.mycompany.ltmproject.session.SessionManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.cloudinary.json.*;

import java.io.IOException;
import java.util.List;

public class OnlineController {

    @FXML
    private TableView<User> onlineTable;
    @FXML
    private TableColumn<User, Integer> colIndex;
    @FXML
    private TableColumn<User, String> colUsername;
    @FXML
    private TableColumn<User, Void> colInvite;
    @FXML
    private Button backButton;

    private final ObservableList<User> onlineUsers = FXCollections.observableArrayList();
    private static boolean listeningStarted = false;

    @FXML
    public void initialize() {
        setupTable();

        List<String> saved = ClientSocket.getInstance().getOnlineUsers();
        updateOnlineList(saved);

        requestOnlineList();

        if (!listeningStarted) {
            ClientSocket.getInstance().startListening(msg -> {
                JSONObject json = new JSONObject(msg);
                String action = json.optString("action", "");

                if ("updateOnline".equals(action)) {
                    JSONArray arr = json.getJSONArray("online");
                    ClientSocket.getInstance().updateOnlineUsers(arr);
                    Platform.runLater(() -> {
                        updateOnlineList(ClientSocket.getInstance().getOnlineUsers());
                    });
                }

                else if ("invite".equals(action)) {
                    String from = json.getString("from");
                    Platform.runLater(() -> showInvitePopup(from));
                }

                else if ("start_game".equals(action)) {
                    System.out.println("🔥 Received start_game: " + msg);
                    String roomId = json.getString("roomId");
                    JSONArray players = json.getJSONArray("players");
                    Platform.runLater(() -> openGameRoom(roomId, players));
                }
            });
            listeningStarted = true;
        }
    }

    private void setupTable() {
        colIndex.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));

        colInvite.setCellFactory(col -> new TableCell<>() {
            private final Button inviteBtn = new Button("Mời");
            {
                inviteBtn.setStyle("-fx-background-color: #2193b0; -fx-text-fill: white; -fx-background-radius: 6;");
                inviteBtn.setOnAction(e -> {
                    User user = getTableView().getItems().get(getIndex());
                    if (user != null) {
                        JSONObject invite = new JSONObject();
                        invite.put("action", "invite");
                        invite.put("target", user.getUsername());
                        ClientSocket.getInstance().send(invite.toString());
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : inviteBtn);
            }
        });

        onlineTable.setItems(onlineUsers);
    }

    private void updateOnlineList(List<String> list) {
        onlineUsers.clear();
        String current = SessionManager.getCurrentUser().getUsername();
        int index = 1;
        for (String username : list) {
            if (!username.equals(current)) { // ⚡ Không hiển thị chính mình
                onlineUsers.add(new User(index++, "", username, "", "", "", 0, null));
            }
        }
    }

    private void requestOnlineList() {
        JSONObject req = new JSONObject();
        req.put("action", "getOnline");
        ClientSocket.getInstance().send(req.toString());
    }

    private void showInvitePopup(String fromUser) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Lời mời chơi");
        alert.setHeaderText(null);
        alert.setContentText("Người chơi " + fromUser + " mời bạn chơi. Bạn có đồng ý?");
        ButtonType accept = new ButtonType("Đồng ý", ButtonBar.ButtonData.OK_DONE);
        ButtonType decline = new ButtonType("Từ chối", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(accept, decline);

        alert.showAndWait().ifPresent(type -> {
            JSONObject res = new JSONObject();
            res.put("action", "invite_response");
            res.put("target", fromUser);
            res.put("accepted", type == accept);
            ClientSocket.getInstance().send(res.toString());
        });
    }

    private void openGameRoom(String roomId, JSONArray players) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/gameInterface.fxml"));
            Parent root = loader.load();
            GameController controller = loader.getController();
            controller.setRoomId(roomId);
            controller.setPlayers(players);
            Stage stage = (Stage) onlineTable.getScene().getWindow();
            stage.setScene(new Scene(root, 800, 520));
            stage.setTitle("Phòng " + roomId);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBack(javafx.event.ActionEvent e) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/home.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 800, 520));
            stage.setTitle("Trang chủ");
            stage.show();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
