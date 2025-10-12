package application;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

//  Represents a single question asked by a user in the application.

public class Question {
    private final UUID questionId;
    private final User author;
    private final ZonedDateTime creationTimestamp;
    
    private String title;
    private String body;
    private QuestionStatus status;
    private ZonedDateTime lastModifiedTimestamp;
    private Tags tags;
    private Answer acceptedAnswer;     
    
    private List<Answer> potentialAnswers;
    
    private boolean isPrivate;
    private boolean isAnonymous;
    private int viewCount;

    //Constructs a new Question with all options. Used when a user creates a new question.
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
    

    
    // Constructs a Question object from existing data. Used when re-creating an object from the database.
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


    public void acceptAnswer(Answer answer) {
        if (potentialAnswers.contains(answer)) {
            this.acceptedAnswer = answer;
            this.status = QuestionStatus.RESOLVED;
            this.lastModifiedTimestamp = ZonedDateTime.now();
        } else {
            throw new IllegalArgumentException("Answer does not belong to this question.");
        }
    }

    public void addAnswer(Answer answer) {
        this.potentialAnswers.add(answer);
    }
    

    public void incrementViewCount() {
        this.viewCount++;
    }
    

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
    // Checks whether this question has an accepted (resolved) answer.
    public boolean isResolved() {
        if (potentialAnswers == null) return false;
        for (Answer a : potentialAnswers) {
            if (a.getResolvesQuestion()) {
                return true;
            }
        }
        return false;
    }


}
