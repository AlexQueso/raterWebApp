package rater.web.app.classes;

import javax.persistence.*;
import java.util.LinkedList;
import java.util.List;

@Entity
public class Test {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private int total;
    private int correct;
    private String testSuite;
    @OneToMany(cascade=CascadeType.ALL)
    private List<TestCase> testCases;
    private String success;

    /**
     * Contructor vacio para que Spring lo considere un Java Bean
     */
    public Test() {
    }

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

    public List<TestCase> getTestCases() {
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
