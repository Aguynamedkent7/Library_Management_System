package api;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class mutate {
    public static void main(String[] args) {
        try {
            String url = System.getenv("LMS_DB_URL");
            Connection conn = DriverManager.getConnection(url);

            if (!conn.isValid(10)) {
                throw new SQLException();
            }

            String test = "mary's lamb";
            System.out.println(toTitleCase(test));
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void AddBookToDatabase(Connection conn,
                                         String title,
                                         String author,
                                         String genre,
                                         String publisher,
                                         String published_date) throws SQLException {
        try {
            conn.setAutoCommit(false);
            int publisher_id = GetPublisherID(conn, publisher);
            if (publisher_id == -1) {
                throw new SQLException("Invalid publisher name");
            }

            ArrayList<Integer> genre_ids = GetGenreID(conn, genre);
            if (genre_ids.isEmpty()) {
                throw new SQLException("Invalid genre name");
            }

            String query = "INSERT INTO books (title, author, publisher_id, published_date) " +
                    "VALUES (?, ?, ?, ?) RETURNING id";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, toTitleCase(title));
            pstmt.setString(2, toTitleCase(author));
            pstmt.setInt(3, publisher_id);
            LocalDate date = LocalDate.parse(published_date);
            pstmt.setDate(4, Date.valueOf(date));
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int book_id = rs.getInt(1);
                for (Integer id : genre_ids) {
                    query = "INSERT INTO genres_of_book (book_id, genre_id) VALUES (?, ?)";
                    pstmt = conn.prepareStatement(query);
                    pstmt.setInt(1, book_id);
                    pstmt.setInt(2, id);
                    pstmt.executeUpdate();
                }
            }

            System.out.println("Book added successfully!");
            pstmt.close();
            conn.commit();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            conn.rollback();
        }
    }

    public static int GetPublisherID(Connection conn, String publisher_name) {
        int publisher_id;
        try {
            // First try to insert the publisher if it doesn't exist and return the id
            String upsertQuery = 
                "WITH ins AS (" +
                "    INSERT INTO publishers (publisher_name) " +
                "    SELECT ? " +
                "    WHERE NOT EXISTS (SELECT 1 FROM publishers WHERE publisher_name = ?) " +
                "    RETURNING id" +
                ") " +
                "SELECT id FROM ins " +
                "UNION ALL " +
                "SELECT id FROM publishers WHERE publisher_name = ? " +
                "LIMIT 1";

            publisher_name = toTitleCase(publisher_name);
            PreparedStatement pstmt = conn.prepareStatement(upsertQuery);
            pstmt.setString(1, publisher_name);
            pstmt.setString(2, publisher_name);
            pstmt.setString(3, publisher_name);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                publisher_id = rs.getInt("id");
            } else {
                publisher_id = -1;
            }
            rs.close();
            pstmt.close();
            
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            publisher_id = -1;
        }

        return publisher_id;
    }

    public static ArrayList<Integer> GetGenreID(Connection conn, String genres) {
        ArrayList<Integer> genre_ids = new ArrayList<>();
        String[] genre_list = genres.split("\\s*,\\s*");
        for (int i = 0; i < genre_list.length; i++) {
            genre_list[i] = toTitleCase(genre_list[i]);
        }

        try {
            for (String genre : genre_list) {
                String query = "SELECT id FROM genres WHERE genre_name = ?";
                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setString(1, genre);
                ResultSet rs = pstmt.executeQuery();
                rs.next();
                genre_ids.add(rs.getInt("id"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return genre_ids;
    }

    public static String toTitleCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        StringBuilder titleCase = new StringBuilder();
        boolean nextTitleCase = true;

        for (char c : input.toLowerCase().toCharArray()) {
            if (Character.isSpaceChar(c) || c == '-') {
                nextTitleCase = true;
            } else if (nextTitleCase) {
                c = Character.toTitleCase(c);
                nextTitleCase = false;
            }
            titleCase.append(c);
        }

        return titleCase.toString();
    }

    public static void DeleteBookFromDatabase(Connection conn, int book_id) throws SQLException {
        try {
            conn.setAutoCommit(false);
            String query = "DELETE FROM books WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, book_id);
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected == 0) {
                throw new SQLException("Book not found in database");
            }

            conn.commit();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            conn.rollback();
            throw e;
        }
    }
}