package it.uniroma3.ingegneriadeidati.llmagent.lucenehw.model;

import org.apache.lucene.document.Document;

public abstract class SearchResult {
    private String matchField;
    private String link;
    private float score;

    public String getMatchField() {
        return matchField;
    }
    public void setMatchField(String matchField) {
        this.matchField = matchField;
    }
    public String getLink() {
        return link;
    }
    public void setLink(String link) {
        this.link = link;
    }
    public float getScore() {
        return score;
    }
    public void setScore(float score) {
        this.score = score;
    }

    // Abstract method to populate the specific fields of the subclass
    public abstract void populateFields(Document doc);
}
