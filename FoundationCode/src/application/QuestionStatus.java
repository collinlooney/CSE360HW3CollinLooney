package application;



 // Represents the status of a Question.

public enum QuestionStatus {
    OPEN,       // The question is active and awaiting answers.
    RESOLVED,   // The question has been answered to the author's satisfaction.
    CLOSED      // The question is closed.
}