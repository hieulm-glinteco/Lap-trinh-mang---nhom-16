package com.mycompany.ltmproject.controller;

import com.mycompany.ltmproject.net.ClientSocket;
import com.mycompany.ltmproject.session.SessionManager;
import com.mycompany.ltmproject.util.DB;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import org.cloudinary.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Timer;
import java.util.TimerTask;

public class GameController {

    @FXML
    private Label lblTimeLeft;

    @FXML
    private Label scorePlayer1;

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

    private volatile int timeLeftSeconds = 30;
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

    @FXML
    public void initialize() {
        // Socket setup and listener
        socket = ClientSocket.getInstance();
        startListening();
        startTimer();

        btnSubmit.setOnAction(e -> handleSubmit());
    }

    public void setSessionInfo(int sessionId, boolean isHost) {
        this.sessionId = sessionId;
        this.isHost = isHost;
        // When session info arrives, request first round
        requestRoundFromServer();
    }

    private void startListening() {
        new Thread(() -> {
            try {
                socket.connectListener("localhost", 8888);
                // register listening
                try {
                    JSONObject start = new JSONObject();
                    start.put("action", "startListening");
                    start.put("userId", SessionManager.getCurrentUser().getId());
                    socket.sendOnListener(start.toString());
                } catch (Exception ignored) {}

                var in = socket.getListenerReader();
                String line;
                while (isRunning && (line = in.readLine()) != null) {
                    try {
                        JSONObject msg = new JSONObject(line);
                        String type = msg.optString("type", "");
                        if ("round_data".equals(type)) {
                            int sId = msg.getInt("sessionId");
                            if (this.sessionId != 0 && this.sessionId != sId) continue;
                            String url = msg.optString("imageUrl", null);
                            correctElephant = msg.optInt("n1", 0);
                            correctSeal = msg.optInt("n2", 0);
                            correctFox = msg.optInt("n3", 0);
                            Platform.runLater(() -> {
                                if (url != null && !url.isEmpty()) {
                                    try {
                                        javafx.scene.image.Image img = new javafx.scene.image.Image(url, true);
                                        animalImageView.setImage(img);
                                    } catch (Exception ignored) {}
                                }
                                // reset inputs
                                txtElephant.clear();
                                txtSeal.clear();
                                txtFox.clear();
                                btnSubmit.setDisable(false);
                                timeLeftSeconds = 30;
                                lblTimeLeft.setText("30s");
                            });
                        } else if ("score_update".equals(type)) {
                            int sId = msg.getInt("sessionId");
                            if (this.sessionId != 0 && this.sessionId != sId) continue;
                            int scoreP1 = msg.getInt("scoreP1");
                            int scoreP2 = msg.getInt("scoreP2");
                            Platform.runLater(() -> {
                                scorePlayer1.setText(String.valueOf(scoreP1));
                                scorePlayer2.setText(String.valueOf(scoreP2));
                            });
                        } else if ("game_end".equals(type)) {
                            int sId = msg.getInt("sessionId");
                            if (this.sessionId != 0 && this.sessionId != sId) continue;
                            int winner = msg.optInt("winner", 0);
                            Platform.runLater(() -> {
                                btnSubmit.setDisable(true);
                                // Simple end notification
                                javafx.scene.control.Alert a = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
                                a.setTitle("Kết thúc ván đấu");
                                a.setHeaderText(null);
                                a.setContentText(winner == 0 ? "Hòa" : ("Người thắng: " + (winner == SessionManager.getCurrentUser().getId() ? "Bạn" : "Đối thủ")));
                                a.showAndWait();
                            });
                        }
                    } catch (Exception ignored) {}
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
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
        int e = parseIntSafe(txtElephant.getText());
        int s = parseIntSafe(txtSeal.getText());
        int f = parseIntSafe(txtFox.getText());

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
                socket.send(req.toString());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    private int parseIntSafe(String v) {
        try {
            return Integer.parseInt(v.trim());
        } catch (Exception e) {
            return -1; // invalid numbers won't match
        }
    }
}


