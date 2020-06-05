package rater.web.app.controllers;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import rater.web.app.classes.Breadcrumb;
import rater.web.app.classes.Project;
import rater.web.app.classes.Report;
import rater.web.app.services.AppService;
import rater.web.app.services.ReportService;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;

@Controller
public class ReportController {

    @Value("${jplagDir.path}")
    private String jplagDirPath;
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

    @GetMapping("/report/{idReference}")
    public String reviewReport(Model model, @PathVariable long idReference){
        Project p = appService.getProjectById(idReference);
        Report report = reportService.getStoredReport(p, idReference);
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

    @GetMapping("/individual-report/{idReference}/{studentName}")
    public String reviewIndividualReport(Model model, @PathVariable long idReference, @PathVariable String studentName){
        Project p = appService.getProjectById(idReference);
        Report report = reportService.getIndividualReport(idReference, studentName);
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
        breadcrumbs.add(new Breadcrumb("Corrección Global", "/global-report-review/" + idReference));
        model.addAttribute("breadcrumb-list", breadcrumbs);
        model.addAttribute("breadcrumb-active", report.getStudentName());

        return "report";
    }

    @GetMapping("/report-global/{id}")
    public String reportGlobal(Model model, @PathVariable long id){
        Project p = appService.getProjectById(id);
        LinkedList<Report> reports = reportService.rateAllStudentProjects(p);

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

    @GetMapping("/report-global-review/{id}")
    public String reportGlobalReview(Model model, @PathVariable long id){

        return null;
    }

    @GetMapping("/download-jplag/{id}")
    public void getFile(@PathVariable long id, HttpServletResponse response) {
        try {
            reportService.getResponse(id, response).flushBuffer();
        } catch (IOException ex) {
            throw new RuntimeException("IOError writing file to output stream");
        }
    }
}
