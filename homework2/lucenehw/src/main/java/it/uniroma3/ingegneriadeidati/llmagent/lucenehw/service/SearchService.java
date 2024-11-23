package it.uniroma3.ingegneriadeidati.llmagent.lucenehw.service;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.uniroma3.ingegneriadeidati.llmagent.lucenehw.config.ResourceManager;
import it.uniroma3.ingegneriadeidati.llmagent.lucenehw.model.SearchResult;
import it.uniroma3.ingegneriadeidati.llmagent.lucenehw.model.SearchResultHTML;
import it.uniroma3.ingegneriadeidati.llmagent.lucenehw.model.SearchResultJSON;


/**
 * Seervizio per eseguire ricerche sull'indice Lucene.
 * La classe 'SearchService contiene la logica per elaborare la query di ricerca
 * e restituire i risultati corrispondenti'
 */
@Service
public class SearchService {

    private final static Logger logger = LoggerFactory.getLogger(SearchService.class);

    @Autowired
    private ResourceManager resourceManager;

    private String regex = "(\\d{4})\\.(\\d{5})";
    private Pattern pattern = Pattern.compile(regex);
    private String baseUrl = "https://ar5iv.labs.arxiv.org/html/";
   
    /**
     * Esegue una ricerca utilizzando la query fornita.
     * La query viene elaborata in base ai campi specifici (es. titolo, autore,
     * contenuto, abstract) e restituisce una lista di risultati.
     * 
     * @param query La query di ricerca inserita dall'utente
     * @return una lista di ogetti 'SearchResult' che rappresentano i documenti
     *         trovati che soddisfano i criteri di ricerca.
     */
    public List<SearchResult> search(String resourceType, String queryStr, int maxResults) throws IOException {        
        // Parse query based on field 'title','authors','content','abstract'
        
        Directory directory = this.resourceManager.getDirectory(resourceType);
        Analyzer analyzer = this.resourceManager.getAnalyzer(resourceType);
        String[] fields = this.resourceManager.getSearchFields(resourceType);

        List<SearchResult> results = new LinkedList<>();

        try(DirectoryReader reader = DirectoryReader.open(directory)) {
            IndexSearcher searcher = new IndexSearcher(reader);
            MultiFieldQueryParser queryParser = new MultiFieldQueryParser(fields, analyzer);

            Query query = queryParser.parse(queryStr);;
            
            TopDocs topDocs = searcher.search(query, maxResults); // Limitando la ricerca ai primi 10 risultati

            // Creazione degli oggetti SearchResult per ogni documento trovato
            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                Document doc = searcher.doc(scoreDoc.doc);
                Explanation explanation = searcher.explain(query, scoreDoc.doc);
                String matchField = getDominantField(explanation, fields);
                
                String filename = doc.get("filename");
                String link = generateLink(filename);
        
                SearchResult searchResult = createSearchResult(resourceType, doc, matchField, link, scoreDoc.doc);
                results.add(searchResult);
            }
        } catch (Exception e) {
            logger.error("Error during search for type '{}': {}", resourceType, e.getMessage());
        }
        return results;
    }


    // chat gpt generates good javadoc :3
    /**
     * Dynamically creates a search result object based on the resource type.
     * <p>
     * This method uses the specified resource type to instantiate the appropriate
     * subclass of {@link SearchResult}, populates its fields from the provided
     * Lucene {@link Document}, and returns the resulting object. Common fields
     * such as match field, link, and score are set on all search result types, 
     * while resource-specific fields are populated via the {@code populateFields}
     * method in the respective subclass.
     * </p>
     *
     * @param resourceType the type of the resource to search (e.g., "html", "json").
     *                     Must correspond to a valid resource type in the application.
     * @param doc the Lucene {@link Document} retrieved from the index.
     *            Contains the fields to populate the search result object.
     * @param matchField the field in the document that matched the query.
     *                   Used to indicate which field was most relevant.
     * @param link the link generated for the resource, usually based on the filename.
     * @param score the relevance score of the document, as computed by Lucene.
     * @return an instance of {@link SearchResult}, populated with data from the document.
     *         The specific type of {@link SearchResult} depends on the {@code resourceType}.
     * @throws IllegalArgumentException if the {@code resourceType} is unknown or unsupported.
     */
    private SearchResult createSearchResult(String resourceType, Document doc, String matchField, String link, float score) {
        SearchResult result;

        switch (resourceType) {
            case "html":
                result = new SearchResultHTML();
                break;

            case "json":
                result = new SearchResultJSON();
                break;

            default:
                throw new IllegalArgumentException("Unknown resource type: " + resourceType); 
        }

        result.setMatchField(matchField);
        result.setLink(link);
        result.setScore(score);
        
        result.populateFields(doc);

        return result;
    }

    private String generateLink(String filename) {
        Matcher matcher = pattern.matcher(filename);
   
        if(matcher.find()) {
            String numericCode = matcher.group(1) + "." + matcher.group(2);
            return baseUrl + numericCode;
        }
        return "Link not valid";
    }

    // Metodo per ottenere il campo con punteggio dominante dall'Explanation
    private String getDominantField(Explanation explanation, String[] fields) {
        for (String field : fields) {
            if (explanation.toString().contains(field)) {
                return field;
            }
        }
        return "N/A"; // Ritorna un valore di default se non è possibile determinare il campo
    }
}
