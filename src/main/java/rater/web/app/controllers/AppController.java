package rater.web.app.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import rater.web.app.classes.Breadcrumb;
import rater.web.app.classes.Project;
import rater.web.app.services.AppService;
import rater.web.app.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import static rater.web.app.RaterWebAppApplication.LOGGER;

@Controller
public class AppController {

    public final AppService appService;

    private static final String PROFESSOR = "professor";
    private static final String STUDENT = "student";
    private static final String INICIO = "Inicio";
    private static final String BREADCRUMB_LIST = "breadcrumb-list";
    private static final String BREADCRUMB_ACTIVE = "breadcrumb-active";

    @Value("${projects.path}")
    private String projectsPath;

    @Autowired
    public AppController(AppService appService) {
        this.appService = appService;
    }

    /**
     * Main page
     */
    @GetMapping("/")
    public String home(Model model){
        List<Project> projects = appService.getAllProjects();
        model.addAttribute("projects", projects);
        model.addAttribute("main", true);

        if (appService.userIsProfessor()) {
            model.addAttribute(PROFESSOR, true);
            model.addAttribute("new-project-btn", true);
        }
        else
            model.addAttribute(STUDENT, true);

        LinkedList<Breadcrumb> breadcrumbs = new LinkedList<>();
        breadcrumbs.add(new Breadcrumb(INICIO, "/"));
        model.addAttribute(BREADCRUMB_LIST, breadcrumbs);
        model.addAttribute(BREADCRUMB_ACTIVE, "");

        return "app";
    }

    /**
     * Project page
     * @param id reference project id
     */
    @GetMapping("/practica/{id}")
    public String projectOverview(@PathVariable long id, Model model){
        Project p = appService.getProjectById(id);
        model.addAttribute("project", p);

        if (appService.userIsProfessor()) {
            model.addAttribute(PROFESSOR, true);
            if (appService.globalReportAlreadyExists(p))
                model.addAttribute("view-report", true);
        }else {
            model.addAttribute(STUDENT, true);
            if (appService.reportAlreadyExists(p.getName())){
                model.addAttribute("view-report", true);
            }
        }

        //breadcrumb
        LinkedList<Breadcrumb> breadcrumbs = new LinkedList<>();
        breadcrumbs.add(new Breadcrumb(INICIO, "/"));
        model.addAttribute(BREADCRUMB_LIST, breadcrumbs);
        model.addAttribute(BREADCRUMB_ACTIVE, p.getName());
        return "app";
    }

    /**
     * New project form page
     */
    @GetMapping("/nueva-practica")
    public String newProject(Model model){
        model.addAttribute("new-project", true);

        if (appService.userIsProfessor())
            model.addAttribute(PROFESSOR, true);
        else
            model.addAttribute(STUDENT, true);

        //breadcrumb
        LinkedList<Breadcrumb> breadcrumbs = new LinkedList<>();
        breadcrumbs.add(new Breadcrumb(INICIO, "/"));
        model.addAttribute(BREADCRUMB_LIST, breadcrumbs);
        model.addAttribute(BREADCRUMB_ACTIVE, "Nueva Práctica");

        return "app";
    }

    /**
     * Upload new reference project file
     * @param project project
     * @param file new project file
     * @return redirects to main page
     */
    @PostMapping("/crear-practica")
    public String saveNewProject(Project project, @RequestParam("file") MultipartFile file){
        try {
            appService.createProject(project, file);
        } catch (InterruptedException e) {
            LOGGER.error(e.getMessage());
            Thread.currentThread().interrupt();
            return Utils.redirectTo("/");
        }
        return Utils.redirectTo("/");
    }

    /**
     * Upload new reference project file to update a existing project
     * @param id reference project id
     * @param file new project file
     * @return redirect to main page
     */
    @PostMapping("/updating-project/{id}")
    public String updatingProject(@PathVariable long id, @RequestParam("file") MultipartFile file){
        try {
            appService.updateProject(id, file);
        } catch (InterruptedException e) {
            LOGGER.error(e.getMessage());
            Thread.currentThread().interrupt();
            return Utils.redirectTo("/");
        }
        return Utils.redirectTo("/");
    }

    /**
     * Upload student project
     * @param id reference project ID
     * @param file student project file
     * @return redirect to report controller
     */
    @PostMapping(value = "/rate-project/{id}")
    public String rateStudentProject(@PathVariable long id, @RequestParam("file") MultipartFile file){
        String studentProjectId = "";
        try {
            studentProjectId = appService.uploadProject(id, file);
            if (studentProjectId!=null){
                return Utils.redirectTo("/report/" + id + "/" + studentProjectId);
            } else {
                return Utils.redirectTo("/practica/" + id);
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            return Utils.redirectTo("/practica/" + id);
        } catch (InterruptedException e) {
            LOGGER.error(e.getMessage());
            Thread.currentThread().interrupt();
            Utils.deleteDirectory(new File(projectsPath + "/" + studentProjectId));
            Utils.deleteFile(new File(projectsPath + "/" + studentProjectId + ".zip"));
            return Utils.redirectTo("/");
        }
    }

    /**
     * Upload a set of projects
     * @param id reference project ID
     * @param file set of projects file
     * @return redirect to report controller
     */
    @PostMapping(value = "/rate-all-projects/{id}")
    public String rateAllProjects(@PathVariable long id, @RequestParam("file") MultipartFile file){
        try {
            appService.uploadProjectSet(id, file);
            return Utils.redirectTo("/report-global/" + id);
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            return Utils.redirectTo("/practica/" + id);
        }
    }

    /**
     * Delete a existing project
     * @param id reference project id
     * @return redirects to main page
     */
    @GetMapping("/delete-project/{id}")
    public String deleteProject(@PathVariable long id){
        appService.deleteProjectById(id);
        return Utils.redirectTo("/");
    }

    /**
     * Update project page
     * @param id reference project id
     */
    @GetMapping("/actualizar-practica/{id}")
    public String updateProject(@PathVariable long id, Model model){
        model.addAttribute("update-project", true);
        model.addAttribute("id-project", id);

        if (appService.userIsProfessor())
            model.addAttribute(PROFESSOR, true);
        else
            model.addAttribute(STUDENT, true);

        //breadcrumb
        LinkedList<Breadcrumb> breadcrumbs = new LinkedList<>();
        breadcrumbs.add(new Breadcrumb(INICIO, "/"));
        model.addAttribute(BREADCRUMB_LIST, breadcrumbs);
        model.addAttribute(BREADCRUMB_ACTIVE, "Actualizar Práctica");

        return "app";
    }
}
