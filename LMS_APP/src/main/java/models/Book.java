package models;

public class Book {
    private int id;
    private String title;
    private String author;
    private String genre;
    private String publisher;
    private String published_date;
    private int available_copies;

    public Book (int id, String title, String author, String genre, String publisher, String published_date, int available_copies) {
        this.id = id;
        this.author = author;
        this.title = title;
        this.genre = genre;
        this.publisher = publisher;
        this.published_date = published_date;
        this.available_copies = available_copies;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getPublished_Date() {
        return published_date;
    }

    public void setPublished_Date(String published_date) {
        this.published_date = published_date;
    }

    public int getAvailableCopies() {
        return available_copies;
    }

    public void setAvailableCopies(int available_copies) {
        this.available_copies = available_copies;
    }
}

