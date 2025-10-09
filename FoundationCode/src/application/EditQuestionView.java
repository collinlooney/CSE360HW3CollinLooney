package application;

import java.sql.SQLException;

import databasePart1.DatabaseHelper;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;


 // Builds and displays the UI for editing an existing question

public class EditQuestionView {

  

    private final DatabaseHelper databaseHelper;
    private final boolean adminFlag; 

    private TextField titleField;
    private TextArea bodyTextArea;
    private ComboBox<Tags> tagComboBox;
    private Label feedbackLabel;


    public EditQuestionView(DatabaseHelper databaseHelper, boolean adminFlag) {
        this.databaseHelper = databaseHelper;
        this.adminFlag = adminFlag; 
    }

 
    public void show(Stage primaryStage, User user, Question questionToEdit) {
        VBox formVBox = new VBox(15);
        formVBox.setPadding(new Insets(20));
        formVBox.setStyle("-fx-background-color: #F4F4F4;");

        Label mainTitleLabel = new Label("Edit Your Question");
        mainTitleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));

        Label titleLabel = new Label("Title");
        titleField = new TextField();

        Label bodyLabel = new Label("Question Body");
        bodyTextArea = new TextArea();
        bodyTextArea.setWrapText(true);
        bodyTextArea.setPrefHeight(150);

        Label tagsLabel = new Label("Tag");
        tagComboBox = new ComboBox<>();
        tagComboBox.getItems().addAll(Tags.values());
        tagComboBox.setMaxWidth(Double.MAX_VALUE);

        // prepopulate the form fields with the existing question data.
        titleField.setText(questionToEdit.getTitle());
        bodyTextArea.setText(questionToEdit.getBody());
        tagComboBox.setValue(questionToEdit.getTag());

        Button saveButton = new Button("Save Changes");
        saveButton.setMaxWidth(Double.MAX_VALUE);
        saveButton.setPrefHeight(40);
        saveButton.setOnAction(event -> handleSaveChangesAction(primaryStage, user, questionToEdit));

        feedbackLabel = new Label();
        feedbackLabel.setWrapText(true);

        formVBox.getChildren().addAll(mainTitleLabel, titleLabel, titleField, bodyLabel, bodyTextArea,
                tagsLabel, tagComboBox, saveButton, feedbackLabel);

        // back button returns to the question detail view without saving changes.
        Button backButton = new Button("← Back to Question");
        backButton.setOnAction(e -> new QuestionDetailView(databaseHelper, adminFlag).show(primaryStage, user, questionToEdit));

        VBox container = new VBox(10, backButton, formVBox);
        container.setPadding(new Insets(10));
        VBox.setVgrow(formVBox, Priority.ALWAYS);

        Scene scene = new Scene(container, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Edit Question");
    }

 
    // helper methods
    private void handleSaveChangesAction(Stage primaryStage, User user, Question questionToEdit) {
        String newTitle = titleField.getText().trim();
        String newBody = bodyTextArea.getText().trim();
        Tags newTag = tagComboBox.getValue();

        if (newTitle.isEmpty() || newBody.isEmpty() || newTag == null) {
            showError("All fields must be filled out.");
            return;
        }

        // update the Question object in memory.
        questionToEdit.setTitle(newTitle);
        questionToEdit.setBody(newBody);
        questionToEdit.setTag(newTag);

        try {
            // save the updated object to the database.
            databaseHelper.updateQuestion(questionToEdit);
            // navigate back to the detail view to see the changes.
            new QuestionDetailView(databaseHelper, adminFlag).show(primaryStage, user, questionToEdit);
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Database Error: Could not save your changes.");
        }
    }


    private void showError(String message) {
        feedbackLabel.setText(message);
        feedbackLabel.setTextFill(Color.RED);
    }
}