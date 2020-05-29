package rater.web.app.classes;

import java.util.LinkedList;

public class Report {
    private String projectName;
    private String date;
    private String studentName;
    private String build;
    private LinkedList<Test> tests;

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

    public LinkedList<Test> getTests() {
        return tests;
    }

    public void setTests(LinkedList<Test> tests) {
        this.tests = tests;
    }
}
