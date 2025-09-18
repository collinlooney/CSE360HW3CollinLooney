package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.geometry.Pos;


import java.sql.SQLException;
import java.util.ArrayList;

import databasePart1.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

/**
 * The SetupAdmin class handles the setup process for creating an administrator account.
 * This is intended to be used by the first user to initialize the system with admin credentials.
 *  09/18/25 - added green check and red x for validation confirmation - Jonathan Waterway
 */
public class AdminSetupPage {
	
    private final DatabaseHelper databaseHelper;

    public AdminSetupPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }
 // Helper method to build a field + checkmark row
    
    private HBox buildFieldRow(TextField field, String prompt, Label checkLabel) {
        field.setPromptText(prompt);
        field.setMaxWidth(250);

        checkLabel.setPrefWidth(20); // Reserve space for check/X
        checkLabel.setAlignment(Pos.CENTER_LEFT);

        HBox box = new HBox(5, field, checkLabel);
        HBox.setHgrow(field, Priority.ALWAYS);
        box.setAlignment(Pos.CENTER);
        box.setTranslateX(+10); // adjust centering
        return box;
    }

    public void show(Stage primaryStage) {
    	
        Button setupButton = new Button("Setup");
        
        HBox buttonBox = new HBox(setupButton);   // wrap it
        buttonBox.setAlignment(Pos.CENTER);       // center horizontally
        
        // Label to display error messages for invalid input
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
        
        TextField userNameField = new TextField();
        Label userNameCheck = new Label();
        HBox userNameBox = buildFieldRow(userNameField, "Enter Admin userName", userNameCheck);

        PasswordField passwordField = new PasswordField();
        Label passwordCheck = new Label();
        HBox passwordBox = buildFieldRow(passwordField, "Enter Password", passwordCheck);

        TextField nameField = new TextField();
        Label nameCheck = new Label();
        HBox nameBox = buildFieldRow(nameField, "Enter name", nameCheck);

        TextField emailField = new TextField();
        Label emailCheck = new Label();
        HBox emailBox = buildFieldRow(emailField, "Enter email", emailCheck);


        setupButton.setOnAction(a -> {
        	// Retrieve user input
            String userName = userNameField.getText();
            String password = passwordField.getText();
            String name = nameField.getText();
            String email = emailField.getText();

            // Validate userName input
            String usernameErrMsg = UserNameRecognizer.checkForValidUserName(userName);
            if (usernameErrMsg.isEmpty()) {
                userNameCheck.setText("✅");
                userNameCheck.setStyle("-fx-text-fill: green;");
            } else {
                userNameCheck.setText("❌ ");
                userNameCheck.setStyle("-fx-text-fill: red;");
            }
            // Validate password input
            String passwordErrMsg = PasswordRecognizer.evaluatePassword(password);
            if (passwordErrMsg.isEmpty()) {
                passwordCheck.setText("✅");
                passwordCheck.setStyle("-fx-text-fill: green;");
            } else {
                passwordCheck.setText("❌ ");
                passwordCheck.setStyle("-fx-text-fill: red;");
            }
            // Validate name input 
            String nameErrMsg = NameValidator.validateName(name);
            if (nameErrMsg.isEmpty()) {
                nameCheck.setText("✅");
                nameCheck.setStyle("-fx-text-fill: green;");
            } else {
                nameCheck.setText("❌ ");
                nameCheck.setStyle("-fx-text-fill: red;");
            }
            // Validate email input 
            String emailErrMsg = EmailValidator.checkForValidEmail(email);
            if (emailErrMsg.isEmpty()) {
                emailCheck.setText("✅");
                emailCheck.setStyle("-fx-text-fill: green;");
            } else {
                emailCheck.setText("❌ ");
                emailCheck.setStyle("-fx-text-fill: red;");
            }
                 
            
            // Construct multi-line error message to indicate if
            // username/password/email are invalid
            String errMsg = usernameErrMsg + passwordErrMsg  + nameErrMsg + emailErrMsg;
            
			if (!errMsg.isEmpty()) {
				// Set Error label to contain error message(s)
				errorLabel.setText(errMsg);
			} else {
				try {
					// Create user with admin role and register in the database
                                        ArrayList<Role> roles = new ArrayList<>();
                                        roles.add(Role.ADMIN);
                                        roles.add(Role.BASIC_USER);

					User user = new User(userName, name, email, password, roles);
					databaseHelper.register(user);
					System.out.println("Administrator setup completed.");

					// Navigate to the Welcome Login Page
					new WelcomeLoginPage(databaseHelper).show(primaryStage, user);
				} catch (SQLException e) {
					System.err.println("Database error: " + e.getMessage());
					e.printStackTrace();
				}
			}
        });

        //VBox layout = new VBox(10, userNameField, passwordField, nameField, emailField, setupButton, errorLabel);
        //layout.setStyle("-fx-padding: 20; -fx-alignment: center;");
        VBox layout = new VBox(
        	    10, userNameBox, passwordBox, nameBox, emailBox, setupButton, errorLabel
        	);
        	layout.setStyle("-fx-padding: 20; -fx-alignment: center;");


        primaryStage.setScene(new Scene(layout, 800, 400));
        primaryStage.setTitle("Administrator Setup");
        primaryStage.show();
    }
}
