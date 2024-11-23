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

        logger.info("Analyzer configuration:");
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
