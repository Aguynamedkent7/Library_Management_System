package models;

public class BorrowedBook {
    private int reference_id;
    private int book_copy_id;
    private String username;
    private String first_name;
    private String last_name;
    private String book_title;
    private String book_author;
    private String borrow_date;
    private String return_date;

    public BorrowedBook(int reference_id, int book_copy_id, String username, String first_name, String last_name,
                        String book_title, String book_author, String borrow_date, String return_date) {
        this.reference_id = reference_id;
        this.book_copy_id = book_copy_id;
        this.username = username;
        this.return_date = return_date;
        this.first_name = first_name;
        this.book_title = book_title;
        this.last_name = last_name;
        this.book_author = book_author;
        this.borrow_date = borrow_date;
    }

    public int getReferenceID() {
        return reference_id;
    }

    public void setReferenceID(int reference_id) {
        this.reference_id = reference_id;
    }

    public int getBookCopyID() {
        return book_copy_id;
    }

    public void setBookCopyID(int book_copy_id) {
        this.book_copy_id = book_copy_id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return first_name;
    }

    public void setFirstName(String first_name) {
        this.first_name = first_name;
    }

    public String getLastName() {
        return last_name;
    }

    public void setLastName(String last_name) {
        this.last_name = last_name;
    }

    public String getBookTitle() {
        return book_title;
    }

    public void setBookTitle(String book_title) {
        this.book_title = book_title;
    }

    public String getBookAuthor() {
        return book_author;
    }

    public void setBookAuthor(String book_author) {
        this.book_author = book_author;
    }

    public String getBorrowDate() {
        return borrow_date;
    }

    public void setBorrowDate(String borrow_date) {
        this.borrow_date = borrow_date;
    }

    public String getReturnDate() {
        return return_date;
    }

    public void setReturnDate(String return_date) {
        this.return_date = return_date;
    }
}
