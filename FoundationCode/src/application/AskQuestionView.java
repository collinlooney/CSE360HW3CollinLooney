package application;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import databasePart1.DatabaseHelper;


 // Builds and displays the UI for asking a new question. 
public class AskQuestionView {

	// Fields

    private final DatabaseHelper databaseHelper;
    private TextField titleField;
    private TextArea bodyTextArea;
    private ComboBox<Tags> tagComboBox;
    private CheckBox anonymousCheckBox;
    private CheckBox privateCheckBox;
    private Label feedbackLabel;

    // Constructor 
    public AskQuestionView(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    public void show(Stage primaryStage, User user) {
        VBox formVBox = new VBox(15);
        formVBox.setPadding(new Insets(20));
        formVBox.setStyle("-fx-background-color: #F4F4F4;");

        Label mainTitleLabel = new Label("Ask a New Question");
        mainTitleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));

        Label titleLabel = new Label("Title");
        titleField = new TextField();
        titleField.setPromptText("Enter a short, descriptive title for your question");

        Button similarThreadsButton = new Button("Check for Similar Threads");
        similarThreadsButton.setOnAction(e -> handleSimilarThreadsAction(primaryStage, user));

        VBox titleGroup = new VBox(5, titleLabel, titleField, similarThreadsButton);

        Label bodyLabel = new Label("Question Body");
        bodyTextArea = new TextArea();
        bodyTextArea.setPromptText("Provide all the details about your question here...");
        bodyTextArea.setWrapText(true);
        bodyTextArea.setPrefHeight(150);

        Label tagsLabel = new Label("Tag");
        tagComboBox = new ComboBox<>();
        tagComboBox.getItems().addAll(Tags.values());
        tagComboBox.setPromptText("Select a tag...");
        tagComboBox.setMaxWidth(Double.MAX_VALUE);

        Label optionsLabel = new Label("Options");
        anonymousCheckBox = new CheckBox("Post Anonymously");
        privateCheckBox = new CheckBox("Post Privately (visible only to you and instructors)");
        HBox optionsBox = new HBox(20, anonymousCheckBox, privateCheckBox);
        optionsBox.setAlignment(Pos.CENTER_LEFT);

        Button submitButton = new Button("Post Your Question");
        submitButton.setMaxWidth(Double.MAX_VALUE);
        submitButton.setPrefHeight(40);
   
        submitButton.setOnAction(event -> handleSubmitButtonAction(user, primaryStage));

        feedbackLabel = new Label();
        feedbackLabel.setWrapText(true);

        formVBox.getChildren().addAll(mainTitleLabel, titleGroup, bodyLabel, bodyTextArea, tagsLabel,
                tagComboBox, optionsLabel, optionsBox, submitButton, feedbackLabel);

        Button backButton = new Button("← Back to Main Menu");
        // Navigate back to the UserHomePage
        backButton.setOnAction(e -> new UserHomePage(databaseHelper).show(primaryStage, user));

        VBox container = new VBox(10, backButton, formVBox);
        container.setPadding(new Insets(10));
        VBox.setVgrow(formVBox, Priority.ALWAYS);

        Scene scene = new Scene(container, 800, 650);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Ask a Question");
    }

    // Action handler to validate user input 
    private void handleSubmitButtonAction(User currentUser, Stage primaryStage) {
        String title = titleField.getText().trim();
        String body = bodyTextArea.getText().trim();
        Tags selectedTag = tagComboBox.getValue();

        if (title.isEmpty()) { showError("Title cannot be empty."); return; }
        if (body.isEmpty()) { showError("Question body cannot be empty."); return; }
        if (selectedTag == null) { showError("Please select a tag."); return; }

        Question newQuestion = new Question(currentUser, title, body, selectedTag,
                privateCheckBox.isSelected(), anonymousCheckBox.isSelected());

        try {
            databaseHelper.addQuestion(newQuestion);
            new QuestionDetailView(databaseHelper).show(primaryStage, currentUser, newQuestion);
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Database Error: Could not save your question. Please try again.");
        }
    }


    // fetches similar posts from database 
    private void handleSimilarThreadsAction(Stage primaryStage, User user) {
        String searchTerm = titleField.getText().trim().toLowerCase();

        if (searchTerm.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Similar Threads");
            alert.setHeaderText(null);
            alert.setContentText("Please type something in the title field first to search for similar threads.");
            alert.showAndWait();
            return;
        }

        try {
            List<Question> allQuestions = databaseHelper.getAllPublicQuestions();
            List<Question> similarQuestions = new ArrayList<>();
            for (Question question : allQuestions) {
                if (question.getTitle().toLowerCase().contains(searchTerm)) {
                    similarQuestions.add(question);
                }
            }
            showSimilarThreadsWindow(similarQuestions, searchTerm, primaryStage, user);
        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Database Error");
            alert.setHeaderText(null);
            alert.setContentText("Could not fetch questions from the database.");
            alert.showAndWait();
        }
    }

     // Creates and displays a new pop up window showing the list of similar questions.
    

    private void showSimilarThreadsWindow(List<Question> similarQuestions, String searchTerm, Stage primaryStage, User user) {
        Stage window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle("Similar Threads for: \"" + searchTerm + "\"");

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(15));

        if (similarQuestions.isEmpty()) {
            layout.getChildren().add(new Label("No similar questions found."));
        } else {
            for (Question question : similarQuestions) {
                layout.getChildren().add(createSimilarQuestionNode(question, primaryStage, user));
            }
        }

        ScrollPane scrollPane = new ScrollPane(layout);
        scrollPane.setFitToWidth(true);

        Scene scene = new Scene(scrollPane, 500, 400);
        window.setScene(scene);
        window.showAndWait();
    }

// Create a clickable component for a single similar question
    private Node createSimilarQuestionNode(Question question, Stage primaryStage, User user) {
        VBox postBox = new VBox(5);
        postBox.setPadding(new Insets(10));

        final String normalStyle = "-fx-border-color: #DDDDDD; -fx-border-width: 1; -fx-border-radius: 3;";
        final String hoverStyle = "-fx-background-color: #F5F5F5; -fx-border-color: #CCCCCC; -fx-border-width: 1; -fx-border-radius: 3;";
        postBox.setStyle(normalStyle);
        postBox.setCursor(Cursor.HAND);

        postBox.setOnMouseEntered(e -> postBox.setStyle(hoverStyle));
        postBox.setOnMouseExited(e -> postBox.setStyle(normalStyle));
        
        // on click go to question detail view

        postBox.setOnMouseClicked(event -> {
        	new QuestionDetailView(databaseHelper).show(primaryStage, user, question);
            Stage popupStage = (Stage) postBox.getScene().getWindow();
            popupStage.close();
        });

        Label titleLabel = new Label(question.getTitle());
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        titleLabel.setWrapText(true);

        String author = question.isAnonymous() ? "Anonymous" : question.getAuthor().getName();
        String date = question.getCreationTimestamp().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"));
        Label infoLabel = new Label("Asked by " + author + " on " + date);
        infoLabel.setTextFill(Color.GRAY);

        postBox.getChildren().addAll(titleLabel, infoLabel);
        return postBox;
    }

    // Sets error message to feedback label 
    private void showError(String message) {
        feedbackLabel.setText(message);
        feedbackLabel.setTextFill(Color.RED);
    }
}