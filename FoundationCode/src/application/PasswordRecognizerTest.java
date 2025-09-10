package application;

import static org.junit.Assert.*;

import org.junit.Test;

public class PasswordRecognizerTest {
	
    // Testing password length (State 0 -> State 1 semantic rule [6])
    @Test
    public void testPasswordLength() {
    	// Testing that an empty password is invalid
        String errMsg = PasswordRecognizer.evaluatePassword("");
        assertFalse("Testing empty password...", errMsg.isEmpty());
    	// Testing that a password less than 8 characters is invalid
        String errMsg1 = PasswordRecognizer.evaluatePassword("aaaa!A7");
        assertFalse("Testing 7 character password...", errMsg1.isEmpty());
    	// Testing that a password 8 characters is valid
        String errMsg2 = PasswordRecognizer.evaluatePassword("aaaa!AA8");
        assertTrue("Testing 8 character password...", errMsg2.isEmpty());
    }

    // Testing allowed character set (State 0 -> State 0) and (State 0 -> State 1)
    @Test
    public void testCharacterSet() {
    	// Testing all allowed character types
    	String errMsg = PasswordRecognizer.evaluatePassword("aA1~`!@#$%^&*()_-+{}[]|:,.?/");
    	assertTrue("Testing all allowed characters...", errMsg.isEmpty());
    	// Test invalid character "'"
    	String errMsg1 = PasswordRecognizer.evaluatePassword("aaaaAAAA1!'");
    	assertFalse("Testing invalid character...", errMsg1.isEmpty());
    }
    
    // Testing required characters
    @Test
    public void testRequiredCharacters() {
    	// Test missing upper case
    	String errMsg1 = PasswordRecognizer.evaluatePassword("aa!456789");
    	assertFalse("Testing missing upper case...", errMsg1.isEmpty());
    	// Test missing lower case
    	String errMsg2 = PasswordRecognizer.evaluatePassword("AA!456789");
    	assertFalse("Testing missing lower case...", errMsg2.isEmpty());
    	// Test missing number
    	String errMsg3 = PasswordRecognizer.evaluatePassword("aA!aaaaaaaaa");
    	assertFalse("Testing missing number...", errMsg3.isEmpty());
    	// Test missing special character
    	String errMsg4 = PasswordRecognizer.evaluatePassword("aAX456789");
    	assertFalse("Testing missing special character...", errMsg4.isEmpty());
    }

}
