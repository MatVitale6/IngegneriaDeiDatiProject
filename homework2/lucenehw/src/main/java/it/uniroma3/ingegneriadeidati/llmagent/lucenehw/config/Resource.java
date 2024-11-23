package it.uniroma3.ingegneriadeidati.llmagent.lucenehw.config;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.store.Directory;

import it.uniroma3.ingegneriadeidati.llmagent.lucenehw.service.IIndexer;

public class Resource {
    private final IIndexer indexer;
    private final Analyzer analyzer;
    private final Directory directory;
    private final String[] searchFields;

    public Resource(IIndexer indexer, Analyzer analyzer, Directory directory, String[] searchFields) {
        this.indexer = indexer;
        this.analyzer = analyzer;
        this.directory = directory;
        this.searchFields = searchFields;
    }

    public IIndexer getIndexer() { return this.indexer; }
    public Analyzer getAnalyzer() { return this.analyzer; }
    public Directory getDirectory() { return this.directory; }
    public String[] getSearchFields() { return this.searchFields; }
}
