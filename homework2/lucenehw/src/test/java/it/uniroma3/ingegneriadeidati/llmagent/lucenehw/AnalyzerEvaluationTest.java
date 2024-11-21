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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import it.uniroma3.ingegneriadeidati.llmagent.lucenehw.model.SearchResultHTML;
import it.uniroma3.ingegneriadeidati.llmagent.lucenehw.service.SearchService;
import it.uniroma3.ingegneriadeidati.llmagent.lucenehw.utils.QueryResult;

import java.io.IOException;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SpringBootTest
public class AnalyzerEvaluationTest {
    private static final Logger logger = LoggerFactory.getLogger(AnalyzerEvaluationTest.class);
    private static final String QUERY_FILE_PATH = "src/test/resources/queries.txt";
    private static final String OUTPUT_DIR_PATH = "src/test/resources/results";

    @Autowired
    private SearchService searchService;

    @Test
    public void evaluateQueryScores() throws Exception {
        List<String> queries = loadQueries(QUERY_FILE_PATH);
        List<QueryResult> queryResults = new ArrayList<>();
        

        for (String queryStr : queries) {
            long startTime = System.nanoTime();
            List<SearchResultHTML> results = searchService.searchHTML(queryStr, 20);
            long endTime = System.nanoTime();
            long durationInMillis = (endTime - startTime) / 1_000_000; 
            List<SearchResultHTML> filteredResults = results.stream()
                .map(result -> {
                    result.setContentSnippet(null);  // Escludi lo snippet
                    result.setAbstract(null);        // Escludi l'abstract
                    return result;
                })
                .collect(Collectors.toList());

            double averageScore = filteredResults.stream().mapToDouble(SearchResultHTML::getScore).average().orElse(0);
            double scoreVariance = calculateVariance(filteredResults, averageScore);
            double scoreDecay = calculateScoreDecay(filteredResults);

            queryResults.add(new QueryResult(queryStr, durationInMillis, filteredResults.size(), averageScore, scoreVariance, scoreDecay, filteredResults));   
        }
        saveAsJSON(queryResults, OUTPUT_DIR_PATH);
    }

    private List<String> loadQueries(String filePath) throws Exception {
        return Files.lines(Paths.get(filePath))
                    .map(String::trim)
                    .filter(line -> !line.isEmpty())
                    .collect(Collectors.toList());
    }

    private double calculateVariance(List<SearchResultHTML> results, double averageScore) {
        if (results.isEmpty()) return 0;
        return results.stream()
                        .mapToDouble(result -> Math.pow(result.getScore() - averageScore, 2))
                        .average()
                        .orElse(0);
    }
    
    private double calculateScoreDecay(List<SearchResultHTML> results) {
        if (results.size() < 2) return 0;
        double maxScore = results.get(0).getScore();
        double minScore = results.get(results.size() - 1).getScore();
        return maxScore - minScore;
    }

    private void saveAsJSON(List<QueryResult> queryResults, String dirPath) throws IOException {
        String nextFileName = getNextFileName(dirPath, "query_results", ".json");
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.writeValue(new File(dirPath, nextFileName), queryResults);
    }

    private String getNextFileName(String dirPath, String baseName, String extension) {
        File dir = new File(dirPath);
        File[] files = dir.listFiles((d, name) -> name.matches(baseName + "\\d+" + extension));
        
        int maxNumber = 0;
        if (files != null) {
            Pattern pattern = Pattern.compile(baseName + "(\\d+)" + extension);
            for (File file : files) {
                Matcher matcher = pattern.matcher(file.getName());
                if (matcher.matches()) {
                    int number = Integer.parseInt(matcher.group(1));
                    maxNumber = Math.max(maxNumber, number);
                }
            }
        }
        return baseName + (maxNumber + 1) + extension;
    }

}
