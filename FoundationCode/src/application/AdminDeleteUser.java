package application;

import databasePart1.DatabaseHelper;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;

public class AdminDeleteUser {

    private final DatabaseHelper databaseHelper;
    private final String userName;

    public AdminDeleteUser(DatabaseHelper db, String userName) {
        this.databaseHelper = db;
        this.userName = userName;
    }

    public void show(Stage stage) {
    	
    	if (userName == null || userName.isBlank()) {                // guard for null/blank userName
            new Alert(Alert.AlertType.ERROR, "Invalid user.").showAndWait();
            new AdminListAllUsers(databaseHelper).show(stage);
            return;
        }
    	
    	Label prompt = new Label("Delete user '" + userName + "'?");
        prompt.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        Button btnOk = new Button("OK");
        btnOk.setDefaultButton(true);

        Button btnCancel = new Button("Cancel");
        btnCancel.setCancelButton(true);

        btnOk.setOnAction(e -> {
            boolean deletingCurrent = userName.equals(databaseHelper.getCurrentUserName());
            try {
            	databaseHelper.verifyConnection();                    
                // Optional: prevent deleting the last admin
                if (databaseHelper.isLastAdmin(userName)) { //guard to prevent last admin deletion 
                     new Alert(Alert.AlertType.WARNING, "You can’t delete the last remaining Admin.").showAndWait();
                     new AdminListAllUsers(databaseHelper).show(stage);
                     return;
                }
                
            	boolean deleted = databaseHelper.deleteUser(userName);
                if (!deleted) {
                    new Alert(Alert.AlertType.WARNING, "No user deleted.").showAndWait();
                    new AdminListAllUsers(databaseHelper).show(stage);
                    return;
                }

                if (deletingCurrent) {
                    // Force logout of the session user is in 
                    databaseHelper.setCurrentUserName(null); // clear session 
                    

                    new Alert(Alert.AlertType.INFORMATION,
                        "Your account was deleted. You have been logged out.").showAndWait();

                    new UserLoginPage(databaseHelper).show(stage); //return to login screen
                } else { // for other users (not logged in) 
                    
                    new AdminListAllUsers(databaseHelper).show(stage);
                }

            } catch (SQLException ex) {
                ex.printStackTrace();
                new Alert(Alert.AlertType.ERROR, "Delete failed: " + ex.getMessage()).showAndWait();
                new AdminListAllUsers(databaseHelper).show(stage); //return to list on error
            }
        });

        btnCancel.setOnAction(e -> new AdminListAllUsers(databaseHelper).show(stage));

        ButtonBar bar = new ButtonBar();
        ButtonBar.setButtonData(btnOk, ButtonBar.ButtonData.OK_DONE);
        ButtonBar.setButtonData(btnCancel, ButtonBar.ButtonData.CANCEL_CLOSE);
        bar.getButtons().addAll(btnCancel, btnOk);

        VBox root = new VBox(12, prompt, bar);
        root.setStyle("-fx-padding: 18; -fx-alignment: center;");

        stage.setScene(new Scene(root, 420, 160));
        stage.setTitle("Confirm Delete");
        stage.show();
        
        btnOk.requestFocus();
    }
}
