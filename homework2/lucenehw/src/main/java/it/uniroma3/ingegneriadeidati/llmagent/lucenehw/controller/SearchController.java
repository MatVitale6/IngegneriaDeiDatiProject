package it.uniroma3.ingegneriadeidati.llmagent.lucenehw.controller;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import it.uniroma3.ingegneriadeidati.llmagent.lucenehw.model.SearchResult;
import it.uniroma3.ingegneriadeidati.llmagent.lucenehw.service.SearchService;

@RestController
public class SearchController {
    @Autowired
    private SearchService searchService;

    @PostMapping("/search")
    public List<SearchResult> search(@RequestParam("inputString") String query) {
        List<SearchResult> results = searchService.search(query);
        return results;
    }
}
