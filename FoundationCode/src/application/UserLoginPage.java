package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.ArrayList;

import databasePart1.*;

/**
 * The UserLoginPage class provides a login interface for users to access their accounts.
 * It validates the user's credentials and navigates to the appropriate page upon successful login.
 */
public class UserLoginPage {
	
    private final DatabaseHelper databaseHelper;

    public UserLoginPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    public void show(Stage primaryStage) {
    	// Input field for the user's userName, password
        TextField userNameField = new TextField();
        userNameField.setPromptText("Enter userName");
        userNameField.setMaxWidth(250);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter Password");
        passwordField.setMaxWidth(250);
        
        // Label to display error messages
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");

        Button loginButton = new Button("Login");
        
        loginButton.setOnAction(a -> {
        	// Retrieve user inputs
            String userName = userNameField.getText();
            String password = passwordField.getText();
            // Validate userName input
            String usernameErrMsg = UserNameRecognizer.checkForValidUserName(userName);
            // Validate password input
            String passwordErrMsg = PasswordRecognizer.evaluatePassword(password);
            
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
                                                        if (roles.size() > 1) {
                                                                welcomeLoginPage.show(primaryStage, user);
                                                        } else {
                                                                // Only has 1 role, immediately go there
                                                                Role r = roles.get(0);
                                                                if (r == Role.ADMIN) {
                                                                        new AdminHomePage(databaseHelper).show(primaryStage);
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
						errorLabel.setText("user account doesn't exists");
					}

				} catch (SQLException e) {
					System.err.println("Database error: " + e.getMessage());
					e.printStackTrace();
				}
			}
        });
        Button returnButton = Logout.LogoutButton(primaryStage, databaseHelper); //return button added to go back if needed 
        returnButton.setText("Return");
        
        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");
        layout.getChildren().addAll(returnButton, userNameField, passwordField, loginButton, errorLabel);

        primaryStage.setScene(new Scene(layout, 800, 400));
        primaryStage.setTitle("User Login");
        primaryStage.show();
    }
}
