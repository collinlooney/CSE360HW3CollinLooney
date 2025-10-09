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
    private final boolean adminFlag; 
    private TextArea bodyTextArea;
    private Label feedbackLabel;

    public EditAnswerView(DatabaseHelper databaseHelper, boolean adminFlag) {
        this.databaseHelper = databaseHelper;
        this.adminFlag = adminFlag; 
    }
    
   public void show(Stage primaryStage, User user, Answer answerToEdit) {
	   showEditor(primaryStage, user, "Edit Your Answer", answerToEdit.getBody(), newBody -> 
	   {answerToEdit.setBody(newBody);
	   		try {
	   			databaseHelper.updateAnswer(answerToEdit);
	   		} catch (SQLException ex) {
	   			throw new RuntimeException(ex);
	   		}
	   },
		
	   answerToEdit.getParentQuestion());
   }
   
   public void show(Stage primaryStage, User user, Comment commentToEdit) {
	   showEditor(primaryStage, user, "Edit Your Comment", commentToEdit.getBody(), newBody -> 
	   {commentToEdit.setBody(newBody);
	   		try {
	   			databaseHelper.updateComment(commentToEdit);
	   		} catch (SQLException ex) {
	   			throw new RuntimeException(ex);
	   		}
	   },
		
	   findParentQuestion(commentToEdit));
   }

   private void showEditor(Stage primaryStage, User user, String title, String initialText, BodySaver saver, Question parentQuestion) {
	   VBox formVBox = new VBox(15);
       formVBox.setPadding(new Insets(20));
       formVBox.setStyle("-fx-background-color: #F4F4F4;");

       Label mainTitleLabel = new Label(title);
       mainTitleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));

       bodyTextArea = new TextArea(initialText);
       bodyTextArea.setWrapText(true);
       bodyTextArea.setPrefHeight(200);

       Button saveButton = new Button("Save Changes");
       saveButton.setMaxWidth(Double.MAX_VALUE);
       saveButton.setPrefHeight(40);
       saveButton.setOnAction(event -> {
    	   String newBody = bodyTextArea.getText().trim();
    	   if (newBody.isEmpty()) {
    		   showError("The text body cannot be empty.");
    		   return;
    	   }
    	   try {
    		   saver.save(newBody);
    		   new QuestionDetailView(databaseHelper, adminFlag).show(primaryStage, user, parentQuestion);
    	   } catch (Exception ex) {
    		   ex.printStackTrace();
    		   showError("Database Error: Could not save your changes.");
    	   }
       });
       
       feedbackLabel = new Label();
       feedbackLabel.setWrapText(true);
       
       formVBox.getChildren().addAll(mainTitleLabel, bodyTextArea, saveButton, feedbackLabel);

       Button backButton = new Button("← Back to Question");
       backButton.setOnAction(e -> new QuestionDetailView(databaseHelper, adminFlag).show(primaryStage, user,parentQuestion));

       VBox container = new VBox(10, backButton, formVBox);
       container.setPadding(new Insets(10));
       VBox.setVgrow(formVBox, Priority.ALWAYS);

       Scene scene = new Scene(container, 800, 600);
       primaryStage.setScene(scene);
       primaryStage.show();
   }
   
   private Question findParentQuestion(Comment comment) {
	   Comment curr = comment; 
	   while (curr.getParentComment() != null) {
		   curr = curr.getParentComment();
	   }
	   return curr.getParentAnswer().getParentQuestion();
   }
   
   
   private interface BodySaver {
	   void save(String newBody) throws Exception;
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
            new QuestionDetailView(databaseHelper, adminFlag).show(primaryStage, user, answerToEdit.getParentQuestion());
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