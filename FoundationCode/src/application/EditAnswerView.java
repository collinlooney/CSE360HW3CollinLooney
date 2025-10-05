package application;

import java.sql.SQLException;
import databasePart1.DatabaseHelper;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;


// Builds UI for updating an answer
public class EditAnswerView {

    private final DatabaseHelper databaseHelper;
    private TextArea bodyTextArea;
    private Label feedbackLabel;

    public EditAnswerView(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }
    
    

    public void show(Stage primaryStage, User user, Answer answerToEdit) {
        VBox formVBox = new VBox(15);
        formVBox.setPadding(new Insets(20));
        formVBox.setStyle("-fx-background-color: #F4F4F4;");

        Label mainTitleLabel = new Label("Edit Your Answer");
        mainTitleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));

        bodyTextArea = new TextArea();
        bodyTextArea.setWrapText(true);
        bodyTextArea.setPrefHeight(200);
        bodyTextArea.setText(answerToEdit.getBody());

        Button saveButton = new Button("Save Changes");
        saveButton.setMaxWidth(Double.MAX_VALUE);
        saveButton.setPrefHeight(40);
        saveButton.setOnAction(event -> handleSaveChangesAction(primaryStage, user, answerToEdit));

        feedbackLabel = new Label();
        feedbackLabel.setWrapText(true);

        formVBox.getChildren().addAll(mainTitleLabel, bodyTextArea, saveButton, feedbackLabel);

        Button backButton = new Button("← Back to Question");
        backButton.setOnAction(e -> new QuestionDetailView(databaseHelper).show(primaryStage, user, answerToEdit.getParentQuestion()));

        VBox container = new VBox(10, backButton, formVBox);
        container.setPadding(new Insets(10));
        VBox.setVgrow(formVBox, Priority.ALWAYS);

        Scene scene = new Scene(container, primaryStage.getScene().getWidth(), primaryStage.getScene().getHeight());
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void handleSaveChangesAction(Stage primaryStage, User user, Answer answerToEdit) {
        String newBody = bodyTextArea.getText().trim();

        if (newBody.isEmpty()) {
            showError("The answer body cannot be empty.");
            return;
        }

        answerToEdit.setBody(newBody);

        try {
            databaseHelper.updateAnswer(answerToEdit);
            new QuestionDetailView(databaseHelper).show(primaryStage, user, answerToEdit.getParentQuestion());
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