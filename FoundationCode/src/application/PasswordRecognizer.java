package application;


public class PasswordRecognizer {
	/**
	 * <p> Title: Directed Graph-translated Password Assessor. </p>
	 * 
	 * <p> Description: A demonstration of the mechanical translation of Directed Graph 
	 * diagram into an executable Java program using the Password Evaluator Directed Graph. 
	 * The code detailed design is based on a while loop with a cascade of if statements</p>
	 * 
	 * <p> Copyright: Lynn Robert Carter © 2022 </p>
	 * 
	 * @author Lynn Robert Carter
	 * 
	 * @version 0.00		2018-02-22	Initial baseline
	 * @version 0.10        2025-08-29  Modify to match updated FSM and application functionality
	 * 
	 */

	/**********************************************************************************************
	 * 
	 * Result attributes to be used for GUI applications where a detailed error message
	 * will enhance the user experience.
	 * 
	 */

	public static String passwordErrorMessage = "";		// The error message text
	public static String passwordInput = "";			// The input being processed
	private static String inputLine = "";				// The input line
	private static boolean running;						// The flag that specifies if the FSM is 
														// running
	private static int charCounter;						// The index of the current character
	private static char currentChar;					// The current character in the line
	
	private static boolean upperCase;					// The flag specifies upper case was found
	private static boolean lowerCase;					// The flag specifies lower case was found
	private static boolean numericChar;					// The flag specifies number was found
	private static boolean specialChar;					// The flag specifies special char was found
	private static boolean longEnough;					// The flag specifies input is >= 8 digits
	private static boolean otherChar;					// The flag specifies invalid character
														// was found
	
	/**********
	 * This method is a mechanical transformation of a Directed Graph diagram into a Java
	 * method.
	 * 
	 * @param input		The input string for directed graph processing
	 * @return			An output string that is empty if every things is okay or it will be
	 * 					a string with a helpful description of the error.
	 */
	public static String evaluatePassword(String input) {
		// The following are the local variable used to perform the Directed Graph simulation
		passwordErrorMessage = "";
		inputLine = input;					// Save the reference to the input line as a global
		charCounter = 0;					// The index of the current character
		
		if(input.length() <= 0) return "\nPASSWORD ERROR: Password cannot be empty";
		
		// The input is not empty, so we can access the first character
		currentChar = input.charAt(0);		// The current character from the above indexed position

		// The Directed Graph simulation continues until the end of the input is reached or at some 
		// state the current character does not match any valid transition to a next state
		passwordInput = input;				// Save a copy of the input
		upperCase = false;                  // Reset the boolean flag
		lowerCase = false;                  // Reset the boolean flag
		numericChar = false;                // Reset the boolean flag
		specialChar = false;                // Reset the boolean flag
		longEnough = false;                 // Reset the boolean flag
		otherChar = false;                  // Reset the boolean flag
		running = true;						// Start the loop
		
		while (running) {
			// The cascading if statement sequentially tries the current character against all of the
			// valid transitions
			if (currentChar >= 'A' && currentChar <= 'Z') {
				System.out.println("Upper case letter found");
				charCounter++;
				upperCase = true;
			} else if (currentChar >= 'a' && currentChar <= 'z') {
				System.out.println("Lower case letter found");
				charCounter++;
				lowerCase = true;
			} else if (currentChar >= '0' && currentChar <= '9') {
				System.out.println("Digit found");
				charCounter++;
				numericChar = true;
			} else if ("~`!@#$%^&*()_-+{}[]|:,.?/".indexOf(currentChar) >= 0) {
				System.out.println("Special character found");
				charCounter++;
				specialChar = true;
			} else {
				otherChar = true;
				running = false;
				break;
			}
			if (charCounter >= 8) {
				System.out.println("At least 8 characters found");
				longEnough = true;
			}
			
			// Go to the next character if there is one
			if (charCounter >= inputLine.length())
				running = false;
			else
				currentChar = input.charAt(charCounter);
			
			System.out.println();
		}
		
		String errMessage = "";
		
		// If password contains invalid char, error message should only include that
		if (otherChar) {
			errMessage += "\nPASSWORD ERROR: Password contains invalid character: " + currentChar;
			return errMessage;
		}
		
		// If any required character type is missing, add note to error message
		boolean missing = !(upperCase && lowerCase && numericChar && specialChar);
		
		if (missing) {
			errMessage += "\nPASSWORD ERROR: Password must include at least ";
			if (!upperCase)
				errMessage += "1 upper case letter, ";
			
			if (!lowerCase)
				errMessage += "1 lower case letter, ";
			
			if (!numericChar)
				errMessage += "1 numeric digit, ";
				
			if (!specialChar) 
				errMessage += "1 special character, ";
			
			errMessage = errMessage.substring(0, errMessage.length() - 2);
		}
		
		// If password is not long enough, add note to error message
		if (!longEnough)
			errMessage += "\nPASSWORD ERROR: Password must be at least 8 characters long";
		
		return errMessage;
	}
}