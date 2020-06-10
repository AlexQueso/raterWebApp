package rater.web.app.services;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import rater.web.app.classes.Project;
import rater.web.app.repositories.ProjectRepository;
import rater.web.app.session.UserSession;
import rater.web.app.utils.Utils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

@Component
public class AppService {

    private final UserSession userSession;
    private final ProjectRepository projectRepository;

    @Value("${projects.path}")
    private String projectsPath;
    @Value("${references.path}")
    private String referencesPath;
    @Value("${nb.properties.file}")
    private String nbPropertiesFile;

    @Autowired
    public AppService(UserSession userSession, ProjectRepository projectRepository) {
        this.userSession = userSession;
        this.projectRepository = projectRepository;
    }

    public boolean userIsProfessor() {
        return userSession.isProfessor();
    }

    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    public Project getProjectById(long id) {
        return projectRepository.findById(id);
    }

    public void deleteProjectById(long id) {
        Project p = getProjectById(id);
        if (p.getPathToDirectory() != null)
            if (p.getPathToDirectory().exists())
                Utils.deleteDirectory(p.getPathToDirectory().getParentFile());

        projectRepository.deleteById(id);
    }

    public String uploadProject(long id, MultipartFile file) throws IOException {
        byte[] bytes = file.getBytes();
        String projectUpdloadedId = Math.abs(userSession.hashCode()) + "_" + System.nanoTime();
        Path path = Paths.get(projectsPath + "/" + projectUpdloadedId + ".zip");
        Files.write(path, bytes);
        return projectUpdloadedId;
    }

    public void uploadProjectSet(long id, MultipartFile file) throws IOException {
        byte[] bytes = file.getBytes();
        Path path = Paths.get(projectsPath + "/" + id + ".zip");
        Files.write(path, bytes);
    }

    public boolean reportAlreadyExists(String key) {
        return userSession.getStudentReports().get(key) != null;
    }

    public boolean globalReportAlreadyExists(Project p) {
        return userSession.getGlobalReports().get(Long.toString(p.getId())) != null;
    }

    public void createProject(Project project, MultipartFile file) {
        projectRepository.save(project);
        setReferenceProject(project, file);
    }

    private void setReferenceProject(Project p, MultipartFile file) {
        long id = p.getId();
        Path path = Paths.get(referencesPath + "/" + id + ".zip");
        File zippedProject = path.toFile();
        try {
            byte[] bytes = file.getBytes();
            Files.write(path, bytes);
            p.setReferenceFile(bytes);
            File referenceProjectDir = Utils.unzipDirectory(zippedProject, new File(referencesPath));
            replaceNbProjectFiles(referenceProjectDir);
            p.setPathToDirectory(referenceProjectDir);
            projectRepository.save(p);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            Utils.deleteFile(zippedProject);
        }
    }

    public void updateProject(long id, MultipartFile file) {
        Project p = getProjectById(id);
        if (p.getPathToDirectory() != null)
            if (p.getPathToDirectory().exists())
                Utils.deleteDirectory(p.getPathToDirectory().getParentFile());
        setReferenceProject(p, file);
    }

    private void replaceNbProjectFiles(File referenceProjectDir) {
        String pathToBuildProperties = referenceProjectDir.getAbsolutePath() + "/nbproject/private/private.properties";
        File f = new File(pathToBuildProperties);
        if (f.exists() && !f.isDirectory()) {
            try {
                BufferedReader file = new BufferedReader(new FileReader(f));
                StringBuilder inputBuffer = new StringBuilder();
                String line;
                while ((line = file.readLine()) != null) {
                    if (line.startsWith("user.properties.file="))
                        inputBuffer.append("user.properties.file=").append(nbPropertiesFile).append("\n");
                    else
                        inputBuffer.append(line).append("\n");
                }
                file.close();
                String inputStr = inputBuffer.toString();
                FileOutputStream fileOut = new FileOutputStream(f);
                fileOut.write(inputStr.getBytes());
                fileOut.close();
            } catch (Exception e) {
                System.err.println("Problem modifying file: " + pathToBuildProperties);
            }
        }
    }

//    private File unzipReferenceProject(File zippedFile, File destination) {
//        File unzippedFile = null;
//        String unzippedFileName = zippedFile.getName().replace(".zip", "");
//        ProcessBuilder processBuilder = new ProcessBuilder();
//        processBuilder.command("bash", "-c", "unzip " + zippedFile.getAbsolutePath() + " -d " +
//                destination.getAbsolutePath() + "/" + unzippedFileName);
//        try {
//            Process process = processBuilder.start();
//            int exitVal = process.waitFor();
//            if (exitVal != 0)
//                throw new RuntimeException("Failure unzipping: " + zippedFile.getName());
//
//            unzippedFile = new File(destination.getPath() + "/" + unzippedFileName);
//            File[] files = unzippedFile.listFiles();
//            for (File f : Objects.requireNonNull(files)) {
//                return f;
//            }
//
//        } catch (IOException | InterruptedException e) {
//            e.printStackTrace();
//            System.err.println("Failure unzipping: " + zippedFile.getName());
//        }
//        return unzippedFile;
//    }

}
