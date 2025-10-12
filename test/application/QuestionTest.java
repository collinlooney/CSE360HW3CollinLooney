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

    
	// dummy user
	private User makeUser() {
        return new User("skyler","Skyler","carwash@aol.com","pw", List.of(Role.BASIC_USER));
    }

	// dummy question. Reread before each test.
    @Before
    public void setUp() {
    	author = makeUser();
        q = new Question(author, "Title", "Body", Tags.HOMEWORK, false, false);
    }
    
    // Ensure question view count increments.
    @Test
    public void incrementViewCount_increments() {
        int start = q.getViewCount();
        q.incrementViewCount();
        assertEquals(start + 1, q.getViewCount());
    }
    
    // Check that question fields store correctly.
    @Test
    public void checkQuestionData1() {
        
    	// Check that author is the same instance that was passed in.
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
    
    // This test ensures that question objects remain independent.
    @Test
    public void twoQuestions_areIndependent() {
        User u1 = new User("skyler","Skyler","carwash@aol.com","pw", List.of(Role.BASIC_USER));
        User u2 = new User("walter","Walter","chemteacher@aol.com","pw", List.of(Role.ADMIN));

        Question q1 = new Question(u1, "T1", "B1", Tags.HOMEWORK, false, false);
        Question q2 = new Question(u2, "T2", "B2", Tags.TEAM_PROJECT, true,  true);

        // Different objects & different IDs.
        assertNotSame(q1, q2);
        assertNotEquals(q1.getQuestionId(), q2.getQuestionId());

        // Each kept its own author/fields.
        assertSame(u1, q1.getAuthor());
        assertSame(u2, q2.getAuthor());
        assertEquals("T1", q1.getTitle());
        assertEquals("T2", q2.getTitle());
        assertEquals(Tags.HOMEWORK, q1.getTag());
        assertEquals(Tags.TEAM_PROJECT, q2.getTag());
        assertFalse(q1.isPrivate());
        assertTrue(q2.isPrivate());

        // Increment affects each separately.
        q1.incrementViewCount();
        assertEquals(1, q1.getViewCount());
        assertEquals(0, q2.getViewCount());
        
        q2.incrementViewCount();
        q2.incrementViewCount();
        assertEquals(1, q1.getViewCount());
        assertEquals(2, q2.getViewCount());
    }
    
    // This test is to ensure the timestamp gets updated on edit of any part of a question.
    @Test
    public void setters_updateLastModified() throws Exception {
    	
    	java.time.ZonedDateTime t0 = q.getLastModifiedTimestamp();  
    	
    	// Edit title advances timestamp.
       	q.setTitle("Electrons");
       	java.time.ZonedDateTime t1 = q.getLastModifiedTimestamp();
       	assertFalse("Updating title should advance timestamp", t1.isBefore(t0));   
       	
        // Edit tag advances timestamp.
    	q.setTag(Tags.HOMEWORK);
    	java.time.ZonedDateTime t2 = q.getLastModifiedTimestamp();
       	assertFalse("Updating tag should advance timestamp", t2.isBefore(t1));	   
       	
        // Edit body advances timestamp.
    	q.setBody("How do they work?");
    	java.time.ZonedDateTime t3 = q.getLastModifiedTimestamp();
       	assertFalse("Updating body should advance timestamp", t3.isBefore(t2));	   
    	
    	
    }
    
    // Test helper: returns a minimal valid Answer associated with the
    // current Question under test (q) and a dummy author. Only the body varies.
   
    private Answer makeAnswer(String body) {        
        return new Answer(q, makeUser(), body); 
    }
    
    // Checks that the answer object is correctly added to the list of a specific question.
    @Test
    public void addAnswer_addsToList() {
    	
    	// Create a valid answer.
        Answer a = makeAnswer("Because");   				
        // Check list size.
        int before = q.getPotentialAnswers().size();			      
        q.addAnswer(a);												
        
        // Ensure list size increased by 1.
        assertEquals(before + 1, q.getPotentialAnswers().size());  
        // Answer object exists with this question.
        assertTrue(q.getPotentialAnswers().contains(a));			
    }
    
    // Checks that question is marked resolved with accepted answer and timestamp moves forward.
    @Test
    public void acceptAnswer_setsResolvedAndAccepted() {
        Answer a = makeAnswer("solution");
        q.addAnswer(a);                       						
        ZonedDateTime t0 = q.getLastModifiedTimestamp();

        assertTrue(q.getPotentialAnswers().contains(a));
        q.acceptAnswer(a);

        assertSame(a, q.getAcceptedAnswer());
        assertEquals(QuestionStatus.RESOLVED, q.getStatus());
        assertFalse(q.getLastModifiedTimestamp().isBefore(t0));		
    }
    
    // Ensures that an answer cannot be accepted as resolving a question if it's not in the question's answer list.
    @Test
    public void acceptAnswer_whenNotPresent_throws_andNoSideEffects() {
        Answer outsider = makeAnswer("not in list");
        IllegalArgumentException ex =
            org.junit.Assert.assertThrows(IllegalArgumentException.class,
                () -> q.acceptAnswer(outsider));

        assertNull(q.getAcceptedAnswer());
        assertEquals(QuestionStatus.OPEN, q.getStatus());
    }
    
    // Verifies that the constructor preserves what's passed in when a question object is created from the database.
    @Test
    public void constructor_withExistingData_usesProvidedValues() {				
        UUID id = UUID.randomUUID();
        ZonedDateTime ts = ZonedDateTime.now().minusDays(1);
        User u = makeUser();

        Question restored = new Question(
            id, "T", "B", u, QuestionStatus.CLOSED,
            ts, Tags.HOMEWORK, true, true, 7
        );

        assertEquals(id, restored.getQuestionId());
        assertEquals("T", restored.getTitle());
        assertEquals("B", restored.getBody());
        assertSame(u, restored.getAuthor()); 
        assertEquals(QuestionStatus.CLOSED, restored.getStatus());
        assertEquals(Tags.HOMEWORK, restored.getTag());
        assertTrue(restored.isPrivate());
        assertTrue(restored.isAnonymous());
        assertEquals(7, restored.getViewCount());
        assertEquals(ts, restored.getCreationTimestamp());
        assertEquals(ts, restored.getLastModifiedTimestamp());
      
    }
    
    // The test checks that a user can switch which answer resolves their question.
    @Test
    public void acceptAnswer_canReplaceAcceptedAnswer_andBumpsTime() {
        Answer a1 = makeAnswer("first");
        Answer a2 = makeAnswer("second");
        q.addAnswer(a1);
        q.addAnswer(a2);

        q.acceptAnswer(a1);
        ZonedDateTime t1 = q.getLastModifiedTimestamp();

        q.acceptAnswer(a2); // switch
        assertSame(a2, q.getAcceptedAnswer());
        assertEquals(QuestionStatus.RESOLVED, q.getStatus());
        assertFalse(q.getLastModifiedTimestamp().isBefore(t1));
    }


    
}