package rater.web.app.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import rater.web.app.classes.Project;
import rater.web.app.repositories.ProjectRepository;
import rater.web.app.session.UserSession;

import javax.annotation.PostConstruct;
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

    public boolean userIsProfessor() {
        return userSession.isProfessor();
    }

    public List<Project> homeOverview() {
        return projectRepository.findAll();
    }

    @PostConstruct
    public void initDataBase(){
        Project p1 = new Project("Práctica 1");
        Project p2 = new Project("Práctica 2");
        Project p3 = new Project("Práctica 3");
        Project p4 = new Project("Examen Final");
        projectRepository.save(p1);
        projectRepository.save(p2);
        projectRepository.save(p3);
        projectRepository.save(p4);
    }
}
