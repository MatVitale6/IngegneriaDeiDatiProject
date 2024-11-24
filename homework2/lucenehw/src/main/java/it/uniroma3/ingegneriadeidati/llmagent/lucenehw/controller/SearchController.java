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
 * This class exposes an endpoint to perform searches on indexed resources.
 * It dynamically handles different resource types (e.g., "html", "json").
 * </p>
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
        @RequestParam(value = "resourceType", defaultValue =  "json") String resourceType,
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
