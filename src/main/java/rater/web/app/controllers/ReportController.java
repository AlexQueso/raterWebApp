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
import java.util.Objects;

import static rater.web.app.RaterWebAppApplication.LOGGER;

@Controller
public class ReportController {

    public final ReportService reportService;
    public final AppService appService;

    private final String ID_PROJECT = "id-project";
    private final String PROFESSOR = "professor";
    private final String STUDENT = "student";
    private final String INICIO = "Inicio";
    private final String BREADCRUMB_LIST = "breadcrumb-list";
    private final String BREADCRUMB_ACTIVE = "breadcrumb-active";
    private final String REPORT = "report";
    private final String NEW_PROJECT_BTN = "new-project-btn";
    private final String PRACTICA = "/practica/";
    private final String CORRECCION_GLOBAL = "Corrección Global";

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
            LOGGER.error(e.getMessage());
            Thread.currentThread().interrupt();
        }
        Report report = reportService.processJsonIndividualProject(Objects.requireNonNull(jsonReport));
        reportService.saveReportInUserSession(p, report);
        reportService.fillModelwithStudentRepor(model, report);

        model.addAttribute(ID_PROJECT, p.getId());
        if (appService.userIsProfessor()) {
            model.addAttribute(PROFESSOR, true);
            model.addAttribute(NEW_PROJECT_BTN, true);
        }
        else
            model.addAttribute(STUDENT, true);

        //breadcrumb
        LinkedList<Breadcrumb> breadcrumbs = new LinkedList<>();
        breadcrumbs.add(new Breadcrumb(INICIO, "/"));
        breadcrumbs.add(new Breadcrumb(p.getName(), PRACTICA +idReference));
        model.addAttribute(BREADCRUMB_LIST, breadcrumbs);
        model.addAttribute(BREADCRUMB_ACTIVE, "Corrección");

        return REPORT;
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

        model.addAttribute(ID_PROJECT, p.getId());
        if (appService.userIsProfessor()) {
            model.addAttribute(PROFESSOR, true);
            model.addAttribute(NEW_PROJECT_BTN, true);
        }
        else
            model.addAttribute(STUDENT, true);

        //breadcrumb
        LinkedList<Breadcrumb> breadcrumbs = new LinkedList<>();
        breadcrumbs.add(new Breadcrumb(INICIO, "/"));
        breadcrumbs.add(new Breadcrumb(p.getName(), PRACTICA +idReference));
        model.addAttribute(BREADCRUMB_LIST, breadcrumbs);
        model.addAttribute(BREADCRUMB_ACTIVE, "Corrección");

        return REPORT;
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

        model.addAttribute(ID_PROJECT, p.getId());
        if (appService.userIsProfessor()) {
            model.addAttribute(PROFESSOR, true);
            model.addAttribute(NEW_PROJECT_BTN, true);
            model.addAttribute("id-report", report.getId());
        }
        else
            model.addAttribute(STUDENT, true);

        //breadcrumb
        LinkedList<Breadcrumb> breadcrumbs = new LinkedList<>();
        breadcrumbs.add(new Breadcrumb(INICIO, "/"));
        breadcrumbs.add(new Breadcrumb(p.getName(), PRACTICA +idReference));
        breadcrumbs.add(new Breadcrumb(CORRECCION_GLOBAL, "/review-global-report/" + idReference));
        model.addAttribute(BREADCRUMB_LIST, breadcrumbs);
        model.addAttribute(BREADCRUMB_ACTIVE, report.getStudentName());

        return REPORT;
    }

    /**
     * Global report
     * @param id reference project id
     */
    @GetMapping("/report-global/{id}")
    public String reportGlobal(Model model, @PathVariable long id){
        Project p = appService.getProjectById(id);
        LinkedList<Report> reports;
        try {
            reports = reportService.rateAllStudentProjects(p);
        } catch (InterruptedException e) {
            LOGGER.error(e.getMessage());
            Thread.currentThread().interrupt();
            return Utils.redirectTo("/");
        }

        model.addAttribute("global-report", true);
        model.addAttribute("project-name", p.getName());
        model.addAttribute("date", reports.getFirst().getDate());
        model.addAttribute("reports", reports);

        model.addAttribute(ID_PROJECT, p.getId());
        if (appService.userIsProfessor()) {
            model.addAttribute(PROFESSOR, true);
            model.addAttribute(NEW_PROJECT_BTN, true);
        }
        else
            model.addAttribute(STUDENT, true);

        //breadcrumb
        LinkedList<Breadcrumb> breadcrumbs = new LinkedList<>();
        breadcrumbs.add(new Breadcrumb(INICIO, "/"));
        breadcrumbs.add(new Breadcrumb(p.getName(), PRACTICA +id));
        model.addAttribute(BREADCRUMB_LIST, breadcrumbs);
        model.addAttribute(BREADCRUMB_ACTIVE, CORRECCION_GLOBAL);

        return REPORT;
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

        model.addAttribute(ID_PROJECT, p.getId());
        if (appService.userIsProfessor()) {
            model.addAttribute(PROFESSOR, true);
            model.addAttribute(NEW_PROJECT_BTN, true);
        }
        else
            model.addAttribute(STUDENT, true);

        //breadcrumb
        LinkedList<Breadcrumb> breadcrumbs = new LinkedList<>();
        breadcrumbs.add(new Breadcrumb(INICIO, "/"));
        breadcrumbs.add(new Breadcrumb(p.getName(), PRACTICA + id));
        model.addAttribute(BREADCRUMB_LIST, breadcrumbs);
        model.addAttribute(BREADCRUMB_ACTIVE, CORRECCION_GLOBAL);

        return REPORT;
    }

    /**
     * Download Jplag report
     * @param id reference project id
     */
    @GetMapping("/download-jplag/{id}")
    public void downloadJplagReport(@PathVariable long id, HttpServletResponse response) {
        try {
            reportService.downloadJplagReport(id, response).flushBuffer();
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
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
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
    }

}