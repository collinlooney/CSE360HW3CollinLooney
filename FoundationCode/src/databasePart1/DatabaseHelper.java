package databasePart1;

import java.sql.*;
import java.util.UUID;
import java.util.*;

// NOTE: Removed direct Role dependency for now
// import application.Role;
import application.User;

public class DatabaseHelper {

    static final String JDBC_DRIVER = "org.h2.Driver";   
    static final String DB_URL = "jdbc:h2:~/FoundationDatabase";  

    static final String USER = "sa"; 
    static final String PASS = ""; 

    public Connection connection = null; // made public so test can access
    private Statement statement = null; 

    public void connectToDatabase() throws SQLException {
        try {
            if (connection != null && !connection.isClosed()) return; 
            Class.forName(JDBC_DRIVER); 
            System.out.println("Connecting to database...");
            connection = DriverManager.getConnection(DB_URL, USER, PASS);
            statement = connection.createStatement(); 
            createTables();
        } catch (ClassNotFoundException e) {
            System.err.println("JDBC Driver not found: " + e.getMessage());
        }
    }

    private void createTables() throws SQLException {
        String userTable = "CREATE TABLE IF NOT EXISTS cse360users ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "userName VARCHAR(255) UNIQUE, "
                + "password VARCHAR(255), "
                + "name VARCHAR(255), "
                + "email VARCHAR(255), "
                + "roles VARCHAR(20))";
        statement.execute(userTable);

        String invitationCodesTable = "CREATE TABLE IF NOT EXISTS InvitationCodes ("
                + "code VARCHAR(10) PRIMARY KEY, "
                + "isUsed BOOLEAN DEFAULT FALSE, "
                + "expiration BIGINT)";
        statement.execute(invitationCodesTable);
    }

    // ----------------------
    // ✅ Your deleteUser method
    // ----------------------
    public boolean deleteUser(String userName) {
        String deleteQuery = "DELETE FROM cse360users WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(deleteQuery)) {
            pstmt.setString(1, userName);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ----------------------
    // 🚧 Temporarily disabled broken methods
    // ----------------------

    /*
    public ArrayList<Role> getUserRoles(String userName) {
        return new ArrayList<>();
    }
    */

    /*
    public String generateInvitationCode() {
        // temporarily disabled for testing deleteUser
        return "TEST";
    }
    */

    // ----------------------
    // Cleanup
    // ----------------------
    public void closeConnection() {
        try { if (statement != null) statement.close(); } catch(SQLException se2) { se2.printStackTrace(); }
        try { if (connection != null) connection.close(); } catch(SQLException se) { se.printStackTrace(); }
    }
}
