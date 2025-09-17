package application;

import databasePart1.DatabaseHelper;
import javafx.scene.Scene;
import javafx.scene.control.*; // Using TableView etc from here 
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.sql.SQLException;
import java.util.List;


// Imports that are new to this class: 
import javafx.collections.ObservableList; // adds ObservableList so that the UI updates when you change stuff 
import javafx.beans.property.ReadOnlyStringWrapper; // turns strings into ObservableValue so that TableColumn can use it 
import javafx.collections.FXCollections; // turns List into ObservableList so that TableColumn can use it
import java.util.stream.Collectors; // using the joining method to make the stream results separated by commas 
import javafx.geometry.Insets; // adds spacing to VBox
import javafx.scene.layout.Region; // allows padding of back button to righthand side 

public class AdminListAllUsers {

    private final DatabaseHelper db;

    public AdminListAllUsers(DatabaseHelper db) {
        this.db = db;
    }

    public void show(Stage stage) {
        // This is the start of our table. 
        TableView<User> table = new TableView<>(); // show rows of type "User" 
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS); // this helps format the columns to fit the page nicer

        
        // This is where the columns are named. If we need to add more, do it here. 
        // This sets the Username, Name, Email, Roles, and Actions columns. 
        TableColumn<User, String> colUsername = new TableColumn<>("Username"); // format here is <Row Type, Cell value type> 
        colUsername.setCellValueFactory(cd -> 
                new ReadOnlyStringWrapper(cd.getValue().getUserName())); // we need ObservableValue(string) here, hence the ReadOnlyStringWrapper.

        TableColumn<User, String> colName = new TableColumn<>("Name");
        colName.setCellValueFactory(cd ->
                new ReadOnlyStringWrapper(cd.getValue().getName()));

        TableColumn<User, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(cd ->
                new ReadOnlyStringWrapper(cd.getValue().getEmail()));

        TableColumn<User, String> colRoles = new TableColumn<>("Roles"); // this is where we are formatting roles to have commas between. 
        colRoles.setCellValueFactory(cd -> {
            List<Role> roles = cd.getValue().getRoles();
            String joined;
            
            if (roles == null) {
            	joined = ""; 
            } else {
            	joined = roles.stream().map(Role::display).collect(Collectors.joining(", "));
            }
            return new ReadOnlyStringWrapper(joined);
        });

        TableColumn<User, Void> colActions = new TableColumn<>("Actions"); // here we are creating the dropdown menu for user updating/deleting
        colActions.setCellFactory(tc -> new TableCell<>() {
            private final MenuItem update = new MenuItem("Update");
            private final MenuItem delete = new MenuItem("Delete");
            private final MenuButton menu = new MenuButton("Actions", null, update, delete);

            {
                update.setOnAction(e -> {  // if you click update, we find the user and navigate to the update page. 
                    User u = getTableView().getItems().get(getIndex());
                    new AdminUpdateUsers(db, u).show(stage);
                });
                
                delete.setOnAction(e -> {
                    User u = getTableView().getItems().get(getIndex());
                    new AdminDeleteUser(db, u.getUserName()).show(stage);
                });
                
            }

            @Override // housekeeping TableCell 
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : menu); // shows nothing for empty row, else menu 
            }
        });
        
        // sets the column order, change it here. 
        table.getColumns().addAll(colUsername, colName, colEmail, colRoles, colActions);

        // back button at top 
        Button btnBack = new Button("Back");
        btnBack.setOnAction(e -> new AdminHomePage(db).show(stage));

        Button btnRefresh = new Button("Refresh");
        btnRefresh.setOnAction(e -> refresh(table));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        ToolBar toolBar = new ToolBar(btnRefresh, spacer, btnBack);

        // ==== VBox root layout ====
        VBox root = new VBox(toolBar, table);
        root.setSpacing(10);
        root.setPadding(new Insets(10));

        // initial load
        refresh(table);

        Scene scene = new Scene(root, 900, 560);
        stage.setTitle("List All Users");
        stage.setScene(scene);
        stage.show();
    }

    private void refresh(TableView<User> table) {
        try {
            List<User> users = db.getAllUsers();
            ObservableList<User> data = FXCollections.observableArrayList(users);
            table.setItems(data);
        } catch (SQLException ex) {
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to load users: " + ex.getMessage()).showAndWait();
        }
    }
}
