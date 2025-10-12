package com.mycompany.ltmproject.controller;

import com.mycompany.ltmproject.dao.UserDAO;
import com.mycompany.ltmproject.model.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.event.ActionEvent;
import javafx.scene.Node;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.util.Pair;

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
    public void initialize() {
        colUsername.setCellValueFactory(pair -> new ReadOnlyObjectWrapper<>(pair.getValue().getKey().getUsername()));
        colScore.setCellValueFactory(pair -> new ReadOnlyObjectWrapper<>(pair.getValue().getKey().getTotalRankScore()));

        // Cột số trận thắng
        colWins = new TableColumn<>("Wins");
        colWins.setCellValueFactory(pair -> new ReadOnlyObjectWrapper<>(pair.getValue().getValue()));
        colWins.setPrefWidth(100);
        rankingTable.getColumns().add(colWins);

        // Cột Top
        TableColumn<Pair<User, Integer>, Number> colTop = new TableColumn<>("Top");
        colTop.setPrefWidth(50);
        colTop.setCellValueFactory(cellData
                -> new ReadOnlyObjectWrapper<>(rankingTable.getItems().indexOf(cellData.getValue()) + 1)
        );
        rankingTable.getColumns().add(0, colTop);

        loadRankingData();
    }

    private void loadRankingData() {
        List<User> users = UserDAO.getAllUsers();

        // Tạo danh sách Pair<User, Integer> (User + Số trận thắng)
        List<Pair<User, Integer>> userWithWinsList = new java.util.ArrayList<>();
        for (User user : users) {
            int winCount = UserDAO.countWins(user.getId());
            userWithWinsList.add(new Pair<>(user, winCount));
        }

        // Sắp xếp theo: trận thắng ↓, điểm ↓, tên ↑
//        userWithWinsList.sort(
//                Comparator.comparingInt(Pair::getValue).reversed() // Sắp xếp theo trận thắng giảm dần
//                        .thenComparing(pair -> pair.getKey().getTotalRankScore(), Comparator.reverseOrder()) // Nếu bằng nhau, sắp xếp theo điểm giảm dần
//                        .thenComparing(pair -> pair.getKey().getName()) // Nếu vẫn bằng nhau, sắp xếp theo tên tăng dần
//        );
        userWithWinsList.sort(
                Comparator.comparingInt((Pair<User, Integer> pair) -> pair.getValue()) // Sắp xếp theo trận thắng giảm dần
                         // Đảo lại để trận thắng giảm dần
                        .thenComparingInt(pair -> pair.getKey().getTotalRankScore()) // Nếu trận thắng bằng nhau, sắp xếp theo điểm giảm dần
                        .reversed() // Đảo lại để điểm giảm dần
                        .thenComparing(pair -> pair.getKey().getName()) // Nếu vẫn bằng nhau, sắp xếp theo tên tăng dần
        );

        ObservableList<Pair<User, Integer>> data = FXCollections.observableArrayList(userWithWinsList);
        rankingTable.setItems(data);
    }
     @FXML
    private void handleBack(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/home.fxml"));
        Stage stage = (Stage)((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
}
