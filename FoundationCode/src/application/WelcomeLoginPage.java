package application;

import databasePart1.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * The WelcomeLoginPage class displays a welcome screen for authenticated users.
 * It uses a clean layout with a logout button in the top-right corner.
 */
public class WelcomeLoginPage {

    private final DatabaseHelper databaseHelper;

    public WelcomeLoginPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    public void show(Stage primaryStage, User user) {

        BorderPane rootLayout = new BorderPane();

        // --- 1. Create the Top Bar with a Logout Button ---
        HBox topBar = new HBox();
        topBar.setPadding(new Insets(10, 15, 10, 15)); // Add some spacing around the bar
        topBar.setAlignment(Pos.CENTER_RIGHT); // This pushes content to the far right

        Button logoutButton = new Button("Logout");
        logoutButton.setOnAction(e -> new UserLoginPage(databaseHelper).show(primaryStage));

        topBar.getChildren().add(logoutButton);
        rootLayout.setTop(topBar);

        // --- 2. Create the Center Content for primary navigation ---
        VBox centerContent = new VBox(15);
        centerContent.setStyle("-fx-alignment: center; -fx-padding: 20;");

        Label welcomeLabel = new Label("Welcome!");
        welcomeLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");
        centerContent.getChildren().add(welcomeLabel);

        Button defaultRoleButton = null;

        // Create buttons for each user role
        for (Role r : user.getRoles()) {
            Button roleButton = new Button("Continue as " + r.display());
            roleButton.setPrefWidth(200);
            roleButton.setOnAction(a -> {
                if (r == Role.ADMIN) {
                    new AdminHomePage(databaseHelper).show(primaryStage, user);
                } else if (r == Role.BASIC_USER) {
                    new UserHomePage(databaseHelper).show(primaryStage);
                }
            });

            // Determine which button should get default focus
            if (r == Role.ADMIN) {
                defaultRoleButton = roleButton;
            } else if (r == Role.BASIC_USER && defaultRoleButton == null && !user.hasAdmin()) {
                // BUG FIX: Assign the actual roleButton, not a new one.
                defaultRoleButton = roleButton;
            }
            centerContent.getChildren().add(roleButton);
        }

        rootLayout.setCenter(centerContent);

        // --- 3. Set up the Scene and Stage ---
        Scene welcomeScene = new Scene(rootLayout, 800, 400);
        primaryStage.setScene(welcomeScene);
        primaryStage.setTitle("Welcome Page");

        // Set initial focus on the most relevant role button
        if (defaultRoleButton != null) {
            Button toFocus = defaultRoleButton;
            Platform.runLater(toFocus::requestFocus);
        }
    }

}