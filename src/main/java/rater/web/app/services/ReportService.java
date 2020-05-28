package rater.web.app.services;

import org.apache.commons.io.FileUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import rater.web.app.classes.Project;
import rater.web.app.session.UserSession;

import java.io.*;
import java.util.Iterator;
import java.util.Objects;

@Component
public class ReportService {

    @Value("${projects.path}")
    private String projectsPath;
    @Value("${jar.path}")
    private String jarPath;

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
        String referenceName = p.getName();

        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("bash", "-c", "java -jar " + jarPath + " -p " + referencePath + " " + projectPath + " "
                + "p1");
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

        JSONObject json = getJSONfromServiceExecution(projectsPath, idProject);
        userSession.addStudentReport(referenceName, json);
        deleteFiles(idProject);
        return json;
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

    private JSONObject getJSONfromServiceExecution(String projectPath, String idProject) {
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
}
