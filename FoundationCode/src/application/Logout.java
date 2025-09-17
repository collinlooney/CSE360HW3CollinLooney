package application;

import javafx.scene.control.Button;
import javafx.stage.Stage;
import databasePart1.DatabaseHelper;

public final class Logout { 

    private Logout() {}

    //  Creates a standard Logout button that navigates to SetupLoginSelectionPage.
    // Added to following pages: 
    // Collin Looney 09.15.2025 
    
    public static Button LogoutButton(Stage stage, DatabaseHelper databaseHelper) {
        Button logoutBtn = new Button("Logout");
        logoutBtn.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
        
        logoutBtn.setOnAction(e -> {
            try {
                databaseHelper.setCurrentUserName(null); 
            	new SetupLoginSelectionPage(databaseHelper).show(stage);
                stage.setTitle("Login / Create Account");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        
        return logoutBtn;
    }
}