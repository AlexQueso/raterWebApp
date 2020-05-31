package rater.web.app.controllers;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import rater.web.app.classes.Breadcrumb;
import rater.web.app.classes.Project;
import rater.web.app.classes.Report;
import rater.web.app.services.AppService;
import rater.web.app.services.ReportService;

import java.util.LinkedList;

@Controller
public class ReportController {

    public final ReportService reportService;
    public final AppService appService;

    @Autowired
    public ReportController(ReportService reportService, AppService appService) {
        this.reportService = reportService;
        this.appService = appService;
    }

    @GetMapping("/report/{idReference}/{idProject}")
    public String reportStudent(Model model, @PathVariable long idReference, @PathVariable String idProject){

        Project p = appService.getProjectById(idReference);
        JSONObject jsonReport = reportService.rateStudentProject(idReference, idProject);
        Report report = reportService.processJsonIndividualProject(jsonReport);
        reportService.saveReportUserSession(p, report);
        reportService.fillModelwithStudentRepor(model, report);

        if (appService.userIsProfessor()) {
            model.addAttribute("professor", true);
            model.addAttribute("new-project-btn", true);
        }
        else
            model.addAttribute("student", true);

        //breadcrumb
        LinkedList<Breadcrumb> breadcrumbs = new LinkedList<>();
        breadcrumbs.add(new Breadcrumb("Inicio", "/"));
        breadcrumbs.add(new Breadcrumb(p.getName(), "/practica/" +idReference));
        model.addAttribute("breadcrumb-list", breadcrumbs);
        model.addAttribute("breadcrumb-active", "Corrección");

        return "report";
    }

    @GetMapping("/report/{idReference}")
    public String reviewReport(Model model, @PathVariable long idReference){
        Project p = appService.getProjectById(idReference);
        Report report = reportService.getStoredReport(p, idReference);
        reportService.fillModelwithStudentRepor(model, report);

        if (appService.userIsProfessor()) {
            model.addAttribute("professor", true);
            model.addAttribute("new-project-btn", true);
        }
        else
            model.addAttribute("student", true);
        
        //breadcrumb
        LinkedList<Breadcrumb> breadcrumbs = new LinkedList<>();
        breadcrumbs.add(new Breadcrumb("Inicio", "/"));
        breadcrumbs.add(new Breadcrumb(p.getName(), "/practica/" +idReference));
        model.addAttribute("breadcrumb-list", breadcrumbs);
        model.addAttribute("breadcrumb-active", "Corrección");

        return "report";
    }

}
