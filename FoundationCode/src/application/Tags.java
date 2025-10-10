package application;
/**
 * Represents the category of a Question.
 */
public enum Tags {
    GENERAL("General"),
    HOMEWORK("Homework"),
    TEAM_PROJECT("Team Project"),
    QUIZZES("Quizzes"),
    EXAMS("Exams"),
    TEAM_FORMATION("Team Formation");

    private final String label;
    Tags(String label) { this.label = label; }

    public String label() { return label; }   // if you want explicit access

    @Override public String toString() { return label; }
}
