package application;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a single comment. A comment can be a reply to an Answer
 * or a nested reply to another Comment, allowing for threaded discussions.
 */
public class Comment {

    // --- Fields ---

    private final UUID commentId;
    private final Answer parentAnswer;
    private final User author;
    private final ZonedDateTime creationTimestamp;
    private final List<Comment> replies;

    private Comment parentComment; // Can be null 
    private String body;

    // --- Constructors ---

    /**
     * Constructs a new comment on an answer.
     * Automatically generates a new UUID and creation timestamp.
     *
     * @param parentAnswer The Answer this comment belongs to.
     * @param author The User who wrote the comment.
     * @param body The main content of the comment.
     */
    public Comment(Answer parentAnswer, User author, String body) {
        this(parentAnswer, null, author, body);
    }

    /**
     * Constructs a new reply to another comment. (for threaded discussion)
     * Automatically generates a new UUID and creation timestamp.
     *
     * @param parentComment The parent Comment this reply belongs to.
     * @param author The User who wrote the reply.
     * @param body The main content of the reply.
     */
    public Comment(Comment parentComment, User author, String body) {
        this(parentComment.getParentAnswer(), parentComment, author, body);
    }

    /**
     * Constructs a Comment from existing data. Used when re-creating an object from the database.
     *
     * @param id The existing UUID of the comment.
     * @param parentAnswer The parent Answer object.
     * @param parentComment The parent Comment object (can be null).
     * @param author The author of the comment.
     * @param body The content of the comment.
     * @param creationTimestamp The original creation timestamp.
     */
    public Comment(UUID id, Answer parentAnswer, Comment parentComment, User author, String body, ZonedDateTime creationTimestamp) {
        this.commentId = id;
        this.parentAnswer = parentAnswer;
        this.parentComment = parentComment;
        this.author = author;
        this.body = body;
        this.creationTimestamp = creationTimestamp;
        this.replies = new ArrayList<>(); // Ensure replies list is always initialized.
    }

    /**
     * Private constructor to handle all new comment creation logic.
     */
    private Comment(Answer parentAnswer, Comment parentComment, User author, String body) {
        this.commentId = UUID.randomUUID();
        this.parentAnswer = parentAnswer;
        this.parentComment = parentComment;
        this.author = author;
        this.body = body;
        this.creationTimestamp = ZonedDateTime.now();
        this.replies = new ArrayList<>();
    }

    // --- Business Methods ---

    /**
     * Adds a reply to this comment's list of replies.
     * @param reply The Comment object to add as a reply.
     */
    public void addReply(Comment reply) {
        this.replies.add(reply);
    }

    // --- Getters and Setters ---

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