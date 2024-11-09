package it.uniroma3.ingegneriadeidati.llmagent.lucenehw.controller;
import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import it.uniroma3.ingegneriadeidati.llmagent.lucenehw.model.SearchResult;
import it.uniroma3.ingegneriadeidati.llmagent.lucenehw.service.SearchService;


/**
 * Controller REST per la gestione delle richieste di ricerca.
 * Questa classe espone un endpoint per effettuare una ricerca basata su una query fornita dall'utente.
 */
@RestController
public class SearchController {

    /**
     * Servizio di ricerca utilizzato per eseguire le operazioni di ricerca.
     * Il bean 'SearchService' viene iniettato automaticamente da Spring tramite '@Autowired'
     */
    @Autowired
    private SearchService searchService;

    /**
     * Endpoint POST per eseguire una ricerca.
     * Riceve una query di ricerca come parametro e restituisce una lista di risultati che soddisfano la query.
     * @param query la stringa id ricerca inserita dall'utente.
     * @return una lista di ogetti 'SearchResult' che rappresentano i documenti trovati.
     * @throws IOException 
     */
    @PostMapping("/search")
    public List<SearchResult> search(@RequestParam("inputString") String query) throws IOException {
        List<SearchResult> results = searchService.search(query);
        return results;
    }
}
