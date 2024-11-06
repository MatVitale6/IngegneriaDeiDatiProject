package it.uniroma3.ingegneriadeidati.llmagent.lucenehw.service;
import java.util.LinkedList;
import java.util.List;
import org.springframework.stereotype.Service;
import it.uniroma3.ingegneriadeidati.llmagent.lucenehw.model.SearchResult;

@Service
public class SearchService {
    public List<SearchResult> search(String query) {
        // Parse query based on field (name, content)

        return new LinkedList<SearchResult>();
    }

}
