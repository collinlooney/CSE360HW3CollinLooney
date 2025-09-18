package application;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.layout.Priority;


import java.sql.SQLException;
import java.util.ArrayList;

import databasePart1.*;

/**
 * SetupAccountPage class handles the account setup process for new users.
 * Users provide their userName, password, name, email and a valid invitation code to register.
 *  09/18/25 - added green check and red x for validation confirmation - Jonathan Waterway
 */
public class SetupAccountPage {
	
    private final DatabaseHelper databaseHelper;
    // DatabaseHelper to handle database operations.
    public SetupAccountPage(DatabaseHelper databaseHelper) {
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

    /**
     * Displays the Setup Account page in the provided stage.
     * @param primaryStage The primary stage where the scene will be displayed.
     */
    public void show(Stage primaryStage) {
    	 // Return button (top)
        Button returnButton = Logout.LogoutButton(primaryStage, databaseHelper);
        returnButton.setText("Return");
        HBox returnButtonBox = new HBox(returnButton);
        returnButtonBox.setAlignment(Pos.CENTER); 

        // Setup button (centered)
        Button setupButton = new Button("Setup");
        HBox setupButtonBox = new HBox(setupButton);
        setupButtonBox.setAlignment(Pos.CENTER);
        
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
        
        TextField inviteCodeField = new TextField();
        Label inviteCheck = new Label();
        HBox inviteBox = buildFieldRow(inviteCodeField, "Enter invitationCode", inviteCheck);


        
        setupButton.setOnAction(a -> {
        	// Retrieve user input
            String userName = userNameField.getText();
            String password = passwordField.getText();
            String name = nameField.getText();
            String email = emailField.getText();
            String code = inviteCodeField.getText();
            
            
            // Validate username input
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
            String errMsg = usernameErrMsg  + passwordErrMsg + nameErrMsg + emailErrMsg;
            
			if (!errMsg.isEmpty()) {
				// Set Error label to contain error message(s)
				errorLabel.setText(errMsg);
			} else {
				try {
					databaseHelper.verifyConnection(); //  checks if connected -C. Looney
					// Check if the user already exists
					if (!databaseHelper.doesUserExist(userName)) {

						// Validate the invitation code
						if (databaseHelper.validateInvitationCode(code)) {

							// Create a new user and register them in the database
                                                        ArrayList<Role> roles = new ArrayList<>();
                                                        roles.add(Role.BASIC_USER);

                                                        User user = new User(userName, name, email, password, roles);
							databaseHelper.register(user);

							// Navigate to the Welcome Login Page
							new WelcomeLoginPage(databaseHelper).show(primaryStage, user);
						} else {
							errorLabel.setText("Please enter a valid invitation code");
							inviteCheck.setText("❌ ");
				            inviteCheck.setStyle("-fx-text-fill: red;");
						}
					} else {
						errorLabel.setText("This useruserName is taken!!.. Please use another to setup an account");
					}

				} catch (SQLException e) {
					System.err.println("Database error: " + e.getMessage());
					e.printStackTrace();
				}
			}
        });
        
        //Button returnButton = Logout.LogoutButton(primaryStage, databaseHelper);
        //returnButton.setText("Return");
        
        /*VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");

        *layout.getChildren().addAll(returnButton, userNameField, passwordField, nameField, emailField, inviteCodeField, setupButton, errorLabel);
         */
        // Build layout
        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");

        // Add everything into VBox
        layout.getChildren().addAll(
            returnButtonBox,   // return button row
            userNameBox,
            passwordBox,
            nameBox,
            emailBox,
            inviteBox,   
            setupButtonBox,    // setup button centered
            errorLabel
        );
        primaryStage.setScene(new Scene(layout, 800, 400));
        primaryStage.setTitle("Account Setup");
        primaryStage.show();
    }
}
