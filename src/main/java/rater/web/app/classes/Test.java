package rater.web.app.classes;

import java.util.LinkedList;

public class Test {

    private int total;
    private int correct;
    private String testSuite;
    private LinkedList<TestCase> testCases;
    private String success;

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getCorrect() {
        return correct;
    }

    public void setCorrect(int correct) {
        this.correct = correct;
    }

    public String getTestSuite() {
        return testSuite;
    }

    public void setTestSuite(String testSuite) {
        this.testSuite = testSuite;
    }

    public LinkedList<TestCase> getTestCases() {
        return testCases;
    }

    public void setTestCases(LinkedList<TestCase> testCases) {
        this.testCases = testCases;
    }

    public String getSuccess() {
        return success;
    }

    public void setSuccess(String success) {
        this.success = success;
    }
}
