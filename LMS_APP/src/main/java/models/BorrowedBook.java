package models;

public class BorrowedBook {
    private int reference_id;
    private int book_copy_id;
    private String first_name;
    private String last_name;


    private String bookTitle;
    private String bookAuthor;
    private String borrow_date;
    private String return_date;

    public BorrowedBook(int reference_id, int book_copy_id, String first_name, String last_name,
                        String title, String author, String borrow_date, String return_date) {
        this.reference_id = reference_id;
        this.book_copy_id = book_copy_id;
        this.return_date = return_date;
        this.first_name = first_name;
        this.last_name = last_name;
        this.bookTitle = title;
        this.bookAuthor = author;
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

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public String getBookAuthor() {
        return bookAuthor;
    }

    public void setBookAuthor(String bookAuthor) {
        this.bookAuthor = bookAuthor;
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
