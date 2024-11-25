package it.uniroma3.ingegneriadeidati.llmagent.lucenehw.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import it.uniroma3.ingegneriadeidati.llmagent.lucenehw.service.ProgressService;

/**
 * Controller responsible for managing HTTP requests related to the indexing process.
 * 
 * <p>This controller provides endpoints to display the indexing status and progress,
 * ensuring that the frontend can dynamically update based on the backend state.</p>
 * 
 * <p>Features:</p>
 * <ul>
 *   <li>Serves the home page with the current indexing status.</li>
 *   <li>Exposes an endpoint to retrieve the progress of the indexing process dynamically.</li>
 * </ul>
 * 
 * <p>Dependencies:</p>
 * <ul>
 *   <li>{@link ProgressService}: Provides the current progress and the resource type being indexed.</li>
 * </ul>
 * 
 * <p>This controller is designed to integrate seamlessly with Thymeleaf templates for the frontend.</p>
 */
@Controller
public class IndexerController {

    @Autowired
    private ProgressService progressService;

    @GetMapping("/")
    public String index(Model model) {
        boolean indexingProcess = this.progressService.getProgress() < 100;
        model.addAttribute("indexingProcess", indexingProcess);
        model.addAttribute("currentResourceType", this.progressService.getCurrentResourceType());
        return "index";
    }

    @GetMapping("/indexing/progress") 
    @ResponseBody
    public int getProgress() {
        return this.progressService.getProgress();
    }
}
