package it.uniroma3.ingegneriadeidati.llmagent.lucenehw.model;

import org.apache.lucene.document.Document;

/**
 * Represents a generic search result from a Lucene index.
 * <p>
 * This abstract class serves as a base for all specific search result types, 
 * providing common fields and methods shared across different resource types 
 * (e.g., HTML or JSON resources). It defines fields for the matched field, 
 * a link to the resource, and the search relevance score.
 * </p>
 * 
 * <h3>Responsibilities:</h3>
 * <ul>
 *   <li>Encapsulates metadata about the search result, such as the field matched, 
 *   the link to the resource, and the relevance score.</li>
 *   <li>Defines an abstract method {@link #populateFields(Document)} to allow 
 *   subclasses to extract and populate their specific fields from a Lucene {@link Document}.</li>
 * </ul>
 * 
 * <h3>Usage:</h3>
 * <p>
 * This class is intended to be extended by resource-specific search result implementations. 
 * Each subclass will implement the {@code populateFields} method to populate fields 
 * relevant to its resource type.
 * </p>
 */
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
