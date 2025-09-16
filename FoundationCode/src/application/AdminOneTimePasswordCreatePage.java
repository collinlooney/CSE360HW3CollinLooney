package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;

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

		// Button to set the one time password
		Button setPasswordButton = new Button("Set Password");

		setPasswordButton.setOnAction(a -> {
		});

		VBox layout = new VBox(10, userNameField, setPasswordButton);
		layout.setStyle("-fx-padding: 20; -fx-alignment: center;");

		primaryStage.setScene(new Scene(layout, 800, 400));
		primaryStage.setTitle("Admin One Time Password Creation");
		primaryStage.show();



	}

}
