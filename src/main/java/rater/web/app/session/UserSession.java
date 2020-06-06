package rater.web.app.session;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;
import rater.web.app.classes.Report;

import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

@Component
@SessionScope
public class UserSession {

    private boolean professor = false;
    private boolean newUser = true;
    private HashMap<String, Report> studentReports = new HashMap<>();
    private HashMap<String, List<Report>> globalReports;

    public boolean isProfessor() {
        return professor;
    }

    public void setProfessor(boolean professor) {
        this.professor = professor;
    }

    public boolean isNewUser() {
        return newUser;
    }

    public void setNewUser(boolean newUser) {
        this.newUser = newUser;
    }

    public HashMap<String, Report> getStudentReports() {
        return studentReports;
    }

    public void setStudentReports(HashMap<String, Report> studentReports) {
        this.studentReports = studentReports;
    }

    @Transactional
    public HashMap<String, List<Report>> getGlobalReports() {
        return globalReports;
    }

    public void setGlobalReports(HashMap<String, List<Report>> globalReports) {
        this.globalReports = globalReports;
    }
}
