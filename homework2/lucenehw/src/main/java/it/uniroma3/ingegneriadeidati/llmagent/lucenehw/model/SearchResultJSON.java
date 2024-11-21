package it.uniroma3.ingegneriadeidati.llmagent.lucenehw.model;

public class SearchResultJSON {
    private String tableId;   // Identificativo della tabella
    private String tableHtml; // Contenuto HTML della tabella
    private String caption;
    private String matchField; // Campo che ha prodotto il match
    private float score;       // Punteggio di rilevanza
    private String link;


public SearchResultJSON() {

}


public SearchResultJSON(String tableId, String tableHtml, String caption, String matchField, float score, String link) {
    this.tableId = tableId;
    this.tableHtml = tableHtml;
    this.caption = caption;
    this.matchField = matchField;
    this.score = score;
    this.link = link;
}


    // Getter e Setter
    public String getTableId() {
        return tableId;
    }

    public void setTableId(String tableId) {
        this.tableId = tableId;
    }

    public String getTableHtml() {
        return tableHtml;
    }

    public void setTableHtml(String tableHtml) {
        this.tableHtml = tableHtml;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getMatchField() {
        return matchField;
    }

    public void setMatchField(String matchField) {
        this.matchField = matchField;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
}
