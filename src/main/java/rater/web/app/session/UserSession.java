package rater.web.app.session;

import org.json.simple.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.util.HashMap;

@Component
@SessionScope
public class UserSession {

    private boolean professor = false;
    private boolean newUser = true;
    private HashMap<String, JSONObject> studentReports = new HashMap<>();
    private HashMap<String, JSONObject> globalReports;
    private HashMap<String, JSONObject> individualReports;

    public boolean isProfessor(){
        return professor;
    }

    public void setProfessor(boolean b){
        professor = b;
    }

    public HashMap<String, JSONObject> getStudentReports() {
        return studentReports;
    }

    public HashMap<String, JSONObject> getGlobalReports() {
        return globalReports;
    }

    public HashMap<String, JSONObject> getIndividualReports() {
        return individualReports;
    }

    public boolean isNewUser() {
        return newUser;
    }

    public void setNewUser(boolean newUser) {
        this.newUser = newUser;
    }

    public void setGlobalReports(HashMap<String, JSONObject> globalReports) {
        this.globalReports = globalReports;
    }

    public void setIndividualReports(HashMap<String, JSONObject> individualReports) {
        this.individualReports = individualReports;
    }

    public void setStudentReports(HashMap<String, JSONObject> studentReports) {
        this.studentReports = studentReports;
    }

    public void addStudentReport(String name, JSONObject report){
        studentReports.put(name, report);
    }

    public void addGlobalReport(String name, JSONObject report){
        globalReports.put(name, report);
    }

    public void addIndividualReport(String name, JSONObject report){
        individualReports.put(name, report);
    }
}
