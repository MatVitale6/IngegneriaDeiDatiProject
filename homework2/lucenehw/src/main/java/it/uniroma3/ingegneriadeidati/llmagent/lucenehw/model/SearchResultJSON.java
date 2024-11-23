package it.uniroma3.ingegneriadeidati.llmagent.lucenehw.model;

import org.apache.lucene.document.Document;

public class SearchResultJSON extends SearchResult {
    private String tableId;   // Identificativo della tabella
    private String tableContent; // Contenuto HTML della tabella
    private String caption;
    private String footnotes;
    private String references;

    public String getTableId() { return tableId; }
    public void setTableId(String tableId) { this.tableId = tableId; }
    
    public String getTableContent() { return tableContent; }
    public void setTableContent(String tableHtml) { this.tableContent = tableHtml; }
    
    public String getCaption() { return caption; }
    public void setCaption(String caption) { this.caption = caption; }
    
    public String getFootnotes() { return footnotes; }
    public void setFootnotes(String footnotes) { this.footnotes = footnotes; }
    
    public String getReferences() { return references; }
    public void setReferences(String references) { this.references = references; }

    @Override
    public void populateFields(Document doc) {
        this.tableId = doc.get("tableId");
        this.tableContent = doc.get("tableContent");
        this.caption = doc.get("caption");
        this.footnotes = doc.get("footnotes");
        this.references = doc.get("references");
    }   
}
