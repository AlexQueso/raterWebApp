package rater.web.app.services;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
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
import rater.web.app.utils.Utils;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static rater.web.app.RaterWebAppApplication.LOGGER;

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
    private final ProjectRepository projectRepository;
    private final ReportRepository reportRepository;

    private final String JPLAG = "Jplag";
    private final String DANGER = "danger";

    @Autowired
    public ReportService(UserSession userSession, AppService appService, ProjectRepository projectRepository, ReportRepository reportRepository) {
        this.userSession = userSession;
        this.appService = appService;
        this.projectRepository = projectRepository;
        this.reportRepository = reportRepository;
    }

    /**
     * rate student project
     * @param idReference reference project id
     * @param idProject student project id
     * @return Json report
     */
    public JSONObject rateStudentProject(long idReference, String idProject) throws InterruptedException {
        Project p = appService.getProjectById(idReference);
        String referencePath = p.getPathToDirectory().getAbsolutePath();
        String projectPath = getProjectPath(idProject);
        String referenceName = p.getName().replace(" ", "_");
        executeRater("-p", referencePath, projectPath, referenceName, "");
        JSONObject json = getIndividualJSONReport(projectsPath, idProject);
        deleteFiles(idProject);
        return json;
    }

    /**
     * rate a set of student projects
     * @param p reference project
     * @return json report list
     */
    public LinkedList<Report> rateAllStudentProjects(Project p) throws InterruptedException {
        String referencePath = p.getPathToDirectory().getAbsolutePath();
        String projectPath = getProjectPath(Long.toString(p.getId()));
        String referenceName = p.getName().replace(" ", "_");
        executeRater("-d", referencePath, projectPath, referenceName, jplagPath);

        LinkedList<JSONObject> jsons = getIndividualJSONReports(p.getId());
        LinkedList<Report> reports = new LinkedList<>();
        for (JSONObject j : jsons)
            reports.add(processJsonIndividualProject(j));

        saveSrc(reports, p.getId());
        userSession.getGlobalReports().put(Long.toString(p.getId()), reports);
        p.setReports(reports);
        saveJplagReport(p);
        projectRepository.save(p);
        deleteFiles(Long.toString(p.getId()));
        return reports;
    }

    /**
     * Save student src files in database
     * @param reports report list
     * @param id reference project id
     */
    private void saveSrc(LinkedList<Report> reports, long id) throws InterruptedException {
        File globalProjectDir = new File(projectsPath + "/" + id);
        for (File f : Objects.requireNonNull(globalProjectDir.listFiles())) {
            if (!(f.getName().equals(JPLAG)) && f.isDirectory()) {
                for (File studentProject : Objects.requireNonNull(f.listFiles())) {
                    for (Report r : reports) {
                        if (studentProject.getName().contains(r.getStudentName().replace(" ", "_"))) {
                            for (File studentFile : Objects.requireNonNull(studentProject.listFiles())) {
                                if (studentFile.getName().equals("src")) {
                                    try {
                                        File zipSrc = Utils.zipDirectory(studentFile, new File(studentFile.getPath() + ".zip"));
                                        byte[] bytes = Files.readAllBytes(zipSrc.toPath());
                                        r.setSrcFile(bytes);
                                        break;
                                    } catch (IOException e) {
                                        LOGGER.error(e.getMessage());
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

    /**
     * save Jplag report in database
     * @param p reference project
     */
    private void saveJplagReport(Project p) throws InterruptedException {
        File studentProjectsDir = new File(projectsPath);
        String idProject = Long.toString(p.getId());
        for (File studentProject : Objects.requireNonNull(studentProjectsDir.listFiles())) {
            if (studentProject.getName().contains(idProject) && studentProject.isDirectory()) {
                for (File jplagDir : Objects.requireNonNull(studentProject.listFiles())) {
                    if (jplagDir.getName().equals(JPLAG)) {
                        try {
                            File zipJplag = Utils.zipDirectory(jplagDir, new File(jplagDir.getPath() + ".zip"));
                            byte[] bytes = Files.readAllBytes(zipJplag.toPath());
                            p.setJplagReport(bytes);
                            break;
                        } catch (IOException e) {
                            LOGGER.error(e.getMessage());
                        }
                    }
                }
            }
        }
    }

    /**
     * Get Json Object from json file
     * @param projectPath path to project
     * @param idProject reference project id
     */
    private JSONObject getIndividualJSONReport(String projectPath, String idProject) {
        File studentsProjectDir = new File(projectPath);
        for (File f : Objects.requireNonNull(studentsProjectDir.listFiles())) {
            if (f.getName().equals(idProject) && f.isDirectory()) {
                for (File file : Objects.requireNonNull(f.listFiles())) {
                    if (file.getName().equals("build_test_report.json"))
                        return Utils.fileToJSONObject(file);
                }
            }
        }
        throw new RuntimeException("unable to find json report in " + projectPath);
    }

    /**
     * Get all json objects from json report failes
     * @param id reference project id
     */
    private LinkedList<JSONObject> getIndividualJSONReports(long id) {
        LinkedList<JSONObject> jsons = new LinkedList<>();
        File globalProjectDir = new File(projectsPath + "/" + id);
        for (File f : Objects.requireNonNull(globalProjectDir.listFiles())) {
            if (!(f.getName().equals(JPLAG)) && f.isDirectory()) {
                for (File studentProject : Objects.requireNonNull(f.listFiles())) {
                    for (File studentFile : Objects.requireNonNull(studentProject.listFiles())) {
                        if (studentFile.getName().equals("build_test_report.json")) {
                            jsons.add(Utils.fileToJSONObject(studentFile));
                            break;
                        }
                    }
                }
            }
        }
        return jsons;
    }

    /**
     * Get project absolute path
     * @param idProject reference project id
     */
    public String getProjectPath(String idProject) {
        File projectsDir = new File(projectsPath);
        for (File f : Objects.requireNonNull(projectsDir.listFiles())) {
            if (f.getName().contains(idProject)) {
                return f.getAbsolutePath();
            }
        }
        throw new RuntimeException("Unable to find " + idProject + " in " + projectsPath);
    }

    /**
     * Delete student files after rating has ended
     * @param idProject reference project id
     */
    private void deleteFiles(String idProject) {
        File studentProjectsDir = new File(projectsPath);
        for (File f : Objects.requireNonNull(studentProjectsDir.listFiles())) {
            if (f.getName().contains(idProject)) {
                if (f.isDirectory()) {
                    Utils.deleteDirectory(f);
                } else {
                    f.delete();
                }
            }
        }
    }

    /**
     * Process JsonObject to generate a Report object
     * @param json JsonObjectobject
     * @return generated report
     */
    public Report processJsonIndividualProject(JSONObject json) {
        Report r = new Report();
        r.setProjectName(((String) json.get("projectName")).replace("_", " "));
        r.setDate(((String) json.get("date")).replace("-", ""));
        r.setBuild((String) json.get("build"));

        if (json.get("studentName") != null)
            r.setStudentName((String) json.get("studentName"));

        if (r.getBuild().contains("SUCCESS"))
            r.setBuildSuccess("success");
        else
            r.setBuildSuccess(DANGER);

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
                    test.setSuccess(DANGER);

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
        } else {
            Test test = new Test();
            test.setTotal(-1);
            test.setCorrect(0);
            test.setSuccess(DANGER);
            test.setTestSuite((String) json.get("test"));
            testLinkedList.add(test);
        }
        r.setTests(testLinkedList);
        return r;
    }

    /**
     * Save individual report in user session so it's accesible during the httpSession lenght
     * @param p project
     * @param report report
     */
    public void saveReportInUserSession(Project p, Report report) {
        userSession.getStudentReports().put(p.getName(), report);
    }

    /**
     * Get individual report stored in userSession
     * @param p project
     * @return stored report
     */
    public Report getReportFromUserSession(Project p) {
        return userSession.getStudentReports().get(p.getName());
    }

    /**
     * Fill model with data from an inidvidual report
     * @param model model
     * @param report report
     */
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
        } else
            model.addAttribute("tests", report.getTests());
    }

    /**
     * Execute rater.jar
     * @param option project o directory
     * @param referencePath path to reference project
     * @param projectPath path to student project(s)
     * @param referenceName project name
     * @param jplag path to jplag
     */
    private void executeRater(String option, String referencePath, String projectPath, String referenceName, String jplag) throws InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("bash", "-c", "java -jar " + jarPath + " " + option + " " + referencePath + " " +
                projectPath + " " + referenceName + " " + jplag);
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

        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
    }

    /**
     * Get individual report from database
     * @param idReference reference project Id
     * @param studentName student name
     * @return stored report
     */
    public Report getIndividualReport(long idReference, String studentName) {
        List<Report> reports = appService.getProjectById(idReference).getReports();
        for (Report r : reports)
            if (r.getStudentName().equals(studentName))
                return r;

        throw new RuntimeException("Unable to find individual report: " + studentName);
    }

    /**
     * Download Jplg report
     * @param id reference project id
     */
    public HttpServletResponse downloadJplagReport(long id, HttpServletResponse response) throws IOException {
        byte[] initialFile = projectRepository.findById(id).getJplagReport();
        InputStream is = new ByteArrayInputStream(initialFile);
        org.apache.commons.io.IOUtils.copy(is, response.getOutputStream());
        return response;
    }

    /**
     * Download student src files
     * @param idReport report id
     */
    public HttpServletResponse downloadStudentSrc(long idReport, HttpServletResponse response) throws IOException {
        byte[] initialFile = reportRepository.findById(idReport).getSrcFile();
        InputStream is = new ByteArrayInputStream(initialFile);
        org.apache.commons.io.IOUtils.copy(is, response.getOutputStream());
        return response;
    }

    /**
     * Get global reports
     * @param p project
     * @return list of reports
     */
    public List<Report> getGlobalReports(Project p) {
        return appService.getProjectById(p.getId()).getReports();
    }

}