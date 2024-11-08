package it.uniroma3.ingegneriadeidati.llmagent.lucenehw.model;

public class SearchResult {
    private String title;
    private String author;
    private String contentSnippet;

    // TODO: write Constructors, Getters and Setters
    
    // Costruttore senza argomenti
    public SearchResult() {
    }

    // Costruttore con tutti i campi
    public SearchResult(String title, String author, String contentSnippet) {
        this.title = title;
        this.author = author;
        this.contentSnippet = contentSnippet;
    }

    // Getter e Setter per 'title'
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    // Getter e Setter per 'author'
    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    // Getter e Setter per 'contentSnippet'
    public String getContentSnippet() {
        return contentSnippet;
    }

    public void setContentSnippet(String contentSnippet) {
        this.contentSnippet = contentSnippet;
    }

}
