package application;

import application.Authorization;

import java.sql.SQLException;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;

import databasePart1.DatabaseHelper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;


// Builds and displays the UI for the Discussion Board, which shows a filterable
// list of all public questions fetched from the database.

public class DiscussionBoardView {

    // fields
    private final DatabaseHelper databaseHelper;
    private TextField searchField;
    // The main container that holds the list of question summary nodes. 
    private VBox postsContainer;
    // flag for if user is admin
    private final boolean adminFlag; 

    // Constructor
    public DiscussionBoardView(DatabaseHelper databaseHelper, boolean adminFlag) {
        this.databaseHelper = databaseHelper;
        this.adminFlag = adminFlag;
    }

    public void show(Stage primaryStage, User user) {
        postsContainer = new VBox(10);
        postsContainer.setPadding(new Insets(10));
        postsContainer.setStyle("-fx-background-color: #FAFAFA;");

        // Search bar setup
        searchField = new TextField();
        searchField.setPromptText("Search questions by title...");
        searchField.setPrefHeight(35);

        HBox searchBox = new HBox(searchField);
        HBox.setHgrow(searchField, Priority.ALWAYS);
        searchBox.setPadding(new Insets(0, 10, 0, 10));

        // Filter setup
        // Allows user to filter pre existing questions
        ComboBox<String> showFilter = new ComboBox<>();
        showFilter.getItems().addAll("All Questions", "My Questions");
        showFilter.setValue("All Questions");

        ComboBox<String> statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("All", "Resolved", "Unresolved");
        statusFilter.setValue("All");

        ComboBox<Tags> tagFilter = new ComboBox<>();
        tagFilter.getItems().addAll(Tags.values());
        tagFilter.setPromptText("All Tags");

        ComboBox<String> sortFilter = new ComboBox<>();
        sortFilter.getItems().addAll("Newest First", "Oldest First");
        sortFilter.setValue("Newest First");

        Button resetButton = new Button("Reset");
        resetButton.setOnAction(e -> {
            showFilter.setValue("All Questions");
            statusFilter.setValue("All");
            tagFilter.setValue(null);
            sortFilter.setValue("Newest First");
            searchField.clear();
            filterQuestions("", primaryStage, user, showFilter, statusFilter, tagFilter, sortFilter);
        });

        HBox filterBar = new HBox(10, new Label("Show:"), showFilter,
                new Label("Status:"), statusFilter,
                new Label("Tag:"), tagFilter,
                new Label("Sort:"), sortFilter,
                resetButton);
        filterBar.setPadding(new Insets(10));
        filterBar.setAlignment(Pos.CENTER_LEFT);

        // Live updates on search or filters
        searchField.textProperty().addListener((obs, oldText, newText) -> filterQuestions(newText, primaryStage, user, showFilter, statusFilter, tagFilter, sortFilter));
        showFilter.setOnAction(e -> filterQuestions(searchField.getText(), primaryStage, user, showFilter, statusFilter, tagFilter, sortFilter));
        statusFilter.setOnAction(e -> filterQuestions(searchField.getText(), primaryStage, user, showFilter, statusFilter, tagFilter, sortFilter));
        tagFilter.setOnAction(e -> filterQuestions(searchField.getText(), primaryStage, user, showFilter, statusFilter, tagFilter, sortFilter));
        sortFilter.setOnAction(e -> filterQuestions(searchField.getText(), primaryStage, user, showFilter, statusFilter, tagFilter, sortFilter));

        // Initial population of the question list
        filterQuestions("", primaryStage, user, showFilter, statusFilter, tagFilter, sortFilter);

        // Scroll pane for the posts
        ScrollPane scrollPane = new ScrollPane(postsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");

        Button backButton = new Button("← Back to Main Menu");
        backButton.setOnAction(e -> {
            if (adminFlag) {
                new AdminHomePage(databaseHelper).show(primaryStage, user);
            } else {
                new UserHomePage(databaseHelper).show(primaryStage, user);
            }
        });

        // Final layout container
        VBox container = new VBox(10, backButton, searchBox, filterBar, scrollPane);
        container.setPadding(new Insets(10));
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        
        Scene scene = new Scene(container, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Discussion Board");
    }

    // Private Helper Methods
    private void filterQuestions(String searchText, Stage primaryStage, User user,
                                 ComboBox<String> showFilter,
                                 ComboBox<String> statusFilter,
                                 ComboBox<Tags> tagFilter,
                                 ComboBox<String> sortFilter) {
        postsContainer.getChildren().clear();

        try {
            List<Question> questions = databaseHelper.getAllPublicQuestions();
            String lowerCaseSearch = searchText.trim().toLowerCase();

            // search filter
            questions.removeIf(q -> !q.getTitle().toLowerCase().contains(lowerCaseSearch));

            // show filter
            if ("My Questions".equals(showFilter.getValue())) {
                questions.removeIf(q -> !q.getAuthor().getUserName().equals(user.getUserName()));
            }

            // resolved/unresolved filter
            if ("Resolved".equals(statusFilter.getValue())) {
                questions.removeIf(q -> {
                    try {
                        return !databaseHelper.hasAcceptedAnswer(q.getQuestionId().toString());
                    } catch (SQLException e) {
                        return true;
                    }
                });
            } else if ("Unresolved".equals(statusFilter.getValue())) {
                questions.removeIf(q -> {
                    try {
                        return databaseHelper.hasAcceptedAnswer(q.getQuestionId().toString());
                    } catch (SQLException e) {
                        return false;
                    }
                });
            }

            // tag filter
            if (tagFilter.getValue() != null) {
                questions.removeIf(q -> q.getTag() != tagFilter.getValue());
            }

            // sort filter
            questions.sort((a, b) -> {
                if ("Newest First".equals(sortFilter.getValue())) {
                    return b.getCreationTimestamp().compareTo(a.getCreationTimestamp());
                } else {
                    return a.getCreationTimestamp().compareTo(b.getCreationTimestamp());
                }
            });

            if (questions.isEmpty()) {
                postsContainer.getChildren().add(new Label("No questions match your filters."));
            } else {
                for (Question q : questions) {
                    postsContainer.getChildren().add(createQuestionSummaryNode(q, primaryStage, user));
                }
            }

        } catch (SQLException e) {
            postsContainer.getChildren().add(new Label("Error: Could not load questions."));
        }
    }

    // Creates a clickable UI node that displays a summary of a single question.
    private Node createQuestionSummaryNode(Question question, Stage primaryStage, User user) {
        VBox summaryBox = new VBox(5);
        summaryBox.setPadding(new Insets(10));

        final String normalStyle = "-fx-background-color: white; -fx-border-color: #E0E0E0; -fx-border-width: 1; -fx-border-radius: 5;";
        final String hoverStyle = "-fx-background-color: #F5F5F5; -fx-border-color: #CCCCCC; -fx-border-width: 1; -fx-border-radius: 5;";
        summaryBox.setStyle(normalStyle);
        summaryBox.setCursor(Cursor.HAND);
        summaryBox.setOnMouseEntered(e -> summaryBox.setStyle(hoverStyle));
        summaryBox.setOnMouseExited(e -> summaryBox.setStyle(normalStyle));

        // Navigate to the detail view when clicked
        summaryBox.setOnMouseClicked(e -> new QuestionDetailView(databaseHelper, adminFlag).show(primaryStage, user, question));

        Label titleLabel = new Label(question.getTitle());
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));

        HBox metadataBox = new HBox(15);
        metadataBox.setAlignment(Pos.CENTER_LEFT);

        Label tagLabel = new Label(question.getTag().toString());
        tagLabel.setFont(Font.font("System", 12));
        tagLabel.setPadding(new Insets(3, 10, 3, 10));
        tagLabel.setStyle("-fx-background-radius: 10; -fx-font-weight: 600;");

        // Tag Colors
        switch (question.getTag()) {
            case GENERAL:
                tagLabel.setStyle(tagLabel.getStyle() + "-fx-background-color: #6C63AC; -fx-text-fill: white;");
                break;
            case HOMEWORK:
                tagLabel.setStyle(tagLabel.getStyle() + "-fx-background-color: #EF7FAF; -fx-text-fill: white;");
                break;
            case TEAM_PROJECT:
                tagLabel.setStyle(tagLabel.getStyle() + "-fx-background-color: #EC6A5C; -fx-text-fill: white;");
                break;
            case QUIZZES:
                tagLabel.setStyle(tagLabel.getStyle() + "-fx-background-color: #F89963; -fx-text-fill: #1F2937;"); // dark gray
                break;
            case EXAMS:
                tagLabel.setStyle(tagLabel.getStyle() + "-fx-background-color: #F5C46D; -fx-text-fill: #1F2937;"); // dark gray
                break;
            case TEAM_FORMATION:
                tagLabel.setStyle(tagLabel.getStyle() + "-fx-background-color: #9BC48C; -fx-text-fill: #1F2937;"); // dark gray
                break;
            default:
                tagLabel.setStyle(tagLabel.getStyle() + "-fx-background-color: #6B7280; -fx-text-fill: white;");
                break;
        }

        String author = question.isAnonymous() ? "Anonymous" : question.getAuthor().getName();
        Label authorLabel = new Label("by " + author);
        authorLabel.setTextFill(Color.GRAY);

        Label timeLabel = new Label("• " + formatTimeSince(question.getCreationTimestamp()));
        timeLabel.setTextFill(Color.GRAY);

        int answerCount = databaseHelper.getAnswerCountForQuestion(question.getQuestionId().toString());
        Label answersLabel = new Label("• " + answerCount + (answerCount == 1 ? " answer" : " answers"));
        answersLabel.setTextFill(Color.DARKBLUE);

        // Resolution status label
        Label statusLabel = new Label();
        boolean resolved = false;
        try {
            // Checks if any answer in DB is marked as resolving this question
            resolved = databaseHelper.hasAcceptedAnswer(question.getQuestionId().toString());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (resolved) {
            statusLabel.setText("Resolved");
            statusLabel.setTextFill(Color.web("#388E3C"));
            statusLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        } else {
            statusLabel.setText("Unresolved");
            statusLabel.setTextFill(Color.web("#9E9E9E"));
            statusLabel.setStyle("-fx-font-size: 12px;");
        }

        // Combine title and status
        HBox headerRow = new HBox(10, titleLabel, statusLabel);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        metadataBox.getChildren().addAll(tagLabel, authorLabel, timeLabel, answersLabel);
        summaryBox.getChildren().addAll(headerRow, metadataBox);

        return summaryBox;
    }

    // Creates time since 
    private String formatTimeSince(ZonedDateTime time) {
        if (time == null) return "some time ago";
        Duration duration = Duration.between(time, ZonedDateTime.now());
        long seconds = duration.getSeconds();

        if (seconds < 60) return "just now";
        long minutes = seconds / 60;
        if (minutes < 60) return minutes + (minutes == 1 ? " minute ago" : " minutes ago");
        long hours = minutes / 60;
        if (hours < 24) return hours + (hours == 1 ? " hour ago" : " hours ago");
        long days = hours / 24;
        if (days < 30) return days + (days == 1 ? " day ago" : " days ago");
        long months = days / 30;
        if (months < 12) return months + (months == 1 ? " month ago" : " months ago");

        long years = months / 12;
        return years + (years == 1 ? " year ago" : " years ago");
    }
}
