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
import rater.web.app.repositories.ProjectRepository;
import rater.web.app.repositories.ReportRepository;
import rater.web.app.session.UserSession;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

@Component
public class ReportService {

    @Value("${projects.path}")
    private String projectsPath;
    @Value("${jar.path}")
    private String jarPath;
    @Value("${jplag.path}")
    private String jplagPath;
//    @Value("${jplagDir.path}")
//    private String jplagDirPath;

    private final UserSession userSession;
    private final AppService appService;
    private final ProjectRepository projectRepository;
    private final ReportRepository reportRepository;

    @Autowired
    public ReportService(UserSession userSession, AppService appService, ProjectRepository projectRepository, ReportRepository reportRepository) {
        this.userSession = userSession;
        this.appService = appService;
        this.projectRepository = projectRepository;
        this.reportRepository = reportRepository;
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
                throw new RuntimeException("rater.jar failure, unable to rate " + projectPath + " project");

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
        for (JSONObject j : jsons)
            reports.add(processJsonIndividualProject(j));

        storeSrcDirectoriesInDB(reports, p.getId());
        userSession.getGlobalReports().put(Long.toString(p.getId()), reports);
        p.setReports(reports);
        saveJplagDirectory(p);
        projectRepository.save(p);
        deleteFiles(Long.toString(p.getId()));
        return reports;
    }

    private void storeSrcDirectoriesInDB(LinkedList<Report> reports, long id) {
        File globalProjectDir = new File(projectsPath + "/" + id);
        for (File f: Objects.requireNonNull(globalProjectDir.listFiles())) {
            if (!(f.getName().equals("Jplag")) && f.isDirectory()) {
                for (File studentProject : Objects.requireNonNull(f.listFiles())) {
                    for (Report r: reports){
                        if (studentProject.getName().contains(r.getStudentName().replace(" ", "_"))){
                            for (File studentFile : Objects.requireNonNull(studentProject.listFiles())){
                                if (studentFile.getName().equals("src")){
                                    try {
                                        File zipSrc = zipDirectory(studentFile, new File(studentFile.getPath()+".zip"));
                                        byte[] bytes = Files.readAllBytes(zipSrc.toPath());
                                        r.setSrcFile(bytes);
                                        break;
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                            break;
                        }
                    }
                }
                break;
            }
        }
    }

    private void saveJplagDirectory(Project p) {
//        File destination = new File (jplagDirPath + "/" + p.getId());
        File studentProjectsDir = new File(projectsPath);
        String idProject = Long.toString(p.getId());
        for (File studentProject : Objects.requireNonNull(studentProjectsDir.listFiles())) {
            if (studentProject.getName().contains(idProject) && studentProject.isDirectory()) {
                for (File jplagDir: Objects.requireNonNull(studentProject.listFiles())){
                    if (jplagDir.getName().equals("Jplag")){
                        try {
                            //FileUtils.copyDirectory(jplagDir, destination);
                            File zipJplag = zipDirectory(jplagDir, new File(jplagDir.getPath() + ".zip"));
                            byte[] bytes = Files.readAllBytes(zipJplag.toPath());
                            p.setJplagReport(bytes);
                            break;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    private LinkedList<JSONObject> getIndividualJSONReports(long id) {
        LinkedList<JSONObject> jsons = new LinkedList<>();
        File globalProjectDir = new File(projectsPath + "/" + id);
        for (File f : Objects.requireNonNull(globalProjectDir.listFiles())) {
            if (!(f.getName().equals("Jplag")) && f.isDirectory()) {
                for (File studentProject : Objects.requireNonNull(f.listFiles())) {
                    for (File studentFile : Objects.requireNonNull(studentProject.listFiles())) {
                        if (studentFile.getName().equals("build_test_report.json")) {
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
        for (File f : Objects.requireNonNull(projectsDir.listFiles())) {
            if (f.getName().contains(idProject)) {
                return f.getAbsolutePath();
            }
        }
        throw new RuntimeException("Unable to find " + idProject + " in " + projectsPath);
    }

    private JSONObject getsingleJSONfromServiceExecution(String projectPath, String idProject) {
        File studentsProjectDir = new File(projectPath);
        for (File f : Objects.requireNonNull(studentsProjectDir.listFiles())) {
            if (f.getName().equals(idProject) && f.isDirectory()) {
                for (File file : Objects.requireNonNull(f.listFiles())) {
                    if (file.getName().equals("build_test_report.json"))
                        return fileToJSONObject(file);
                }
            }
        }
        throw new RuntimeException("unable to find json report in " + projectPath);
    }

    private JSONObject fileToJSONObject(File file) {
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

    private void deleteFiles(String idProject) {
        File studentProjectsDir = new File(projectsPath);
        for (File f : Objects.requireNonNull(studentProjectsDir.listFiles())) {
            if (f.getName().contains(idProject)) {
                if (f.isDirectory()) {
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
        if (json.get("test") instanceof JSONArray) {
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
        } else {
            Test test = new Test();
            test.setTotal(-1);
            test.setCorrect(0);
            test.setSuccess("danger");
            test.setTestSuite((String) json.get("test"));
            testLinkedList.add(test);
            r.setTests(testLinkedList);
        }
        return r;
    }

    public void saveReportUserSession(Project p, Report report) {
        userSession.getStudentReports().put(p.getName(), report);
    }

    public Report getStoredReport(Project p, long idReference) {
        return userSession.getStudentReports().get(p.getName());
    }

    public void fillModelwithStudentRepor(Model model, Report report) {

        model.addAttribute("individual-report", true);
        //header
        model.addAttribute("project-name", report.getProjectName());
        model.addAttribute("date", report.getDate());
        //build
        model.addAttribute("build-success", report.getBuildSuccess());
        model.addAttribute("build", report.getBuild());
        //tests
        if (report.getTests().get(0).getTotal() == -1) {
            model.addAttribute("tests", false);
            model.addAttribute("test-failure", true);
            model.addAttribute("test-failure-msg", report.getTests().get(0).getTestSuite());
        }else
            model.addAttribute("tests", report.getTests());

    }

    private void executeRater(String option, String referencePath, String projectPath, String referenceName, String jplag) {
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
                throw new RuntimeException("rater.jar failure, unable to rate " + projectPath + " project");

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public Report getIndividualReport(long idReference, String studentName) {
        List<Report> reports = appService.getProjectById(idReference).getReports();
        for (Report r : reports)
            if (r.getStudentName().equals(studentName))
                return r;

        return null;
    }

    public HttpServletResponse getJplagReport(long id, HttpServletResponse response) throws IOException {
        // get your file as InputStream
//        File initialFile = zipJplagDirectory(new File(jplagDirPath + "/" + id));
//        InputStream is = new FileInputStream(Objects.requireNonNull(initialFile));
        byte[] initialFile = projectRepository.findById(id).getJplagReport();
        InputStream is = new ByteArrayInputStream(initialFile);
        // copy it to response's OutputStream
        org.apache.commons.io.IOUtils.copy(is, response.getOutputStream());
        return response;
    }

    public HttpServletResponse getReportSrc(long idReport, HttpServletResponse response) throws IOException {
        byte[] initialFile = reportRepository.findById(idReport).getSrcFile();
        InputStream is = new ByteArrayInputStream(initialFile);
        org.apache.commons.io.IOUtils.copy(is, response.getOutputStream());
        return response;
    }

    private File zipDirectory (File dir, File destination){
        try {
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command("bash", "-c", "zip -r " + destination.getPath() + " " + dir.getPath());
            Process process = processBuilder.start();
            int exitVal = process.waitFor();
            if (exitVal != 0)
                throw new RuntimeException("Failure zipping: " + dir.getPath());
            if (!destination.exists())
                throw new RuntimeException("Failure zipping: " + dir.getPath());

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            System.err.println("Failure zipping: " + dir.getPath());
        }
        return destination;
    }

    public List<Report> getStoredGlobalReports(Project p) {
        return appService.getProjectById(p.getId()).getReports();
    }
}
