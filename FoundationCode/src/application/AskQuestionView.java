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
    private TitledPane similarThreadsPane; // new collapsible panel
    private VBox similarThreadsContent; // holds similar question cards

    // Constructor 
    public AskQuestionView(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    // Main UI Layout
    public void show(Stage primaryStage, User user) {
        VBox formVBox = new VBox(15);
        formVBox.setPadding(new Insets(20));
        formVBox.setStyle("-fx-background-color: #F4F4F4;");

        Label mainTitleLabel = new Label("Ask a New Question");
        mainTitleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));

        Label titleLabel = new Label("Title");
        titleField = new TextField();
        titleField.setPromptText("Enter a short, descriptive title for your question");
        
        // automatically search for similar threads as user types (removed button)
        titleField.textProperty().addListener((observable, oldValue, newValue) -> {
        	// Only start searching and showing similar threads after the user types at least 2 characters.
            if (newValue != null && newValue.trim().length() > 2) {
                handleSimilarThreadsAction(primaryStage, user);
            } else {
                similarThreadsPane.setManaged(false);
                similarThreadsPane.setVisible(false);
            }
        });

        VBox titleGroup = new VBox(5, titleLabel, titleField);
        
        // Collapsible panel that shows similar questions 
        similarThreadsContent = new VBox(5);
        similarThreadsContent.setPadding(new Insets(5));
        
        ScrollPane similarScrollPane = new ScrollPane(similarThreadsContent);
        similarScrollPane.setFitToWidth(true);
        similarScrollPane.setPrefHeight(200);
        similarScrollPane.setStyle("-fx-background: white; -fx-border-color: transparent;");
        
        similarThreadsPane = new TitledPane("Similar Threads", similarScrollPane);
        similarThreadsPane.setExpanded(true);
        similarThreadsPane.setManaged(false);
        similarThreadsPane.setVisible(false);
        // end collapsable panel
        
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
        
        // Inline similarThreadsPane (no popup window)
        formVBox.getChildren().addAll(mainTitleLabel, titleGroup, similarThreadsPane, bodyLabel, bodyTextArea, tagsLabel,
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


    // fetches similar posts from database while user is typing
    private void handleSimilarThreadsAction(Stage primaryStage, User user) {
        String searchTerm = titleField.getText().trim().toLowerCase();

        if (searchTerm.isEmpty() || searchTerm.length() < 3) { // At least 3 characters must be entered to search
            similarThreadsPane.setManaged(false);
            similarThreadsPane.setVisible(false);
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
            // Update collapsible panel
            showSimilarThreadsCollapsible(similarQuestions, primaryStage, user);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Displays similar questions in collapsible panel below title field 
    private void showSimilarThreadsCollapsible(List<Question> similarQuestions, Stage primaryStage, User user) {
        similarThreadsContent.getChildren().clear();
        
        if (similarQuestions.isEmpty()) {
            similarThreadsPane.setManaged(false);
            similarThreadsPane.setVisible(false);
            return;
        }
        
        similarThreadsPane.setManaged(true);
        similarThreadsPane.setVisible(true);
        
        for (Question question : similarQuestions) {
            VBox questionBox = createSimilarQuestionCard(question, primaryStage, user);
            similarThreadsContent.getChildren().add(questionBox);
        }
    }
    
    // Creates clickable card for each similar question under "Similar Threads"
    private VBox createSimilarQuestionCard(Question question, Stage primaryStage, User user) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(10));
        card.setStyle("-fx-background-color: white; -fx-border-color: #E0E0E0; -fx-border-width: 1; -fx-border-radius: 3; -fx-background-radius: 3;");
        card.setCursor(Cursor.HAND);
        
        // Hover and click for each card
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #F5F5F5; -fx-border-color: #B0B0B0; -fx-border-width: 1; -fx-border-radius: 3; -fx-background-radius: 3;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: white; -fx-border-color: #E0E0E0; -fx-border-width: 1; -fx-border-radius: 3; -fx-background-radius: 3;"));
        
        card.setOnMouseClicked(event -> {
            new QuestionDetailView(databaseHelper).show(primaryStage, user, question);
        });
        
        // Card text layout
        VBox textBox = new VBox(3);
        
        Label titleLabel = new Label(question.getTitle());
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        titleLabel.setWrapText(true);
        
        String bodyPreview = question.getBody();
        if (bodyPreview != null && bodyPreview.length() > 100) {
            bodyPreview = bodyPreview.substring(0, 100) + "...";
        } else if (bodyPreview == null) {
            bodyPreview = "";
        }
        Label bodyLabel = new Label(bodyPreview);
        bodyLabel.setFont(Font.font("System", 11));
        bodyLabel.setTextFill(Color.GRAY);
        bodyLabel.setWrapText(true);
        
        textBox.getChildren().addAll(titleLabel, bodyLabel);
        
        // Card footer info (tag, author, date)
        HBox bottomRow = new HBox(15);
        bottomRow.setAlignment(Pos.CENTER_LEFT);
        bottomRow.setPadding(new Insets(5, 0, 0, 0));
        
        Label tagLabel = new Label(question.getTag().toString());
        tagLabel.setFont(Font.font("System", 10));
        tagLabel.setTextFill(Color.WHITE);
        tagLabel.setStyle("-fx-background-color: #0078D4; -fx-padding: 2 6 2 6; -fx-background-radius: 3;");
        
        String author = question.isAnonymous() ? "Anonymous" : question.getAuthor().getName();
        Label authorLabel = new Label(author);
        authorLabel.setFont(Font.font("System", 10));
        authorLabel.setTextFill(Color.GRAY);
        
        String date = question.getCreationTimestamp().format(DateTimeFormatter.ofPattern("MMM d"));
        Label dateLabel = new Label(date);
        dateLabel.setFont(Font.font("System", 10));
        dateLabel.setTextFill(Color.GRAY);
        
        bottomRow.getChildren().addAll(tagLabel, authorLabel, dateLabel);
        
        card.getChildren().addAll(textBox, bottomRow);
        
        return card;
    }

    // Sets error message to feedback label 
    private void showError(String message) {
        feedbackLabel.setText(message);
        feedbackLabel.setTextFill(Color.RED);
    }
}
