package it.uniroma3.ingegneriadeidati.llmagent.lucenehw;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import it.uniroma3.ingegneriadeidati.llmagent.lucenehw.model.SearchResult;
import it.uniroma3.ingegneriadeidati.llmagent.lucenehw.service.SearchService;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SpringBootTest
public class AnalyzerEvaluationTest {
    private static final Logger logger = LoggerFactory.getLogger(AnalyzerEvaluationTest.class);
    private static final String QUERY_FILE_PATH = "src/test/resources/queries.txt";

    @Autowired
    private SearchService searchService;

    @Test
    public void evaluateQueryScores() throws Exception {
        List<String> queries = loadQueries(QUERY_FILE_PATH);
        logger.info(QUERY_FILE_PATH);

        for (String queryStr : queries) {
            List<SearchResult> results = searchService.search(queryStr, 20); 

            logger.info("Query: {}", queryStr);
            logger.info("Total Matches: {}", results.size());

            double averageScore = results.stream().mapToDouble(SearchResult::getScore).average().orElse(0);
            double scoreVariance = calculateVariance(results, averageScore);
            double scoreDecay = calculateScoreDecay(results);

            logger.info("Average Score: {}", averageScore);
            logger.info("Score Variance: {}", scoreVariance);
            logger.info("Score Decay: {}", scoreDecay);

            for (SearchResult result : results) {
                logger.info("Title: {}", result.getTitle());
                logger.info("Score: {} on Field: {}", result.getScore(), result.getMatchField());
            }
            logger.info("\n");
        }
    }

    private List<String> loadQueries(String filePath) throws Exception {
        return Files.lines(Paths.get(filePath))
                    .map(String::trim)
                    .filter(line -> !line.isEmpty())
                    .collect(Collectors.toList());
    }

    private double calculateVariance(List<SearchResult> results, double averageScore) {
        if (results.isEmpty()) return 0;
        return results.stream()
                        .mapToDouble(result -> Math.pow(result.getScore() - averageScore, 2))
                        .average()
                        .orElse(0);
    }
    
    private double calculateScoreDecay(List<SearchResult> results) {
        if (results.size() < 2) return 0;
        double maxScore = results.get(0).getScore();
        double minScore = results.get(results.size() - 1).getScore();
        return maxScore - minScore;
    }
}
