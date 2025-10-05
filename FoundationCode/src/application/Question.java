package application;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a single question asked by a user in the application.
 * This class holds all data related to a question, including its content,
 * author, status, and associated answers.
 */
public class Question {

    // --- Fields ---

    private final UUID questionId;
    private final User author;
    private final ZonedDateTime creationTimestamp;
    
    private String title;
    private String body;
    private QuestionStatus status;
    private ZonedDateTime lastModifiedTimestamp;
    private Tags tags;
    private Answer acceptedAnswer;      // Can be null
    
    private List<Answer> potentialAnswers;
    
    private boolean isPrivate;
    private boolean isAnonymous;
    private int viewCount;

    // --- Constructors ---

    /**
     * Constructs a new Question with all options. Used when a user creates a new question.
     * Automatically generates a new UUID and timestamps.
     *
     * @param author The user asking the question.
     * @param title The title of the question.
     * @param body The detailed content of the question.
     * @param tag The single relevant tag for the question.
     * @param isPrivate True if the question should be visible only to specific roles.
     * @param isAnonymous True if the author's name should be hidden.
     */
    public Question(User author, String title, String body, Tags tag, boolean isPrivate, boolean isAnonymous) {
        this.questionId = UUID.randomUUID();
        this.author = author;
        this.title = title;
        this.body = body;
        this.tags = tag;
        this.isPrivate = isPrivate;
        this.isAnonymous = isAnonymous;
        
        this.status = QuestionStatus.OPEN;
        this.viewCount = 0;
        this.creationTimestamp = ZonedDateTime.now();
        this.lastModifiedTimestamp = this.creationTimestamp;
        
        this.potentialAnswers = new ArrayList<>();
    }
    

    
    /**
     * Constructs a Question object from existing data. Used when re-creating an
     * object from the database.
     */
    public Question(UUID id, String title, String body, User author, QuestionStatus status,
                    ZonedDateTime creationTimestamp, Tags tag, boolean isPrivate,
                    boolean isAnonymous, int viewCount) {
        this.questionId = id;
        this.title = title;
        this.body = body;
        this.author = author;
        this.status = status;
        this.creationTimestamp = creationTimestamp;
        this.tags = tag;
        this.isPrivate = isPrivate;
        this.isAnonymous = isAnonymous;
        this.viewCount = viewCount;
        
        this.lastModifiedTimestamp = creationTimestamp; 
        
        this.potentialAnswers = new ArrayList<>();
    }

    // --- Business Methods ---

    /**
     * Accepts an answer, marking it as the solution and resolving the question.
     * @param answer The answer that resolves the question.
     * @throws IllegalArgumentException if the answer does not belong to this question.
     */
    public void acceptAnswer(Answer answer) {
        if (potentialAnswers.contains(answer)) {
            this.acceptedAnswer = answer;
            this.status = QuestionStatus.RESOLVED;
            this.lastModifiedTimestamp = ZonedDateTime.now();
        } else {
            throw new IllegalArgumentException("Answer does not belong to this question.");
        }
    }

    /**
     * Adds an answer to this question's list of potential answers.
     * @param answer The Answer object to add.
     */
    public void addAnswer(Answer answer) {
        this.potentialAnswers.add(answer);
    }
    
    
    /**
     * Increments the view count for this question by one.
     */
    public void incrementViewCount() {
        this.viewCount++;
    }
    
    // --- Getters and Setters ---

    public UUID getQuestionId() {
        return questionId;
    }

    public User getAuthor() {
        return author;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        this.lastModifiedTimestamp = ZonedDateTime.now();
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
        this.lastModifiedTimestamp = ZonedDateTime.now();
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public boolean isAnonymous() {
        return isAnonymous;
    }

    public QuestionStatus getStatus() {
        return status;
    }

    public ZonedDateTime getCreationTimestamp() {
        return creationTimestamp;
    }

    public ZonedDateTime getLastModifiedTimestamp() {
        return lastModifiedTimestamp;
    }

    public Tags getTag() {
        return tags;
    }
    
    public void setTag(Tags tag) {
        this.tags = tag;
        this.lastModifiedTimestamp = ZonedDateTime.now();
    }

    public int getViewCount() {
        return viewCount;
    }
    
    public List<Answer> getPotentialAnswers() {
        return potentialAnswers;
    }
    
    public void setPotentialAnswers(List<Answer> answers) {
        this.potentialAnswers = answers;
    }

    public Answer getAcceptedAnswer() {
        return acceptedAnswer;
    }

}