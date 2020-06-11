package rater.web.app.services;

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
import java.util.*;

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
        File zippedProject = path.toFile();
        Files.write(path, bytes);
        try {
            File studentProjectDir = Utils.unzipDirectory(zippedProject, zippedProject.getParentFile());
            if (!checkStudentProjectFiles(studentProjectDir)){
                System.err.println("Formato de fichero erroneo en práctica de alumno, no se ha ejecutado la corrección");
                projectUpdloadedId = null;
                Utils.deleteFile(zippedProject);
            }
            Utils.deleteDirectory(studentProjectDir.getParentFile());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

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

    public void createProject(Project p, MultipartFile file) {
        projectRepository.save(p);
        long id = p.getId();
        Path path = Paths.get(referencesPath + "/" + id + ".zip");
        File zippedProject = path.toFile();
        try {
            byte[] bytes = file.getBytes();
            Files.write(path, bytes);
            File referenceProjectDir = Utils.unzipDirectory(zippedProject, new File(referencesPath));
            if (checkReferenceProjectFiles(referenceProjectDir)){
                p.setReferenceFile(bytes);
                replaceNbProjectFiles(referenceProjectDir);
                p.setPathToDirectory(referenceProjectDir);
                projectRepository.save(p);
            } else {
                System.err.println("No se ha guardado la práctica " + p.getName() + ". Error de formato");
                Utils.deleteDirectory(referenceProjectDir.getParentFile());
                projectRepository.deleteById(id);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            Utils.deleteFile(zippedProject);
        }
    }

    public void updateProject(long id, MultipartFile file) {
        Project p = getProjectById(id);
        Path path = Paths.get(referencesPath + "/" + id + "_" + userSession.hashCode() + ".zip");
        File zippedProject = path.toFile();
        try {
            byte[] bytes = file.getBytes();
            Files.write(path, bytes);
            File newReferenceProjectDir = Utils.unzipDirectory(zippedProject, new File(referencesPath));
            if (checkReferenceProjectFiles(newReferenceProjectDir)) {
                p.setReferenceFile(bytes);
                replaceNbProjectFiles(newReferenceProjectDir);
                Utils.deleteDirectory(p.getPathToDirectory().getParentFile());
                newReferenceProjectDir.getParentFile().renameTo(p.getPathToDirectory().getParentFile());
                projectRepository.save(p);
            } else {
                Utils.deleteDirectory(newReferenceProjectDir.getParentFile());
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            Utils.deleteFile(zippedProject);
        }
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

    private boolean checkReferenceProjectFiles(File file){
        int aux = 0;

        for (File f: Objects.requireNonNull(file.listFiles()))
            if (f.getName().equals("build") || f.getName().equals("build.xml") || f.getName().equals("test") ||
                    f.getName().equals("nbproject"))
                aux ++;

        return aux == 4;
    }

    private boolean checkStudentProjectFiles(File file){
        for(File f: Objects.requireNonNull(file.listFiles()))
            if (f.getName().equals("src"))
                return true;
        return false;
    }
}
