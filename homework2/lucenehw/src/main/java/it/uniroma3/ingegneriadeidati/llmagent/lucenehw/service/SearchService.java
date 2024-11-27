package it.uniroma3.ingegneriadeidati.llmagent.lucenehw.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
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
import org.apache.lucene.search.KnnFloatVectorQuery;
import org.apache.lucene.search.KnnVectorQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.MergedAnnotations.Search;
import org.springframework.stereotype.Service;

import it.uniroma3.ingegneriadeidati.llmagent.lucenehw.config.ResourceManager;
import it.uniroma3.ingegneriadeidati.llmagent.lucenehw.model.SearchResult;
import it.uniroma3.ingegneriadeidati.llmagent.lucenehw.model.SearchResultHTML;
import it.uniroma3.ingegneriadeidati.llmagent.lucenehw.model.SearchResultJSON;


/**
 * Service for performing search operations on the Lucene index.
 * <p>
 * This service handles the logic for querying the Lucene index and returning a list of search results
 * that match the provided query string. It supports multiple resource types (e.g., "html", "json") and
 * dynamically creates the appropriate {@link SearchResult} subclass for each resource type.
 * </p>
 * 
 * <h3>Responsibilities:</h3>
 * <ul>
 *   <li>Constructs and executes Lucene queries using resource-specific analyzers and fields.</li>
 *   <li>Retrieves and processes matching documents from the Lucene index.</li>
 *   <li>Generates resource-specific search result objects populated with relevant data.</li>
 *   <li>Logs detailed information about the search process, including query execution and results.</li>
 * </ul>
 *
 * <h3>Usage:</h3>
 * This service is used to execute search queries and return the corresponding results.
 * The results can be further processed or displayed to the user in the application frontend.
 * 
 * @see SearchResult
 * @see SearchResultHTML
 * @see SearchResultJSON
 */
@Service
public class SearchService {

    private final static Logger logger = LoggerFactory.getLogger(SearchService.class);

    @Autowired
    private ResourceManager resourceManager;

    @Autowired
    private Optional<EmbeddingServerService> embeddingServerService;

    private String regex = "(\\d{4})\\.(\\d{5})";
    private Pattern pattern = Pattern.compile(regex);
    private String baseUrl = "https://ar5iv.labs.arxiv.org/html/";
   
    /**
     * Executes a search on the Lucene index for the specified resource type.
     * <p>
     * The query string is parsed and matched against the fields of the given resource type
     * (e.g., "title", "authors", "content", "abstract"). The search results include
     * detailed information about each document, including its relevance score and the field
     * that most closely matched the query.
     * </p>
     *
     * @param resourceType the type of resource to search (e.g., "html", "json").
     * @param queryStr the search query entered by the user.
     * @param maxResults the maximum number of results to retrieve.
     * @return a list of {@link SearchResult} objects representing the matching documents.
     * @throws IOException if an error occurs while accessing the Lucene index.
     */
    public List<SearchResult> search(String resourceType, String queryStr, int maxResults) throws IOException {                
        Directory directory = this.resourceManager.getDirectory(resourceType);
        Analyzer analyzer = this.resourceManager.getAnalyzer(resourceType);
        String[] fields = this.resourceManager.getSearchFields(resourceType);

        List<SearchResult> results = new LinkedList<>();

        try(DirectoryReader reader = DirectoryReader.open(directory)) {
            IndexSearcher searcher = new IndexSearcher(reader);
            MultiFieldQueryParser queryParser = new MultiFieldQueryParser(fields, analyzer);

            Query textQuery = queryParser.parse(queryStr);
            TopDocs textTopDocs = searcher.search(textQuery, maxResults); // Limitando la ricerca ai primi 10 risultati
            
            KnnFloatVectorQuery knnQuery = null;
            TopDocs knnTopDocs = null;

            if (this.embeddingServerService.isPresent()) {
                float[] queryEmbedding = embeddingServerService.get().computeEmbedding(queryStr);
                knnQuery = new KnnFloatVectorQuery("captoin_vector", queryEmbedding, maxResults);
                knnTopDocs = searcher.search(knnQuery, maxResults);
            }

            results = mergeResults(searcher, textTopDocs, textQuery, knnTopDocs, knnQuery, resourceType, fields, maxResults);

        } catch (Exception e) {
            logger.error("Error during search for type '{}': {}", resourceType, e.getMessage());
        }
        return results;
    }

    private List<SearchResult> mergeResults(IndexSearcher searcher, TopDocs textTopDocs, Query textQuery, TopDocs knnTopDocs, KnnFloatVectorQuery knnQuery, String resourceType, String[] fields, int maxResults) {
        List<SearchResult>  results = new ArrayList<>();
        List<ScoreDoc> allDocs = new ArrayList<>();

        float weight = 0.5f;

        if (textTopDocs != null) {
            for (ScoreDoc scoreDoc : textTopDocs.scoreDocs) {
                scoreDoc.score *= weight;
                allDocs.add(scoreDoc);
            }
        }
    
        // Add KNN-based results
        if (knnTopDocs != null) {
            for (ScoreDoc scoreDoc : knnTopDocs.scoreDocs) {
                scoreDoc.score *= (1 - weight);
                allDocs.add(scoreDoc);
            }
        }

        allDocs.stream()
            .distinct()
            .sorted((d1,d2) -> Float.compare(d2.score, d1.score))
            .limit(maxResults)
            .forEach(scoreDoc -> {
                try {
                    Document doc = searcher.doc(scoreDoc.doc);

                    Explanation explanation;
                    if (textTopDocs != null && containsDoc(textTopDocs, scoreDoc.doc)) {
                        explanation = searcher.explain(textQuery, scoreDoc.doc); 
                    } else if (knnTopDocs != null && containsDoc(knnTopDocs, scoreDoc.doc)) {
                        explanation = searcher.explain(knnQuery, scoreDoc.doc);
                    } else {
                        explanation = Explanation.noMatch("No matching query explanation available");
                    }

                    String matchField = getDominantField(explanation, fields);
                    String filename = doc.get("filename");
                    String link = generateLink(filename);

                    SearchResult result = createSearchResult(resourceType, doc, matchField, link, scoreDoc.score);
                    results.add(result);
                } catch (IOException e) {
                    logger.error("Error merging results: {}", e);
                }
            });
        return results;
    }

    private boolean containsDoc(TopDocs topDocs, int docId) {
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            if (scoreDoc.doc == docId) {
                return true;
            }
        }
        return false;
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

    public boolean shouldPerformEmbeddingSearch() {
        return embeddingServerService.isPresent();
    }
}
