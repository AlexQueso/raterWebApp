package rater.web.app.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import rater.web.app.classes.Breadcrumb;
import rater.web.app.classes.Project;
import rater.web.app.services.AppService;
import rater.web.app.utils.Utils;

import java.io.File;
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

        if (userIsProfessor()) {
            model.addAttribute("professor", true);
            model.addAttribute("new-project-btn", true);
        }
        else
            model.addAttribute("student", true);

        LinkedList<Breadcrumb> breadcrumbs = new LinkedList<>();
        breadcrumbs.add(new Breadcrumb("Inicio", "/"));
        model.addAttribute("breadcrumb-list", breadcrumbs);
        model.addAttribute("breadcrumb-active", "");

        return "app";
    }

    @GetMapping("/practica/{id}")
    public String projectOverview(@PathVariable long id, Model model){
        Project p = appService.getProjectById(id);
        model.addAttribute("project", p);

        if (userIsProfessor())
            model.addAttribute("professor", true);
        else
            model.addAttribute("student", true);

        //breadcrumb
        LinkedList<Breadcrumb> breadcrumbs = new LinkedList<>();
        breadcrumbs.add(new Breadcrumb("Inicio", "/"));
        model.addAttribute("breadcrumb-list", breadcrumbs);
        model.addAttribute("breadcrumb-active", p.getName());
        return "app";
    }

    @GetMapping("/nueva-practica")
    public String newProject(Model model){
        model.addAttribute("new-project", true);

        if (userIsProfessor())
            model.addAttribute("professor", true);
        else
            model.addAttribute("student", true);

        //breadcrumb
        LinkedList<Breadcrumb> breadcrumbs = new LinkedList<>();
        breadcrumbs.add(new Breadcrumb("Inicio", "/"));
        model.addAttribute("breadcrumb-list", breadcrumbs);
        model.addAttribute("breadcrumb-active", "Nueva Práctica");

        return "app";
    }

    @PostMapping("/crear-practica")
    public String saveNewProject(Project project){
        appService.saveProject(project);

        return Utils.redirectTo("/");
    }

    @PostMapping("/rate-project")
    public String rateStudentProject(File file){
        return "";
    }

    @PostMapping("/rate-all-projects")
    public String rateAllProjects(File file){

        return "";
    }

    @GetMapping("/delete-project/{id}")
    public String deleteProject(@PathVariable long id){
        appService.deleteProjectById(id);
        return Utils.redirectTo("/");
    }

    public boolean userIsProfessor(){
        return appService.userIsProfessor();
    }
}
