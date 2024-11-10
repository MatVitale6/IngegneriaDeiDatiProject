package it.uniroma3.ingegneriadeidati.llmagent.lucenehw.service;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.search.Query;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import it.uniroma3.ingegneriadeidati.llmagent.lucenehw.model.SearchResult;

/**
 * Seervizio per eseguire ricerche sull'indice Lucene.
 * La classe 'SearchService contiene la logica per elaborare la query di ricerca
 * e restituire i risultati corrispondenti'
 */
@Service
public class SearchService {

    @Autowired
    private Directory directory;

    @Autowired
    private Analyzer analyzer;

    /**
     * Esegue una ricerca utilizzando la query fornita.
     * La query viene elaborata in base ai campi specifici (es. titolo, autore,
     * contenuto, abstract) e restituisce una lista di risultati.
     * 
     * @param query La query di ricerca inserita dall'utente
     * @return una lista di ogetti 'SearchResult' che rappresentano i documenti
     *         trovati che soddisfano i criteri di ricerca.
     */
    public List<SearchResult> search(String queryStr) throws IOException {
        LinkedList<SearchResult> results = new LinkedList<SearchResult>();
        
        // Parse query based on field 'title','authors','content','abstract'
        DirectoryReader reader = DirectoryReader.open(directory);
        IndexSearcher searcher = new IndexSearcher(reader);
    
        // Parsing della query sui campi 'title', 'authors', 'content', 'abstract'
        String[] fields = { "title", "authors", "content", "abstract" };
        MultiFieldQueryParser queryParser = new MultiFieldQueryParser(fields, analyzer);
    
        Query query = null;
        try {
            query = queryParser.parse(queryStr); 
        } catch (Exception e) {
            System.err.println("Errore durante il parsing della query: " + queryStr);
            e.printStackTrace();
            return results; // Restituisce una lista vuota in caso di errore
        }
    
        TopDocs topDocs = searcher.search(query, 10); // Limitando la ricerca ai primi 10 risultati
    
        // Creazione degli oggetti SearchResult per ogni documento trovato
        for (int i = 0; i < topDocs.scoreDocs.length; i++) {
            int docId = topDocs.scoreDocs[i].doc;
            Document doc = searcher.doc(docId);

            Explanation explanation = searcher.explain(query, docId);
            String matchField = getDominantField(explanation, fields);
    
            SearchResult searchResult = new SearchResult();
            searchResult.setTitle(doc.get("title"));
            searchResult.setAuthor(doc.get("authors"));
            searchResult.setContentSnippet(doc.get("content"));
            searchResult.setAbstract(doc.get("abstract"));
            searchResult.setMatchField(matchField);
            results.add(searchResult);
        }
    
        reader.close();
        return results;
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
