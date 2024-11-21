package it.uniroma3.ingegneriadeidati.llmagent.lucenehw.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import it.uniroma3.ingegneriadeidati.llmagent.lucenehw.service.JsonIndexer;
import it.uniroma3.ingegneriadeidati.llmagent.lucenehw.service.ProgressService;

@Controller
public class IndexerController {

    @Autowired
    private ProgressService progressService;

    @Autowired
    private JsonIndexer jsonIndexer;

    @GetMapping("/")
    public String index(Model model) {
        boolean indexingProcess = progressService.getProgress() < 100;
        model.addAttribute("indexingProcess", indexingProcess);
        return "index";
    }

    @GetMapping("/indexing/progress") 
    @ResponseBody
    public int getProgress() {
        return progressService.getProgress();
    }

    @GetMapping("/index/json")
    @ResponseBody
    public String startJsonIndexing() {
        if (progressService.getProgress() < 100) {
            return "Indexing already in progress. Please wait.";
        }
        new Thread(jsonIndexer::run).start(); // Esecuzione in un thread separato per non bloccare il server.
        return "JSON indexing started successfully!";
    }
}
