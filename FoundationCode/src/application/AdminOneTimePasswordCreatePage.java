package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.ArrayList;

import databasePart1.*;

/**
 * Page for an Admin set a One Time Password for a User
 */
public class AdminOneTimePasswordCreatePage {
	private final DatabaseHelper databaseHelper;

	public AdminOneTimePasswordCreatePage(DatabaseHelper databaseHelper) {
		this.databaseHelper = databaseHelper;
	}

	public void show(Stage primaryStage, String userName) {
		// Input field for userName to set OTP
		TextField userNameField = new TextField();
		userNameField.setPromptText("Enter username to set a one time password for");
		userNameField.setMaxWidth(250);
		
		// If username was passed then set field automatically
		if (!userName.isEmpty()) {
			userNameField.setText(userName);
		}

		// Input field for manual password
		TextField passwordField = new TextField();
		passwordField.setPromptText("Enter one time password");
		passwordField.setMaxWidth(250);

		// Button to set the one time password
		Button setPasswordButton = new Button("Set Password");

		// Label to display error/success messages
		Label msgLabel = new Label();
		msgLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");

		setPasswordButton.setOnAction(a -> {
			// Load text fields
			String userNameV = userNameField.getText();
			String newPassword = passwordField.getText();

			// Validate password
			String passwordErrMsg = PasswordRecognizer.evaluatePassword(newPassword);
			if (!passwordErrMsg.isEmpty()) {
				msgLabel.setText(passwordErrMsg);
				return;
			}

			// Validate user exists
			boolean exists = this.databaseHelper.doesUserExist(userNameV);
			if (!exists) {
				msgLabel.setText("ERROR: User with userName '" + userNameV + "' not found.");
				return;
			}

			// Load user data for update
			String name = this.databaseHelper.getUserNameField(userNameV);
			String email = this.databaseHelper.getUserEmail(userNameV);
			ArrayList<Role> roles = this.databaseHelper.getUserRoles(userNameV);
			User newInfo = new User(userNameV, name, email, newPassword, roles);

			// Updating user database entry with onetime password
			String updateErrMsg = this.databaseHelper.updateUserInfo(userNameV, newInfo);
			if (!updateErrMsg.isEmpty()) {
				msgLabel.setText(updateErrMsg);
			} else {
				// Adding user to one time password table
				try {
					this.databaseHelper.addOtpUser(userNameV);
				} catch (SqlException e) {
					e.printStackTrace();
					return;
				}
				msgLabel.setStyle("-fx-text-fill: green; -fx-font-size: 12px;");
				String m = "One time password set successfully\n";
				m += "Username: '" + userNameV + "'\n";
				m += "New password: '" + newPassword + "'\n";
				msgLabel.setText(m);
			}


		});

		VBox layout = new VBox(10, userNameField, passwordField, setPasswordButton, msgLabel);
		layout.setStyle("-fx-padding: 20; -fx-alignment: center;");

		primaryStage.setScene(new Scene(layout, 800, 400));
		primaryStage.setTitle("Admin One Time Password Creation");
		primaryStage.show();

	}

}
