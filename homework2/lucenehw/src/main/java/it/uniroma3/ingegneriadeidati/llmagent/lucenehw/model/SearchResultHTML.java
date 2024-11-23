package it.uniroma3.ingegneriadeidati.llmagent.lucenehw.model;

import org.apache.lucene.document.Document;

/**
 * Modello per rappresentare un risultato di ricerca.
 * La classe 'SearchResult' contiene i campi essenziali per descrivere un documento trovato tramite la ricerca:
 * - titolo 
 * - autore
 * - estratto del contenuto
 * - abstract 
 */
public class SearchResultHTML extends SearchResult {
    private String title;
    private String authors;
    private String contentSnippet;
    private String abstractText;

    @Override
    public void populateFields(Document doc) {
        this.title = doc.get("title");
        this.authors = doc.get("authors");
        this.contentSnippet = doc.get("content");
        this.abstractText = doc.get("abstract");
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return authors; }
    public void setAuthor(String authors) { this.authors = authors; }

    public String getContentSnippet() { return contentSnippet; }
    public void setContentSnippet(String contentSnippet) { this.contentSnippet = contentSnippet; }

    public String getAbstract() { return abstractText; }
    public void setAbstract(String abstractText) { this.abstractText = abstractText; }


}
