package databasePart1;
import java.sql.*;
import application.RoleManager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import application.User;


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

	public void connectToDatabase() throws SQLException {
		try {
			Class.forName(JDBC_DRIVER); // Load the JDBC driver
			System.out.println("Connecting to database...");
			connection = DriverManager.getConnection(DB_URL, USER, PASS);
			statement = connection.createStatement(); 
			// You can use this command to clear the database and restart from fresh.
			//statement.execute("DROP ALL OBJECTS");

			createTables();  // Create the necessary tables if they don't exist
		} catch (ClassNotFoundException e) {
			System.err.println("JDBC Driver not found: " + e.getMessage());
		}
	}

	private void createTables() throws SQLException {
	    // ✅ Users Table
	    String userTable = "CREATE TABLE IF NOT EXISTS cse360users ("
	            + "id INT AUTO_INCREMENT PRIMARY KEY, "
	            + "userName VARCHAR(255) UNIQUE, "
	            + "password VARCHAR(255), "
	            + "role VARCHAR(20))";
	    statement.execute(userTable);

	    // ✅ Invitation Codes Table
	    String invitationCodesTable = "CREATE TABLE IF NOT EXISTS InvitationCodes ("
	            + "code VARCHAR(10) PRIMARY KEY, "
	            + "isUsed BOOLEAN DEFAULT FALSE)";
	    statement.execute(invitationCodesTable);

	    // ✅ Questions Table (New)
	    String questionsTable = "CREATE TABLE IF NOT EXISTS QUESTIONS ("
	            + "id INT AUTO_INCREMENT PRIMARY KEY, "
	            + "question_text TEXT)";
	    statement.execute(questionsTable);

	    // ✅ Answers Table (Linked to Questions)
	    String answersTable = "CREATE TABLE IF NOT EXISTS ANSWERS ("
	            + "id INT AUTO_INCREMENT PRIMARY KEY, "
	            + "question_id INT, "
	            + "answer_text TEXT, "
	            + "FOREIGN KEY (question_id) REFERENCES QUESTIONS(id) ON DELETE CASCADE)";
	    statement.execute(answersTable);
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
		String insertUser = "INSERT INTO cse360users (userName, password, role) VALUES (?, ?, ?)";
		try (PreparedStatement pstmt = connection.prepareStatement(insertUser)) {
			pstmt.setString(1, user.getUserName());
			pstmt.setString(2, user.getPassword());
			pstmt.setString(3, user.getRole());
			pstmt.executeUpdate();
		}
	}

	// Validates a user's login credentials.
	public boolean login(User user) throws SQLException {
		String query = "SELECT * FROM cse360users WHERE userName = ? AND password = ? AND role = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, user.getUserName());
			pstmt.setString(2, user.getPassword());
			pstmt.setString(3, user.getRole());
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
	
	// Retrieves the role of a user from the database using their UserName.
	public String getUserRole(String userName) {
	    String query = "SELECT role FROM cse360users WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, userName);
	        ResultSet rs = pstmt.executeQuery();
	        
	        if (rs.next()) {
	            return rs.getString("role"); // Return the role if user exists
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return null; // If no user exists or an error occurs
	}
	
	// Generates a new invitation code and inserts it into the database.
	public String generateInvitationCode() {
	    String code = UUID.randomUUID().toString().substring(0, 4); // Generate a random 4-character code
	    String query = "INSERT INTO InvitationCodes (code) VALUES (?)";

	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, code);
	        pstmt.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }

	    return code;
	}
	
	// Validates an invitation code to check if it is unused.
	public boolean validateInvitationCode(String code) {
	    String query = "SELECT * FROM InvitationCodes WHERE code = ? AND isUsed = FALSE";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, code);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	            // Mark the code as used
	            markInvitationCodeAsUsed(code);
	            return true;
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return false;
	}
	
	public boolean isUserQuestion(String username, int questionId) {
	    String query = "SELECT created_by FROM QUESTIONS WHERE id = ?";
	    try (PreparedStatement stmt = connection.prepareStatement(query)) {
	        stmt.setInt(1, questionId);
	        ResultSet rs = stmt.executeQuery();

	        if (rs.next()) {
	            String questionOwner = rs.getString("created_by");
	            return questionOwner.equals(username); // ✅ Ensure the logged-in user matches
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return false;
	}


	public void deleteQuestion(int questionId) {
	    String query = "DELETE FROM QUESTIONS WHERE id = ?";
	    try (PreparedStatement stmt = connection.prepareStatement(query)) {
	        stmt.setInt(1, questionId);
	        stmt.executeUpdate();
	        System.out.println("✔ Deleted Question ID: " + questionId);
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
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
	
//------------------------------
	// Retrieves all users from the database
	public ResultSet getAllUsers() throws SQLException {
	    String query = "SELECT userName, role FROM cse360users";
	    return statement.executeQuery(query);
	}

	// Updates the role of a user in the database
	public void updateUserRole(String userName, String newRole) {
	    String query = "UPDATE cse360users SET role = ? WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, newRole);
	        pstmt.setString(2, userName);
	        pstmt.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}

	// Deletes a user from the database
	public void deleteUser(String userName) {
	    String query = "DELETE FROM cse360users WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, userName);
	        pstmt.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}

	
//------------------------------
	
	// ✅ Fetch answer for a specific question
	public String getAnswerForQuestion(int questionId) {
	    String query = "SELECT answer_text FROM ANSWERS WHERE question_id = ?";
	    
	    try (PreparedStatement stmt = connection.prepareStatement(query)) {
	        stmt.setInt(1, questionId);
	        ResultSet rs = stmt.executeQuery();
	        if (rs.next()) {
	            return rs.getString("answer_text");
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return null; // No answer found
	}

	// ✅ Add an answer to a question
	public void addAnswerToQuestion(int questionId, String answerText) {
	    String query = "INSERT INTO ANSWERS (question_id, answer_text) VALUES (?, ?)";

	    try (PreparedStatement stmt = connection.prepareStatement(query)) {
	        stmt.setInt(1, questionId);
	        stmt.setString(2, answerText);
	        stmt.executeUpdate();
	        System.out.println("✔ Answer added for Question ID: " + questionId);
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}

	// ✅ Update an existing answer
	public void updateAnswer(int questionId, String newAnswerText) {
	    String query = "UPDATE ANSWERS SET answer_text = ? WHERE question_id = ?";

	    try (PreparedStatement stmt = connection.prepareStatement(query)) {
	        stmt.setString(1, newAnswerText);
	        stmt.setInt(2, questionId);
	        stmt.executeUpdate();
	        System.out.println("✔ Answer updated for Question ID: " + questionId);
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}

    
	public void addQuestion(String questionText) {
	    String query = "INSERT INTO QUESTIONS (question_text) VALUES (?)"; // ✅ Reverting to previous working version

	    try (PreparedStatement stmt = connection.prepareStatement(query)) {
	        stmt.setString(1, questionText);
	        stmt.executeUpdate();
	        System.out.println("✔ Added Question: " + questionText);
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}



	// ✅ Fetch all questions with their IDs
	public List<Map.Entry<Integer, String>> getAllQuestionsWithIds() {
	    List<Map.Entry<Integer, String>> questions = new ArrayList<>();
	    String query = "SELECT id, question_text FROM QUESTIONS";

	    try (Statement stmt = connection.createStatement();
	         ResultSet rs = stmt.executeQuery(query)) {
	        while (rs.next()) {
	            int id = rs.getInt("id");
	            String text = rs.getString("question_text");
	            questions.add(new AbstractMap.SimpleEntry<>(id, text));
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return questions;
	}

	// ✅ Update an existing question
	public void updateQuestion(int questionId, String newQuestionText) {
	    String query = "UPDATE QUESTIONS SET question_text = ? WHERE id = ?";

	    try (PreparedStatement stmt = connection.prepareStatement(query)) {
	        stmt.setString(1, newQuestionText);
	        stmt.setInt(2, questionId);
	        stmt.executeUpdate();
	        System.out.println("✔ Updated Question ID " + questionId + " to: " + newQuestionText);
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

}
