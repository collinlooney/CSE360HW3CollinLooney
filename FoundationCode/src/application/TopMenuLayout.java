package application;

import databasePart1.DatabaseHelper;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;

public final class TopMenuLayout {

    private TopMenuLayout() {}

    // Session" menubar for all users
    public static MenuBar createSessionMenuBar(DatabaseHelper databaseHelper, Stage primaryStage, User user) {
        
    	MenuBar menuBar = new MenuBar();
        Menu sessionMenu = new Menu("Session");

        MenuItem returnItem = new MenuItem("Return to Welcome");
        returnItem.setOnAction(e -> {
            if (user != null) {
                new WelcomeLoginPage(databaseHelper).show(primaryStage, user);
            } else {
                new SetupLoginSelectionPage(databaseHelper).show(primaryStage);
            }
        });

        MenuItem logoutItem = new MenuItem("Logout");
        logoutItem.setOnAction(e -> {
            // Use the same destination as your Admin flow for consistency:
            new SetupLoginSelectionPage(databaseHelper).show(primaryStage);
            primaryStage.setTitle("Login / Create Account");
        });

        sessionMenu.getItems().addAll(returnItem, logoutItem);
        menuBar.getMenus().add(sessionMenu);
        return menuBar;
    }

    // Admin menubar for admins ("User Management") 
    public static MenuBar createAdminMenuBar(DatabaseHelper databaseHelper, Stage primaryStage, User user) {
        
    	MenuBar menuBar = createSessionMenuBar(databaseHelper, primaryStage, user);
        Menu userManagementMenu = new Menu("User Management");

        MenuItem inviteUserItem = new MenuItem("Create Invitation");
        inviteUserItem.setOnAction(e -> new InvitationPage().show(databaseHelper, primaryStage));

        MenuItem createOtpItem = new MenuItem("Create One-Time Password");
        createOtpItem.setOnAction(e -> new AdminOneTimePasswordCreatePage(databaseHelper).show(primaryStage, "", user));

        MenuItem listUsersItem = new MenuItem("List All Users");
        listUsersItem.setOnAction(e -> new AdminListAllUsers(databaseHelper).show(primaryStage));

        userManagementMenu.getItems().addAll(inviteUserItem, createOtpItem, listUsersItem);

        menuBar.getMenus().add(userManagementMenu);
        return menuBar;
    }
}
