package it.uniroma3.ingegneriadeidati.llmagent.lucenehw.controller;
import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import it.uniroma3.ingegneriadeidati.llmagent.lucenehw.config.ResourceManager;
import it.uniroma3.ingegneriadeidati.llmagent.lucenehw.model.SearchResult;
import it.uniroma3.ingegneriadeidati.llmagent.lucenehw.service.SearchService;


/**
 * REST Controller for handling search requests.
 * <p>
 * This controller provides endpoints for executing searches on indexed resources.
 * It supports dynamic handling of various resource types (e.g., "html", "json") 
 * and validates resource types based on the application's configuration.
 * </p>
 * 
 * <h4>Responsibilities:</h4>
 * <ul>
 *   <li>Processes incoming search requests with user-provided query strings.</li>
 *   <li>Validates resource types before performing a search.</li>
 *   <li>Communicates with the {@link SearchService} to fetch search results.</li>
 *   <li>Returns search results or appropriate HTTP error responses.</li>
 * </ul>
 * 
 * <h4>Dependencies:</h4>
 * <ul>
 *   <li>{@link SearchService}: Executes search queries on the specified resource type.</li>
 *   <li>{@link ResourceManager}: Validates resource types and retrieves resource-specific configurations.</li>
 * </ul>
 * 
 * <h4>Endpoints:</h4>
 * <ul>
 *   <li><strong>POST /search</strong>: Searches for content in the specified resource type and returns the results.</li>
 * </ul>
 * 
 * <p>This class is designed to be used as part of a Spring Boot REST API and integrates with the backend indexing and search system.</p>
 */
@RestController
public class SearchController {

    @Autowired
    private SearchService searchService;

    @Autowired
    private ResourceManager resourceManager;

    /**
     * Endpoint POST for performing a search.
     * <p>
     * Receives a query string and a resource type, and returns a list of results
     * that satisfy the query for the specified resource.
     * </p>
     *
     * @param resourceType the type of resource to search (e.g., "html", "json").
     * @param queryStr     the search query entered by the user.
     * @param resultCount  the maximum number of results to return.
     * @return a list of search result objects, dynamically typed based on the resource.
     */
    @PostMapping("/search")
    public ResponseEntity<?> searchArticles(
        @RequestParam("resourceType") String resourceType,
        @RequestParam("inputString") String queryStr,
        @RequestParam("resultCount") int resultCount) {
        
        if(!this.resourceManager.getRegisteredTypes().contains(resourceType)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid resource type: " + resourceType);
        }
        
        try {
            List<SearchResult> results = this.searchService.search(resourceType, queryStr, resultCount);
            return ResponseEntity.ok(results);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while performing the search");
        }
    }
}
