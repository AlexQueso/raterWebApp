package rater.web.app.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import rater.web.app.classes.Project;
import rater.web.app.services.AppService;
import rater.web.app.services.LoginService;

import java.util.List;

@Controller
public class AppController {

    public final AppService appService;

    @Autowired
    public AppController(AppService appService) {
        this.appService = appService;
    }

    @GetMapping("/")
    public String home(Model model){
        List<Project> projects = appService.homeOverview();
        model.addAttribute("projects", projects);
        if (userIsProfessor())
            model.addAttribute("professor", true);
        else
            model.addAttribute("student", true);
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

    public boolean userIsProfessor(){
        return appService.userIsProfessor();
    }
}
