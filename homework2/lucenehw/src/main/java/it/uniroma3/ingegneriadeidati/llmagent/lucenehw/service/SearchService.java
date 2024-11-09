package it.uniroma3.ingegneriadeidati.llmagent.lucenehw.service;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import it.uniroma3.ingegneriadeidati.llmagent.lucenehw.model.SearchResult;

/**
 * Seervizio per eseguire ricerche sull'indice Lucene.
 * La classe 'SearchService contiene la logica per elaborare la query di ricerca e restituire i risultati corrispondenti'
 */
@Service
public class SearchService {
    
     @Autowired
    private Directory directory;

    @Autowired
    private Analyzer analyzer;
    
    /**
 * Esegue una ricerca su un indice Lucene in base alla query di input.
 * 
 * La funzione analizza la query fornita dall'utente, ricerca i documenti nell'indice Lucene
 * e restituisce una lista di risultati che corrispondono ai criteri di ricerca.
 * Utilizza un IndexSearcher per cercare nei campi specifici ("content" nel nostro esempio) e
 * converte i documenti corrispondenti in oggetti SearchResult, che contengono dettagli
 * quali il titolo, l'autore e un frammento del contenuto.
 * 
 * @param queryStr La query di ricerca fornita dall'utente; rappresenta i termini di ricerca.
 *                 La query viene analizzata e applicata sul campo specificato (es. "content").
 * 
 * @return Una lista di oggetti SearchResult. Ogni SearchResult rappresenta un documento
 *         trovato nell'indice e contiene:
 *         - title: il titolo del documento, se disponibile.
 *         - author: il nome dell'autore o degli autori, se disponibile.
 *         - contentSnippet: una parte del contenuto del documento, solitamente i primi
 *           200 caratteri per dare un'anteprima del contenuto.
 * 
 * @throws IOException se c'è un problema nell'accesso o nella lettura dell'indice Lucene.
 * 
 * Esempio di utilizzo:
 * <pre>
 *     SearchService searchService = new SearchService();
 *     List<SearchResult> results = searchService.search("esempio di query");
 *     // results contiene i documenti che soddisfano i criteri di ricerca
 * </pre>
 */
    public List<SearchResult> search(String queryStr) {
        List<SearchResult> results = new ArrayList<>();

        try (DirectoryReader reader = DirectoryReader.open(directory)) {
            IndexSearcher searcher = new IndexSearcher(reader);

            // Analizziamo la query per il campo "content" o qualsiasi altro campo richiesto
            QueryParser parser = new QueryParser("content", analyzer);
            Query query = parser.parse(queryStr);

            TopDocs topDocs = searcher.search(query, 10); // Recuperiamo i primi 10 risultati

            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                Document doc = searcher.doc(scoreDoc.doc);

                // Creazione di SearchResult con i campi del documento
                SearchResult result = new SearchResult();
                result.setTitle(doc.get("title"));
                result.setAuthor(doc.get("authors"));
                result.setContentSnippet(doc.get("content").substring(0, Math.min(doc.get("content").length(), 200))); // snippet dei primi 200 caratteri

                results.add(result);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return results;
    }

}
