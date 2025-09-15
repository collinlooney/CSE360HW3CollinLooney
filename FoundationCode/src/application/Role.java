package application;

public enum Role {
	ADMIN,
	BASIC_USER;

	// Helper method to convert a Role into an integer for DB storage
	public int toInt() {
		switch (this) {
			case ADMIN: { 
				return 1;
			}
			case BASIC_USER: {
				return 2;	
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
			default: {
				throw new IllegalArgumentException("Invalid role code: " + n);
			}
		}
	}

	// Helper method to get the String representation of a Role's variant
	public String display() {
		String lower = this.name().toLowerCase();
		return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
	}
}
