package rater.web.app.classes;

import javax.persistence.*;
import java.util.LinkedList;
import java.util.List;

@Entity
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private String projectName;
    private String date;
    private String studentName;
    private String build;
    private String buildSuccess;
    @OneToMany(cascade=CascadeType.ALL)
    private List<Test> tests;

    public Report() {
        tests = new LinkedList<>();
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getBuild() {
        return build;
    }

    public void setBuild(String build) {
        this.build = build;
    }

    public List<Test> getTests() {
        return tests;
    }

    public void setTests(LinkedList<Test> tests) {
        this.tests = tests;
    }

    public String getBuildSuccess() {
        return buildSuccess;
    }

    public void setBuildSuccess(String buildSuccess) {
        this.buildSuccess = buildSuccess;
    }
}
