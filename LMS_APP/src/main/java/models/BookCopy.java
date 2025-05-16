package models;

public class BookCopy {
    private int copyID;
    private String title;
    private String author;
    private String genre;
    private String publisher;
    private String datePublished;
    private String status;

    public BookCopy(int id, String title, String author, String genre, String publisher, String published_date, String status) {
        this.copyID = id;
        this.author = author;
        this.title = title;
        this.genre = genre;
        this.publisher = publisher;
        this.datePublished = published_date;
        this.status = status;
    }

    public int getCopyID() {
        return copyID;
    }

    public void setCopyID(int copyID) {
        this.copyID = copyID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getDatePublished() {
        return datePublished;
    }

    public void setDatePublished(String datePublished) {
        this.datePublished = datePublished;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
