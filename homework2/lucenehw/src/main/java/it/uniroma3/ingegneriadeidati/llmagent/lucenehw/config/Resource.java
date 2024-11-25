package it.uniroma3.ingegneriadeidati.llmagent.lucenehw.config;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.store.Directory;

import it.uniroma3.ingegneriadeidati.llmagent.lucenehw.service.IIndexer;

/**
 * Represents a resource in the Lucene indexing and search system.
 * 
 * <p>This class encapsulates the components required for managing a resource
 * such as the indexer, analyzer, directory, and search fields. It ensures a
 * unified and modular way to handle resource-specific configurations.</p>
 * 
 * <p>A resource typically corresponds to a specific type of data (e.g., "HTML",
 * "JSON") and includes the following components:</p>
 * <ul>
 *   <li>{@link IIndexer}: The indexer responsible for processing and indexing the data.</li>
 *   <li>{@link Analyzer}: The analyzer used for tokenizing and analyzing the data for search.</li>
 *   <li>{@link Directory}: The Lucene directory where the indexed data is stored.</li>
 *   <li>Search Fields: An array of fields used for searching within this resource.</li>
 * </ul>
 * 
 * <p>This class is immutable and ensures that the components of a resource
 * remain consistent once initialized.</p>
 */
public class Resource {
    private final IIndexer indexer;
    private final Analyzer analyzer;
    private final Directory directory;
    private final String[] searchFields;

    /**
     * Constructs a {@code Resource} object with the specified components.
     *
     * @param indexer       the {@link IIndexer} responsible for indexing the data
     * @param analyzer      the {@link Analyzer} used for analyzing the data
     * @param directory     the {@link Directory} where the indexed data is stored
     * @param searchFields  an array of field names used for searching within this resource
     */
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
