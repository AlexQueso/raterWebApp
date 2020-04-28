package rater.web.app.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import rater.web.app.classes.Project;
import rater.web.app.repositories.ProjectRepository;
import rater.web.app.session.UserSession;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.HashMap;
import java.util.List;

@Component
public class AppService {

    private final UserSession userSession;
    private final ProjectRepository projectRepository;

    @Autowired
    public AppService(UserSession userSession, ProjectRepository projectRepository) {
        this.userSession = userSession;
        this.projectRepository = projectRepository;
    }

    @PostConstruct
    public void initDataBase(){
        Project p1 = new Project("Práctica 1", "Árboles n-arios y binarios", new File(""));
        Project p2 = new Project("Práctica 2", "\n", new File(""));
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
}
