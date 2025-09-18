package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.geometry.Pos;
import java.util.ArrayList;

import databasePart1.*;



public class UserOneTimePasswordResetPage {
	
    private final DatabaseHelper databaseHelper;

    public UserOneTimePasswordResetPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    public void show(Stage primaryStage) {
        // Label to display error messages
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");

        // Input fields
        TextField userNameField = new TextField();
        userNameField.setPromptText("Enter userName");
        userNameField.setMaxWidth(250);

        PasswordField oneTimePasswordField = new PasswordField();
        oneTimePasswordField.setPromptText("Enter one time password");
        oneTimePasswordField.setMaxWidth(250);

        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("Enter new password");
        newPasswordField.setMaxWidth(250);

        Button updateButton = new Button("Update Password");
        updateButton.setDefaultButton(true);
        HBox updateButtonBox = new HBox(updateButton);
        updateButtonBox.setAlignment(Pos.CENTER);

        updateButton.setOnAction(a -> {
            String userName = userNameField.getText();
            String oneTimePassword = oneTimePasswordField.getText();
            String newPassword = newPasswordField.getText();

            // Validate username & one time password match
            User user = new User(userName, "", "", oneTimePassword, new ArrayList<>());
            try {
                if (!databaseHelper.login(user)) {
                    errorLabel.setText("ERROR: Invalid one time password");
                    return;
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return;
            }

            // Validate new password
            String passwordErrMsg = PasswordRecognizer.evaluatePassword(newPassword);
            if (!passwordErrMsg.isEmpty()) {
                errorLabel.setText(passwordErrMsg);
                return;
            }

            // Get current info of user
            String name = databaseHelper.getUserNameField(userName);
            String email = databaseHelper.getUserEmail(userName);
            ArrayList<Role> roles = databaseHelper.getUserRoles(userName);

            // Update user's password
            User newUser = new User(userName, name, email, newPassword, roles);
            String updateErrMsg = databaseHelper.updateUserInfo(userName, newUser);

            if (!updateErrMsg.isEmpty()) {
                errorLabel.setText(updateErrMsg);
                return;
            }

            // Return to login page
            System.out.println("Password updated");
            new UserLoginPage(databaseHelper).show(primaryStage);
        });

        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");

        layout.getChildren().addAll(
            userNameField,
            oneTimePasswordField,
            newPasswordField,
            updateButtonBox,
            errorLabel
        );
        primaryStage.setScene(new Scene(layout, 800, 400));
        primaryStage.setTitle("User Reset Password");
        primaryStage.show();
    }
}
