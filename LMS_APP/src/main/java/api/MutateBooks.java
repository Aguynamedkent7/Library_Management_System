package api;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;

public class MutateBooks {
    public static void main(String[] args) {
        try {
            String url = System.getenv("LMS_DB_URL");
            Connection conn = DriverManager.getConnection(url);

            if (!conn.isValid(10)) {
                throw new SQLException();
            }

            int account_id = 3;
            int book_id = 2;

            ReturnBook(conn, 1);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    // MUTATE BOOKS
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

    public static void UpdateBookInDatabase(Connection conn,
                                            int book_id, String title,
                                            String author, String genre,
                                            String publisher, String published_date
    ) throws SQLException {
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
            String query = "UPDATE books SET title = ?, author = ?, " +
                    "publisher_id = ?, published_date = ? WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, toTitleCase(title));
            pstmt.setString(2, toTitleCase(author));
            pstmt.setInt(3, publisher_id);
            LocalDate date = LocalDate.parse(published_date);
            pstmt.setDate(4, Date.valueOf(date));
            pstmt.setInt(5, book_id);
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected == 0) {
                throw new SQLException("Book not found in database");
            }

            // First, delete existing genre associations
            query = "DELETE FROM genres_of_book WHERE book_id = ?";
            pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, book_id);
            pstmt.executeUpdate();

            // Then, insert new genre associations
            query = "INSERT INTO genres_of_book (book_id, genre_id) VALUES (?, ?)";
            PreparedStatement genrePstmt = conn.prepareStatement(query);
            for (Integer genre_id : genre_ids) {
                genrePstmt.setInt(1, book_id);
                genrePstmt.setInt(2, genre_id);
                genrePstmt.addBatch();
            }
            genrePstmt.executeBatch();

            System.out.println("Book updated successfully!");
            pstmt.close();
            genrePstmt.close();
            conn.commit();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            conn.rollback();
            throw e;
        }
    }

    public static void BorrowBook(Connection conn, int account_id, int book_id, String return_date) throws SQLException {
        try {
            String query = "INSERT INTO borrowed_books (account_id, book_id, borrow_date, return_date) " +
                    "VALUES (?, ?, ?, ?)";
            conn.setAutoCommit(false);

            Date borrow_date = Date.valueOf(LocalDate.now());
            Date return_date_obj = Date.valueOf(LocalDate.parse(return_date));

            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, account_id);
            pstmt.setInt(2, book_id);
            pstmt.setDate(3, borrow_date);
            pstmt.setDate(4, return_date_obj);
            pstmt.executeUpdate();
            pstmt.close();
            conn.commit();
            System.out.println("Book borrowed successfully!");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            conn.rollback();
            throw e;
        }
    }

    public static void ReturnBook(Connection conn, int reference_id) throws SQLException {
        try {
            String query = "DELETE FROM borrowed_books WHERE reference_id = ?";
            conn.setAutoCommit(false);
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, reference_id);
            pstmt.executeUpdate();
            pstmt.close();
            conn.commit();
            System.out.println("Book returned successfully!");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            conn.rollback();
            throw e;
        }
    }
}