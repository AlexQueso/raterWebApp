package rater.web.app.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import rater.web.app.classes.Project;
import rater.web.app.services.AppService;

import java.util.LinkedList;
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
        List<Project> projects = appService.getAllProjects();
        model.addAttribute("projects", projects);
        model.addAttribute("main", true);

        if (userIsProfessor())
            model.addAttribute("professor", true);
        else
            model.addAttribute("student", true);

        //breadcrumb
        Breadcrumb inicio = new Breadcrumb("Inicio", "/");
        model.addAttribute("breadcrumb-list", false);
        model.addAttribute("breadcrumb-active", inicio);

        return "app";
    }

    @GetMapping("/login")
    public String goToLogIn(Model model){
        model.addAttribute("login", true);

        //breadcrumb
        Breadcrumb login = new Breadcrumb("Iniciar Sesión", "/");
        model.addAttribute("breadcrumb-list", false);
        model.addAttribute("breadcrumb-active", login);

        return "app";
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



    private class Breadcrumb {
        private String tag;
        private String href;

        public Breadcrumb(String tag, String href) {
            this.tag = tag;
            this.href = href;
        }

        public String getTag() {
            return tag;
        }

        public void setTag(String tag) {
            this.tag = tag;
        }

        public String getHref() {
            return href;
        }

        public void setHref(String href) {
            this.href = href;
        }
    }
}
