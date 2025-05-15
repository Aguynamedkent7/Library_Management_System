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
                query = "INSERT INTO book_copies (book_id, status) VALUES (?, 'AVAILABLE')";
                pstmt = conn.prepareStatement(query);
                pstmt.setInt(1, book_id);
                pstmt.executeUpdate();
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

    public static int BorrowBook(Connection conn, String borrower_fname, String borrower_lname, int book_id, String return_date) throws SQLException {
        try {
            String query = "SELECT copy_id FROM book_copies WHERE book_id = ? AND status = 'AVAILABLE' LIMIT 1";
            conn.setAutoCommit(false);

            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, book_id);
            ResultSet rs = pstmt.executeQuery();
            if (!rs.next()) {
                throw new SQLException("No available copies of this book");
            }

            int copy_id = rs.getInt("copy_id");

            query = "INSERT INTO borrowed_books (borrower_fname, borrower_lname, book_copy_id, borrow_date, return_date) " +
                    "VALUES (?, ?, ?, ?, ?)";

            Date borrow_date = Date.valueOf(LocalDate.now());
            Date return_date_obj = Date.valueOf(LocalDate.parse(return_date));

            pstmt = conn.prepareStatement(query);
            pstmt.setString(1, borrower_fname);
            pstmt.setString(2, borrower_lname);
            pstmt.setInt(3, copy_id);
            pstmt.setDate(4, borrow_date);
            pstmt.setDate(5, return_date_obj);
            pstmt.executeUpdate();

            query = "UPDATE book_copies SET status = 'BORROWED' WHERE copy_id = ?";
            pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, copy_id);
            pstmt.executeUpdate();

            pstmt.close();
            conn.commit();
            System.out.println("Book borrowed successfully!");
            return copy_id;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            conn.rollback();
            throw e;
        }
    }

    public static void ReturnBook(Connection conn, int book_copy_id) throws SQLException {
        try {
            conn.setAutoCommit(false);

            String query = "UPDATE book_copies SET status = 'AVAILABLE' WHERE copy_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, book_copy_id);
            pstmt.executeUpdate();

            String newQuery = "DELETE FROM borrowed_books WHERE book_copy_id = ?";
            pstmt = conn.prepareStatement(newQuery);
            pstmt.setInt(1, book_copy_id);
            pstmt.executeUpdate();

            pstmt.close();
            conn.commit();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            conn.rollback();
            throw e;
        }
    }

    public static void addBookCopy(Connection conn, int book_id, int numberOfCopies) throws SQLException {
        try {
            String query = "INSERT INTO book_copies (book_id, status) VALUES (?, 'AVAILABLE')";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, book_id);

            for (int i = 0; i < numberOfCopies; i++) {
                pstmt.executeUpdate();
            }
            pstmt.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            throw e;
        }
    }

    public static void removeBookCopy(Connection conn, int book_id, int numberOfCopies) throws SQLException {
        try {
            String query = "DELETE FROM book_copies WHERE copy_id IN " +
                    "(SELECT copy_id FROM book_copies WHERE book_id = ? AND status = 'AVAILABLE' LIMIT ?)";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, book_id);
            pstmt.setInt(2, numberOfCopies);

            pstmt.executeUpdate();

            pstmt.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            throw e;
        }
    }
}