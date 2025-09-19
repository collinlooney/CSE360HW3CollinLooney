package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import databasePart1.DatabaseHelper;
import application.User;
import application.Role;
import java.util.ArrayList;
import java.sql.*;

/**
 * - Handles the GUI for updating user roles in our admin system.
 * - Allows admins to change user permissions.
 * 
 * Main features:
 * - Checkboxes to add/remove Admin and Basic User roles
 * - Validation to prevent removing the last admin or an admin removing their own role
 */
public class AdminUpdateUsers {
    
    // === INSTANCE VARIABLES ===
    private final DatabaseHelper databaseHelper;  // Handles all database operations
    private final User selectedUser;              // User being edited
    private String currentAdminUsername;          // Who is logged in (to prevent self-role-removal)
    
    // === UI COMPONENTS ===
    private CheckBox adminRoleBox;      // Checkbox for Admin role
    private CheckBox basicUserRoleBox;  // Checkbox for Basic User role
    private Label statusLabel;          // Shows success/error messages to user
    private ArrayList<Role> originalRoles;  // Keeps track of what roles user had before changes
    
    // === THEME CONFIGURATION ===
    private static final String BACKGROUND_STYLE = "-fx-background-color: #f5f5f5;";
    
    private static final String TOOLBAR_STYLE = 
        "-fx-background-color: #ffffff; -fx-padding: 6 12; -fx-border-color: #e0e0e0; " +
        "-fx-border-width: 0 0 1 0; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 1, 0, 0, 1);";
    
    private static final String CONTENT_CARD_STYLE = 
        "-fx-background-color: #ffffff; -fx-padding: 20; -fx-border-color: #e8e8e8; " +
        "-fx-border-width: 1; -fx-border-radius: 6; -fx-background-radius: 6; " +
        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.03), 4, 0, 0, 1);";
    
    // Table styles
    private static final String TABLE_HEADER_STYLE = 
        "-fx-background-color: #fafafa; -fx-border-color: #e8e8e8; -fx-border-width: 0 0 1 0; " +
        "-fx-padding: 10 12; -fx-font-weight: 600; -fx-text-fill: #424242; -fx-font-size: 12px;";
    
    private static final String TABLE_CELL_STYLE = 
        "-fx-background-color: #ffffff; -fx-border-color: #f0f0f0; -fx-border-width: 0 0 1 0; " +
        "-fx-padding: 10 12; -fx-text-fill: #616161; -fx-font-size: 12px;";
    
    // Button styles
    private static final String PRIMARY_BUTTON_STYLE = 
        "-fx-background-color: #424242; -fx-text-fill: #ffffff; -fx-border-color: transparent; " +
        "-fx-padding: 8 16; -fx-font-size: 12px; -fx-background-radius: 4; " +
        "-fx-cursor: hand; -fx-font-weight: 500;";
    
    private static final String SECONDARY_BUTTON_STYLE = 
        "-fx-background-color: #f5f5f5; -fx-text-fill: #616161; -fx-border-color: #e0e0e0; " +
        "-fx-border-width: 1; -fx-padding: 4 10; -fx-font-size: 11px; -fx-background-radius: 3; " +
        "-fx-border-radius: 3; -fx-cursor: hand; -fx-font-weight: 500;";
    
    private static final String SUCCESS_BUTTON_STYLE = 
        "-fx-background-color: #37474f; -fx-text-fill: #ffffff; -fx-border-color: transparent; " +
        "-fx-padding: 8 20; -fx-font-size: 12px; -fx-background-radius: 4; " +
        "-fx-cursor: hand; -fx-font-weight: 500;";
    
    // === CONSTRUCTOR ===
    public AdminUpdateUsers(DatabaseHelper databaseHelper, User user) {
        this.databaseHelper = databaseHelper;
        this.selectedUser = user;
        this.currentAdminUsername = "";  
    }
    
    // === STAGE SETUP METHOD ===
    public void show(Stage primaryStage) {
        // Main container 
        VBox root = new VBox(0);
        root.setStyle(BACKGROUND_STYLE);
        
        // Top toolbar (with navigatiion)
        HBox toolbar = createToolbar(primaryStage);
        
        // Main content area
        VBox contentCard = new VBox(16);
        contentCard.setStyle(CONTENT_CARD_STYLE);
        
        // Page title
        Label title = new Label("Update User Roles");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: 650; -fx-text-fill: #212121;");
        
        // User information table
        VBox userInfoSection = createUserInfoSection();
        
        // Role modification section
        VBox rolesSection = createRolesSection();
        
        // Action buttons
        HBox buttonContainer = createActionButtons();
        
        // Status message label
        statusLabel = new Label();
        statusLabel.setStyle("-fx-font-size: 12px; -fx-padding: 8 0 0 0;");
        
        // Content card
        contentCard.getChildren().addAll(title, userInfoSection, rolesSection, buttonContainer, statusLabel);
        
        // Add to main container
        VBox.setMargin(contentCard, new Insets(12, 16, 16, 16)); //spacing
        root.getChildren().addAll(toolbar, contentCard);
        
        // Load the user's current roles from database to set checkboxes accordingly
        loadCurrentRoles();
        
        // Launch the user interface
        Scene scene = new Scene(root, 650, 500);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Update User Roles");
        primaryStage.show();
    }
    
    // === TOOLBAR CREATION ===
    // Top bar with navigation buttons
    private HBox createToolbar(Stage primaryStage) {
        HBox toolbar = new HBox();
        toolbar.setStyle(TOOLBAR_STYLE);
        toolbar.setAlignment(Pos.CENTER_RIGHT);
        
        // Back button
        Button backButton = new Button("← Back");
        backButton.setStyle(SECONDARY_BUTTON_STYLE);
        backButton.setOnAction(e -> new AdminListAllUsers(databaseHelper).show(primaryStage));
        
        // Hover effect
        addHoverEffect(backButton, SECONDARY_BUTTON_STYLE, "#eeeeee");
        
        toolbar.getChildren().add(backButton);
        return toolbar;
    }
    
    // === USER INFO SECTION ===
    // Information display card
    private VBox createUserInfoSection() {
        VBox section = new VBox(10);
        
        Label sectionTitle = new Label("User Information");
        sectionTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #424242;");
        
        // Compact table container
        VBox tableContainer = new VBox(0);
        tableContainer.setStyle("-fx-border-color: #e8e8e8; -fx-border-width: 1; -fx-border-radius: 4; -fx-background-radius: 4;");
        
        VBox table = createUserTable();
        tableContainer.getChildren().add(table);
        
        section.getChildren().addAll(sectionTitle, tableContainer);
        return section;
    }
    
    // === USER TABLE ===
    // Table design
    private VBox createUserTable() {
        VBox table = new VBox(0);
        
        // Headers for User Table
        HBox headerRow = new HBox(0);
        
        Label usernameHeader = createTableCell("Username", 130, true);
        Label nameHeader = createTableCell("Name", 130, true);
        Label emailHeader = createTableCell("Email", 180, true);
        Label rolesHeader = createTableCell("Roles", 160, true);
        
        headerRow.getChildren().addAll(usernameHeader, nameHeader, emailHeader, rolesHeader);
        
        // Data row with user information
        HBox dataRow = new HBox(0);
        
        Label usernameValue = createTableCell(selectedUser.getUserName(), 130, false);
        Label nameValue = createTableCell(selectedUser.getName(), 130, false);
        Label emailValue = createTableCell(selectedUser.getEmail(), 180, false);
        Label rolesValue = createTableCell(getCurrentRolesText(), 160, false);
        
        dataRow.getChildren().addAll(usernameValue, nameValue, emailValue, rolesValue);
        
        table.getChildren().addAll(headerRow, dataRow);
        return table;
    }
    
    // === TABLE CELL HELPER ===
    private Label createTableCell(String text, int width, boolean isHeader) {
        Label cell = new Label(text);
        cell.setPrefWidth(width);
        cell.setMaxWidth(width);
        cell.setMinWidth(width); // Force consistent width
        
        if (isHeader) {
            cell.setStyle(TABLE_HEADER_STYLE);
        } else {
            cell.setStyle(TABLE_CELL_STYLE);
        }
        
        return cell;
    }
    
    // === ROLES SECTION ===
    private VBox createRolesSection() {
        VBox section = new VBox(10);
        
        Label sectionTitle = new Label("Modify Roles");
        sectionTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #424242;");
        
        // Checkbox container holding admin and basic user 
        VBox checkboxContainer = new VBox(10);
        checkboxContainer.setStyle("-fx-padding: 16; -fx-border-color: #e8e8e8; -fx-border-width: 1; " +
                                  "-fx-border-radius: 4; -fx-background-color: #fafafa; -fx-background-radius: 4;");
        
        // Role checkboxes for amin/user
        adminRoleBox = new CheckBox("Administrator");
        basicUserRoleBox = new CheckBox("Basic User");
        
        String checkboxStyle = "-fx-font-size: 13px; -fx-text-fill: #424242; -fx-font-weight: 500;";
        adminRoleBox.setStyle(checkboxStyle);
        basicUserRoleBox.setStyle(checkboxStyle);
        
        checkboxContainer.getChildren().addAll(adminRoleBox, basicUserRoleBox);
        section.getChildren().addAll(sectionTitle, checkboxContainer);
        
        return section;
    }
    
    // === ACTION BUTTONS ===
    private HBox createActionButtons() {
        HBox buttonContainer = new HBox(12);
        buttonContainer.setAlignment(Pos.CENTER_LEFT);
        
        // Save button
        Button saveButton = new Button("Save Changes");
        saveButton.setStyle(SUCCESS_BUTTON_STYLE);
        saveButton.setOnAction(e -> saveChanges());
        
        // Hover effect
        addHoverEffect(saveButton, SUCCESS_BUTTON_STYLE, "#263238");
        
        buttonContainer.getChildren().add(saveButton);
        return buttonContainer;
    }
    
    // === HOVER EFFECTS ===
    private void addHoverEffect(Button button, String baseStyle, String hoverColor) {
        button.setOnMouseEntered(e -> button.setStyle(
            baseStyle.replace(extractBackgroundColor(baseStyle), hoverColor)));
        button.setOnMouseExited(e -> button.setStyle(baseStyle));
    }
    
    // Helper method to extract background color from style string
    // Returns the color portion (e.g., "#424242") for use in hover effect
    private String extractBackgroundColor(String style) {
        String[] parts = style.split(";");
        for (String part : parts) {
            if (part.trim().startsWith("-fx-background-color:")) {
                return part.split(":")[1].trim();
            }
        }
        return "#424242"; // fallback
    }
    
    // === HELPER METHOD FOR DISPLAYING ROLES ===
    // // Returns roles as comma-separated text (e.g., "Admin, Basic User")
    private String getCurrentRolesText() {
        if (selectedUser.getRoles() == null || selectedUser.getRoles().isEmpty()) {
            return "None";
        }
        // Used Java streams to join all role names with commas
        return selectedUser.getRoles().stream()
            .map(Role::display)      // Get display name for each role
            .reduce((a, b) -> a + ", " + b)  // Join them with commas
            .orElse("None");         // Fallback if something goes wrong
    }
    
    // === LOAD CURRENT ROLES FROM DATABASE ===
    // Gets the user's current roles from database and sets checkboxes accordingly
    private void loadCurrentRoles() {
        try {
            // Get roles from database
            originalRoles = databaseHelper.getUserRoles(selectedUser.getUserName());
            
            // Set checkboxes based on what roles the user currently has
            adminRoleBox.setSelected(originalRoles.contains(Role.ADMIN));
            basicUserRoleBox.setSelected(originalRoles.contains(Role.BASIC_USER));
            
            // Show confirmation that data loaded successfully
            statusLabel.setText("User data loaded successfully");
            statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #757575; -fx-padding: 8 0 0 0;");
        } catch (Exception e) {
            // If something goes wrong, show error message
            showStatus("Error loading user roles: " + e.getMessage(), false);
        }
    }
    
    // === STATUS MESSAGE HELPER ===
    // Shows success (green) or error (red) messages to the user
    private void showStatus(String message, boolean success) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-font-size: 12px; -fx-padding: 8 0 0 0; -fx-font-weight: 500; -fx-text-fill: " + 
            (success ? "#2e7d32;" : "#d32f2f;"));  // Green for success, red for errors
    }
    
    // === SAVE CHANGES METHOD ===
    // Save role changes to database
    private void saveChanges() {
        try {
            // Build new roles list based on checkbox selections
            ArrayList<Role> newRoles = new ArrayList<>();
            if (adminRoleBox.isSelected()) newRoles.add(Role.ADMIN);
            if (basicUserRoleBox.isSelected()) newRoles.add(Role.BASIC_USER);
            
            // === VALIDATION LOGIC ===
            // These two checks prevent admins from accidentally breaking everything
            
            // Check 1: Don't let admin remove their own admin role 
            if (selectedUser.getUserName().equals(currentAdminUsername) && 
                originalRoles.contains(Role.ADMIN) && !newRoles.contains(Role.ADMIN)) {
                showStatus("Cannot remove your own Administrator role", false);
                adminRoleBox.setSelected(true);  // Put the checkbox back
                return;
            }
            
            // Check 2: Don't remove the last admin role (would break)
            if (originalRoles.contains(Role.ADMIN) &&
                !newRoles.contains(Role.ADMIN) &&
                isLastAdmin(selectedUser.getUserName())) {
                showStatus("Cannot remove the last Administrator role from the system", false);
                adminRoleBox.setSelected(true);  // Puts the checkbox back 
                return;
            }
            
            // === ACTUAL UPDATE ===
            // Update user with new roles and save to database
            User updatedUser = new User(selectedUser.getUserName(), selectedUser.getName(), 
                selectedUser.getEmail(), getCurrentPassword(selectedUser.getUserName()), newRoles);
            
            // Save to database
            String result = databaseHelper.updateUserInfo(selectedUser.getUserName(), updatedUser);
            
            // Checks if update was successful
            if (result.isEmpty()) {
                // Success! Updates data and shows confirmation
                showStatus("User roles updated successfully", true);
                originalRoles = new ArrayList<>(newRoles);      // Update our backup
                selectedUser.setRoles(new ArrayList<>(newRoles)); // Update the user object
            } else {
                // Something messed up with the database update
                showStatus("Update failed: " + result, false);
            }
            
        } catch (Exception e) {
            // Should catch any unexpected errors
            showStatus("Error updating user roles: " + e.getMessage(), false);
        }
    }
    
    // === DATABASE HELPER METHODS ===   
    // Checks if the current user is the last admin in the system
    private boolean isLastAdmin(String userName) {
    try {
            return databaseHelper.isLastAdmin(userName);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Gets the current password for a user (for user update)
    // Maybe in the future we can have a method in DatabaseHelper that only
    // updates roles so its not having to use the users password? 
    private String getCurrentPassword(String userName) throws Exception {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT password FROM cse360users WHERE userName = ?")) {
            pstmt.setString(1, userName);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() ? rs.getString("password") : "";
            }
        }
    }
    
    // Gets database connection (should probably be moved to DatabaseHelper??)
    private Connection getConnection() throws Exception {
        databaseHelper.verifyConnection();
        return java.sql.DriverManager.getConnection("jdbc:h2:~/FoundationDatabase", "sa", "");
    }
}
