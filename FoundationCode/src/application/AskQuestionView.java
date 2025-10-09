package application;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
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
    private final boolean adminFlag; 
    private TextField titleField;
    private TextArea bodyTextArea;
    private CheckBox anonymousCheckBox;
    private CheckBox privateCheckBox;
    private Label feedbackLabel;
    private TitledPane similarThreadsPane; // collapsible panel
    private VBox similarThreadsContent; // holds similar question cards
    private ToggleGroup tagGroup; // colored tag selection

    // Constructor
    public AskQuestionView(DatabaseHelper databaseHelper, boolean adminFlag) {
        this.databaseHelper = databaseHelper;
        this.adminFlag = adminFlag; 
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

        // automatically search for similar threads as user types
        titleField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue.trim().length() > 2) {
                handleSimilarThreadsAction(primaryStage, user);
            } else {
                similarThreadsPane.setManaged(false);
                similarThreadsPane.setVisible(false);
            }
        });

        VBox titleGroup = new VBox(5, titleLabel, titleField);

        // Collapsable panel for similar questions
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

        // Question body section
        Label bodyLabel = new Label("Question Body");
        bodyTextArea = new TextArea();
        bodyTextArea.setPromptText("Provide all the details about your question here...");
        bodyTextArea.setWrapText(true);
        bodyTextArea.setPrefHeight(150);

        // Tags section
        Label tagsLabel = new Label("Tag");
        FlowPane tagPane = new FlowPane(8, 8); // tag spacing
        tagPane.setPadding(new Insets(5, 0, 0, 0));
        tagGroup = new ToggleGroup();

        // Tag color
        String[][] tags = {
                {"GENERAL", "#6366F1"},
                {"HOMEWORK", "#A855F7"},
                {"TEAM PROJECT", "#3B82F6"},
                {"QUIZZES", "#14B8A6"},
                {"EXAMS", "#F43F5E"},
                {"TEAM FORMATION", "#F59E0B"}
        };

        for (String[] tag : tags) {
        	// Creates a tag button for each category with unique color
            String text = tag[0];
            String color = tag[1];

            ToggleButton btn = new ToggleButton(text);
            btn.setToggleGroup(tagGroup);
            btn.setPrefHeight(28);
            btn.setMinWidth(90);

            String defaultStyle = "-fx-background-color: white; -fx-text-fill: #374151; -fx-font-size: 12px; "
                    + "-fx-font-weight: 500; -fx-border-color: #D1D5DB; -fx-border-width: 1.2; "
                    + "-fx-border-radius: 5; -fx-background-radius: 5; -fx-cursor: hand;";
            String selectedStyle = String.format("-fx-background-color: %s; -fx-text-fill: white; "
                    + "-fx-font-size: 12px; -fx-font-weight: 500; -fx-border-color: %s; "
                    + "-fx-border-width: 1.2; -fx-border-radius: 5; -fx-background-radius: 5;", color, color);

            btn.setStyle(defaultStyle);

            btn.selectedProperty().addListener((obs, oldVal, isSelected) -> {
                btn.setStyle(isSelected ? selectedStyle : defaultStyle);
            });

            tagPane.getChildren().add(btn);
        }

        // Show selected tag in feedback area (Can remove if you guys don't think its needed)
        tagGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle != null) {
                ToggleButton selected = (ToggleButton) newToggle;
                feedbackLabel.setText("Selected tag: " + selected.getText());
                feedbackLabel.setTextFill(Color.GRAY);
            }
        });

        VBox tagsBox = new VBox(5, tagsLabel, tagPane);

        // Options section
        Label optionsLabel = new Label("Options");
        anonymousCheckBox = new CheckBox("Post Anonymously");
        privateCheckBox = new CheckBox("Post Privately (visible only to you and instructors)");
        HBox optionsBox = new HBox(20, anonymousCheckBox, privateCheckBox);
        optionsBox.setAlignment(Pos.CENTER_LEFT);

        // Submit button
        Button submitButton = new Button("Post Your Question");
        submitButton.setMaxWidth(Double.MAX_VALUE);
        submitButton.setPrefHeight(40);
        submitButton.setOnAction(event -> handleSubmitButtonAction(user, primaryStage));

        feedbackLabel = new Label();
        feedbackLabel.setWrapText(true);

        // Add all components in order
        formVBox.getChildren().addAll(
                mainTitleLabel, titleGroup, similarThreadsPane,
                bodyLabel, bodyTextArea,
                tagsBox,
                optionsLabel, optionsBox,
                submitButton, feedbackLabel
        );

        Button backButton = new Button("← Back to Main Menu");
        backButton.setOnAction(e -> new UserHomePage(databaseHelper).show(primaryStage, user));

        VBox container = new VBox(10, backButton, formVBox);
        container.setPadding(new Insets(10));
        VBox.setVgrow(formVBox, Priority.ALWAYS);

        Scene scene = new Scene(container, 800, 650);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Ask a Question");
    }

    // Validate and submit
    private void handleSubmitButtonAction(User currentUser, Stage primaryStage) {
        String title = titleField.getText().trim();
        String body = bodyTextArea.getText().trim();
        ToggleButton selectedBtn = (ToggleButton) tagGroup.getSelectedToggle();
        String selectedTag = (selectedBtn != null) ? selectedBtn.getText() : null;

        if (title.isEmpty()) { showError("Title cannot be empty."); return; }
        if (body.isEmpty()) { showError("Question body cannot be empty."); return; }
        if (selectedTag == null) { showError("Please select a tag."); return; }

        Question newQuestion = new Question(
                currentUser,
                title,
                body,
                Tags.valueOf(selectedTag.replace(" ", "_")), // converts text to enum
                privateCheckBox.isSelected(),
                anonymousCheckBox.isSelected()
        );

        try {
            databaseHelper.addQuestion(newQuestion);
            new QuestionDetailView(databaseHelper, adminFlag).show(primaryStage, currentUser, newQuestion);
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Database Error: Could not save your question. Please try again.");
        }
    }

    // Fetch similar posts
    private void handleSimilarThreadsAction(Stage primaryStage, User user) {
        String searchTerm = titleField.getText().trim().toLowerCase();

        if (searchTerm.isEmpty() || searchTerm.length() < 3) {
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
            showSimilarThreadsCollapsible(similarQuestions, primaryStage, user);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Show similar threads
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
        	// Builds card preview showing title, short body, tag, author, and date
            VBox questionBox = createSimilarQuestionCard(question, primaryStage, user);
            similarThreadsContent.getChildren().add(questionBox);
        }
    }

    // Create clickable cards for similar questions
    private VBox createSimilarQuestionCard(Question question, Stage primaryStage, User user) {
    	
    	// Card container 
        VBox card = new VBox(5);
        card.setPadding(new Insets(10));
        card.setStyle("-fx-background-color: white; -fx-border-color: #E0E0E0; -fx-border-width: 1; "
                + "-fx-border-radius: 3; -fx-background-radius: 3;");
        card.setCursor(Cursor.HAND);
        
        // Hover effect
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #F5F5F5; -fx-border-color: #B0B0B0; "
                + "-fx-border-width: 1; -fx-border-radius: 3; -fx-background-radius: 3;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: white; -fx-border-color: #E0E0E0; "
                + "-fx-border-width: 1; -fx-border-radius: 3; -fx-background-radius: 3;"));
        
        // Navigates to the question detail view when clicked
        card.setOnMouseClicked(event -> new QuestionDetailView(databaseHelper,adminFlag).show(primaryStage, user, question));
        
        // Text content
        VBox textBox = new VBox(3);
        Label titleLabel = new Label(question.getTitle());
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        titleLabel.setWrapText(true);
        
        // Displays a short preview (first 100 char)
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

        HBox bottomRow = new HBox(15);
        bottomRow.setAlignment(Pos.CENTER_LEFT);
        bottomRow.setPadding(new Insets(5, 0, 0, 0));
        
        // Creates tag label and applies matching color 
        Label tagLabel = new Label(question.getTag().toString());
        tagLabel.setFont(Font.font("System", 10));
        tagLabel.setTextFill(Color.WHITE);

        // Assigns a color to the tag label based on its type for visual consistency with tag buttons
        String color;
        switch (question.getTag()) {
            case GENERAL -> color = "#6366F1";
            case HOMEWORK -> color = "#A855F7";     
            case TEAM_PROJECT -> color = "#3B82F6";
            case QUIZZES -> color = "#14B8A6";
            case EXAMS -> color = "#F43F5E";
            case TEAM_FORMATION -> color = "#F59E0B";
            default -> color = "#6B7280";     
        }

        tagLabel.setStyle(String.format(
                "-fx-background-color: %s; -fx-padding: 2 6 2 6; -fx-background-radius: 3;", color));
        
        // Display author name or "Anonymous" if chosen
        String author = question.isAnonymous() ? "Anonymous" : question.getAuthor().getName();
        Label authorLabel = new Label(author);
        authorLabel.setFont(Font.font("System", 10));
        authorLabel.setTextFill(Color.GRAY);
        
        // Display creation date in short format
        String date = question.getCreationTimestamp().format(DateTimeFormatter.ofPattern("MMM d"));
        Label dateLabel = new Label(date);
        dateLabel.setFont(Font.font("System", 10));
        dateLabel.setTextFill(Color.GRAY);
        
        // Combine metadata into one row (tag, name, date etc)
        bottomRow.getChildren().addAll(tagLabel, authorLabel, dateLabel);

        // adds all components to the card
        card.getChildren().addAll(textBox, bottomRow);

        return card;
    }

    // Error helper
    private void showError(String message) {
        feedbackLabel.setText(message);
        feedbackLabel.setTextFill(Color.RED);
    }
}
