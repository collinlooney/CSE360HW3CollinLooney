package application;

import javafx.geometry.Pos;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import javafx.stage.Stage;

import databasePart1.DatabaseHelper; // added for use with database C.L.


/**
 * AdminPage class represents the user interface for the admin user.
 * This page displays a simple welcome message for the admin.
 */

public class AdminHomePage {
	/**
     * Displays the admin page in the provided primary stage.
     * @param primaryStage The primary stage where the scene will be displayed.
     */
	 
	private final DatabaseHelper databaseHelper; 
	public AdminHomePage (DatabaseHelper databaseHelper) { //identical implementation to WelcomeLoginPage.java C.L.
		 this.databaseHelper = databaseHelper; 
	 }
	
	public void show(Stage primaryStage) { // How I implemented AdminListAllUsers; leaving here for now 
        try {
            String current = databaseHelper.getCurrentUserName();
            if (current != null && !current.isBlank()) {
                User u = databaseHelper.getUserByUserName(current);
                if (u != null) {
                    show(primaryStage, u);
                    return;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        new SetupLoginSelectionPage(databaseHelper).show(primaryStage);
    }
	
	public void show(Stage primaryStage, User user) {
	    // Root layout
	    BorderPane layout = new BorderPane();

	    // --- Top menubar: Session + User Management (admin)
	    layout.setTop(TopMenuLayout.createAdminMenuBar(databaseHelper, primaryStage, user));

	    // --- Center content (match UserHomePage wiring)
	    VBox centerContent = new VBox(20);
	    centerContent.setAlignment(Pos.CENTER);

	    Label adminLabel = new Label("Welcome, " + (user != null ? user.getName() : "Admin") + "!");
	    adminLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
	    
	    Label lowerLabel = new Label("You are currently logged in as an Admin.");
	    lowerLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #555555;");

	    Button askButton = new Button("Ask a New Question");
	    askButton.setPrefSize(300, 60);
	    askButton.setFont(Font.font(18));
	    askButton.setOnAction(e -> new AskQuestionView(databaseHelper, true).show(primaryStage, user));

	    Button discussButton = new Button("Go to Discussion Board");
	    discussButton.setPrefSize(300, 60);
	    discussButton.setFont(Font.font(18));
	    discussButton.setOnAction(e -> new DiscussionBoardView(databaseHelper, true).show(primaryStage, user));

	    centerContent.getChildren().addAll(adminLabel, lowerLabel, askButton, discussButton);
	    layout.setCenter(centerContent);

	    Scene adminScene = new Scene(layout, 800, 400);
	    primaryStage.setScene(adminScene);
	    primaryStage.setTitle("Admin Dashboard");
	}

}
