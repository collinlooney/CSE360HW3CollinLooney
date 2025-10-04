package application;

public enum Role {
	ADMIN,
	BASIC_USER,
	STUDENT;

	// Helper method to convert a Role into an integer for DB storage
	public int toInt() {
		switch (this) {
			case ADMIN: { 
				return 1;
			}
			case BASIC_USER: {
				return 2;	
			}
			case STUDENT:{
				return 3;
			}
			default: {
				return 2;
			}
		}
	}
	// Helper method to convert an integer into a Role
	public static Role fromInt(int n) {
		switch (n) {
			case 1: {
				return ADMIN;
			}
			case 2: {
				return BASIC_USER;
			}
			case 3: {
				return STUDENT;
			}
			default: {
				throw new IllegalArgumentException("Invalid role code: " + n);
			}
		}
	}

	// Helper method to get the String representation of a Role's variant
	public String display() {
	    switch (this) {
	        case ADMIN:
	            return "Admin";
	        case BASIC_USER:
	            return "Basic User";
	        case STUDENT:
	        	return "Student";            
	            // A fall back just in case other roles are added
	        default:
	            return this.name();
	    }
	}
}
