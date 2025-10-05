package application;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


 // Represents a single comment. A comment can be a reply to an Answer
 // or a nested reply to another Comment, allowing for threaded discussions.

public class Comment {

    private final UUID commentId;
    private final Answer parentAnswer;
    private final User author;
    private final ZonedDateTime creationTimestamp;
    private final List<Comment> replies;

    private Comment parentComment;
    private String body;

    
     //  constructs a new comment on an answer.
    public Comment(Answer parentAnswer, User author, String body) {
        this(parentAnswer, null, author, body);
    }

    // constructs a new reply to another comment. (for threaded discussion)
    public Comment(Comment parentComment, User author, String body) {
        this(parentComment.getParentAnswer(), parentComment, author, body);
    }


     // constructs a Comment from existing data. Used when recreating an object from the database.

    public Comment(UUID id, Answer parentAnswer, Comment parentComment, User author, String body, ZonedDateTime creationTimestamp) {
        this.commentId = id;
        this.parentAnswer = parentAnswer;
        this.parentComment = parentComment;
        this.author = author;
        this.body = body;
        this.creationTimestamp = creationTimestamp;
        this.replies = new ArrayList<>(); // ensure replies list is always initialized.
    }

    // private constructor to handle all new comment creation logic.
 
    private Comment(Answer parentAnswer, Comment parentComment, User author, String body) {
        this.commentId = UUID.randomUUID();
        this.parentAnswer = parentAnswer;
        this.parentComment = parentComment;
        this.author = author;
        this.body = body;
        this.creationTimestamp = ZonedDateTime.now();
        this.replies = new ArrayList<>();
    }


    public void addReply(Comment reply) {
        this.replies.add(reply);
    }

    public UUID getCommentId() {
        return commentId;
    }

    public Answer getParentAnswer() {
        return parentAnswer;
    }
    
    public Comment getParentComment() {
        return parentComment;
    }

    public void setParentComment(Comment parent) {
        this.parentComment = parent;
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

    public ZonedDateTime getCreationTimestamp() {
        return creationTimestamp;
    }

    public List<Comment> getReplies() {
        return replies;
    }
}