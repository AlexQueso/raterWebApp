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

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

@Controller
public class ReportController {

    public final ReportService reportService;
    public final AppService appService;

    @Autowired
    public ReportController(ReportService reportService, AppService appService) {
        this.reportService = reportService;
        this.appService = appService;
    }

    /**
     * individual report page
     * @param idReference reference project id
     * @param idProject student project id
     */
    @GetMapping("/report/{idReference}/{idProject}")
    public String reportStudent(Model model, @PathVariable long idReference, @PathVariable String idProject){
        Project p = appService.getProjectById(idReference);
        JSONObject jsonReport = null;
        try {
            jsonReport = reportService.rateStudentProject(idReference, idProject);
        } catch (InterruptedException e) {
            System.err.println("Error ejecutando JRater para  el proyecto con id " + idReference + ": " + e.getMessage());
        }
        Report report = reportService.processJsonIndividualProject(jsonReport);
        reportService.saveReportInUserSession(p, report);
        reportService.fillModelwithStudentRepor(model, report);

        model.addAttribute("id-project", p.getId());
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

    /**
     * individual report page review
     * @param idReference reference project id
     */
    @GetMapping("/report/{idReference}")
    public String reviewReport(Model model, @PathVariable long idReference){
        Project p = appService.getProjectById(idReference);
        Report report = reportService.getReportFromUserSession(p);
        reportService.fillModelwithStudentRepor(model, report);

        model.addAttribute("id-project", p.getId());
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

    /**
     * individual report page review from global report
     * @param idReference reference project id
     * @param studentName Student name
     */
    @GetMapping("/individual-report/{idReference}/{studentName}")
    public String reviewIndividualReport(Model model, @PathVariable long idReference, @PathVariable String studentName){
        Project p = appService.getProjectById(idReference);
        Report report = reportService.getIndividualReport(idReference, studentName);
        reportService.fillModelwithStudentRepor(model, report);

        model.addAttribute("id-project", p.getId());
        if (appService.userIsProfessor()) {
            model.addAttribute("professor", true);
            model.addAttribute("new-project-btn", true);
            model.addAttribute("id-report", report.getId());
        }
        else
            model.addAttribute("student", true);

        //breadcrumb
        LinkedList<Breadcrumb> breadcrumbs = new LinkedList<>();
        breadcrumbs.add(new Breadcrumb("Inicio", "/"));
        breadcrumbs.add(new Breadcrumb(p.getName(), "/practica/" +idReference));
        breadcrumbs.add(new Breadcrumb("Corrección Global", "/review-global-report/" + idReference));
        model.addAttribute("breadcrumb-list", breadcrumbs);
        model.addAttribute("breadcrumb-active", report.getStudentName());

        return "report";
    }

    /**
     * Global report
     * @param id reference project id
     */
    @GetMapping("/report-global/{id}")
    public String reportGlobal(Model model, @PathVariable long id){
        Project p = appService.getProjectById(id);
        LinkedList<Report> reports = null;
        try {
            reports = reportService.rateAllStudentProjects(p);
        } catch (InterruptedException e) {
            System.err.println("Error ejecutando JRater para un conjunto de proyectos:" + e.getMessage());
            return Utils.redirectTo("/");
        }

        model.addAttribute("global-report", true);
        model.addAttribute("project-name", p.getName());
        model.addAttribute("date", reports.getFirst().getDate());
        model.addAttribute("reports", reports);

        model.addAttribute("id-project", p.getId());
        if (appService.userIsProfessor()) {
            model.addAttribute("professor", true);
            model.addAttribute("new-project-btn", true);
        }
        else
            model.addAttribute("student", true);

        //breadcrumb
        LinkedList<Breadcrumb> breadcrumbs = new LinkedList<>();
        breadcrumbs.add(new Breadcrumb("Inicio", "/"));
        breadcrumbs.add(new Breadcrumb(p.getName(), "/practica/" +id));
        model.addAttribute("breadcrumb-list", breadcrumbs);
        model.addAttribute("breadcrumb-active", "Corrección Global");

        return "report";
    }

    /**
     * Global report review
     * @param id reference project id
     */
    @GetMapping("/review-global-report/{id}")
    public String reportGlobalReview(Model model, @PathVariable long id){
        Project p = appService.getProjectById(id);
        List<Report> reports = p.getReports();

        model.addAttribute("global-report", true);
        model.addAttribute("project-name", p.getName());
        model.addAttribute("date", reports.get(0).getDate());
        model.addAttribute("reports", reports);

        model.addAttribute("id-project", p.getId());
        if (appService.userIsProfessor()) {
            model.addAttribute("professor", true);
            model.addAttribute("new-project-btn", true);
        }
        else
            model.addAttribute("student", true);

        //breadcrumb
        LinkedList<Breadcrumb> breadcrumbs = new LinkedList<>();
        breadcrumbs.add(new Breadcrumb("Inicio", "/"));
        breadcrumbs.add(new Breadcrumb(p.getName(), "/practica/" + id));
        model.addAttribute("breadcrumb-list", breadcrumbs);
        model.addAttribute("breadcrumb-active", "Corrección Global");

        return "report";
    }

    /**
     * Download Jplag report
     * @param id reference project id
     */
    @GetMapping("/download-jplag/{id}")
    public void downloadJplagReport(@PathVariable long id, HttpServletResponse response) {
        try {
            reportService.downloadJplagReport(id, response).flushBuffer();
        } catch (IOException ex) {
            throw new RuntimeException("IOError writing file to output stream");
        }
    }

    /**
     * Download student src files
     * @param id report id
     */
    @GetMapping("/download-src/{id}")
    public void downloadSrc(@PathVariable long id, HttpServletResponse response){
        try {
            reportService.downloadStudentSrc(id, response).flushBuffer();
        } catch (IOException ex) {
            throw new RuntimeException("IOError writing file to output stream");
        }
    }

}