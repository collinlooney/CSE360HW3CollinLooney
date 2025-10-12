package application;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.time.ZonedDateTime;
import java.util.UUID;

public class QuestionTest {
	private User author;
	private Question q;

    private User makeUser() {
        return new User("skyler","Skyler","carwashlady@aol.com","junior", List.of(Role.BASIC_USER));
    }


    @Before
    public void setUp() {
    	author = makeUser();
        q = new Question(author, "Title", "Body", Tags.HOMEWORK, false, false);
    }

    @Test
    public void incrementViewCount_increments() {
        int start = q.getViewCount();
        q.incrementViewCount();
        assertEquals(start + 1, q.getViewCount());
    }
    
    @Test
    public void checkQuestionData1() {
        // author is the same instance that was passed in
        assertSame(author, q.getAuthor());

        // basic wiring
        assertEquals("Title", q.getTitle());
        assertEquals("Body", q.getBody());
        assertEquals(Tags.HOMEWORK, q.getTag());
        assertFalse(q.isPrivate());
        assertFalse(q.isAnonymous());

        // defaults
        assertEquals(QuestionStatus.OPEN, q.getStatus());
        assertEquals(0, q.getViewCount());
        assertNotNull(q.getQuestionId());
        assertEquals(q.getCreationTimestamp(), q.getLastModifiedTimestamp());
        assertTrue(q.getPotentialAnswers().isEmpty());

        assertTrue(q.getAuthor().getRoles().contains(Role.BASIC_USER));
    }
    
    @Test
    public void twoQuestions_areIndependent() {
        User u1 = new User("skyler","Skyler","s@aol.com","pw", List.of(Role.BASIC_USER));
        User u2 = new User("walter","Walter","w@aol.com","pw", List.of(Role.ADMIN));

        Question q1 = new Question(u1, "T1", "B1", Tags.HOMEWORK, false, false);
        Question q2 = new Question(u2, "T2", "B2", Tags.TEAM_PROJECT, true,  true);

        // Different objects & different IDs
        assertNotSame(q1, q2);
        assertNotEquals(q1.getQuestionId(), q2.getQuestionId());

        // Each kept its own author/fields
        assertSame(u1, q1.getAuthor());
        assertSame(u2, q2.getAuthor());
        assertEquals("T1", q1.getTitle());
        assertEquals("T2", q2.getTitle());
        assertEquals(Tags.HOMEWORK, q1.getTag());
        assertEquals(Tags.TEAM_PROJECT, q2.getTag());
        assertFalse(q1.isPrivate());
        assertTrue(q2.isPrivate());

        // Increment affects each separately
        q1.incrementViewCount();
        assertEquals(1, q1.getViewCount());
        assertEquals(0, q2.getViewCount());
        
        q2.incrementViewCount();
        q2.incrementViewCount();
        assertEquals(1, q1.getViewCount());
        assertEquals(2, q2.getViewCount());
    }
    @Test
    public void setters_updateLastModified() throws Exception {
    	
    	java.time.ZonedDateTime t0 = q.getLastModifiedTimestamp();
    	
       	q.setTitle("Electrons");
       	java.time.ZonedDateTime t1 = q.getLastModifiedTimestamp();
       	assertFalse("Title should advance timestamp", t1.isBefore(t0));
       	
    	q.setTag(Tags.HOMEWORK);
    	java.time.ZonedDateTime t2 = q.getLastModifiedTimestamp();
       	assertFalse("Tag should advance timestamp", t2.isBefore(t1));
       	
    	q.setBody("How do they work?");
    	java.time.ZonedDateTime t3 = q.getLastModifiedTimestamp();
       	assertFalse("Tag should advance timestamp", t2.isBefore(t1));
    	
    	
    }
    @Test
    public void addAnswer_addsToList() {
    	
    }
}