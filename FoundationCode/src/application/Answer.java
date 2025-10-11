package application;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


 // Represents a single answer provided by a user to a specific question.

public class Answer {



    private final UUID answerId;
    private final Question parentQuestion;
    private final User author;
    private String body;
    private final ZonedDateTime creationTimestamp;
    private boolean resolvesQuestion;

    private final List<Comment> comments;


     // Constructs a new Answer object. Used when a user creates a new answer.

    public Answer(Question parentQuestion, User author, String body) {
        this.answerId = UUID.randomUUID();
        this.parentQuestion = parentQuestion;
        this.author = author;
        this.body = body;
        this.creationTimestamp = ZonedDateTime.now();
        this.resolvesQuestion = false;
        this.comments = new ArrayList<>();
    }
    
   
     // Constructs an Answer object from existing data. Used when recreating from database

    public Answer(UUID id, Question parentQuestion, User author, String body, ZonedDateTime creationTimestamp, boolean resolvesQuestion) {
        this.answerId = id;
        this.parentQuestion = parentQuestion;
        this.author = author;
        this.body = body;
        this.creationTimestamp = creationTimestamp;
        this.resolvesQuestion = resolvesQuestion;
        this.comments = new ArrayList<>();
    }

  

    public UUID getAnswerId() {
        return answerId;
    }

    public Question getParentQuestion() {
        return parentQuestion;
    }

    public User getAuthor() {
        return author;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
    
    public void addComment(Comment comment) {
        this.comments.add(comment);
    }
    
    public List<Comment> getComments() {
        return comments;
    }

    public ZonedDateTime getCreationTimestamp() {
        return creationTimestamp;
    }

    public boolean getResolvesQuestion() {
        return this.resolvesQuestion;
    }

}
