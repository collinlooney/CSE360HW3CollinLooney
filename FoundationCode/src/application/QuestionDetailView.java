package application;

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

// Builds and displays the UI for a single question, including answers and comments.
 
public class QuestionDetailView {

    private final DatabaseHelper databaseHelper;

    
    public QuestionDetailView(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
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
        backButton.setOnAction(e -> new DiscussionBoardView(databaseHelper).show(primaryStage, user));

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

        boolean isOwner = user.getUserName().equals(question.getAuthor().getUserName());
        if (isOwner) {
            Button editButton = new Button("Edit");
            
            
            // to be uncommented when Update fucntionality is added
            // editButton.setOnAction(e -> new EditQuestionView(databaseHelper).show(primaryStage, user, question));

            Button deleteButton = new Button("Delete");
            deleteButton.setStyle("-fx-background-color: #FFCDD2;");
            deleteButton.setOnAction(e -> handleDeleteQuestionAction(primaryStage, user, question));

            HBox ownerControls = new HBox(10, editButton, deleteButton);
            ownerControls.setAlignment(Pos.CENTER_RIGHT);
            titleBar.getChildren().add(ownerControls);
        }

        // Metadata UI
        Label authorLabel = new Label(question.isAnonymous() ? "Anonymous" : question.getAuthor().getName());
        authorLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        Label timeSinceLabel = new Label(formatTimeSince(question.getCreationTimestamp()));
        Label tagLabel = new Label("in " + question.getTag().name());
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

        Label bodyLabel = new Label(question.getBody());
        bodyLabel.setWrapText(true);
        bodyLabel.setPadding(new Insets(10, 0, 10, 0));

        VBox answersSection = createAnswersSection(primaryStage, user, question);

        postBox.getChildren().addAll(titleBar, metadataPane, new Separator(), bodyLabel, new Separator(), answersSection);
        return postBox;
    }

    private VBox createAnswersSection(Stage primaryStage, User user, Question question) {
        VBox sectionVBox = new VBox(15);
        List<Answer> answers = question.getPotentialAnswers();

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
        VBox answerBox = new VBox(8);

        Label answerBody = new Label(answer.getBody());
        answerBody.setWrapText(true);

        String authorName = answer.getAuthor().getName();
        String timeSince = formatTimeSince(answer.getCreationTimestamp());
        Label infoLabel = new Label("answered by " + authorName + " • " + timeSince);
        infoLabel.setTextFill(Color.GRAY);

        VBox commentsContainer = new VBox(10);
        commentsContainer.setPadding(new Insets(5, 0, 0, 20));
        for (Comment comment : answer.getComments()) {
            if (comment.getParentComment() == null) {
                commentsContainer.getChildren().add(createCommentNode(primaryStage, user, question, comment));
            }
        }

        Button commentOnAnswerButton = new Button("Comment");
        VBox commentInputContainer = new VBox(5);
        commentInputContainer.setManaged(false); 
        commentOnAnswerButton.setOnAction(e -> toggleCommentInput(primaryStage, user, question, commentInputContainer, answer, null));

        HBox footer = new HBox(10, commentOnAnswerButton);
        footer.setAlignment(Pos.CENTER_LEFT);

        boolean isOwner = user.getUserName().equals(answer.getAuthor().getUserName());
        if (isOwner) {
            Pane spacer = new Pane();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Button editButton = new Button("Edit");
            // Uncomment after Update answer
            // editButton.setOnAction(e -> new EditAnswerView(databaseHelper).show(primaryStage, user, answer));

            Button deleteButton = new Button("Delete");
            deleteButton.setStyle("-fx-background-color: #FFCDD2;");
            deleteButton.setOnAction(e -> handleDeleteAnswerAction(primaryStage, user, question, answer));
            
            footer.getChildren().addAll(spacer, editButton, deleteButton);
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
        replyButton.setFont(Font.font(10));

        HBox footer = new HBox(10, replyButton);
        footer.setAlignment(Pos.CENTER_LEFT);

        boolean isOwner = user.getUserName().equals(comment.getAuthor().getUserName());
        if (isOwner) {
            Pane spacer = new Pane();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Button editButton = new Button("Edit");
            editButton.setFont(Font.font(10));
            editButton.setOnAction(e -> handleEditCommentAction(primaryStage, user, question, comment));

            Button deleteButton = new Button("Delete");
            deleteButton.setFont(Font.font(10));
            deleteButton.setStyle("-fx-text-fill: #D32F2F;");
            deleteButton.setOnAction(e -> handleDeleteCommentAction(primaryStage, user, question, comment));

            footer.getChildren().addAll(spacer, editButton, deleteButton);
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
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION, "This will also delete all associated answers and comments.", ButtonType.OK, ButtonType.CANCEL);
        confirmation.setTitle("Delete Question");
        confirmation.setHeaderText("Are you sure you want to delete this question?");
        Optional<ButtonType> result = confirmation.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                databaseHelper.deleteQuestion(question.getQuestionId().toString());
                new DiscussionBoardView(databaseHelper).show(primaryStage, user);
            } catch (SQLException ex) {
                ex.printStackTrace();
                new Alert(Alert.AlertType.ERROR, "Failed to delete the question.").showAndWait();
            }
        }
    }

    private void handleDeleteAnswerAction(Stage primaryStage, User user, Question question, Answer answer) {
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