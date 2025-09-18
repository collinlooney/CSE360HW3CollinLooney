package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.geometry.Pos;
import javafx.scene.layout.Priority;

import java.sql.SQLException;
import java.util.ArrayList;

import databasePart1.*;

/**
 * The UserLoginPage class provides a login interface for users to access their accounts.
 * It validates the user's credentials and navigates to the appropriate page upon successful login.
 * 09/18/25 - added green check and red x for validation confirmation - Jonathan Waterway
 */
public class UserLoginPage {
	
    private final DatabaseHelper databaseHelper;

    public UserLoginPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    public void show(Stage primaryStage) {
    
        // Label to display error messages
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");

        Button loginButton = new Button("Login");
        loginButton.setDefaultButton(true); // 'Enter' will click the button 
        HBox loginButtonBox = new HBox(loginButton);
        loginButtonBox.setAlignment(Pos.CENTER);
        
        // Input fields
        TextField userNameField = new TextField();
        userNameField.setPromptText("Enter userName");
        userNameField.setMaxWidth(250);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter Password");
        passwordField.setMaxWidth(250);

        // Checkmark labels
        Label userNameCheck = new Label();
        userNameCheck.setPrefWidth(20);
        userNameCheck.setAlignment(Pos.CENTER_LEFT);

        Label passwordCheck = new Label();
        passwordCheck.setPrefWidth(20);
        passwordCheck.setAlignment(Pos.CENTER_LEFT);

        // HBoxes for aligned rows
        HBox userNameBox = new HBox(5, userNameField, userNameCheck);
        HBox.setHgrow(userNameField, Priority.ALWAYS);
        userNameBox.setAlignment(Pos.CENTER);
        userNameBox.setTranslateX(+10);

        HBox passwordBox = new HBox(5, passwordField, passwordCheck);
        HBox.setHgrow(passwordField, Priority.ALWAYS);
        passwordBox.setAlignment(Pos.CENTER);
        passwordBox.setTranslateX(+10);

        
        loginButton.setOnAction(a -> {
        	// Retrieve user inputs
            String userName = userNameField.getText();
            String password = passwordField.getText();
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
            
            // Construct multi-line error message to indicate if
            // username/password are invalid
            String errMsg = usernameErrMsg + passwordErrMsg;
            
            if (!errMsg.isEmpty()) {
            	// Set Error label to contain error message(s)
            	errorLabel.setText(errMsg);
			} else {
				try {
					databaseHelper.verifyConnection(); //  checks if connected -C. Looney
          
          User user = new User(userName, "", "", password, new ArrayList<>());

					WelcomeLoginPage welcomeLoginPage = new WelcomeLoginPage(databaseHelper);

					// Retrieve user's roles, name, email from the database using userName
					ArrayList<Role> roles = databaseHelper.getUserRoles(userName);
                                        String name = databaseHelper.getUserNameField(userName);
                                        String email = databaseHelper.getUserEmail(userName);

					if ((!roles.isEmpty()) && (name != null) && (email != null)) {
						user.setRoles(roles);
                                                user.setName(name);
                                                user.setEmail(email);
						if (databaseHelper.login(user)) {
                                                        // If user has multiple roles, go to role selection page
														databaseHelper.setCurrentUserName(user.getUserName());
                                                        if (roles.size() > 1) {
                                                                welcomeLoginPage.show(primaryStage, user);
                                                        } else {
                                                                // Only has 1 role, immediately go there
                                                                Role r = roles.get(0);
                                                                if (r == Role.ADMIN) {
                                                                        new AdminHomePage(databaseHelper).show(primaryStage, user);
                                                                } else if (r == Role.BASIC_USER) {
                                                                        new UserHomePage(databaseHelper).show(primaryStage);
                                                                }
                                                        }
						} else {
							// Display an error if the login fails
							errorLabel.setText("Error logging in");
						}
					} else {
						// Display an error if the account does not exist
						errorLabel.setText("User account doesn't exist!");
					}

				} catch (SQLException e) {
					System.err.println("Database error: " + e.getMessage());
					e.printStackTrace();
				}
			}
        });
        Button returnButton = Logout.LogoutButton(primaryStage, databaseHelper); //return button added to go back if needed 
        returnButton.setCancelButton(true); // 'ESC' will trigger
        returnButton.setText("Return");
        
        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");

        // Add rows instead of raw fields
        layout.getChildren().addAll(
            returnButton,
            userNameBox,
            passwordBox,
            loginButtonBox,
            errorLabel
        );
        primaryStage.setScene(new Scene(layout, 800, 400));
        primaryStage.setTitle("User Login");
        primaryStage.show();
    }
}
