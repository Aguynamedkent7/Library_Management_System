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

            // SAMPLE QUERY
           ArrayList<BorrowedBook> borrowedBooks = QueryBorrowedBooks(conn);
           if (borrowedBooks.isEmpty()) {
               System.out.println("Empty search results.");
           } else {
               for (BorrowedBook b : borrowedBooks) {
                   System.out.println("Reference ID: " + b.getReferenceID());
                   System.out.println("Borrower username: " + b.getUsername());
                   System.out.println("Borrower full name: " + b.getFirstName() + " " + b.getLastName());
                   System.out.println("Book title: " + b.getBookTitle());
                   System.out.println("Book author: " + b.getBookAuthor());
                   System.out.println("Borrow date: " + b.getBorrowDate());
                   System.out.println("Return date: " + b.getReturnDate());
                   System.out.println();
               }
           }

       } catch (SQLException e) {
           System.out.println(e.getMessage());
       }

    }

    // search books by title, author, genre, publisher, or published_date
    public static ArrayList<Book> SearchBooks(Connection Conn, String SearchQuery, String SearchBy) {
        ArrayList<Book> BookResults = new ArrayList<>();
        try {
            final Set<String> VALID_COLUMNS = Set.of(
                    "title", "author", "genre", "publisher", "published_date"
            );
            if (!VALID_COLUMNS.contains(SearchBy)) {
                throw new IllegalArgumentException("Invalid column: " + SearchBy);
            }
            String query = "SELECT " +
                    "  books.id, books.title, books.author, string_agg(genres.genre_name, ', ') AS genres," +
                    "  publisher_name, published_date, is_borrowed " +
                    "FROM books " +
                    "INNER JOIN genres_of_book ON books.id = genres_of_book.book_id " +
                    "INNER JOIN genres ON genres_of_book.genre_id = genres.id " +
                    "INNER JOIN publishers ON publishers.id = books.publisher_id " +
                    "WHERE " + SearchBy + " LIKE ? " +
                    "GROUP BY books.id, books.title, publisher_name, published_date " +
                    "ORDER BY books.id";
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
                        rs.getBoolean("is_borrowed")
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
    public static ArrayList<Book> QueryAllBooks(Connection Conn) {
        ArrayList<Book> BookResults = new ArrayList<>();
        try {
            String query = "SELECT " +
                    "  books.id, books.title, books.author, string_agg(genres.genre_name, ', ') AS genres," +
                    "  publisher_name, published_date, is_borrowed " +
                    "FROM books " +
                    "INNER JOIN genres_of_book ON books.id = genres_of_book.book_id " +
                    "INNER JOIN genres ON genres_of_book.genre_id = genres.id " +
                    "INNER JOIN publishers ON publishers.id = books.publisher_id " +
                    "GROUP BY books.id, books.title, publisher_name, published_date " +
                    "ORDER BY books.id";
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
                        rs.getBoolean("is_borrowed")
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

    public static ArrayList<BorrowedBook> QueryBorrowedBooks(Connection conn) throws SQLException {
        ArrayList<BorrowedBook> borrowedBooks = new ArrayList<>();

        try {
            String query = "SELECT reference_id, accounts.username, accounts.first_name, accounts.last_name, " +
                    "books.title, books.author, borrow_date, return_date " +
                    "FROM accounts INNER JOIN borrowed_books ON accounts.id = borrowed_books.account_id " +
                    "INNER JOIN books ON borrowed_books.book_id = books.id";
            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int reference_id = rs.getInt("reference_id");
                String username = rs.getString("username");
                String first_name = rs.getString("first_name");
                String last_name = rs.getString("last_name");
                String title = rs.getString("title");
                String author = rs.getString("author");
                String borrow_date = rs.getDate("borrow_date").toString();
                String return_date = rs.getDate("return_date").toString();
                BorrowedBook borrowed_book_instance = new BorrowedBook(reference_id, username,
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