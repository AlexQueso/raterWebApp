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
import rater.web.app.utils.Utils;

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

        model.addAttribute("individual-report", true);
        //header
        model.addAttribute("project-name", report.getProjectName());
        model.addAttribute("date", report.getDate());
        //build
        if (report.getBuild().contains("SUCCESSFUL"))
            model.addAttribute("build-success", "success");
        else
            model.addAttribute("build-success", "danger");
        model.addAttribute("build", report.getBuild());
        //tests
        model.addAttribute("tests", report.getTests());

        //breadcrumb
        LinkedList<Breadcrumb> breadcrumbs = new LinkedList<>();
        breadcrumbs.add(new Breadcrumb("Inicio", "/"));
        breadcrumbs.add(new Breadcrumb(p.getName(), "/practica/" +idReference));
        model.addAttribute("breadcrumb-list", breadcrumbs);
        model.addAttribute("breadcrumb-active", "Corrección");

        return "report";
    }

}
