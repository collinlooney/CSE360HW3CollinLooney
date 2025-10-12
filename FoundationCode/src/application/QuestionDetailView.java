package application;

import application.Authorization;
import application.Role; 

import java.sql.SQLException;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import databasePart1.DatabaseHelper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.scene.layout.StackPane; // Used for overlaying solved badge on accepted answers


// Builds and displays the UI for a single question, including answers and comments.
public class QuestionDetailView {

    private final DatabaseHelper databaseHelper;
    private final boolean adminFlag; 

    public QuestionDetailView(DatabaseHelper databaseHelper, boolean adminFlag) {
        this.databaseHelper = databaseHelper;
        this.adminFlag = adminFlag;
    }

    public void show(Stage primaryStage, User user, Question question) {
        // Increment view count if the current user is not the author.
        if (user != null && !user.getUserName().equals(question.getAuthor().getUserName())) {
            question.incrementViewCount();
            try {
                this.databaseHelper.updateQuestionViewCount(question);
            } catch (SQLException e) {
                e.printStackTrace(); // Log if the update fails
            }
        }

        // Load all answers and comments from the database.
        try {
            question.setPotentialAnswers(new ArrayList<>());
            databaseHelper.loadAnswersAndCommentsForQuestion(question);
        } catch (SQLException e) {
            e.printStackTrace();
            VBox errorBox = new VBox(new Label("Error: Could not load question details."));
            primaryStage.getScene().setRoot(errorBox);
            return;
        }

        VBox postContent = createFullQuestionPostView(primaryStage, user, question);

        ScrollPane scrollPane = new ScrollPane(postContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");

        Button backButton = new Button("← Back to Discussion Board");
        backButton.setOnAction(e -> new DiscussionBoardView(databaseHelper, adminFlag).show(primaryStage, user));

        VBox container = new VBox(10, backButton, scrollPane);
        container.setPadding(new Insets(10));
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        Scene scene = new Scene(container, 800, 700);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Question Details");
    }

    private VBox createFullQuestionPostView(Stage primaryStage, User user, Question question) {
        VBox postBox = new VBox(15);
        postBox.setPadding(new Insets(15));
        postBox.setStyle("-fx-background-color: white;");

        Label titleLabel = new Label(question.getTitle());
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        titleLabel.setWrapText(true);

        HBox titleBar = new HBox();
        titleBar.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(titleLabel, Priority.ALWAYS);
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        titleBar.getChildren().add(titleLabel);

        // admin/owner controls
        boolean showEdit = Authorization.canEditQuestion(user, databaseHelper, question);
        boolean showDelete = Authorization.canDeleteQuestion(user, databaseHelper, question, adminFlag);

        HBox ownerControls = new HBox(10);
        ownerControls.setAlignment(Pos.CENTER_RIGHT);

        if (showEdit) {
            Button editButton = new Button("Edit");
            editButton.setOnAction(e -> new EditQuestionView(databaseHelper, adminFlag).show(primaryStage, user, question));
            ownerControls.getChildren().add(editButton);
        }

        if (showDelete) {
            Button deleteButton = new Button("Delete");
            deleteButton.setStyle("-fx-background-color: #FFCDD2;");
            deleteButton.setOnAction(e -> handleDeleteQuestionAction(primaryStage, user, question));
            ownerControls.getChildren().add(deleteButton);
        }

        if (!ownerControls.getChildren().isEmpty()) {
            titleBar.getChildren().add(ownerControls);
        }

        // Metadata UI
        Label authorLabel = new Label(question.isAnonymous() ? "Anonymous" : question.getAuthor().getName());
        authorLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        Label timeSinceLabel = new Label(formatTimeSince(question.getCreationTimestamp()));
        Label tagLabel = new Label("in " + question.getTag().toString());
        tagLabel.setStyle("-fx-background-color: #E3F2FD; -fx-padding: 3 5 3 5; -fx-background-radius: 5;");
        HBox timeTagBox = new HBox(10, timeSinceLabel, tagLabel);
        timeTagBox.setAlignment(Pos.CENTER_LEFT);
        VBox leftMetadata = new VBox(5, authorLabel, timeTagBox);

        Label viewCountLabel = new Label(String.valueOf(question.getViewCount()));
        VBox viewColumn = new VBox(2, viewCountLabel, new Label("views"));
        viewColumn.setAlignment(Pos.CENTER);
        HBox rightMetadata = new HBox(15, viewColumn);
        rightMetadata.setAlignment(Pos.CENTER);

        BorderPane metadataPane = new BorderPane();
        metadataPane.setLeft(leftMetadata);
        metadataPane.setRight(rightMetadata);

        // QUESTION BODY
        // Container with padding, border, and background.
        Label bodyLabel = new Label(question.getBody());
        bodyLabel.setWrapText(true);
        bodyLabel.setTextFill(Color.web("#1F2937"));
        bodyLabel.setStyle("-fx-font-size: 14px;");

        VBox bodyContainer = new VBox(bodyLabel);
        bodyContainer.setPadding(new Insets(16));
        bodyContainer.setStyle(
            "-fx-background-color: #F9FAFB;" +
            "-fx-background-radius: 8;" +
            "-fx-border-radius: 8;" +
            "-fx-border-color: #E5E7EB;" +
            "-fx-border-width: 1;"
        );
        // ANSWERS SECTION
        // Loads and displays all answers below the question, including accepted-answer highlighting and nested comments.
        VBox answersSection = createAnswersSection(primaryStage, user, question);
        postBox.getChildren().addAll(titleBar, metadataPane, new Separator(), bodyContainer, new Separator(), answersSection);
        return postBox;
    }

    private VBox createAnswersSection(Stage primaryStage, User user, Question question) {
        VBox sectionVBox = new VBox(15);
        List<Answer> answers = question.getPotentialAnswers();
        // Sort so that accepted answers appear first
        answers.sort((a, b) -> Boolean.compare(!a.getResolvesQuestion(), !b.getResolvesQuestion()));


        Label answersHeader = new Label();
        answersHeader.setFont(Font.font("System", FontWeight.BOLD, 18));
        sectionVBox.getChildren().add(answersHeader);

        if (answers.isEmpty()) {
            answersHeader.setText("No Answers Yet");
        } else {
            answersHeader.setText(answers.size() + (answers.size() == 1 ? " Answer" : " Answers"));
            VBox answersContainer = new VBox(15);
            for (Answer answer : answers) {
                answersContainer.getChildren().add(createAnswerNode(primaryStage, user, question, answer));
            }
            sectionVBox.getChildren().add(answersContainer);
        }
        sectionVBox.getChildren().add(createAnswerSubmissionNode(primaryStage, user, question));
        return sectionVBox;
    }

    private Node createAnswerSubmissionNode(Stage primaryStage, User user, Question question) {
        VBox submissionBox = new VBox(10);
        Label answerPromptLabel = new Label("Your Answer");
        answerPromptLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        TextArea answerArea = new TextArea();
        answerArea.setPromptText("Type your answer here...");
        answerArea.setPrefHeight(120);
        Button submitAnswerButton = new Button("Post Your Answer");

        submitAnswerButton.setOnAction(e -> {
            String answerText = answerArea.getText().trim();
            if (!answerText.isEmpty()) {
                Answer newAnswer = new Answer(question, user, answerText);
                try {
                    databaseHelper.addAnswer(newAnswer);
                    // Refresh the view to show the new answer
                    this.show(primaryStage, user, question);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    new Alert(Alert.AlertType.ERROR, "Could not save your answer.").showAndWait();
                }
            }
        });
        submissionBox.getChildren().addAll(answerPromptLabel, answerArea, submitAnswerButton);
        return submissionBox;
    }

    private Node createAnswerNode(Stage primaryStage, User user, Question question, Answer answer) {
        // ANSWER CARD
        // Card with border, shadow, and padding for each answer.
        VBox answerBox = new VBox();
        answerBox.setPadding(new Insets(14));
        answerBox.setSpacing(6);
        answerBox.setStyle(
            "-fx-background-color: linear-gradient(to right, #F8FAF8, #F8FAFA);" +
            "-fx-border-color: #E0E0E0;" +
            "-fx-border-width: 1.5;" +
            "-fx-border-radius: 10;" +
            "-fx-background-radius: 10;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.03), 6, 0, 0, 1);"
        );

        Label answerBody = new Label(answer.getBody());
        answerBody.setWrapText(true);
        answerBody.setStyle("-fx-text-fill: #374151; -fx-font-size: 14px;");

        String authorName = answer.getAuthor().getName();
        String timeSince = formatTimeSince(answer.getCreationTimestamp());
        Label infoLabel = new Label("answered by " + authorName + " • " + timeSince);
        infoLabel.setTextFill(Color.web("#6B7280"));
        infoLabel.setStyle("-fx-font-size: 12px;");

        VBox commentsContainer = new VBox(10);
        commentsContainer.setPadding(new Insets(5, 0, 0, 20));
        for (Comment comment : answer.getComments()) {
            if (comment.getParentComment() == null) {
                commentsContainer.getChildren().add(createCommentNode(primaryStage, user, question, comment));
            }
        }
        
        // COMMENT BUTTON
        // Style for comment button
        Button commentOnAnswerButton = new Button("Comment");
        commentOnAnswerButton.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: #6B7280;" +
            "-fx-underline: true;" +
            "-fx-font-size: 12px;"
        );
        commentOnAnswerButton.setCursor(Cursor.HAND);

        VBox commentInputContainer = new VBox(5);
        commentInputContainer.setManaged(false);
        commentOnAnswerButton.setOnAction(e -> toggleCommentInput(primaryStage, user, question, commentInputContainer, answer, null));

        HBox footer = new HBox(10, commentOnAnswerButton);
        footer.setAlignment(Pos.CENTER_LEFT);

        // admin/owner controls
        boolean showEdit = Authorization.canEditAnswer(user, databaseHelper, answer);
        boolean showDelete = Authorization.canDeleteAnswer(user, databaseHelper, answer, adminFlag);

        Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        footer.getChildren().add(spacer);

        if (showEdit) {
            Button editButton = new Button("Edit");
            editButton.setStyle("-fx-font-size: 12px;");
            editButton.setOnAction(e -> new EditAnswerView(databaseHelper, adminFlag).show(primaryStage, user, answer));
            footer.getChildren().add(editButton);
        }
        if (showDelete) {
        	//DELETE BUTTON
            Button deleteButton = new Button("Delete");
            deleteButton.setStyle(
                "-fx-background-color: #FFE5E5;" +
                "-fx-text-fill: #C62828;" +
                "-fx-font-size: 12px;" +
                "-fx-background-radius: 5;"
            );
            deleteButton.setOnAction(e -> handleDeleteAnswerAction(primaryStage, user, question, answer));
            footer.getChildren().add(deleteButton);
        }

        // ACCEPTED ANSWER STATE
        // If this answer is marked as the question’s resolution, highlight and show SOLVED badge.
        if (answer.getResolvesQuestion()) {
            answerBox.setStyle(
                "-fx-background-color: linear-gradient(to right, #E8F5E9, #F1FAF2);" +
                "-fx-border-color: #81C784;" +
                "-fx-border-width: 2;" +
                "-fx-border-radius: 10;" +
                "-fx-background-radius: 10;" +
                "-fx-effect: dropshadow(gaussian, rgba(76,175,80,0.25), 8, 0, 0, 2);"
            );

            Label solvedBadge = new Label("SOLVED");
            solvedBadge.setStyle(
                "-fx-background-color: #4CAF50;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 10px;" +
                "-fx-padding: 3 8 3 8;" +
                "-fx-background-radius: 12;"
            );

            // Overlay badge using StackPane
            StackPane badgeContainer = new StackPane();
            badgeContainer.getChildren().addAll(answerBox, solvedBadge);
            StackPane.setAlignment(solvedBadge, Pos.TOP_RIGHT);
            StackPane.setMargin(solvedBadge, new Insets(4, 6, 0, 0));

            // Allow question creator to unmark accepted answer
            if (user != null && user.getUserName().equals(question.getAuthor().getUserName())) {
                Button unmarkButton = new Button("Unmark as accepted answer");
                unmarkButton.setCursor(Cursor.HAND);
                unmarkButton.setStyle(
                    "-fx-background-color: transparent;" +
                    "-fx-text-fill: #6B7280;" +
                    "-fx-font-size: 12px;" +
                    "-fx-underline: true;"
                );
                unmarkButton.setOnAction(e -> {
                    try {
                        databaseHelper.updateAnswerResolutionStatus(answer.getAnswerId().toString(), false);
                        new Alert(Alert.AlertType.INFORMATION, "Answer unmarked as accepted.").showAndWait();
                        this.show(primaryStage, user, question);
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                        new Alert(Alert.AlertType.ERROR, "Failed to unmark accepted answer.").showAndWait();
                    }
                });
                footer.getChildren().add(unmarkButton);
            }

            answerBox.getChildren().addAll(answerBody, infoLabel, footer, commentInputContainer, commentsContainer, new Separator());
            return badgeContainer;
        }
        // MARK ANSWER AS ACCEPTED
        // If current user is the author, shows button to mark answer as accepted.
        else if (user != null && user.getUserName().equals(question.getAuthor().getUserName())) {
            Button markAsResolutionButton = new Button("Mark as accepted answer");
            markAsResolutionButton.setCursor(Cursor.HAND);
            markAsResolutionButton.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-text-fill: #6B7280;" +
                "-fx-font-size: 12px;" +
                "-fx-underline: true;"
            );
            markAsResolutionButton.setOnAction(e -> {
                try {
                    databaseHelper.updateAnswerResolutionStatus(answer.getAnswerId().toString(), true);
                    new Alert(Alert.AlertType.INFORMATION, "Marked as accepted answer!").showAndWait();
                    this.show(primaryStage, user, question);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    new Alert(Alert.AlertType.ERROR, "Failed to mark answer as accepted.").showAndWait();
                }
            });
            footer.getChildren().add(markAsResolutionButton);
        }

        answerBox.getChildren().addAll(answerBody, infoLabel, footer, commentInputContainer, commentsContainer, new Separator());
        return answerBox;
    }

    private Node createCommentNode(Stage primaryStage, User user, Question question, Comment comment) {
        VBox commentBox = new VBox(5);

        Label commentBody = new Label(comment.getBody());
        commentBody.setWrapText(true);

        String authorName = comment.getAuthor().getName();
        String timeSince = formatTimeSince(comment.getCreationTimestamp());
        Label infoLabel = new Label(authorName + " • " + timeSince);
        infoLabel.setFont(Font.font(11));
        infoLabel.setTextFill(Color.DARKSLATEGRAY);

        Button replyButton = new Button("Reply");
        replyButton.setFont(Font.font(11));

        HBox footer = new HBox(10, replyButton);
        footer.setAlignment(Pos.CENTER_LEFT);

        // owner/admin controls
        boolean showEdit = Authorization.canEditComment(user, databaseHelper, comment);
        boolean showDelete = Authorization.canDeleteComment(user, databaseHelper, comment, adminFlag);

        Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        footer.getChildren().add(spacer);

        if (showEdit) {
            Button editButton = new Button("Edit");
            editButton.setFont(Font.font(11));
            editButton.setOnAction(e -> new EditAnswerView(databaseHelper, adminFlag).show(primaryStage, user, comment));
            footer.getChildren().add(editButton);
        }
        if (showDelete) {
            Button deleteButton = new Button("Delete");
            deleteButton.setFont(Font.font(11));
            deleteButton.setStyle("-fx-background-color: #FFCDD2;");
            deleteButton.setOnAction(e -> handleDeleteCommentAction(primaryStage, user, question, comment));
            footer.getChildren().add(deleteButton);
        }

        VBox repliesContainer = new VBox(10);
        repliesContainer.setPadding(new Insets(5, 0, 0, 20));
        for (Comment reply : comment.getReplies()) {
            repliesContainer.getChildren().add(createCommentNode(primaryStage, user, question, reply));
        }

        VBox replyInputContainer = new VBox(5);
        replyInputContainer.setManaged(false); 
        replyButton.setOnAction(e -> toggleCommentInput(primaryStage, user, question, replyInputContainer, null, comment));

        commentBox.getChildren().addAll(commentBody, infoLabel, footer, replyInputContainer, repliesContainer);
        return commentBox;
    }

    private void handleDeleteQuestionAction(Stage primaryStage, User user, Question question) {
        if (!Authorization.canDeleteQuestion(user, databaseHelper, question, adminFlag)) {
        	new Alert(Alert.AlertType.ERROR, "You do not have permission to delete this question.").showAndWait();
        	return;
        }
    	
    	Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION, "This will also delete all associated answers and comments.", ButtonType.OK, ButtonType.CANCEL);
        confirmation.setTitle("Delete Question");
        confirmation.setHeaderText("Are you sure you want to delete this question?");
        Optional<ButtonType> result = confirmation.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                databaseHelper.deleteQuestion(question.getQuestionId().toString());
                new DiscussionBoardView(databaseHelper, adminFlag).show(primaryStage, user);
            } catch (SQLException ex) {
                ex.printStackTrace();
                new Alert(Alert.AlertType.ERROR, "Failed to delete the question.").showAndWait();
            }
        }
    }

    private void handleDeleteAnswerAction(Stage primaryStage, User user, Question question, Answer answer) {
    	if (!Authorization.canDeleteQuestion(user, databaseHelper, question, adminFlag)) {
        	new Alert(Alert.AlertType.ERROR, "You do not have permission to delete this answer.").showAndWait();
        	return;
        }
    	
    	Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION, "This will permanently remove the answer and all its comments.", ButtonType.OK, ButtonType.CANCEL);
        confirmation.setTitle("Delete Answer");
        confirmation.setHeaderText("Are you sure you want to delete this answer?");
        Optional<ButtonType> result = confirmation.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                databaseHelper.deleteAnswer(answer.getAnswerId().toString());
                this.show(primaryStage, user, question); // Refresh view
            } catch (SQLException ex) {
                ex.printStackTrace();
                new Alert(Alert.AlertType.ERROR, "Failed to delete the answer.").showAndWait();
            }
        }
    }

    private void handleDeleteCommentAction(Stage primaryStage, User user, Question question, Comment comment) {
    	if (!Authorization.canDeleteQuestion(user, databaseHelper, question, adminFlag)) {
        	new Alert(Alert.AlertType.ERROR, "You do not have permission to delete this comment.").showAndWait();
        	return;
        }
    	
    	Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION, "This will also permanently delete all replies to it.", ButtonType.OK, ButtonType.CANCEL);
        confirmation.setTitle("Delete Comment");
        confirmation.setHeaderText("Are you sure you want to delete this comment?");
        Optional<ButtonType> result = confirmation.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                databaseHelper.deleteComment(comment.getCommentId().toString());
                this.show(primaryStage, user, question); // Refresh view
            } catch (SQLException ex) {
                ex.printStackTrace();
                new Alert(Alert.AlertType.ERROR, "Failed to delete comment.").showAndWait();
            }
        }
    }

    private void handleEditCommentAction(Stage primaryStage, User user, Question question, Comment comment) {
        TextInputDialog dialog = new TextInputDialog(comment.getBody());
        dialog.setTitle("Edit Comment");
        dialog.setHeaderText("Update your comment text:");
        dialog.setContentText("Comment:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newText -> {
            if (!newText.trim().isEmpty()) {
                comment.setBody(newText.trim());
                try {
                    databaseHelper.updateComment(comment);
                    this.show(primaryStage, user, question); // Refresh view
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    new Alert(Alert.AlertType.ERROR, "Failed to update comment.").showAndWait();
                }
            }
        });
    }

    private void toggleCommentInput(Stage primaryStage, User user, Question question, VBox inputContainer, Answer parentAnswer, Comment parentComment) {
        if (inputContainer.isManaged()) {
            inputContainer.getChildren().clear();
            inputContainer.setManaged(false);
        } else {
            TextArea commentArea = new TextArea();
            commentArea.setPromptText("Write a reply...");
            commentArea.setPrefHeight(60);
            Button postButton = new Button("Post");

            postButton.setOnAction(event -> {
                String commentText = commentArea.getText().trim();
                if (!commentText.isEmpty()) {
                    Comment newComment;
                    if (parentComment != null) {
                        newComment = new Comment(parentComment, user, commentText);
                    } else {
                        newComment = new Comment(parentAnswer, user, commentText);
                    }
                    try {
                        databaseHelper.addComment(newComment);
                        this.show(primaryStage, user, question); // Refresh view
                    } catch (SQLException e) {
                        e.printStackTrace();
                        new Alert(Alert.AlertType.ERROR, "Could not save your comment.").showAndWait();
                    }
                }
            });

            inputContainer.getChildren().addAll(commentArea, postButton);
            inputContainer.setManaged(true);
        }
    }    
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
        return days + (days == 1 ? " day ago" : " days ago");
    }
}
