package databasePart1;
import java.sql.*;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import application.User;
import application.Answer;
import application.Comment;
import application.Question;
import application.QuestionStatus;
import application.Role;
import application.Tags;

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
		try (Statement statement = connection.createStatement()) {
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
	    
	    
	    //newly added database tables 
	    
        String questionsTable = "CREATE TABLE IF NOT EXISTS questions ("
                + "    id              VARCHAR(36) PRIMARY KEY,"
                + "    title           VARCHAR(255),"
                + "    body            TEXT,"
                + "    authorUserName  VARCHAR(255),"
                + "    status          VARCHAR(50),"
                + "    creationTimestamp TIMESTAMP WITH TIME ZONE,"
                + "    tag             VARCHAR(50),"
                + "    isPrivate       BOOLEAN,"
                + "    isAnonymous     BOOLEAN,"
                + "    viewCount       INT,"
                + "    FOREIGN KEY (authorUserName) REFERENCES cse360users(userName) ON DELETE CASCADE"
                + ")";
            statement.execute(questionsTable);

            String answersTable = "CREATE TABLE IF NOT EXISTS answers ("
                + "    id              VARCHAR(36) PRIMARY KEY,"
                + "    body            TEXT,"
                + "    authorUserName  VARCHAR(255),"
                + "    questionId      VARCHAR(36),"
                + "    creationTimestamp TIMESTAMP WITH TIME ZONE,"
		+ "    resolvesQuestion BOOLEAN DEFAULT FALSE,"
                + "    FOREIGN KEY (authorUserName) REFERENCES cse360users(userName) ON DELETE CASCADE,"
                + "    FOREIGN KEY (questionId) REFERENCES questions(id) ON DELETE CASCADE"
                + ")";
            statement.execute(answersTable);

            String commentsTable = "CREATE TABLE IF NOT EXISTS comments ("
                + "    id              VARCHAR(36) PRIMARY KEY,"
                + "    body            TEXT,"
                + "    authorUserName  VARCHAR(255),"
                + "    answerId        VARCHAR(36),"
                + "    parentCommentId VARCHAR(36),"
                + "    creationTimestamp TIMESTAMP WITH TIME ZONE,"
                + "    FOREIGN KEY (authorUserName) REFERENCES cse360users(userName) ON DELETE CASCADE,"
                + "    FOREIGN KEY (answerId) REFERENCES answers(id) ON DELETE CASCADE,"
                + "    FOREIGN KEY (parentCommentId) REFERENCES comments(id) ON DELETE CASCADE"
                + ")";
            statement.execute(commentsTable);

            System.out.println("Tables created or already exist.");
		}
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
	
	
	//Newly added DB functions

	
    // Question Methods
    

     // inserts a new question into the database.
    public void addQuestion(Question question) throws SQLException {
        String sql = "INSERT INTO questions (id, title, body, authorUserName, status, creationTimestamp, tag, isPrivate, isAnonymous, viewCount) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, question.getQuestionId().toString());
            pstmt.setString(2, question.getTitle());
            pstmt.setString(3, question.getBody());
            pstmt.setString(4, question.getAuthor().getUserName());
            pstmt.setString(5, question.getStatus().name());
            pstmt.setTimestamp(6, Timestamp.from(question.getCreationTimestamp().toInstant()));
            pstmt.setString(7, question.getTag().name());
            pstmt.setBoolean(8, question.isPrivate());
            pstmt.setBoolean(9, question.isAnonymous());
            pstmt.setInt(10, question.getViewCount());
            pstmt.executeUpdate();
        }
    }
    

    // Updates an existing question in the database.
    public void updateQuestion(Question question) throws SQLException {
        String sql = "UPDATE questions SET title = ?, body = ?, tag = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, question.getTitle());
            pstmt.setString(2, question.getBody());
            pstmt.setString(3, question.getTag().name());
            pstmt.setString(4, question.getQuestionId().toString());
            pstmt.executeUpdate();
        }
    }
    

     // deletes a question and all its related data (answers, comments) from the database.

    public void deleteQuestion(String questionId) throws SQLException {
        String sql = "DELETE FROM questions WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, questionId);
            pstmt.executeUpdate();
        }
    }
    

     // Updates only the view count for a specific question.
    public void updateQuestionViewCount(Question question) throws SQLException {
        String sql = "UPDATE questions SET viewCount = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, question.getViewCount());
            pstmt.setString(2, question.getQuestionId().toString());
            pstmt.executeUpdate();
        }
    }


     // Retrieves all public questions from the database, ordered by most recent.

    public List<Question> getAllPublicQuestions() throws SQLException {
        List<Question> questions = new ArrayList<>();
        Map<String, User> userMap = getAllUsers().stream()
                .collect(Collectors.toMap(User::getUserName, user -> user));

        String sql = "SELECT * FROM questions WHERE isPrivate = FALSE ORDER BY creationTimestamp DESC";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String authorUserName = rs.getString("authorUserName");
                User author = userMap.get(authorUserName);
                if (author != null) {
                    Question q = new Question(
                        UUID.fromString(rs.getString("id")),
                        rs.getString("title"),
                        rs.getString("body"),
                        author,
                        QuestionStatus.valueOf(rs.getString("status")),
                        rs.getTimestamp("creationTimestamp").toInstant().atZone(ZoneId.systemDefault()),
                        Tags.valueOf(rs.getString("tag")),
                        rs.getBoolean("isPrivate"),
                        rs.getBoolean("isAnonymous"),
                        rs.getInt("viewCount")
                    );
                    questions.add(q);
                }
            }
        }
        return questions;
    }


    
    // Answer Methods
    

     // Inserts a new answer into the database.

    public void addAnswer(Answer answer) throws SQLException {
        String sql = "INSERT INTO answers (id, body, authorUserName, questionId, creationTimestamp) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, answer.getAnswerId().toString());
            pstmt.setString(2, answer.getBody());
            pstmt.setString(3, answer.getAuthor().getUserName());
            pstmt.setString(4, answer.getParentQuestion().getQuestionId().toString());
            pstmt.setTimestamp(5, Timestamp.from(answer.getCreationTimestamp().toInstant()));
            pstmt.executeUpdate();
        }
    }
    

     // Updates an existing answer in the database.

    public void updateAnswer(Answer answer) throws SQLException {
        String sql = "UPDATE answers SET body = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, answer.getBody());
            pstmt.setString(2, answer.getAnswerId().toString());
            pstmt.executeUpdate();
        }
    }


     // Deletes an answer and its related comments from the database.

    public void deleteAnswer(String answerId) throws SQLException {
        String sql = "DELETE FROM answers WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, answerId);
            pstmt.executeUpdate();
        }
    }

    
     // Gets the number of answers for a specific question.

    public int getAnswerCountForQuestion(String questionId) {
        String sql = "SELECT COUNT(*) FROM answers WHERE questionId = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, questionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    

     // Fetches all answers for a given question and populates their full comment threads.
    public void loadAnswersAndCommentsForQuestion(Question question) throws SQLException {
    	Map<String, User> userMap = getAllUsers().stream()
                .collect(Collectors.toMap(User::getUserName, user -> user));


        String answersSql = "SELECT * FROM answers WHERE questionId = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(answersSql)) {
            pstmt.setString(1, question.getQuestionId().toString());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                	User author = userMap.get(rs.getString("authorUserName"));
                    if (author != null) {
                        Answer answer = new Answer(
                            UUID.fromString(rs.getString("id")),
                            question, 
                            author, 
                            rs.getString("body"),
                            rs.getTimestamp("creationTimestamp").toInstant().atZone(ZoneId.systemDefault()),
			    rs.getBoolean("resolvesQuestion")
                        );
                        loadCommentTreeForAnswer(answer, userMap);
                        question.addAnswer(answer);
                    }
                }
            }
        }
    }

    // Updates an answer as resolution / not resolution
    public void updateAnswerResolutionStatus(String answerId, boolean resolvesQuestion) throws SQLException {
        String sql = "UPDATE answers SET resolvesQuestion = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setBoolean(1, resolvesQuestion);
            pstmt.setString(2, answerId);
            pstmt.executeUpdate();
        }
    }
    
    // Comment Methods

     // Inserts a new comment into the database.
    public void addComment(Comment comment) throws SQLException {
        String sql = "INSERT INTO comments (id, body, authorUserName, answerId, parentCommentId, creationTimestamp) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, comment.getCommentId().toString());
            pstmt.setString(2, comment.getBody());
            pstmt.setString(3, comment.getAuthor().getUserName());
            pstmt.setString(4, comment.getParentAnswer().getAnswerId().toString());
            if (comment.getParentComment() != null) {
                pstmt.setString(5, comment.getParentComment().getCommentId().toString());
            } else {
                pstmt.setNull(5, java.sql.Types.VARCHAR);
            }
            pstmt.setTimestamp(6, Timestamp.from(comment.getCreationTimestamp().toInstant()));
            pstmt.executeUpdate();
        }
    }
    

     // Updates an existing comment in the database.

    public void updateComment(Comment comment) throws SQLException {
        String sql = "UPDATE comments SET body = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, comment.getBody());
            pstmt.setString(2, comment.getCommentId().toString());
            pstmt.executeUpdate();
        }
    }
    

     // Deletes a comment from the database. This will also delete all replies.
    public void deleteComment(String commentId) throws SQLException {
        String sql = "DELETE FROM comments WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, commentId);
            pstmt.executeUpdate();
        }
    }


     // Helper method to fetch all comments for an answer and build the nested reply tree.
    private void loadCommentTreeForAnswer(Answer answer, Map<String, User> userMap) throws SQLException {
        String commentsSql = "SELECT * FROM comments WHERE answerId = ?";
        Map<String, Comment> commentMap = new HashMap<>();
        List<Comment> topLevelComments = new ArrayList<>();

        try (PreparedStatement pstmt = connection.prepareStatement(commentsSql,
                                                               ResultSet.TYPE_SCROLL_INSENSITIVE,
                                                               ResultSet.CONCUR_READ_ONLY)) {
            
            pstmt.setString(1, answer.getAnswerId().toString());
            try (ResultSet rs = pstmt.executeQuery()) {
                // first pass: create all comment objects and put them in a map for easy lookup.
                while (rs.next()) {
                    User author = userMap.get(rs.getString("authorUserName"));
                    Comment comment = new Comment(
                        UUID.fromString(rs.getString("id")),
                        answer,
                        null, // Parent comment is linked in the second pass.
                        author,
                        rs.getString("body"),
                        rs.getTimestamp("creationTimestamp").toInstant().atZone(ZoneId.systemDefault())
                    );
                    commentMap.put(rs.getString("id"), comment);
                }
                
                // second pass: link replies to their parents.
                rs.beforeFirst(); 
                while(rs.next()) {
                    String commentId = rs.getString("id");
                    String parentCommentId = rs.getString("parentCommentId");
                    Comment child = commentMap.get(commentId);

                    if (parentCommentId != null) {
                        Comment parent = commentMap.get(parentCommentId);
                        if (parent != null) {
                            parent.addReply(child);
                            child.setParentComment(parent);
                        }
                    } else {
                        topLevelComments.add(child);
                    }
                }
            }
        }
        
        // Add the fully constructed comment threads to the answer object.
        for (Comment comment : topLevelComments) {
            answer.addComment(comment);
        }
    }
    // Checks whether a given question has an accepted (resolved) answer.
    public boolean hasAcceptedAnswer(String questionId) throws SQLException {
        String query = "SELECT COUNT(*) FROM answers WHERE questionId = ? AND resolvesQuestion = TRUE";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, questionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
}

