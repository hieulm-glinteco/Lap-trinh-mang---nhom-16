package com.mycompany.ltmproject.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;          // <-- thêm import này
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
  @Override public void start(Stage stage) throws Exception {
    Parent root = FXMLLoader.load(App.class.getResource("/fxml/login.fxml"));
    stage.setScene(new Scene(root, 800, 500));
    stage.setTitle("Count Game");
    stage.show();
  }
  public static void main(String[] args){ launch(args); }
}
