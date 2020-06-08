package rater.web.app.services;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import rater.web.app.classes.Project;
import rater.web.app.repositories.ProjectRepository;
import rater.web.app.session.UserSession;

import javax.annotation.PostConstruct;
import javax.validation.Valid;
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

    @PostConstruct
    public void initDataBase(){
        Project p1 = new Project("Práctica 1", "Árboles n-arios y binarios", new File("/home/alex/Desktop/projects/references/practica_1"));
        Project p2 = new Project("Práctica 2", "", new File(""));
        Project p3 = new Project("Práctica 3", "Diccionarios Ordenados", new File(""));
        Project p4 = new Project("Examen Final", "Convocatoria ordinaria", new File(""));
        Project p5 = new Project("Examen Final", "Convocatoria extraordinaria", new File(""));
        projectRepository.save(p1);
        projectRepository.save(p2);
        projectRepository.save(p3);
        projectRepository.save(p4);
        projectRepository.save(p5);
    }

    public boolean userIsProfessor() {
        return userSession.isProfessor();
    }

    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    public Project getProjectById(long id){
        return projectRepository.findById(id);
    }

    public void deleteProjectById(long id){
        projectRepository.deleteById(id);
    }

    public String uploadProject(long id, MultipartFile file){
        try {
            byte[] bytes = file.getBytes();
            String projectUpdloadedId = Math.abs(userSession.hashCode()) + "_" + System.nanoTime();
            Path path = Paths.get(projectsPath + "/" + projectUpdloadedId + ".zip");
            Files.write(path, bytes);
            return projectUpdloadedId;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void uploadProjectSet(long id, MultipartFile file){
        try {
            byte[] bytes = file.getBytes();
            Path path = Paths.get(projectsPath + "/" + id + ".zip");
            Files.write(path, bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean reportAlreadyExists(String key) {
        return userSession.getStudentReports().get(key) != null;
    }

    public void createProject(Project project, MultipartFile file) {
        try {
            projectRepository.save(project);
            long id = project.getId();
            //crear carpeta en /references cuyo nombre sea el id del proyecto en la base de datos
            File newReference = new File(referencesPath + "/" + id);
            //copiar el fichero zip en esa carpeta
            byte[] bytes = file.getBytes();
            Path path = Paths.get(referencesPath + "/" + id + ".zip");
            File zippedProject = path.toFile();
            Files.write(path, bytes);
            //guardar array de bytes en el proyecto
            project.setReferenceFile(bytes);
            //unzipear el proyecto
            File referenceProjectDir = unzipReferenceProject(zippedProject, new File(referencesPath));
            //eliminar el fichero zipeado y quedarse solo con el directorio unzippeado
            deleteZippedFile(zippedProject);
            //cambiar el fichero build.properties del proyecto
            replaceNbProjectFiles(referenceProjectDir);

            project.setPathToDirectory(referenceProjectDir);
            projectRepository.save(project);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void replaceNbProjectFiles(File referenceProjectDir) {
        String pathToBuildProperties = referenceProjectDir.getAbsolutePath() + "/nbproject/private/private.properties";
        File f = new File(pathToBuildProperties);
        if (f.exists() && !f.isDirectory()) {
            try {
                // input the file content to the StringBuffer "input"
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
                System.out.println(inputStr); // display the original file for debugging

                // write the new string with the replaced line OVER the same file
                FileOutputStream fileOut = new FileOutputStream(f);
                fileOut.write(inputStr.getBytes());
                fileOut.close();
            } catch (Exception e) {
                System.out.println("Problem modifying file: " + pathToBuildProperties);
            }
        }
    }

    private File unzipReferenceProject(File zippedFile, File destination){
        File unzippedFile = null;
        String unzippedFileName = zippedFile.getName().replace(".zip", "");
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("bash", "-c", "unzip " + zippedFile.getAbsolutePath() + " -d " +
                destination.getAbsolutePath() + "/" + unzippedFileName);
        try {
            Process process = processBuilder.start();
            int exitVal = process.waitFor();
            if (exitVal != 0)
                throw new RuntimeException("Failure unzipping: " + zippedFile.getName());

            unzippedFile = new File (destination.getPath() + "/" + unzippedFileName);
            File[] files = unzippedFile.listFiles();
            for (File f: Objects.requireNonNull(files)){
                return f;
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            System.err.println("Failure unzipping: " + zippedFile.getName());
        }
        return unzippedFile;
    }

    private void mkdir(File file){
        try {
            FileUtils.forceMkdir(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteZippedFile(File file){
        try {
            FileUtils.forceDelete(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean globalReportAlreadyExists(Project p) {
        return userSession.getGlobalReports().get(Long.toString(p.getId())) != null;
    }
}
