package api;

import java.sql.*;

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
           Statement stmt = conn.createStatement();
           ResultSet rs = stmt.executeQuery("SELECT * FROM books");
           while (rs.next()) {
               int id = rs.getInt("id");
               String title = rs.getString("title");
               String author = rs.getString("author");
               String published_date = rs.getString("published_date");
               String publisher = rs.getString("publisher");
               String is_borrowed = rs.getString("is_borrowed");

               System.out.println("Book ID:" + id);
               System.out.println("Title: " + title);
               System.out.println("Author: " + author);
               System.out.println("Published Date: " + published_date);
               System.out.println("Publisher: " + publisher);
               System.out.println("Is Borrowed: " + is_borrowed);
           }
           rs.close();
           stmt.close();
           conn.close();
       } catch (SQLException e) {
           System.out.println("Failed to connect to database: " + e);
       }

    }
}