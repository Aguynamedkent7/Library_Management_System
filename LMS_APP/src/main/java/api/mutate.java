package api;

import java.sql.*;

public class mutate {
    public static void main(String[] args) {
        try {
            String url = System.getenv("LMS_DB_URL");
            Connection conn = DriverManager.getConnection(url);

            if (!conn.isValid(10)) {
                throw new SQLException();
            }

            String query = "SELECT id FROM genres WHERE genre_name = 'slice of life'";

            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                System.out.println(rs.getInt("id"));
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void AddBookToDatabase(Connection conn,
                                         String title,
                                         String author,
                                         String genre,
                                         String publisher,
                                         String published_date) {

    }
}
