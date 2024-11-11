package it.uniroma3.ingegneriadeidati.llmagent.lucenehw;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import it.uniroma3.ingegneriadeidati.llmagent.lucenehw.model.SearchResult;
import it.uniroma3.ingegneriadeidati.llmagent.lucenehw.service.SearchService;

import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class AnalyzerEvaluationTest {

    @Autowired
    private SearchService searchService;

    private final String[] queries = {"Large Language Model", "With the advancement of Large Language Models (LLMs), many researchers have employed LLMs as the ESC models"};
    private final String[][] expectedRelevantTitles = {
        {"ESC-Eval: Evaluating Emotion Support Conversations in Large Language Models"},
        {"ESC-Eval: Evaluating Emotion Support Conversations in Large Language Models"}
    };

    @Test
    public void testQueryMatchingUsingSearchService() throws Exception {
        for (int i = 0; i < queries.length; i++) {
            String queryStr = queries[i];
            List<SearchResult> results = searchService.search(queryStr);

            int relevantCount = 0;
            int totalHits = results.size();
            double averageScore = 0;

            System.out.println("Query: " + queryStr);
            System.out.println("Total Matches: " + totalHits);

            for (SearchResult result : results) {
                System.out.println("Title: " + result.getTitle());
                System.out.println("Score: " + result.getScore());  // Assuming SearchResult has a score attribute
                averageScore += result.getScore();  // Collect score for average calculation

                // Check if the result is in the expected relevant documents
                for (String expectedTitle : expectedRelevantTitles[i]) {
                    if (result.getTitle().equalsIgnoreCase(expectedTitle)) {
                        relevantCount++;
                        break;
                    }
                }
            }

            // Calculate average score
            averageScore = totalHits > 0 ? averageScore / totalHits : 0;
            System.out.println("Average Score: " + averageScore);
            System.out.println("Relevant Matches: " + relevantCount);

            // Calculate and print precision and recall
            double precision = totalHits > 0 ? (double) relevantCount / totalHits : 0;
            double recall = (double) relevantCount / expectedRelevantTitles[i].length;

            System.out.println("Precision: " + precision);
            System.out.println("Recall: " + recall);
            System.out.println("----");

            // Basic assertions
            assertTrue(relevantCount > 0, "Expected at least one relevant match for query: " + queryStr);
            assertTrue(precision > 0, "Expected non-zero precision for query: " + queryStr);
            assertTrue(recall > 0, "Expected non-zero recall for query: " + queryStr);
        }
    }
}
