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

    /**
     * check if current user has signed in
     *
     * @return true if user is a professor
     */
    public boolean userIsProfessor() {
        return userSession.isProfessor();
    }

    /**
     * Get all projects from database
     */
    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    /**
     * Get a project ny Id from database
     *
     * @param id project id
     */
    public Project getProjectById(long id) {
        return projectRepository.findById(id);
    }

    /**
     * delete project in database by id
     *
     * @param id project Id
     */
    public void deleteProjectById(long id) {
        Project p = getProjectById(id);
        if (p.getPathToDirectory() != null)
            if (p.getPathToDirectory().exists())
                Utils.deleteDirectory(p.getPathToDirectory().getParentFile());

        projectRepository.deleteById(id);
    }

    /**
     * Upload a project file
     *
     * @param id   project id
     * @param file uploaded file
     * @return uploaded project file name
     */
    public String uploadProject(long id, MultipartFile file) throws IOException, InterruptedException {
        byte[] bytes = file.getBytes();
        int userSessionHashCode = userSession.hashCode();
        if (userSessionHashCode < 0)
            userSessionHashCode = -userSessionHashCode;
        String projectUpdloadedId = userSessionHashCode + "_" + System.nanoTime();
        Path path = Paths.get(projectsPath + "/" + projectUpdloadedId + ".zip");
        File zippedProject = path.toFile();
        Files.write(path, bytes);

        File studentProjectDir = Utils.unzipDirectory(zippedProject, zippedProject.getParentFile());
        if (!checkStudentProjectFiles(studentProjectDir)) {
            System.err.println("Formato de fichero erroneo en práctica de alumno, no se ha ejecutado la corrección");
            projectUpdloadedId = null;
            Utils.deleteFile(zippedProject);
        }
        Utils.deleteDirectory(studentProjectDir.getParentFile());

        return projectUpdloadedId;
    }

    /**
     * Upload a set of projects
     *
     * @param id   reference project id
     * @param file projects zipped file
     */
    public void uploadProjectSet(long id, MultipartFile file) throws IOException {
        byte[] bytes = file.getBytes();
        Path path = Paths.get(projectsPath + "/" + id + ".zip");
        Files.write(path, bytes);
    }

    /**
     * check if there's already a idividual report for a specefic reference projet
     *
     * @param id reference project id
     */
    public boolean reportAlreadyExists(String id) {
        return userSession.getStudentReports().get(id) != null;
    }

    /**
     * check if there's already a global report for a specefic reference projet
     *
     * @param p project
     */
    public boolean globalReportAlreadyExists(Project p) {
        return userSession.getGlobalReports().get(Long.toString(p.getId())) != null;
    }

    /**
     * Create a new project from a n auploaded file
     *
     * @param p    project
     * @param file uploaded file
     */
    public void createProject(Project p, MultipartFile file) throws InterruptedException {
        projectRepository.save(p);
        long id = p.getId();
        Path path = Paths.get(referencesPath + "/" + id + ".zip");
        File zippedProject = path.toFile();
        try {
            byte[] bytes = file.getBytes();
            Files.write(path, bytes);
            File referenceProjectDir = Utils.unzipDirectory(zippedProject, new File(referencesPath));
            if (checkReferenceProjectFiles(referenceProjectDir)) {
                p.setReferenceFile(bytes);
                replaceNbProjectFiles(referenceProjectDir);
                p.setPathToDirectory(referenceProjectDir);
                projectRepository.save(p);
            } else {
                System.err.println("No se ha guardado la práctica " + p.getName() + ". Error de formato");
                Utils.deleteDirectory(referenceProjectDir.getParentFile());
                projectRepository.deleteById(id);
            }
        } catch (IOException e) {
            System.err.println("Error creando el proyecto con id: " + p.getId() + " " + e.getMessage());
        } finally {
            Utils.deleteFile(zippedProject);
        }
    }

    /**
     * Update a project reference file from an uploaded file
     *
     * @param id   project id
     * @param file uploaded file
     */
    public void updateProject(long id, MultipartFile file) throws InterruptedException {
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
                if (!newReferenceProjectDir.getParentFile().renameTo(p.getPathToDirectory().getParentFile()))
                    System.err.println("Error al renombrar el directorio padre del directorio del proyecto con id: " + p.getId());
                projectRepository.save(p);
            } else {
                Utils.deleteDirectory(newReferenceProjectDir.getParentFile());
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        } finally {
            Utils.deleteFile(zippedProject);
        }
    }

    /**
     * replace NetBeans project files when uploading a new project
     *
     * @param referenceProjectDir reference project file
     */
    private void replaceNbProjectFiles(File referenceProjectDir) {
        String pathToBuildProperties = referenceProjectDir.getAbsolutePath() + "/nbproject/private/private.properties";
        File f = new File(pathToBuildProperties);
        if (f.exists() && !f.isDirectory()) {
            try (BufferedReader file = new BufferedReader(new FileReader(f));
                 FileOutputStream fileOut = new FileOutputStream(f); ){
                StringBuilder inputBuffer = new StringBuilder();
                String line;
                while ((line = file.readLine()) != null) {
                    if (line.startsWith("user.properties.file="))
                        inputBuffer.append("user.properties.file=").append(nbPropertiesFile).append("\n");
                    else
                        inputBuffer.append(line).append("\n");
                }
                String inputStr = inputBuffer.toString();
                fileOut.write(inputStr.getBytes());
            } catch (Exception e) {
                System.err.println("Problem modifying file: " + pathToBuildProperties);
            }
        }
    }

    /**
     * check reference project structure
     *
     * @param file reference project directorby
     * @return true if project structure is fine
     */
    private boolean checkReferenceProjectFiles(File file) {
        int aux = 0;

        for (File f : Objects.requireNonNull(file.listFiles()))
            if (f.getName().equals("build") || f.getName().equals("build.xml") || f.getName().equals("test") ||
                    f.getName().equals("nbproject"))
                aux++;

        return aux == 4;
    }

    /**
     * check student project structure
     *
     * @param file student project directory
     * @return true if there's a src directory
     */
    private boolean checkStudentProjectFiles(File file) {
        for (File f : Objects.requireNonNull(file.listFiles()))
            if (f.getName().equals("src"))
                return true;
        return false;
    }
}
