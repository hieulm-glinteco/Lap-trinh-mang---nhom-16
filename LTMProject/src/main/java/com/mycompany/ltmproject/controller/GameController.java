package com.mycompany.ltmproject.controller;

import com.mycompany.ltmproject.net.ClientSocket;
import com.mycompany.ltmproject.session.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import org.cloudinary.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import java.io.IOException;

public class GameController {

    @FXML
    private Label lblTimeLeft;

    @FXML
    private Label scorePlayer1;

    @FXML
    private Label lblRound;

    @FXML
    private Label scorePlayer2;

    @FXML
    private ImageView animalImageView;

    @FXML
    private TextField txtElephant;

    @FXML
    private TextField txtSeal;

    @FXML
    private TextField txtFox;

    @FXML
    private Button btnSubmit;
    
    @FXML
    private Button btnQuit;

    private volatile int timeLeftSeconds = 60;
    private Timer timer;

    // Correct answers loaded from DB Image table
    private int correctElephant;
    private int correctSeal;
    private int correctFox;

    private int sessionId = 0;
    private boolean isHost = false;
    private ClientSocket socket;
    private Thread listenerThread;
    private volatile boolean isRunning = true;

    private int roundIndex = 0;
    private final int totalRounds = 5;

    @FXML
    public void initialize() {
        // Socket setup and listener
        socket = ClientSocket.getInstance();
        startListening();
        if (lblRound != null) {
            lblRound.setText("Vòng 0/" + totalRounds);
        }

        btnSubmit.setOnAction(e -> handleSubmit());
        btnQuit.setOnAction(e -> handleQuit());
    }

    public void setSessionInfo(int sessionId, boolean isHost) {
        this.sessionId = sessionId;
        this.isHost = isHost;
        if (isHost && roundIndex == 0) {
            requestRoundFromServer();
        }
    }

    private void startListening() {
        listenerThread = new Thread(() -> {
            try {
                socket.connectListener("localhost", 8888);
                // register listening
                try {
                    JSONObject start = new JSONObject();
                    start.put("action", "startListening");
                    start.put("userId", SessionManager.getCurrentUser().getId());
                    socket.sendOnListener(start.toString());
                } catch (Exception ignored) {
                }

                var in = socket.getListenerReader();
                String line;
                while (isRunning && (line = in.readLine()) != null) {
                    try {
                        JSONObject msg = new JSONObject(line);
                        String type = msg.optString("type", "");
                        if ("round_data".equals(type)) {
                            int sId = msg.getInt("sessionId");
                            if (this.sessionId != 0 && this.sessionId != sId) {
                                return;
                            }

                            String url = msg.optString("imageUrl", null);
                            correctElephant = msg.optInt("n1", 0);
                            correctSeal = msg.optInt("n2", 0);
                            correctFox = msg.optInt("n3", 0);

                            roundIndex++; // tăng số vòng hiện tại

                            Platform.runLater(() -> {
                                if (url != null && !url.isEmpty()) {
                                    try {
                                        javafx.scene.image.Image img = new javafx.scene.image.Image(url, true);
                                        animalImageView.setImage(img);
                                    } catch (Exception ignored) {
                                    }
                                }

                                // reset inputs
                                txtElephant.clear();
                                txtSeal.clear();
                                txtFox.clear();
                                btnSubmit.setDisable(false);
                                lblTimeLeft.setText("60s");
                                timeLeftSeconds = 60;
                                if (lblRound != null) {
                                    lblRound.setText("Vòng " + roundIndex + "/" + totalRounds);
                                }

                                // restart countdown timer
                                if (timer != null) {
                                    timer.cancel();
                                }
                                startTimer();
                            });
                        } else if ("score_update".equals(type)) {
                            int sId = msg.getInt("sessionId");
                            if (this.sessionId != 0 && this.sessionId != sId) {
                                continue;
                            }
                            int scoreP1 = msg.getInt("scoreP1");
                            int scoreP2 = msg.getInt("scoreP2");
                            Platform.runLater(() -> {
                                if (isHost) {
                                    scorePlayer1.setText(String.valueOf(scoreP1));
                                    scorePlayer2.setText(String.valueOf(scoreP2));
                                } else {
                                    scorePlayer1.setText(String.valueOf(scoreP2));
                                    scorePlayer2.setText(String.valueOf(scoreP1));
                                }
                            });
                        } else if ("game_end".equals(type)) {
                            int sId = msg.getInt("sessionId");
                            if (this.sessionId != 0 && this.sessionId != sId) {
                                return;
                            }

                            int scoreP1 = msg.getInt("scoreP1");
                            int scoreP2 = msg.getInt("scoreP2");
                            int winner = msg.getInt("winner");
                            Platform.runLater(() -> {
                                if (timer != null) {
                                    timer.cancel();
                                }
                                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                String result;
                                int myId = SessionManager.getCurrentUser().getId();
                                if (winner == 0) {
                                    result = "Hòa!";
                                } else if (winner == myId) {
                                    result = "Bạn thắng!";
                                } else {
                                    result = "Bạn thua!";
                                }
                                alert.setTitle("Kết quả trận đấu");
                                alert.setHeaderText(result);
                                alert.setContentText("Điểm của bạn: " + (winner == myId ? scoreP1 : scoreP2)
                                        + "\nĐiểm đối thủ: " + (winner == myId ? scoreP2 : scoreP1));
                                alert.showAndWait();

                                // Dọn dẹp tài nguyên trước khi điều hướng
                                isRunning = false;
                                if (timer != null) {
                                    timer.cancel();
                                }
                                
                                // Đóng listener socket
                                socket.disconnectListener();
                                
                                // Chờ listener thread kết thúc
                                if (listenerThread != null && listenerThread.isAlive()) {
                                    listenerThread.interrupt();
                                    try {
                                        listenerThread.join(1000);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                                
                                // Quay về màn hình chính
                                try {
                                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/home.fxml"));
                                    Parent root = loader.load();
                                    Stage stage = (Stage) btnSubmit.getScene().getWindow();
                                    stage.setScene(new Scene(root, 800, 520));
                                    stage.setTitle("Trang chủ");
                                    stage.show();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            });
                        }

                    } catch (Exception ignored) {
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        listenerThread.start();
    }

    private void requestRoundFromServer() {
        new Thread(() -> {
            try {
                JSONObject req = new JSONObject();
                req.put("action", "requestRound");
                req.put("sessionId", sessionId);
                socket.send(req.toString());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    private void startTimer() {
        lblTimeLeft.setText(timeLeftSeconds + "s");
        timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                timeLeftSeconds--;
                Platform.runLater(() -> lblTimeLeft.setText(timeLeftSeconds + "s"));
                if (timeLeftSeconds <= 0) {
                    timer.cancel();
                    Platform.runLater(() -> {
                        btnSubmit.setDisable(true);
                        evaluateAndUpdateScore();
                    });
                }
            }
        }, 1000, 1000);
    }

    private void handleSubmit() {
        btnSubmit.setDisable(true);
        if (timer != null) {
            timer.cancel();
        }
        evaluateAndUpdateScore();
    }

    private void evaluateAndUpdateScore() {
        String elephantInput = txtElephant.getText() == null ? "" : txtElephant.getText().trim();
        String sealInput = txtSeal.getText() == null ? "" : txtSeal.getText().trim();
        String foxInput = txtFox.getText() == null ? "" : txtFox.getText().trim();

        int e = parseIntSafe(elephantInput);
        int s = parseIntSafe(sealInput);
        int f = parseIntSafe(foxInput);

        boolean hasAnswer = !elephantInput.isEmpty() || !sealInput.isEmpty() || !foxInput.isEmpty();
        String status;
        if (!hasAnswer) {
            status = "no_answer";
        } else if (e == correctElephant && s == correctSeal && f == correctFox) {
            status = "correct";
        } else {
            status = "wrong";
        }

        // Submit to server; server will compute and broadcast updated scores
        new Thread(() -> {
            try {
                JSONObject req = new JSONObject();
                req.put("action", "submitAnswers");
                req.put("sessionId", sessionId);
                req.put("userId", SessionManager.getCurrentUser().getId());
                req.put("e", e);
                req.put("s", s);
                req.put("f", f);
                req.put("n1", correctElephant);
                req.put("n2", correctSeal);
                req.put("n3", correctFox);
                req.put("status", status);
                req.put("roundIndex", roundIndex);
                socket.send(req.toString());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    private void handleQuit() {
        // Xác nhận với người chơi trước khi thoát
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Xác nhận thoát");
        confirmAlert.setHeaderText("Bạn có chắc muốn thoát trận đấu?");
        confirmAlert.setContentText("Bạn sẽ bị tính là thua cuộc nếu thoát.");
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Gửi action playerQuit lên server
                new Thread(() -> {
                    try {
                        JSONObject req = new JSONObject();
                        req.put("action", "playerQuit");
                        req.put("sessionId", sessionId);
                        req.put("userId", SessionManager.getCurrentUser().getId());
                        socket.send(req.toString());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }).start();
            }
        });
    }

    private int parseIntSafe(String v) {
        try {
            return Integer.parseInt(v.trim());
        } catch (Exception e) {
            return -1; // invalid numbers won't match
        }
    }
}
