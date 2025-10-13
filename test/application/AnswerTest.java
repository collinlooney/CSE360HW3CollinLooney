package application;

import static org.junit.Assert.*;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

public class AnswerTest {
	
	private User author;
	private Question q;
	
	// dummy user
	private User makeUser() {
		return new User("hanzee", "Hanzee", "hanzee@gmail.com", "pw", List.of(Role.BASIC_USER));	
	}
	
	// dummy question
	private Question makeQuestion() {
		return new Question(makeUser(), "Title", "Body", Tags.HOMEWORK, false, false);
	}
	
	// dummy answer
	private Answer makeAnswer(String body) {
		return new Answer(q, author, body);
	}
	
	@Before
	
	// runs before each test
	public void setUp() {
		author = makeUser();
		q = makeQuestion();
	}
	
	// ----tests-----
	
	
	// Ensure answer fields store correctly.
	@Test
	public void constructor_wiresFields_andDefaults() {
		Answer a = makeAnswer("Because...");

		// basic wiring
		assertSame(q, a.getParentQuestion());
		assertSame(author, a.getAuthor());
		assertEquals("Because...", a.getBody());
		
		// defaults
		assertNotNull(a.getAnswerId());
		assertNotNull(a.getCreationTimestamp());
		assertFalse(a.getResolvesQuestion());
		assertTrue(a.getComments().isEmpty());
			
	}
	
	// Verifies that the constructor preserves what's passed in when an answer object is created from the database.
	@Test
	public void constructor_fromDb_preservesProvidedValues() {
		UUID id = UUID.randomUUID();
        ZonedDateTime ts = ZonedDateTime.now().minusDays(2);
        Answer a = new Answer(id, q, author, "db body", ts, true);

        assertEquals(id, a.getAnswerId());
        assertSame(q, a.getParentQuestion());
        assertSame(author, a.getAuthor());
        assertEquals("db body", a.getBody());
        assertEquals(ts, a.getCreationTimestamp());
        assertTrue(a.getResolvesQuestion());
        assertTrue(a.getComments().isEmpty());
	}
	
	// Ensures updating answer doesn't modify anything else including timestamp.
	@Test
    public void setBody_updatesText_only() {
        Answer a = makeAnswer("first");
        ZonedDateTime t0 = a.getCreationTimestamp();
        boolean resolves0 = a.getResolvesQuestion();

        a.setBody("second");

        assertEquals("second", a.getBody());
        // class has no lastModified; creation should remain unchanged
        assertEquals(t0, a.getCreationTimestamp());
        assertEquals(resolves0, a.getResolvesQuestion());
    }
	
	// Ensures that answer objects remain independent.
	@Test
	public void answers_areIndependent() {
	    Answer a1 = makeAnswer("x");
	    Answer a2 = makeAnswer("y");

	    assertNotSame(a1, a2);
	    assertNotEquals(a1.getAnswerId(), a2.getAnswerId());
	    assertEquals("x", a1.getBody());
	    assertEquals("y", a2.getBody());
	}
	
	// Verifies adding comment to a list.
	@Test
	public void addComment_addsToList() {
	    Answer a = makeAnswer("body");
	    int before = a.getComments().size();

	    Comment c = new Comment(a, author, "nice answer");  // (Answer, User, String)
	    a.addComment(c);

	    assertEquals(before + 1, a.getComments().size());
	    assertTrue(a.getComments().contains(c));
	}
	
	// Comment constructor works correctly.
	@Test
	public void comment_onAnswer_wiresFields_defaults() {
	    Answer a = makeAnswer("A");
	    Comment c = new Comment(a, author, "c1");

	    assertSame(a, c.getParentAnswer());
	    assertNull(c.getParentComment());
	    assertEquals("c1", c.getBody());
	    assertNotNull(c.getCommentId());
	    assertNotNull(c.getCreationTimestamp());
	    assertTrue(c.getReplies().isEmpty());
	}
	
	// Adding comments has no affect on resolvesQuestion.
	@Test
	public void addComment_hasNoSideEffectsOnResolves() {
	    Answer a = makeAnswer("body");
	    boolean before = a.getResolvesQuestion();

	    a.addComment(new Comment(a, author, "nice"));

	    assertEquals(before, a.getResolvesQuestion());
	}
    
}




