package databasePart1;
import java.sql.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import java.util.*;

import application.User;
import application.Role;

/**
 * The DatabaseHelper class is responsible for managing the connection to the database,
 * performing operations such as user registration, login validation, and handling invitation codes.
 */
public class DatabaseHelper {

	// JDBC driver name and database URL 
	static final String JDBC_DRIVER = "org.h2.Driver";   
	static final String DB_URL = "jdbc:h2:~/FoundationDatabase";  

	//  Database credentials 
	static final String USER = "sa"; 
	static final String PASS = ""; 

	private Connection connection = null;
	private Statement statement = null; 
	//	PreparedStatement pstmt
	
	private String currentUserName; // allows tracking of logged in user 
	
	public void setCurrentUserName(String userName) { //setter
		this.currentUserName = userName;
	}
	
	public String getCurrentUserName() { //getter
		return this.currentUserName;
	}

	public void connectToDatabase() throws SQLException {
		try {
			if (connection != null && !connection.isClosed()) return; // do not act if connection is established -C Looney 
			Class.forName(JDBC_DRIVER); // Load the JDBC driver
			System.out.println("Connecting to database...");
			connection = DriverManager.getConnection(DB_URL, USER, PASS);
			System.out.println("Database connection successful!");
			statement = connection.createStatement(); 
			// You can use this command to clear the database and restart from fresh.
			//statement.execute("DROP ALL OBJECTS");
			createTables();  // Create the necessary tables if they don't exist
		} catch (SQLException e) {
			System.err.println("Failed to connect to the database: " + e.getMessage());
		} catch (ClassNotFoundException e) {
			System.err.println("JDBC Driver not found: " + e.getMessage());
		}
	}
	
	// below method by C. Looney  to guard against lost connections 
	public void verifyConnection() throws SQLException {
		if (connection == null || connection.isClosed()) {
			connectToDatabase(); 
		}
	}

	private void createTables() throws SQLException {
		String userTable = "CREATE TABLE IF NOT EXISTS cse360users ("
				+ "id INT AUTO_INCREMENT PRIMARY KEY, "
				+ "userName VARCHAR(255) UNIQUE, "
				+ "password VARCHAR(255), "
				+ "name VARCHAR(255), "
				+ "email VARCHAR(255), "
				// Roles stored as "1,2", convert to/from int on read/write
				+ "roles VARCHAR(20))";
		statement.execute(userTable);
		
		// Create the invitation codes table including expiration
	    String invitationCodesTable = "CREATE TABLE IF NOT EXISTS InvitationCodes ("
	            + "code VARCHAR(10) PRIMARY KEY, "
	            + "isUsed BOOLEAN DEFAULT FALSE, "
		    + "expiration BIGINT)";
	    statement.execute(invitationCodesTable);

	    // Table of users with a one time password set
	    String otpUserTable = "CREATE TABLE IF NOT EXISTS cse360otpusers ("
		    + "id INT AUTO_INCREMENT PRIMARY KEY, "
		    + "userName VARCHAR(255) UNIQUE)";
	    statement.execute(otpUserTable);
	}



	// Check if the database is empty
	public boolean isDatabaseEmpty() throws SQLException {
		String query = "SELECT COUNT(*) AS count FROM cse360users";
		ResultSet resultSet = statement.executeQuery(query);
		if (resultSet.next()) {
			return resultSet.getInt("count") == 0;
		}
		return true;
	}
	
	// Registers a new user in the database.
	public void register(User user) throws SQLException {
		String insertUser = "INSERT INTO cse360users (userName, password, name, email, roles) VALUES (?, ?, ?, ?, ?)";
		try (PreparedStatement pstmt = connection.prepareStatement(insertUser)) {
			pstmt.setString(1, user.getUserName());
			pstmt.setString(2, user.getPassword());
			pstmt.setString(3, user.getName());
			pstmt.setString(4, user.getEmail());
			pstmt.setString(5, user.rolesToString());
			pstmt.executeUpdate();
		}
	}

	// Validates a user's login credentials
	public boolean login(User user) throws SQLException {
		String query = "SELECT * FROM cse360users WHERE userName = ? AND password = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, user.getUserName());
			pstmt.setString(2, user.getPassword());
			try (ResultSet rs = pstmt.executeQuery()) {
				return rs.next();
			}
		}
	}
	
	// Checks if a user already exists in the database based on their userName.
	public boolean doesUserExist(String userName) {
	    String query = "SELECT COUNT(*) FROM cse360users WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        
	        pstmt.setString(1, userName);
	        ResultSet rs = pstmt.executeQuery();
	        
	        if (rs.next()) {
	            // If the count is greater than 0, the user exists
	            return rs.getInt(1) > 0;
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return false; // If an error occurs, assume user doesn't exist
	}

	// Add user to table containing usernames with 1 time password set
	public void addOtpUser(String userName) throws SQLException {
		String sql = "INSERT INTO cse360otpusers (userName) VALUES (?)";
		try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
			pstmt.setString(1, userName);
			pstmt.executeUpdate();
		}
	}

	// Remove a user from being marked as having 1 time password set
	public void removeOtpUser(String userName) throws SQLException {
		String sql = "DELETE FROM cse360otpusers WHERE userName = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
			pstmt.setString(1, userName);
			pstmt.executeUpdate();
		}
	}

	// Check if a user has 1 time password set
	public boolean isOtpUser(String userName) throws SQLException {
		String sql = "SELECT 1 FROM cse360otpusers WHERE userName = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
			pstmt.setString(1, userName);
			try (ResultSet rs = pstmt.executeQuery()) {
				return rs.next();
			}
		}
	}


	// Updates stored information for `userName` with `newUserInfo`.
	// Does not update userName field.
	public String updateUserInfo(String userName, User newUser) {

		// Verify that user exists
		boolean exists = this.doesUserExist(userName);
		if (!exists) {
			System.out.println("User with userName '" + userName + "' not found.");
			return "User with userName '" + userName + "' not found.";
		}

		// Updating user
		String q = "UPDATE cse360users SET userName = ?, password = ?, name = ?, email = ?, roles = ? WHERE userName = ?";

		try (PreparedStatement pstmt = connection.prepareStatement(q)) {
			pstmt.setString(1, userName);
			pstmt.setString(2, newUser.getPassword());
			pstmt.setString(3, newUser.getName());
			pstmt.setString(4, newUser.getEmail());
			pstmt.setString(5, newUser.rolesToString());
			pstmt.setString(6, userName);
			pstmt.executeUpdate();

		} catch (SQLException e) {
	                e.printStackTrace();
			return "Error: SQL error";
	        }

		// Empty string to indicate success
		return "";
	}
	
	// Retrieves the roles of a user from the database using their UserName.
	public ArrayList<Role> getUserRoles(String userName) {
	    String query = "SELECT roles FROM cse360users WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, userName);
	        ResultSet rs = pstmt.executeQuery();
	        
	        if (rs.next()) {
		    String rolesStr = rs.getString("roles");
		    ArrayList<Role> roles = User.rolesFromString(rolesStr);
		    return roles;
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    // If no user exists or an error occurs
	    return new ArrayList<>();
	}
	
	// Retrieves the name of a user from the database using their UserName.
	public String getUserNameField(String userName) {
	    String query = "SELECT name FROM cse360users WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, userName);
	        ResultSet rs = pstmt.executeQuery();
	        
	        if (rs.next()) {
		    return rs.getString("name");
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    // If no user exists or an error occurs
	    return null;
	}
	
	// Retrieves the email of a user from the database using their UserName.
	public String getUserEmail(String userName) {
	    String query = "SELECT email FROM cse360users WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, userName);
	        ResultSet rs = pstmt.executeQuery();
	        
	        if (rs.next()) {
		    return rs.getString("email");
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    // If no user exists or an error occurs
	    return null;
	}
	
	// Generates a new invitation code and inserts it into the database,
	// including a 10day expiration (as seconds since UNIX epoch)
	public String generateInvitationCode() {
	    String code = UUID.randomUUID().toString().substring(0, 4); // Generate a random 4-character code
	    // Current time in seconds since UNIX epoch
	    long now = System.currentTimeMillis() / 1000;
	    // 10 days from now
	    long expiration = now + (10L * 24 * 60 * 60);
	    String query = "INSERT INTO InvitationCodes (code, expiration) VALUES (?, ?)";

	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, code);
		pstmt.setLong(2, expiration);
	        pstmt.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }

	    return code;
	}
	
	// Validates an invitation code to check if it is unused and not expired
	public boolean validateInvitationCode(String code) {
	    String query = "SELECT * FROM InvitationCodes WHERE code = ? AND isUsed = FALSE";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, code);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
			long expiration = rs.getLong("expiration");
			long now = System.currentTimeMillis() / 1000;

			if (now <= expiration) {
				// Mark the code as used
				markInvitationCodeAsUsed(code);
				return true;
			} else {
				System.out.println("Invitation code expired");
			}

	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return false;
	}
	
	// Marks the invitation code as used in the database.
	private void markInvitationCodeAsUsed(String code) {
	    String query = "UPDATE InvitationCodes SET isUsed = TRUE WHERE code = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, code);
	        pstmt.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}

	// Closes the database connection and statement.
	public void closeConnection() {
		try{ 
			if(statement!=null) statement.close(); 
		} catch(SQLException se2) { 
			se2.printStackTrace();
		} 
		try { 
			if(connection!=null) connection.close(); 
		} catch(SQLException se){ 
			se.printStackTrace(); 
		} 
	}
	
	// Returns all users
	public List<User> getAllUsers() throws SQLException {
	    verifyConnection();
	    String q = "SELECT userName, password, name, email, roles FROM cse360users ORDER BY userName ASC";
	    List<User> users = new ArrayList<>();
	    try (PreparedStatement pstmt = connection.prepareStatement(q);
	         ResultSet rs = pstmt.executeQuery()) {
	        while (rs.next()) {
	            String userName = rs.getString("userName");
	            String password = rs.getString("password");
	            String name = rs.getString("name");
	            String email = rs.getString("email");
	            String rolesStr = rs.getString("roles");
	            ArrayList<Role> roles = (rolesStr == null || rolesStr.isEmpty())
	                    ? new ArrayList<>()
	                    : User.rolesFromString(rolesStr);
	            users.add(new User(userName, name, email, password, roles));
	        }
	    }
	    return users;
	}

	public User getUserByUserName(String userName) throws SQLException {
	    verifyConnection();
	    String q = "SELECT userName, password, name, email, roles FROM cse360users WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(q)) {
	        pstmt.setString(1, userName);
	        try (ResultSet rs = pstmt.executeQuery()) {
	            if (rs.next()) {
	                String password = rs.getString("password");
	                String name = rs.getString("name");
	                String email = rs.getString("email");
	                String rolesStr = rs.getString("roles");
	                ArrayList<Role> roles = (rolesStr == null || rolesStr.isEmpty())
	                        ? new ArrayList<>()
	                        : User.rolesFromString(rolesStr);
	                return new User(userName, name, email, password, roles);
	            }
	        }
	    }
	    return null;
	}

	// Deletes a user from the database by userName
	public boolean deleteUser(String userName) {
		String deleteQuery = "DELETE FROM cse360users WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(deleteQuery)) {
	        pstmt.setString(1, userName);
	        int rowsAffected = pstmt.executeUpdate();
	        return rowsAffected > 0; // true if at least one row was deleted
	    } catch (SQLException e) {
	        System.err.println("Error deleting user: " + e.getMessage());
	        e.printStackTrace();
	    }
	    return false; // If an error occurs, assume deletion did not work
	}
		
	
	public boolean isLastAdmin(String userName) throws SQLException { 
		verifyConnection();
		
		final String q = "SELECT roles FROM cse360users WHERE userName = ?";
		boolean isUserAdmin = false; 
		try (PreparedStatement ps = connection.prepareStatement(q)) {
			ps.setString(1, userName);
			
			try (ResultSet rs = ps.executeQuery()) {
				if (!rs.next()) { // if there's no match in database, user doesnt exist 
					return false;
				}
				
				String roles = rs.getString("roles"); //get column
				if (roles == null || roles.isBlank()) {
					return false;
				}
				//Unknown the order yet, so this will cover all bases 
				isUserAdmin = roles.equals("1") || roles.startsWith("1,") || roles.endsWith(",1") || roles.contains(",1,"); 
			}
		}
		
		if (!isUserAdmin) {
			return false;
		}
		// count up the admins 
		final String qAdminCount = "SELECT COUNT(*) FROM cse360users WHERE roles = '1' " +
								   "OR roles LIKE '1,%' OR roles LIKE '%,1' OR roles LIKE '%,1,%' ";
		
		int adminCount = 0; 
		
		try (Statement st = connection.createStatement();
				ResultSet rs = st.executeQuery(qAdminCount)) {
					if (rs.next()) {
						adminCount = rs.getInt(1); // get value 
					}
				}
				
		return adminCount <= 1; // last admin 
	}
	

}
