package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import databasePart1.DatabaseHelper;

public class AdminUpdateUsers {
    
    private final DatabaseHelper databaseHelper;
    private final User user;

    public AdminUpdateUsers(DatabaseHelper databaseHelper, User user) {
        this.databaseHelper = databaseHelper;
        this.user = user; 
    }

    public void show(Stage primaryStage) {
        VBox layout = new VBox(10);
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");
        
        Label title = new Label("Admin: Update Users");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        Label placeholder = new Label("This is the AdminUpdateUsers page. (Work in progress) ");
        
        Button backButton = new Button("Back");
        backButton.setOnAction(e -> {
            AdminHomePage adminHomePage = new AdminHomePage(databaseHelper);
            adminHomePage.show(primaryStage);
        });

        layout.getChildren().addAll(title, placeholder, backButton);
        
        Scene scene = new Scene(layout, 400, 300);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
