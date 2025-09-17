package application;

public class NameValidator {
/**
 * <p> Title: FSM based Name Validator. </p>
 * <p> Description: A Java class that validates full name based on Finite State Machine (FSM).
 * The code is designed to follow a clear, state transition model within a while loop. It checks for
 * valid characters, proper structure, and length constraint.</p>
 * 
 * @author Ashenafi Teressa
 * @version 1.00 2025-09-15 Initial implementation of the FSM name validator.
 * 
 */
	
	
	/********************************************************************************************
	 *  * Result attributes to be used for GUI applications where a detailed error message will
	 *  enhance the user experience. 
	 */
	public static String nameErrorMessage = ""; 	// The error message text
	public static String nameInput = "";			// The input being processed 
	
	
	/********************************************************************************************
	 * 	* The private attributes to manage the FSM execution. 
	 */
	
	private static String inputLine;		// The input line being processed
	private static int charCounter;			// The index of the current char
	private static char currentChar;		// The current character in the line
	private static boolean running;			// A boolean flag to control the FSM loop
	
	
	
	// State variables and constants
	private static int currentState;		// The current state of the FSM
	private static final int STATE_START = 0;
	private static final int STATE_IN_LETTER = 1;
	private static final int STATE_IN_SEPARATOR = 2;
	private static final int STATE_INVALID = 3;
	
	/***********
	 * This method is a mechanical transformation of the Name Validator FSM diagram into a Java method.
	 * 
	 * @param input		The input string for FSM processing.
	 * @return			An empty string if the name is valid, otherwise a string with a helpful description of the error.
	 */
	public static String validateName(String input) 
	{
		
		// Initialize the FSM for a new validation run
		nameInput = input;
		inputLine = input;
		charCounter = 0;
		
		
		// Check for empty input after trimming 
		if(inputLine.isEmpty())
		{
			nameErrorMessage = "\nNAME ERROR: Name cannot be empty.";
			return nameErrorMessage;
		}
		
		// Check length constraints
		if(inputLine.length() < 2 || inputLine.length() > 100)
		{
			nameErrorMessage = "\nNAME ERROR: Name must be between 2 and 100 characters long.";
			return nameErrorMessage;
		}
		
		// Initialize FSM state and start the loop
		currentState = STATE_START;
		running = true;
		
		while(running) 
		{
			
			// get the character for the current position 
			currentChar =inputLine.charAt(charCounter);
			
			switch(currentState)
			{
			
			case STATE_START: // initial state
				if(Character.isLetter(currentChar)) {
					currentState = STATE_IN_LETTER; // First char is a letter, valid transition 
				}else {
					currentState = STATE_INVALID; // First char is not a letter, invalid
					nameErrorMessage = "\nNAME ERROR: Name must start with a letter.";
				}
				break;
			case STATE_IN_LETTER: // the state when the previous char was a letter
				if(Character.isLetter(currentChar)) 
				{
					currentState = STATE_IN_LETTER; // loop back to this state. valid state
				}else if(currentChar == ' ' || currentChar == '-' || currentChar == '\'')
				{
					currentState = STATE_IN_SEPARATOR; // transition to separator state
				}else {
					currentState = STATE_INVALID; // an invalid character found
					nameErrorMessage = "\nNAME ERROR: Name contains invalid character: '" + currentChar + "'";
				}
				break;
				
			case STATE_IN_SEPARATOR: // state when the previous char was a separator 
				if(Character.isLetter(currentChar)) {
					currentState = STATE_IN_LETTER; // separator must be followed by letter, valid
				}else {
					currentState = STATE_INVALID; // consecutive separators of invalid character
					if(currentChar == ' ' || currentChar == '-' || currentChar == '\'')
					{
						nameErrorMessage = "\nNAME ERROR: A space, hyphen or apostrophe must be followed by a letter.";
					}else {
						nameErrorMessage = "\nNAME ERROR: Name contains invalid character: '" + currentChar + "'";
					}
				}
				break;
			case STATE_INVALID:		
				running = false; // exit the while loop to start processing 
				break;	
				
			}
			
			
			if(currentState == STATE_INVALID) {
				running = false; // stop processing, if in invalid state
			}
			
			charCounter++; // move to the next character
			
			if(charCounter >= inputLine.length()) {
				running = false; // end of the input string
			}
			
		}
		
		// If the FSM ended in on invalid state, return the specific error message
		if(currentState == STATE_INVALID) {
			return nameErrorMessage;
		}
		
		// A valid name must end in a letter 
		if(currentState != STATE_IN_LETTER)
		{
			nameErrorMessage = "\nNAME ERROR: Name cannot end with a space, hyphen or apostrophe.";
			return nameErrorMessage;
		}
		
		// If all checks pass, the name is valid
		nameErrorMessage = "";
		
		return nameErrorMessage;
	}
}
