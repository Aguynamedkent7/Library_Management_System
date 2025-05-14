package api;

import models.Account;
import models.Book;
import models.BorrowedBook;

import java.sql.*;
import java.util.ArrayList;
import java.util.Set;


public class Query {
    public static void main(String[] args) {
       String url = System.getenv("LMS_DB_URL");//TO FIX

       try {
           Connection conn = DriverManager.getConnection(url);

           if (!conn.isValid(10)) {
               throw new SQLException();
           }

       } catch (SQLException e) {
           System.out.println(e.getMessage());
       }

    }

    // search books by title, author, genre, publisher, or published_date
    public static ArrayList<Book> SearchBooks(Connection Conn, String SearchQuery, String SearchBy) {
        ArrayList<Book> BookResults = new ArrayList<>();
        try {
            final Set<String> VALID_SEARCH_COLUMNS = Set.of(
                    "title", "author", "genre"
            );
            if (!VALID_SEARCH_COLUMNS.contains(SearchBy)) {
                throw new IllegalArgumentException("Invalid column: " + SearchBy);
            }
            String query = "SELECT books.id, title, author, string_agg(DISTINCT genres.genre_name, ', ') AS genres, " +
                    "publisher_name, published_date, COUNT(DISTINCT book_copies.copy_id) FILTER (WHERE book_copies.status = 'AVAILABLE') " +
                    "AS available_copies " +
                    "FROM books " +
                    "LEFT JOIN book_copies ON book_copies.book_id = books.id " +
                    "LEFT JOIN genres_of_book ON books.id = genres_of_book.book_id " +
                    "LEFT JOIN genres ON genres_of_book.genre_id = genres.id " +
                    "LEFT JOIN publishers ON books.publisher_id = publishers.id " +
                    "GROUP BY books.id, title, author, publisher_name, published_date " +
                    "WHERE " + SearchBy + " = ?";
            PreparedStatement pstmt = Conn.prepareStatement(query);
            pstmt.setString(1, '%' + SearchQuery + '%');
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Book book = new Book (
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getString("genres"),
                        rs.getString("publisher_name"),
                        rs.getString("published_date"),
                        rs.getInt("available_copies")
                );
                BookResults.add(book);
            }
            rs.close();
            pstmt.close();
        } catch (SQLException | IllegalArgumentException e) {
            System.out.println(e.getMessage());
            return null;
        }
        return BookResults;
    }

    // search books by title, author, genre, publisher, or published_date
    public static ArrayList<Book> QueryAllAvailableBooks(Connection Conn) {
        ArrayList<Book> BookResults = new ArrayList<>();
        try {
            String query = "SELECT books.id, title, author, string_agg(DISTINCT genres.genre_name, ', ') AS genres, " +
                    "publisher_name, published_date, COUNT(DISTINCT book_copies.copy_id) FILTER (WHERE book_copies.status = 'AVAILABLE') " +
                    "AS available_copies " +
                    "FROM books " +
                    "LEFT JOIN book_copies ON book_copies.book_id = books.id " +
                    "LEFT JOIN genres_of_book ON books.id = genres_of_book.book_id " +
                    "LEFT JOIN genres ON genres_of_book.genre_id = genres.id " +
                    "LEFT JOIN publishers ON books.publisher_id = publishers.id " +
                    "GROUP BY books.id, title, author, publisher_name, published_date ";
            PreparedStatement pstmt = Conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Book book = new Book (
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getString("genres"),
                        rs.getString("publisher_name"),
                        rs.getString("published_date"),
                        rs.getInt("available_copies")
                );
                BookResults.add(book);
            }
            rs.close();
            pstmt.close();
        } catch (SQLException | IllegalArgumentException e) {
            System.out.println(e.getMessage());
            return null;
        }
        return BookResults;
    }

    public static ArrayList<String> QueryAllGenres(Connection conn) {
        ArrayList<String> genres = new ArrayList<>();
        try {
            String query = "SELECT DISTINCT genres.genre_name FROM genres";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                genres.add(rs.getString("genre_name"));
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }
        return genres;
    }

    public static ArrayList<Account> QueryAllAccounts(Connection conn) {
        ArrayList<Account> accounts = new ArrayList<>();

        try {
            String query = "SELECT id, username, first_name, last_name, role_name " +
                    "FROM accounts " +
                    "INNER JOIN roles ON accounts.role_id = roles.id";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                int id = rs.getInt("id");
                String username = rs.getString("username");
                String first_name = rs.getString("first_name");
                String last_name = rs.getString("last_name");
                String role_name = rs.getString("role_name");
                Account account = new Account(id, username, first_name, last_name, role_name);
                accounts.add(account);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return accounts;
    }

    public static Account QueryAccountByUsername(Connection conn, String username) {
        try {
            String first_name = null, last_name = null, role_name = null;
            int id = 0;
            String query = "SELECT accounts.id, username, first_name, last_name, role_name " +
                    "FROM accounts " +
                    "INNER JOIN roles ON accounts.role_id = roles.id " +
                    "WHERE username = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                id = rs.getInt("id");
                first_name = rs.getString("first_name");
                last_name = rs.getString("last_name");
                role_name = rs.getString("role_name");
            }
            return new Account(id, username, first_name, last_name, role_name);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public static ArrayList<BorrowedBook> QueryAllBookBorrowers(Connection conn) throws SQLException {
        ArrayList<BorrowedBook> borrowedBooks = new ArrayList<>();

        try {
            String query = "SELECT reference_id, book_copy_id, borrower_fname, borrower_lname, " +
                    "books.title, books.author, borrow_date, return_date " +
                    "FROM borrowed_books " +
                    "INNER JOIN book_copies ON borrowed_books.book_copy_id = book_copies.copy_id " +
                    "INNER JOIN books ON book_copies.book_id = books.id";
            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int reference_id = rs.getInt("reference_id");
                int book_copy_id = rs.getInt("book_copy_id");
                String first_name = rs.getString("borrower_fname");
                String last_name = rs.getString("borrower_lname");
                String title = rs.getString("title");
                String author = rs.getString("author");
                String borrow_date = rs.getDate("borrow_date").toString();
                String return_date = rs.getDate("return_date").toString();
                BorrowedBook borrowed_book_instance = new BorrowedBook(reference_id, book_copy_id,
                        first_name, last_name,
                        title, author, borrow_date, return_date);
                borrowedBooks.add(borrowed_book_instance);
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            throw e;
        }

        return borrowedBooks;
    }

}