package rater.web.app.session;

import org.json.simple.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.util.HashMap;

@Component
@SessionScope
public class User {

    private boolean professor;
    private HashMap<String, JSONObject> studentReports;
    private HashMap<String, JSONObject> globalReports;
    private HashMap<String, JSONObject> individualReports;

    public User(boolean professor) {
        this.professor = professor;
        if (professor){
            globalReports = new HashMap<>();
            individualReports = new HashMap<>();
        } else {
            studentReports = new HashMap<>();
        }
    }

    public User(){
        this(false);
    }

    public boolean isProfessor(){
        return professor;
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
