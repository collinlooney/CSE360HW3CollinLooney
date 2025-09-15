package application;

import static org.junit.Assert.*;

import org.junit.Test;

public class NameValidatorTest {
    // Testing name length constraints
    @Test
    public void testNameLength() {
        // Testing that an empty name is invalid
        String errMsg = NameValidator.validateName("");
        assertFalse("Testing empty name...", errMsg.isEmpty());

        // Testing that a name less than 2 characters is invalid
        String errMsg1 = NameValidator.validateName("A");
        assertFalse("Testing 1 character name...", errMsg1.isEmpty());

        // Testing that a name of 2 characters is valid
        String errMsg2 = NameValidator.validateName("Ed");
        assertTrue("Testing 2 character name...", errMsg2.isEmpty());
        
        // Testing that a name of 100 characters is valid
        String maxLengthName = new String(new char[100]).replace('\0', 'a');
        String errMsg3 = NameValidator.validateName(maxLengthName);
        assertTrue("Testing 100 character name...", errMsg3.isEmpty());

        // Testing that a name over 100 characters is invalid
        String tooLongName = new String(new char[101]).replace('\0', 'b');
        String errMsg4 = NameValidator.validateName(tooLongName);
        assertFalse("Testing 101 character name...", errMsg4.isEmpty());
    }

    // Testing allowed and disallowed characters
    @Test
    public void testCharacterSet() {
        // Testing a name with a combination of allowed characters
        String errMsg = NameValidator.validateName("Jean-Luc O'Malley");
        assertTrue("Testing allowed characters (letters, space, hyphen, apostrophe)...", errMsg.isEmpty());

        // Test an invalid character (number)
        String errMsg2 = NameValidator.validateName("James Bond 007");
        assertFalse("Testing invalid character (number)...", errMsg2.isEmpty());

        // Test an invalid character (symbol)
        String errMsg3 = NameValidator.validateName("Mr$Money");
        assertFalse("Testing invalid character (symbol)...", errMsg3.isEmpty());
    }
    
    // Testing structural and formatting rules based on the FSM
    @Test
    public void testFormattingRules() {
        // Test name starting with a hyphen
        String errMsg1 = NameValidator.validateName("-InvalidStart");
        assertFalse("Testing name starting with a hyphen...", errMsg1.isEmpty());
        
        // Test name ending with a space (should be trimmed and become valid)
        String errMsg2 = NameValidator.validateName("ValidEnd ");
        assertTrue("Testing name ending with a space...", errMsg2.isEmpty());
        
        // Test name with consecutive separators (hyphens)
        String errMsg3 = NameValidator.validateName("Bad--Format");
        assertFalse("Testing name with consecutive hyphens...", errMsg3.isEmpty());
        
        // Test name with consecutive separators (spaces)
        String errMsg4 = NameValidator.validateName("Bad  Format");
        assertFalse("Testing name with consecutive spaces...", errMsg4.isEmpty());
        
        // Test that leading/trailing whitespace is properly trimmed and becomes valid
        String errMsg5 = NameValidator.validateName("  Valid Name  ");
        assertTrue("Testing that surrounding whitespace is trimmed...", errMsg5.isEmpty());
    }
}
