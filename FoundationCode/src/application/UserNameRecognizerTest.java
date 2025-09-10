package application;

import static org.junit.Assert.*;
import org.junit.Test;

public class UserNameRecognizerTest {
	
	// Testing length of username (State 1 as final state)
    @Test
    public void testInvalidLength() {
    	// Testing that an empty username is invalid
    	String errMsg = UserNameRecognizer.checkForValidUserName("");
    	assertFalse("Testing empty username...", errMsg.isEmpty());
        // Testing that a username less than 4 characters is invalid
        String errMsg1 = UserNameRecognizer.checkForValidUserName("abc");
        assertFalse("Testing less than 4 characters...", errMsg1.isEmpty());
    	// Testing that a username more than 16 characters is invalid
        String errMsg2 = UserNameRecognizer.checkForValidUserName("aaaabbbbccccddddx");
        assertFalse("Testing more than 16 characters...", errMsg2.isEmpty());
    }

    @Test
    public void testMinMaxLength() {
    	// Testing that a username exactly 4 characters is valid
        String errMsg1 = UserNameRecognizer.checkForValidUserName("aaaa");
        assertTrue("Testing 4 characters...", errMsg1.isEmpty());
    	// Testing that a username exactly 16 characters is valid
        String errMsg2 = UserNameRecognizer.checkForValidUserName("aaaabbbbccccdddd");
        assertTrue("Testing 16 characters...", errMsg2.isEmpty());
    }

    // Testing First Character (state 0 -> state 1)
    @Test
    public void testFirstCharacter() {
    	// Testing that a username starting with a letter is valid
        String errMsg = UserNameRecognizer.checkForValidUserName("a123");
        assertTrue("Testing starts with letter...", errMsg.isEmpty());
    	// Testing that a username starting with number is invalid
        String errMsg1 = UserNameRecognizer.checkForValidUserName("1aaaa");
        assertFalse("Testing starts with number...", errMsg1.isEmpty());
    	// Testing that a username starting with a valid special char is invalid
        String errMsg2 = UserNameRecognizer.checkForValidUserName("_aaaa");
        assertFalse("Testing starts with special...", errMsg2.isEmpty());
    }

    // Testing special character set
    @Test
    public void testSpecialCharacterSet() {
    	// Test that all 3 special characters are valid if between alphanumeric
        String errMsg = UserNameRecognizer.checkForValidUserName("a_a-a.a1_1-1.1");
        assertTrue("Testing 'a_a-a.a1_1-1.1'", errMsg.isEmpty());
        // Testing that username with invalid character is invalid
        String errMsg2 = UserNameRecognizer.checkForValidUserName("user@name");
        assertFalse("Testing contains '@'", errMsg2.isEmpty());
    }

    // Testing special character placement rules (State 1 -> State 2 -> State 1)
    @Test
    public void testDoubleSpecialCharsDuplicateInvalid() {
    	// Testing that 2 of the same special characters in a row error
        String errMsg1 = UserNameRecognizer.checkForValidUserName("user..name");
        assertFalse("Test double period should error...", errMsg1.isEmpty());
        String errMsg2 = UserNameRecognizer.checkForValidUserName("user__name");
        assertFalse("Test Double underscore should error...", errMsg2.isEmpty());
        String errMsg3 = UserNameRecognizer.checkForValidUserName("user--name");
        assertFalse("Test double minus should error...", errMsg3.isEmpty());
    }
    
    @Test
    public void testDoubleSpecialCharsDifferentInvalid() {
    	// Testing that 2 different special characters in a row error
        String errMsg1 = UserNameRecognizer.checkForValidUserName("user._name");
        assertFalse("Test '._' should error...", errMsg1.isEmpty());
        String errMsg2 = UserNameRecognizer.checkForValidUserName("user.-name");
        assertFalse("Test '.-' should error...", errMsg2.isEmpty());
        
        String errMsg3 = UserNameRecognizer.checkForValidUserName("user_.name");
        assertFalse("Test '_.' should error...", errMsg3.isEmpty());
        String errMsg4 = UserNameRecognizer.checkForValidUserName("user_-name");
        assertFalse("Test '_-' should error...", errMsg4.isEmpty());
        
        String errMsg5 = UserNameRecognizer.checkForValidUserName("user-.name");
        assertFalse("Test '-.' should error...", errMsg5.isEmpty());
        String errMsg6 = UserNameRecognizer.checkForValidUserName("user-_name");
        assertFalse("Test '-_' should error...", errMsg6.isEmpty());
    }

}
