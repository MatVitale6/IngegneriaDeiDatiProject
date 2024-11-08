package it.uniroma3.ingegneriadeidati.llmagent.lucenehw.model;

/**
 * Modello per rappresentare un risultato di ricerca.
 * La classe 'SearchResult' contiene i campi essenziali per descrivere un documento trovato tramite la ricerca:
 * - titolo 
 * - autore
 * - estratto del contenuto
 */
public class SearchResult {
    private String title;
    private String author;
    private String contentSnippet;

    
     /**
     * Costruttore senza argomenti per la classe `SearchResult`.
     * Necessario per la deserializzazione e per framework che richiedono un costruttore vuoto.
     */
    public SearchResult() {
    }

    /**
     * Costruttore con tutti i campi per inizializzare un ogetto 'SearchResult'.
     * @param title il titolo del documento
     * @param author l'autore del documento
     * @param contentSnippet l'estratto del contenuto rilevante del docuemnto
     */
    public SearchResult(String title, String author, String contentSnippet) {
        this.title = title;
        this.author = author;
        this.contentSnippet = contentSnippet;
    }

    /**
     * restituisce il titolo del documento trovato
     * @return il titolo del documento
     */
    public String getTitle() {
        return title;
    }

    /**
     * Imposta il titolo del documento trovato
     * @param title il titolo del documento
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Restituisce l'autore del documento trovato.
     * @return l'autore del documento.
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Imposta l'autore del documento trovato.
     * @param author l'autore del documento.
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * Restituisce un estratto del contenuto del documento trovato.
     * @return un snippet di contenuto rilevante.
     */
    public String getContentSnippet() {
        return contentSnippet;
    }

    /**
     * Imposta un estratto del contenuto del documento trovato.
     * @param contentSnippet lo snippet di contenuto.
     */
    public void setContentSnippet(String contentSnippet) {
        this.contentSnippet = contentSnippet;
    }

}
