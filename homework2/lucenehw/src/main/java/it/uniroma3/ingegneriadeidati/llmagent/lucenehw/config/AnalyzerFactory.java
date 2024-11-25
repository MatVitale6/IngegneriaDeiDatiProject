package it.uniroma3.ingegneriadeidati.llmagent.lucenehw.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory class for creating and configuring {@link Analyzer} instances for different resource types.
 * 
 * <p>An {@code Analyzer} is responsible for tokenizing and analyzing text fields during
 * the indexing and search processes in Apache Lucene. This class provides specific analyzers
 * for "html" and "json" resource types, and uses {@link PerFieldAnalyzerWrapper} to allow
 * field-specific analyzer configurations.</p>
 * 
 * <p>Features:</p>
 * <ul>
 *   <li>Provides HTML-specific analyzers with customized field configurations.</li>
 *   <li>Provides JSON-specific analyzers with customized field configurations.</li>
 *   <li>Logs the configuration of each analyzer for debugging purposes.</li>
 * </ul>
 * 
 * <p>If an unknown resource type is requested, an {@link IllegalArgumentException} is thrown.</p>
 */
public class AnalyzerFactory {

    private static Logger logger = LoggerFactory.getLogger(AnalyzerFactory.class);

    public static Analyzer getAnalyzer(String type) {
        if ("html".equalsIgnoreCase(type)) {
            return createHTMLAnalyzer();
        } else if ("json".equalsIgnoreCase(type)) {
            return createJSONAnalyzer();
        }

        throw new IllegalArgumentException("Unknown Analyzer type: " + type);
    }

    private static Analyzer createHTMLAnalyzer() {
        Map<String, Analyzer> perFieldAnalyzer = new HashMap<>();
        perFieldAnalyzer.put("title", new KeywordAnalyzer());
        perFieldAnalyzer.put("authors", new WhitespaceAnalyzer());
        perFieldAnalyzer.put("content", new EnglishAnalyzer());
        perFieldAnalyzer.put("abstract", new EnglishAnalyzer());
        PerFieldAnalyzerWrapper perFieldAnalyzerWrapper = new PerFieldAnalyzerWrapper(new StandardAnalyzer(), perFieldAnalyzer);

        logger.info("Analyzer HTML configuration:");
        perFieldAnalyzer.forEach((field, fieldAnalyzer) -> 
            logger.info("Field '{}', Analyzer '{}'", field, fieldAnalyzer.getClass().getName())
        );

        return perFieldAnalyzerWrapper;
    }

    private static Analyzer createJSONAnalyzer() {
        // Mappa per definire analizzatori specifici per ogni campo
        Map<String, Analyzer> perFieldAnalyzer = new HashMap<>();

        perFieldAnalyzer.put("caption", new EnglishAnalyzer()); 
        perFieldAnalyzer.put("table", new StandardAnalyzer()); 
        perFieldAnalyzer.put("footnotes", new WhitespaceAnalyzer());
        perFieldAnalyzer.put("references", new EnglishAnalyzer()); 
        PerFieldAnalyzerWrapper perFieldAnalyzerWrapper = new PerFieldAnalyzerWrapper(new StandardAnalyzer(), perFieldAnalyzer);

        logger.info("Analyzer JSON configuration:");
        perFieldAnalyzer.forEach((field, fieldAnalyzer) -> 
            logger.info("Field '{}', Analyzer '{}'", field, fieldAnalyzer.getClass().getName())
        );

        return perFieldAnalyzerWrapper;
    }
}
