package it.uniroma3.ingegneriadeidati.llmagent.lucenehw.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import it.uniroma3.ingegneriadeidati.llmagent.lucenehw.service.ProgressService;

@Controller
public class IndexerController {

    @Autowired
    private ProgressService progressService;

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
}
