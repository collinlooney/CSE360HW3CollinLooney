package application;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import databasePart1.DatabaseHelper; 

/**
 * This page displays a simple welcome message for the user.
 */

public class UserHomePage {
	
	private final DatabaseHelper databaseHelper;
	public UserHomePage(DatabaseHelper databaseHelper) { // now uses databaseHelper CL 
        this.databaseHelper = databaseHelper;
    }

    public void show(Stage primaryStage) {
    	
        BorderPane rootLayout = new BorderPane();

        // --- 1. Create the Top Bar with a Logout Button ---
        HBox topBar = new HBox();
        topBar.setPadding(new Insets(10, 15, 10, 15)); // Add some spacing around the bar
        topBar.setAlignment(Pos.CENTER_RIGHT); // This pushes content to the far right

        Button logoutButton = new Button("Logout");
        logoutButton.setOnAction(e -> new UserLoginPage(databaseHelper).show(primaryStage));

        topBar.getChildren().add(logoutButton);
        rootLayout.setTop(topBar);
        
    	VBox centerContent = new VBox();
    	centerContent.setAlignment(Pos.CENTER);
    	centerContent.setSpacing(10);
    	
	    //layout.getChildren().add(Logout.LogoutButton(primaryStage, databaseHelper)); //logout button CL 
	    // Label to display Hello user
	    Label userLabel = new Label("Hello, User!");
	    userLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

	    centerContent.getChildren().add(userLabel);
	    
	    rootLayout.setCenter(centerContent);
	    Scene userScene = new Scene(rootLayout, 800, 400);

	    // Set the scene to primary stage
	    primaryStage.setScene(userScene);
	    primaryStage.setTitle("User Page");
    	
    }
}