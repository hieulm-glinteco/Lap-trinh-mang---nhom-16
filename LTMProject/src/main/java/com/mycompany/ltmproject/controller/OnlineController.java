package com.mycompany.ltmproject.controller;

<<<<<<< HEAD
import com.mycompany.ltmproject.net.ClientSocket;
import com.mycompany.ltmproject.session.SessionManager;
import com.mycompany.ltmproject.model.User;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.cloudinary.json.JSONArray;
import org.cloudinary.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
=======
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
>>>>>>> origin/HoangND

public class OnlineController {

    @FXML
<<<<<<< HEAD
    private TableView<OnlinePlayerModel> onlineTable;

    @FXML
    private TableColumn<OnlinePlayerModel, Integer> colIndex;

    @FXML
    private TableColumn<OnlinePlayerModel, String> colUsername;

    @FXML
    private TableColumn<OnlinePlayerModel, Button> colInvite;

    @FXML
    private Label statusLabel;

    @FXML
    private Button backButton;

    private ObservableList<OnlinePlayerModel> playersList;
    private ClientSocket clientSocket;
    private User currentUser;
    private Thread listenerThread;
    private volatile boolean isRunning = true;

    @FXML
    public void initialize() {
        currentUser = SessionManager.getCurrentUser();
        clientSocket = ClientSocket.getInstance();

        setupTable();
        loadOnlinePlayers();
        startListeningForUpdates();
    }

    private void setupTable() {
        colIndex.setCellValueFactory(new PropertyValueFactory<>("index"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colInvite.setCellValueFactory(new PropertyValueFactory<>("inviteButton"));

        playersList = FXCollections.observableArrayList();
        onlineTable.setItems(playersList);
    }

    private void loadOnlinePlayers() {
        new Thread(() -> {
            try {
                JSONObject request = new JSONObject();
                request.put("action", "getOnlinePlayers");
                request.put("userId", currentUser.getId());

                clientSocket.send(request.toString());
                String response = clientSocket.receive();

                JSONObject jsonResponse = new JSONObject(response);

                if ("success".equals(jsonResponse.getString("status"))) {
                    JSONArray playersArray = jsonResponse.getJSONArray("players");
                    int count = jsonResponse.getInt("count");

                    Platform.runLater(() -> {
                        updatePlayersList(playersArray);
                        statusLabel.setText("👥 Online: " + count + " người chơi");
                    });
                }
            } catch (IOException e) {
                System.err.println("❌ Error loading online players: " + e.getMessage());
                Platform.runLater(() -> statusLabel.setText("❌ Lỗi kết nối"));
            }
        }).start();
    }

    private void updatePlayersList(JSONArray playersArray) {
        playersList.clear();

        for (int i = 0; i < playersArray.length(); i++) {
            JSONObject player = playersArray.getJSONObject(i);
            int playerId = player.getInt("id");
            String username = player.getString("username");

            Button inviteBtn = createInviteButton(playerId, username);

            OnlinePlayerModel model = new OnlinePlayerModel(
                    i + 1,
                    username,
                    inviteBtn,
                    playerId
            );
            playersList.add(model);
        }
    }

    private void startListeningForUpdates() {
        listenerThread = new Thread(() -> {
            try {
                // Tạo kết nối listener riêng
                clientSocket.connectListener("localhost", 8888);
                BufferedReader in = clientSocket.getListenerReader();

                String message;
                while (isRunning && (message = in.readLine()) != null) {
                    try {
                        JSONObject json = new JSONObject(message);
                        String type = json.optString("type", "");

                        if ("online_update".equals(type)) {
                            System.out.println("🔄 Received realtime update");
                            JSONArray players = json.getJSONArray("players");
                            int count = json.getInt("count");

                            Platform.runLater(() -> {
                                updatePlayersList(players);
                                statusLabel.setText("👥 Online: " + count + " người chơi");
                            });
                        } else if ("invitation".equals(type)) {
                            String fromUsername = json.getString("fromUsername");
                            int fromUserId = json.getInt("fromUserId");

                            Platform.runLater(() -> {
                                handleInvitationReceived(fromUsername, fromUserId);
                            });
                        } else if ("invite_response".equals(type)) {
                            String fromUsername = json.getString("fromUsername");
                            boolean accepted = json.getBoolean("accepted");

                            Platform.runLater(() -> {
                                if (accepted) {
                                        showAlert("🎮 Trận đấu bắt đầu!", fromUsername + " đã chấp nhận lời mời của bạn!");
                                        try {
                                            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/gameInterface.fxml"));
                                            Parent root = loader.load();
                                            com.mycompany.ltmproject.controller.GameController gameController = loader.getController();
                                            if (gameController != null) {
                                                gameController.setMatchContext(-1, fromUsername);
                                            }
                                            Stage stage = (Stage) statusLabel.getScene().getWindow();
                                            stage.setScene(new Scene(root, 800, 600));
                                            stage.setTitle("Trận đấu với " + fromUsername);
                                            stage.show();
                                        } catch (IOException ex) {
                                            Logger.getLogger(OnlineController.class.getName()).log(Level.SEVERE, null, ex);
                                        }

                                } else {
                                    showAlert("Từ chối", fromUsername + " đã từ chối lời mời của bạn");
                                }
                            });
                        }

                    } catch (Exception e) {
                        System.err.println("⚠️ Error parsing message: " + e.getMessage());
                    }
                }
            } catch (IOException e) {
                System.err.println("❌ Listener thread stopped: " + e.getMessage());
            }
        });

        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    private void handleInvitationReceived(String fromUsername, int fromUserId) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Lời mời chơi");
        alert.setHeaderText(null);
        alert.setContentText(fromUsername + " đã mời bạn chơi!");

        ButtonType acceptBtn = new ButtonType("Chấp nhận");
        ButtonType declineBtn = new ButtonType("Từ chối", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(acceptBtn, declineBtn);

        alert.showAndWait().ifPresent(result -> {
            if (result == acceptBtn) {
                System.out.println("✅ Accepted invitation from " + fromUsername);
                sendInviteResponse(fromUserId, true);
                showAlert("Bắt đầu", "🎮 Bắt đầu trận đấu với " + fromUsername + "!");
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/gameInterface.fxml"));
                    Parent root = loader.load();
                    com.mycompany.ltmproject.controller.GameController gameController = loader.getController();
                    if (gameController != null) {
                        gameController.setMatchContext(fromUserId, fromUsername);
                    }
                    Stage stage = (Stage) statusLabel.getScene().getWindow();
                    stage.setScene(new Scene(root, 800, 600));
                    stage.setTitle("Trận đấu với " + fromUsername);
                    stage.show();
                } catch (IOException ex) {
                    Logger.getLogger(OnlineController.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                System.out.println("❌ Declined invitation from " + fromUsername);
                sendInviteResponse(fromUserId, false);
            }

        });
    }

    private Button createInviteButton(int playerId, String username) {
        Button btn = new Button("Mời");
        btn.setStyle(
                "-fx-background-color: #2193b0; "
                + "-fx-text-fill: white; "
                + "-fx-padding: 6px 12px; "
                + "-fx-border-radius: 5; "
                + "-fx-cursor: hand;"
                + "-fx-font-weight: bold;"
        );

        btn.setOnAction(e -> handleInvite(playerId, username, btn));

        return btn;
    }

    private void handleInvite(int playerId, String username, Button btn) {
        new Thread(() -> {
            try {
                JSONObject invitation = new JSONObject();
                invitation.put("action", "sendInvite");
                invitation.put("fromUserId", currentUser.getId());
                invitation.put("toUserId", playerId);
                invitation.put("fromUsername", currentUser.getUsername());

                clientSocket.send(invitation.toString());
                String response = clientSocket.receive();

                JSONObject jsonResponse = new JSONObject(response);

                Platform.runLater(() -> {
                    if ("success".equals(jsonResponse.getString("status"))) {
                        showAlert("Thành công", "✅ Đã gửi lời mời cho " + username);
                        btn.setDisable(true);
                        btn.setText("Đã mời");
                    } else {
                        String error = jsonResponse.optString("error", "");
                        if ("user_offline".equals(error)) {
                            showAlert("Lỗi", "❌ " + username + " đã offline");
                        } else {
                            showAlert("Lỗi", "❌ Không thể gửi lời mời");
                        }
                    }
                });
            } catch (IOException e) {
                System.err.println("❌ Error sending invite: " + e.getMessage());
                Platform.runLater(() -> showAlert("Lỗi", "❌ Lỗi kết nối"));
            }
        }).start();
    }

    private void sendInviteResponse(int toUserId, boolean accepted) {
        new Thread(() -> {
            JSONObject response = new JSONObject();
            response.put("action", "respondInvite");
            response.put("fromUserId", currentUser.getId());
            response.put("toUserId", toUserId);
            response.put("fromUsername", currentUser.getUsername());
            response.put("accepted", accepted);
            clientSocket.send(response.toString());
        }).start();
    }

    @FXML
    public void handleBack(ActionEvent event) {
        try {
            isRunning = false;

            // ⭐ QUAN TRỌNG: Đóng listener socket NGAY
            clientSocket.disconnectListener();

            // Chờ thread kết thúc
            if (listenerThread != null && listenerThread.isAlive()) {
                listenerThread.interrupt();
                try {
                    listenerThread.join(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            Thread.sleep(200); // Chờ thêm để listener hoàn toàn đóng

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/home.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 800, 520));
            stage.setTitle("Trang chủ");
            stage.show();
        } catch (Exception e) {
=======
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
>>>>>>> origin/HoangND
            e.printStackTrace();
        }
    }

<<<<<<< HEAD
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class OnlinePlayerModel {

        private int index;
        private String username;
        private Button inviteButton;
        private int userId;

        public OnlinePlayerModel(int index, String username, Button inviteButton, int userId) {
            this.index = index;
            this.username = username;
            this.inviteButton = inviteButton;
            this.userId = userId;
        }

        public int getIndex() {
            return index;
        }

        public String getUsername() {
            return username;
        }

        public Button getInviteButton() {
            return inviteButton;
        }

        public int getUserId() {
            return userId;
=======
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
>>>>>>> origin/HoangND
        }
    }
}
