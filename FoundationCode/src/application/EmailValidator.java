package application;

public class EmailValidator {
	/**
	 * <p> Title: FSM-translated EmailValidator. </p>
	 *
	 * <p> Description: A demonstration of the mechanical translation of a Finite State Machine
	 * into an executable Java program. This variant validates emails per three rules:
	 * (1) '@' must occur before '.', (2) an alphanumeric must appear immediately before '.',
	 * (3) an alphabetic must appear immediately after '.'. </p>
	 *
	 * <p> Derived structure from UserNameRecognizer by Lynn Robert Carter © 2024. </p>
	 *
	 * @author Jonathan Waterway, Collin Looney
	 * @version 1.00  2025-09-15  Initial version based on UserNameRecognizer FSM style
	 */

	/**********************************************************************************************
	 * Result attributes to be used for GUI applications.
	 */
	
	public static String emailValidatorErrorMessage = "";	// The error message text
	public static String emailValidatorInput = "";			// The input being processed
	private static String inputLine = "";					// The input line
	
	public static int emailValidatorIndexofError = -1;		// The index of error location
	private static int state = 0;							// The current state value
	private static int nextState = 0;						// The next state value
	private static int emailSize = 0;			       		// size of email entry 
	private static int currentCharNdx;						// The index of the current character
	private static int atIndex = -1;						// Count how many @
	private static int dotCount = 0;						// How many dots seen after @ in FSM
	private static int totalDots = 0;						// How many total dots after @ in input
	
	private static char currentChar;						// The current character in the line
	private static char prevChar = '\0'; 					//added to track '.' location 
	
	private static boolean running;							// The flag that specifies if the FSM is running
	private static boolean validLocal = false;				// Valid input in local part of email (<local>@domain.TLD)
	private static boolean otherChar = false;				// Invalid char found in local part of email
	private static boolean finalState = false;				// Is this state a final state?
	private static boolean badHyphen = false;				// Hyphen in wrong spot?
	
	
	
	private static boolean isAlpha(char c) { // helper to check if input char is Alphabetical 
	    if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z')) {
	        return true;
	    } else {
	        return false;
	    }
	}

	private static boolean isAlphanum(char c) { // helper to check if input char is Alphanumeric 
	    if (isAlpha(c) || (c >= '0' && c <= '9')) {
	        return true;
	    } else {
	        return false;
	    }
	}
	
	private static void displayDebuggingInfo() { //same as LR Carter's code except line 64 (emailSize) 
		// Display the current state of the FSM as part of an execution trace
		if (currentCharNdx >= inputLine.length())
			// display the line with the current state numbers aligned
			System.out.println(((state > 99) ? " " : (state > 9) ? "  " : "   ") + state + 
					((finalState) ? "       F   " : "           ") + "None");
		else
			System.out.println(((state > 99) ? " " : (state > 9) ? "  " : "   ") + state + 
				((finalState) ? "       F   " : "           ") + "  " + currentChar + " " + 
				((nextState > 99) ? "" : (nextState > 9) || (nextState == -1) ? "   " : "    ") + 
				nextState + "     " + emailSize);
	}
	
	// Private method to move to the next character within the limits of the input line
		private static void moveToNextCharacter() {
			prevChar = currentChar; // tracks previous character for purpose of '.' check implementation 
			currentCharNdx++;
			if (currentCharNdx < inputLine.length())
				currentChar = inputLine.charAt(currentCharNdx);
			else {
				currentChar = ' ';
				running = false;
			}
		}
		
		
		/** FSM 
		 * States:
		 * 0: validates the local part of the email address
		 * 1: transition to domain upon '@' symbol
		 * 2: period entered 
		 * 3: alpha character entered after period, completing requirements (Accepting state)
		 */
		
		public static String checkForValidEmail (String input) { 
			if (input == null || input.length() <= 0) {
				emailValidatorIndexofError = 0;
				return "\nEMAIL ERROR: The input is empty"; 
			}
				
		// The local variables used to perform the Finite State Machine simulation
		state = 0;							// This is the FSM state number
		inputLine = input;					// Save the reference to the input line as a global
		currentCharNdx = 0;					// The index of the current character
		currentChar = input.charAt(0);		// The current character from above indexed position
		prevChar = '\0'; 					// Previous character for '.' tracking 
		
		// The Finite State Machines continues until the end of the input is reached or at some 
		// state the current character does not match any valid transition to a next state

		emailValidatorInput = input;	// Save a copy of the input
		running = true;						// Start the loop
		nextState = -1;						// There is no next state
		finalState = false; 				// still running 
		validLocal = false;					// reset validLocal
		otherChar = false;       			// reset otherChar
		badHyphen = false;					// reset badHyphen flag
		atIndex = -1;
		dotCount = 0;
		totalDots = 0;
		
		// Special pre-scan errors
		System.out.println(input); //debug
		
		// Count and index @
		for (int i = 0; i < input.length(); i++) {
		    char c = input.charAt(i);
		    if (c == '@') 
		    	if (atIndex == -1) {
		    	 	atIndex = i;
		    	} else 	return "EMAIL ERROR: Multiple '@' symbols are not allowed.\n";
		}
		
		// Return error if no @
		
		if (atIndex == -1) return "EMAIL ERROR: Missing '@' symbol.\n";
		
		// count dots in input
		for (int i = atIndex; i < input.length() - 1; i++) {
			char c = input.charAt(i);
			if (c == '.') totalDots++;				
		}
		
		if (totalDots == 0) {
		    return "EMAIL ERROR: Missing '.' in domain.\n";
		}
		
		System.out.println("\nCurrent Final Input  Next  Size\nState   State Char  State  Size");
		
		// This is the place where semantic actions for a transition to the initial state occur
		
		emailSize = 0;					// Initialize the email size
		
		while (running) { 
			switch (state) {
			case 0: 
				// Allow Alphanumeric and "~`!#$%^&*_-+{}|'?/" -> state 0
				// . -> state 1 if previousChar is alphaNum
				// @ -> state 2 unless empty local
				if (currentChar == '@') {
					    if (!validLocal) {
					    	running = false;
					    	break;
					    }
				    nextState = 2;
				    emailSize++;
				} else if (currentChar == '.') {
					    if (!validLocal) {
					    	running = false;
					    	break;
					    }
				    nextState = 1;
				    emailSize++;
				} else if (isAlphanum(currentChar) || ("~`!#$%^&*_-+{}|'?/".indexOf(currentChar) >= 0)) {
					nextState = 0; 
					emailSize++; 
					validLocal = true;
				} else {
					running = false; 
					otherChar = true;				
				}
				break;
			
			case 1: 
				//  
				if ((isAlphanum(currentChar) || ("~`!#$%^&*_-+{}|'?/".indexOf(currentChar) >= 0)) ) { 
					nextState = 0; 
					emailSize++; 
				} else {
					running = false; 
				}
				break;
			
			
			case 2: // handle domain
				
				if (currentChar == '-' && (prevChar == '@' || prevChar == '.')) {
					badHyphen = true;
					running = false;
					break;		
				}
				
				if (currentChar == '.') {
					if (isAlphanum(prevChar)) {
						if (dotCount == totalDots - 1) {
							nextState = 3; 
							emailSize++;
						} else {
							dotCount++; 
							nextState = 2; 
							emailSize++;
						}
					} else {
						running = false; 
					}
				} else if (isAlphanum(currentChar) || currentChar == '-') { //hyphen's ok in domain label 
					nextState = 2; 
					emailSize++;
				} else {
					running = false; 
				}
				break;
				
			case 3:
				// After '.', requires alphabetic char 
				if (isAlpha (currentChar)) { 
					nextState = 4; 
					emailSize++; 
				} else { 
					running = false; 
				}
				break;
				
			case 4: 
				// Continue accepting until a non-alphabetic char is entered
				if (currentChar == '-') { 
					if (input.length() == emailSize + 1) {
						badHyphen = true;
						running = false;
						break;
					}
				
				}
				if (isAlpha(currentChar) || currentChar == '-') { 
					nextState = 4; 
					emailSize++; 
				} else {
					running = false; 
				}
				break;
			}
			
			
			if (running) {
			    displayDebuggingInfo();
			    moveToNextCharacter();        // <<< ADDED: advance character
			    state = nextState;            // <<< ADDED: commit transition
			    finalState = (state == 4);    // <<< ADDED: accepting if in state 3
			    nextState = -1;               // <<< ADDED: reset
			}
		}
				
			// After loop
			displayDebuggingInfo();
			System.out.println("The loop has ended.");

			emailValidatorIndexofError = currentCharNdx; 
			emailValidatorErrorMessage = "\nEMAIL ERROR: ";

			// Valid if accepting and input fully consumed
			if (finalState && currentCharNdx >= input.length()) {
			    emailValidatorIndexofError = -1; 
			    emailValidatorErrorMessage = ""; 
			    return emailValidatorErrorMessage; 
			}

			// Otherwise, report specific error
			switch (state) {                                                                                       
			    case 0:
			    	if (otherChar) {
			    		return emailValidatorErrorMessage + "Invalid character in local part of email.\n";	
			    	}
			    	if (currentChar == '@' && (!validLocal)) {
			    		return emailValidatorErrorMessage + "Email can't start with '@'.\n";	
			    	}
			    	if (currentChar == '.' && (!validLocal)) {
			    		return emailValidatorErrorMessage + "Email can't start with '.'.\n";	
			    	}
			    	return emailValidatorErrorMessage + "Missing '@' to transition from local part to domain.\n";
			    case 1:
			    	return emailValidatorErrorMessage + "Only alphanumeric or \"~`!#$%^&*_-+{}|'?/\" are allowed between '.' in the local part of email.\n";
			    		
			    case 2:                                                                                            
			        if (prevChar == '@') { 
			        		if (badHyphen) {
			        			return emailValidatorErrorMessage + "Domain can't start with a hyphen.\n";
			        		}
			   
			        } else if (currentChar == '-' && prevChar == '.') {
			        	return emailValidatorErrorMessage + "A hyphen can not follow a '.' in the domain.\n"; 
			        } else {
			            return emailValidatorErrorMessage + "A '.' must appear in the domain and the character immediately before '.' must be alphanumeric.\n"; 
			        }
			        return emailValidatorErrorMessage + "Domain must start with a letter or digit.\n";
			        
			    case 3:                                                                                           
			        return emailValidatorErrorMessage + "The character immediately after the last '.' must be alphabetic (A-Z or a-z).\n"; 
			    case 4: 
			    	if (currentChar == '-') {
			    		return emailValidatorErrorMessage + "Email can not end with hyphen.\n";			    		
			    	}
			        return emailValidatorErrorMessage + "Only alphabetic characters or hyphens are allowed in TLD.\n";
			    
			    default:                                                                                          
			        return emailValidatorErrorMessage + "Invalid email format.\n";                                 
			}        
			
	}
}
