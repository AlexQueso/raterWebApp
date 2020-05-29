package rater.web.app.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import rater.web.app.classes.Project;
import rater.web.app.repositories.ProjectRepository;
import rater.web.app.session.UserSession;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Component
public class AppService {

    private final UserSession userSession;
    private final ProjectRepository projectRepository;
    private static String UPLOADED_FOLDER = "/home/alex/Desktop/projects/students/"; //dentro del docker

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

    public void saveProject(Project project) {
        projectRepository.save(project);
    }

    public void deleteProjectById(long id){
        projectRepository.deleteById(id);
    }

    public String uploadProject(long id, MultipartFile file){
        try {
            byte[] bytes = file.getBytes();
            String projectUpdloadedId = Math.abs(userSession.hashCode()) + "_" + System.nanoTime();
            Path path = Paths.get(UPLOADED_FOLDER + projectUpdloadedId + ".zip");
            Files.write(path, bytes);
            return projectUpdloadedId;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean reportAlreadyExists(String key) {
        return userSession.getStudentReports().get(key) != null;
    }
}
