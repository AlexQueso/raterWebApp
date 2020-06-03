package rater.web.app.services;

import org.apache.commons.io.FileUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import rater.web.app.classes.Project;
import rater.web.app.classes.Report;
import rater.web.app.classes.Test;
import rater.web.app.classes.TestCase;
import rater.web.app.session.UserSession;

import java.io.*;
import java.util.LinkedList;
import java.util.Objects;

@Component
public class ReportService {

    @Value("${projects.path}")
    private String projectsPath;
    @Value("${jar.path}")
    private String jarPath;
    @Value("${jplag.path}")
    private String jplagPath;

    private final UserSession userSession;
    private final AppService appService;

    @Autowired
    public ReportService(UserSession userSession, AppService appService) {
        this.userSession = userSession;
        this.appService = appService;
    }

    public JSONObject rateStudentProject(long idReference, String idProject) {
        Project p = appService.getProjectById(idReference);
        String referencePath = p.getPathToDirectory().getAbsolutePath();
        String projectPath = getProjectPath(idProject);
        String referenceName = p.getName().replace(" ", "_");

        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("bash", "-c", "java -jar " + jarPath + " -p " + referencePath + " " + projectPath + " "
                + referenceName);
        try {
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            int exitVal = process.waitFor();
            if (exitVal != 0)
                throw new RuntimeException("rater.jar failure, unable to rate " + projectPath +  " project");

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        JSONObject json = getsingleJSONfromServiceExecution(projectsPath, idProject);
        deleteFiles(idProject);
        return json;
    }

    public LinkedList<Report> rateAllStudentProjects(Project p) {
        String referencePath = p.getPathToDirectory().getAbsolutePath();
        String projectPath = getProjectPath(Long.toString(p.getId()));
        String referenceName = p.getName().replace(" ", "_");
        executeRater("-d", referencePath, projectPath, referenceName, jplagPath);

        LinkedList<JSONObject> jsons = getIndividualJSONReports(p.getId());
        LinkedList<Report> reports = new LinkedList<>();
        for (JSONObject j: jsons)
            reports.add(processJsonIndividualProject(j));

        userSession.getGlobalReports().put(Long.toString(p.getId()), reports);
        deleteFiles(Long.toString(p.getId()));
        return reports;
    }

    private LinkedList<JSONObject> getIndividualJSONReports(long id) {
        LinkedList<JSONObject> jsons = new LinkedList<>();
        File globalProjectDir = new File(projectsPath + "/" + id);
        for (File f: Objects.requireNonNull(globalProjectDir.listFiles())){
            if (!(f.getName().equals("Jplag")) && f.isDirectory()){
                for(File studentProject: Objects.requireNonNull(f.listFiles())){
                    for (File studentFile: Objects.requireNonNull(studentProject.listFiles())){
                        if (studentFile.getName().equals("build_test_report.json")){
                            jsons.add(fileToJSONObject(studentFile));
                            break;
                        }
                    }
                }
            }
        }
        return jsons;
    }

    public String getProjectPath(String idProject) {
        File projectsDir = new File(projectsPath);
        for (File f: Objects.requireNonNull(projectsDir.listFiles())){
            if (f.getName().contains(idProject)){
                return f.getAbsolutePath();
            }
        }
        throw new RuntimeException("Unable to find " + idProject +" in " + projectsPath);
    }

    private JSONObject getsingleJSONfromServiceExecution(String projectPath, String idProject) {
        File studentsProjectDir = new File(projectPath);
        for (File f: Objects.requireNonNull(studentsProjectDir.listFiles())){
            if (f.getName().equals(idProject) && f.isDirectory()){
                for(File file: Objects.requireNonNull(f.listFiles())){
                    if (file.getName().equals("build_test_report.json"))
                        return fileToJSONObject(file);
                }
            }
        }
        throw new RuntimeException("unable to find json report in " + projectPath);
    }

    private JSONObject fileToJSONObject(File file){
        JSONParser parser = new JSONParser();
        JSONObject jsonObject = null;
        try (Reader reader = new FileReader(file.getPath())) {
            jsonObject = (JSONObject) parser.parse(reader);
            System.out.println(jsonObject);
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    private void deleteFiles(String idProject){
        File studentProjectsDir = new File (projectsPath);
        for (File f: Objects.requireNonNull(studentProjectsDir.listFiles())){
            if (f.getName().contains(idProject)){
                if (f.isDirectory()){
                    try {
                        FileUtils.deleteDirectory(f);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    f.delete();
                }
            }
        }
    }

    public Report processJsonIndividualProject(JSONObject json) {
        Report r = new Report();
        r.setProjectName(((String) json.get("projectName")).replace("_", " "));
        r.setDate(((String) json.get("date")).replace("-", ""));
        r.setBuild((String) json.get("build"));
        if (r.getBuild().contains("SUCCESS"))
            r.setBuildSuccess("success");
        else
            r.setBuildSuccess("danger");
        if (json.get("studentName") != null)
            r.setStudentName((String) json.get("studentName"));
        LinkedList<Test> testLinkedList = new LinkedList<>();

        JSONArray tests = (JSONArray) json.get("test");
        for (Object o : tests) {
            JSONObject jsonTest = (JSONObject) o;
            Test test = new Test();
            test.setTotal(Math.toIntExact((Long) jsonTest.get("total")));
            test.setCorrect(Math.toIntExact((Long) jsonTest.get("correct")));
            test.setTestSuite((String) jsonTest.get("testSuite"));
            if (test.getCorrect() == test.getTotal())
                test.setSuccess("success");
            else
                test.setSuccess("danger");

            if (test.getTotal() > test.getCorrect()) {
                LinkedList<TestCase> testCasesLinkedList = new LinkedList<>();

                JSONArray testCases = (JSONArray) jsonTest.get("testCases");
                for (Object aCase : testCases) {
                    JSONObject jsonTestCase = (JSONObject) aCase;
                    TestCase testCase = new TestCase();
                    testCase.setTrace((String) jsonTestCase.get("trace"));
                    testCase.setCause((String) jsonTestCase.get("cause"));
                    testCase.setTestName((String) jsonTestCase.get("testName"));
                    testCasesLinkedList.add(testCase);
                }
                test.setTestCases(testCasesLinkedList);
            }
            testLinkedList.add(test);
        }
        r.setTests(testLinkedList);
        return r;
    }

    public void saveReportUserSession(Project p, Report report) {
        userSession.getStudentReports().put(p.getName(), report);
    }

    public Report getStoredReport(Project p, long idReference) {
        return userSession.getStudentReports().get(p.getName());
    }

    public void fillModelwithStudentRepor(Model model, Report report){

        model.addAttribute("individual-report", true);
        //header
        model.addAttribute("project-name", report.getProjectName());
        model.addAttribute("date", report.getDate());
        //build
        model.addAttribute("build-success", report.getBuildSuccess());
        model.addAttribute("build", report.getBuild());
        //tests
        model.addAttribute("tests", report.getTests());

    }

    private void executeRater(String option, String referencePath, String projectPath, String referenceName, String jplag){
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("bash", "-c", "java -jar " + jarPath + " " + option + " " + referencePath + " " + projectPath + " "
                + referenceName + " " + jplag);
        try {
            Process process = processBuilder.start();
            System.out.println(processBuilder.command().get(2));
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            int exitVal = process.waitFor();
            if (exitVal != 0)
                throw new RuntimeException("rater.jar failure, unable to rate " + projectPath +  " project");

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

}
