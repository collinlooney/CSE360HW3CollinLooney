package application;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import databasePart1.DatabaseHelper; // added for use with database C.L.


/**
 * AdminPage class represents the user interface for the admin user.
 * This page displays a simple welcome message for the admin.
 */

public class AdminHomePage {
	/**
     * Displays the admin page in the provided primary stage.
     * @param primaryStage The primary stage where the scene will be displayed.
     */
	 
	private final DatabaseHelper databaseHelper; 
	public AdminHomePage (DatabaseHelper databaseHelper) { //identical implementation to WelcomeLoginPage.java C.L.
		 this.databaseHelper = databaseHelper; 
	 }
	
<<<<<<< HEAD
    public void show(Stage primaryStage, User user) {
=======
	public void show(Stage primaryStage) { // How I implemented AdminListAllUsers; leaving here for now 
        try {
            String current = databaseHelper.getCurrentUserName();
            if (current != null && !current.isBlank()) {
                User u = databaseHelper.getUserByUserName(current);
                if (u != null) {
                    show(primaryStage, u);
                    return;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        new SetupLoginSelectionPage(databaseHelper).show(primaryStage);
    }
	
	public void show(Stage primaryStage, User user) {
>>>>>>> d00b58f (Admin: merged MenuBar dashboard + Return to Welcome; List Users; Delete stub; session tracking; last-admin protection; login/logout tweaks)

    	// BorderPane is ideal for a top menu bar and center content
        BorderPane layout = new BorderPane();

        // --- Create the MenuBar ---
        MenuBar menuBar = new MenuBar();

        // --- Create the "Session" Menu ---
        Menu sessionMenu = new Menu("Session");
        
<<<<<<< HEAD
=======
        MenuItem returnItem = new MenuItem("Return to Welcome");
        returnItem.setOnAction(e -> {
            if (user != null) {
                new WelcomeLoginPage(databaseHelper).show(primaryStage, user);
            } else {
                new SetupLoginSelectionPage(databaseHelper).show(primaryStage);
            }
        });
        
>>>>>>> d00b58f (Admin: merged MenuBar dashboard + Return to Welcome; List Users; Delete stub; session tracking; last-admin protection; login/logout tweaks)
        MenuItem logoutItem = new MenuItem("Logout");
        logoutItem.setOnAction(e -> {
            new SetupLoginSelectionPage(databaseHelper).show(primaryStage);
            primaryStage.setTitle("Login / Create Account");
        });
        
<<<<<<< HEAD
        sessionMenu.getItems().add(logoutItem);
=======
        sessionMenu.getItems().addAll(returnItem, logoutItem);
>>>>>>> d00b58f (Admin: merged MenuBar dashboard + Return to Welcome; List Users; Delete stub; session tracking; last-admin protection; login/logout tweaks)

        // --- Create the "User Management" Menu ---
        Menu userManagementMenu = new Menu("User Management");

        MenuItem inviteUserItem = new MenuItem("Create Invitation");
        inviteUserItem.setOnAction(e -> {
            new InvitationPage().show(databaseHelper,primaryStage);
        });

        MenuItem createOtpItem = new MenuItem("Create One-Time Password");
        createOtpItem.setOnAction(e -> {
<<<<<<< HEAD
            new AdminOneTimePasswordCreatePage(databaseHelper).show(primaryStage, user.getUserName());
        });

        MenuItem deleteUserItem = new MenuItem("Delete User");
        deleteUserItem.setOnAction(e -> {
        	//to be added when logic is implemented 
        });

        MenuItem listUsersItem = new MenuItem("List All Users");
        listUsersItem.setOnAction(e -> {
        	//to be added when logic is implemented 
        });

        MenuItem updateUserItem = new MenuItem("Update User");
        updateUserItem.setOnAction(e -> {
            //to be added when logic is implemented 
        });
=======
            new AdminOneTimePasswordCreatePage(databaseHelper).show(primaryStage, (user!= null ? user.getUserName() : ""));
        });


        MenuItem listUsersItem = new MenuItem("List All Users"); // removed update and delete buttons, since this is done through the list
        listUsersItem.setOnAction(e -> {
            new AdminListAllUsers(databaseHelper).show(primaryStage);
        });

>>>>>>> d00b58f (Admin: merged MenuBar dashboard + Return to Welcome; List Users; Delete stub; session tracking; last-admin protection; login/logout tweaks)

        // Add all admin actions as MenuItems to this menu
        userManagementMenu.getItems().addAll(
            inviteUserItem,
            createOtpItem,
<<<<<<< HEAD
            deleteUserItem,
            listUsersItem,
            updateUserItem
=======
            listUsersItem
>>>>>>> d00b58f (Admin: merged MenuBar dashboard + Return to Welcome; List Users; Delete stub; session tracking; last-admin protection; login/logout tweaks)
        );

        // Add both Menus to the MenuBar
        menuBar.getMenus().addAll(sessionMenu, userManagementMenu);

        // --- Center Content ---
        Label adminLabel = new Label("Welcome, Admin!");
        adminLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        // Center the label within the BorderPane's center region
        BorderPane.setAlignment(adminLabel, Pos.CENTER);


        // --- Assemble the Layout ---
        layout.setTop(menuBar);       // Place menu bar at the top
        layout.setCenter(adminLabel); // Place welcome label in the center

        Scene adminScene = new Scene(layout, 800, 400);

        primaryStage.setScene(adminScene);
        primaryStage.setTitle("Admin Dashboard");
    }
}