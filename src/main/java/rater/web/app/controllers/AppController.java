package rater.web.app.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class AppController {

    @GetMapping("/")
    public String home(Model model){
        return "app";
    }

    @GetMapping("/login")
    public String goToLogIn(Model model){
        return "";
    }

    @GetMapping("/practica/{id}")
    public String projectOverview(Model model){
        return "";
    }

    @GetMapping("/nueva-practica")
    public String newProject(Model model){
        return "";
    }
}
