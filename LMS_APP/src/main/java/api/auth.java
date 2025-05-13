package api;

import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class auth {
    public static void main(String[] args) {
        try {
            String url = System.getenv("LMS_DB_URL");
            Connection conn = DriverManager.getConnection(url);

            if (!conn.isValid(10)) {
                throw new SQLException();
            }

            int id = login(conn, "student1", "bilat");
            if (id == -1) {
                System.out.println("Invalid username or password");
            } else {
                System.out.println("Login successful!");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static int login(Connection conn, String username, String password) throws SQLException {
        // RETURNS THE ID OF ACCOUNT IF LOGIN SUCCESSFUL
        int res = -1;

        String query = "SELECT id, password FROM accounts WHERE username = ?";
        try (java.sql.PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, username);
            java.sql.ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String hashedPassword = rs.getString("password");
                if (BCrypt.checkpw(password, hashedPassword)) {
                    res = rs.getInt("id");
                }
            }
            rs.close();
            pstmt.close();
            return res;

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            throw e;
        }
    }

}
