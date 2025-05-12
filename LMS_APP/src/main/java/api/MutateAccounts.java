package api;

import java.sql.*;
import java.util.ArrayList;

import models.Account;
import org.mindrot.jbcrypt.BCrypt;

public class MutateAccounts {
    public static final int STUDENT_ROLE_ID = 2;
    public static final int FACULTY_STAFF_ROLE_ID = 1;

    public static void main(String[] args) {
        String url = System.getenv("LMS_DB_URL");
        try {
            Connection conn = DriverManager.getConnection(url);
            if (!conn.isValid(10)) {
                throw new SQLException();
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void RegisterStudentAccount(Connection conn, String username,
                                       String password,
                                       String first_name, String last_name) throws SQLException {
        try {
            String query = "INSERT INTO accounts (username, password, first_name, last_name, role_id) " +
                    "VALUES (?, ?, ?, ?, ?)";
            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);
            pstmt.setString(3, first_name);
            pstmt.setString(4, last_name);
            pstmt.setInt(5, STUDENT_ROLE_ID);
            pstmt.executeUpdate();
            System.out.println("Student account registered successfully!");
            pstmt.close();
        } catch (SQLException e) {
            System.out.println("Failed to register account: " + e.getMessage());
            throw e;
        }
    }

    public static void RegisterFacultyAccount(Connection conn, String username,
                                              String password,
                                              String first_name, String last_name) throws SQLException {
        try {
            String query = "INSERT INTO accounts (username, password, first_name, last_name, role_id) " +
                    "VALUES (?, ?, ?, ?, ?)";
            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);
            pstmt.setString(3, first_name);
            pstmt.setString(4, last_name);
            pstmt.setInt(5, FACULTY_STAFF_ROLE_ID);
            pstmt.executeUpdate();
            System.out.println("Faculty account registered successfully!");
            pstmt.close();
        } catch (SQLException e) {
            System.out.println("Failed to register account: " + e.getMessage());
            throw e;
        }
    }
}
