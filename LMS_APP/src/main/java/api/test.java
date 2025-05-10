package api;

import models.Book;

import java.sql.*;
import java.util.ArrayList;
import java.util.Set;


public class test {
    public static void main(String[] args) {
        // SET ENVIRONMENT VARIABLE AND NAME IT "LMS_PASS"
       String password = System.getenv("LMS_PASS");
       String url = "jdbc:postgresql://db.fhvoxrqibepnmtraenrd.supabase.co:5432/postgres?user=postgres&password=%s";
       url = String.format(url, password);

       try {
           Connection conn = DriverManager.getConnection(url);

           if (!conn.isValid(10)) {
               throw new SQLException();
           }

            // SAMPLE QUERY
           ArrayList<Book> Books = searchBooks(conn, "Bl", "; DROP TABLE books; --");

           if (Books != null) {
               for (Book book : Books) {
                   System.out.println("Book " + book.getId());
                   System.out.println("Title: " + book.getTitle());
                   System.out.println("Author: " + book.getAuthor());
                   System.out.println("Genre/s: " + book.getGenre());
                   System.out.println("Publisher: " + book.getPublisher());
                   System.out.println("Published Date: " + book.getPublished_Date());
                   System.out.println("Is Borrowed? " + book.isBorrowed());
                   System.out.println();
               }
           } else {
               System.out.println("Empty search results.");
           }

       } catch (SQLException e) {
           System.out.println(e.getMessage());
       }

    }

    public static ArrayList<Book> searchBooks(Connection Conn, String SearchQuery, String SearchBy) {
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
                    "GROUP BY books.id, books.title, publisher_name, published_date";
            PreparedStatement pstmt = Conn.prepareStatement(query);
//            pstmt.setString(1, SearchBy);
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
}