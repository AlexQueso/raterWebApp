package rater.web.app.session;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;
import rater.web.app.classes.Report;

import java.util.HashMap;
import java.util.LinkedList;

@Component
@SessionScope
public class UserSession {

    private boolean professor = false;
    private boolean newUser = true;
    private HashMap<String, Report> studentReports = new HashMap<>(); //todo:cambiar string por long
    private HashMap<String, LinkedList<Report>> globalReports;

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

    public HashMap<String, LinkedList<Report>> getGlobalReports() {
        return globalReports;
    }

    public void setGlobalReports(HashMap<String, LinkedList<Report>> globalReports) {
        this.globalReports = globalReports;
    }
}
