package com.mycompany.ltmproject.controller;

<<<<<<< HEAD
import com.mycompany.ltmproject.net.ClientSocket;
import com.mycompany.ltmproject.session.SessionManager;
import com.mycompany.ltmproject.model.User;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import org.cloudinary.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

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

	private Timeline countdown;
	private int timeLeftSeconds = 30;
	private int myScore = 0;
	private int opponentScore = 0;
	private int opponentUserId;
	private String opponentUsername;
	private final ClientSocket clientSocket = ClientSocket.getInstance();
	private final User currentUser = SessionManager.getCurrentUser();

	@FXML
	public void initialize() {
		// Load placeholder image if available
		try {
			Image img = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/Pic1.png")));
			animalImageView.setImage(img);
		} catch (Exception ignored) { }

		btnSubmit.setOnAction(e -> handleSubmit());
		startCountdown();
		updateScoreLabels();
	}

	public void setMatchContext(int opponentUserId, String opponentUsername) {
		this.opponentUserId = opponentUserId;
		this.opponentUsername = opponentUsername;
	}

	private void startCountdown() {
		lblTimeLeft.setText(timeLeftSeconds + "s");
		countdown = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
			timeLeftSeconds--;
			lblTimeLeft.setText(timeLeftSeconds + "s");
			if (timeLeftSeconds <= 0) {
				countdown.stop();
				handleSubmit();
			}
		}));
		countdown.setCycleCount(Timeline.INDEFINITE);
		countdown.playFromStart();
	}

	private void handleSubmit() {
		btnSubmit.setDisable(true);
		if (countdown != null) countdown.stop();

		int elephant = parseSafe(txtElephant.getText());
		int seal = parseSafe(txtSeal.getText());
		int fox = parseSafe(txtFox.getText());

		// Send to server for validation/scoring if protocol exists
		new Thread(() -> {
			try {
				JSONObject request = new JSONObject();
				request.put("action", "submitRound");
				request.put("fromUserId", currentUser != null ? currentUser.getId() : -1);
				request.put("toUserId", opponentUserId);
				request.put("elephant", elephant);
				request.put("seal", seal);
				request.put("fox", fox);

				clientSocket.send(request.toString());
				String resp = clientSocket.receive();

				Platform.runLater(() -> handleServerScore(resp));
			} catch (IOException ex) {
				Platform.runLater(() -> {
					// Fallback local scoring if server not available
					localScore(elephant, seal, fox);
				});
			}
		}).start();
	}

	private void handleServerScore(String resp) {
		try {
			if (resp == null || resp.isEmpty()) {
				// Fallback local scoring
				localScore(parseSafe(txtElephant.getText()), parseSafe(txtSeal.getText()), parseSafe(txtFox.getText()));
				return;
			}
			JSONObject json = new JSONObject(resp);
			String status = json.optString("status", "fail");
			if (!"success".equals(status)) {
				localScore(parseSafe(txtElephant.getText()), parseSafe(txtSeal.getText()), parseSafe(txtFox.getText()));
				return;
			}
			int myDelta = json.optInt("myDelta", 0);
			int oppDelta = json.optInt("oppDelta", 0);
			myScore += myDelta;
			opponentScore += oppDelta;
			updateScoreLabels();
			showInfo("Kết quả", "Bạn +" + myDelta + " điểm. Đối thủ +" + oppDelta + " điểm.");
		} catch (Exception e) {
			localScore(parseSafe(txtElephant.getText()), parseSafe(txtSeal.getText()), parseSafe(txtFox.getText()));
		}
	}

	private void localScore(int elephant, int seal, int fox) {
		// Simple local scoring: award 1 point per non-negative entry as placeholder
		int delta = 0;
		if (elephant >= 0) delta++;
		if (seal >= 0) delta++;
		if (fox >= 0) delta++;
		myScore += delta;
		updateScoreLabels();
		showInfo("Kết quả (tạm)", "Bạn +" + delta + " điểm.");
	}

	private void updateScoreLabels() {
		scorePlayer1.setText(String.valueOf(myScore));
		scorePlayer2.setText(String.valueOf(opponentScore));
	}

	private int parseSafe(String s) {
		try {
			return Integer.parseInt(s == null ? "0" : s.trim());
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	private void showInfo(String title, String message) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}
}


=======
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
>>>>>>> origin/HoangND
