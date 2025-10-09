package application;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
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

    public void show(Stage primaryStage, User user) {
    	
        BorderPane rootLayout = new BorderPane();

        // --- 1. Create the Top Bar with a Logout Button ---
       
        rootLayout.setTop(TopMenuLayout.createSessionMenuBar(databaseHelper, primaryStage, user));
        
    	VBox centerContent = new VBox(20);
    	centerContent.setAlignment(Pos.CENTER);
    	centerContent.setSpacing(10);
    	
	    //layout.getChildren().add(Logout.LogoutButton(primaryStage, databaseHelper)); //logout button CL 
	    // Label to display Hello user
	    Label userLabel = new Label("Welcome, " + user.getName() + "!");
	    userLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
	    
        Button askButton = new Button("Ask a New Question");
        askButton.setPrefSize(300, 60);
        askButton.setFont(Font.font(18));
        askButton.setOnAction(e -> new AskQuestionView(databaseHelper, false).show(primaryStage, user));
        
        
        Button discussButton = new Button("Go to Discussion Board");
        discussButton.setPrefSize(300, 60);
        discussButton.setFont(Font.font(18));
        discussButton.setOnAction(e -> new DiscussionBoardView(databaseHelper, false).show(primaryStage, user));
        
        
	    centerContent.getChildren().addAll(userLabel, askButton, discussButton);
	    
	    rootLayout.setCenter(centerContent);
	    Scene userScene = new Scene(rootLayout, 800, 400);

	    // Set the scene to primary stage
	    primaryStage.setScene(userScene);
	    primaryStage.setTitle("User Page");
	    
	    
    	
    }
}