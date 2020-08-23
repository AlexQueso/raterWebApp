package rater.web.app.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import rater.web.app.classes.Project;
import rater.web.app.repositories.ProjectRepository;
import rater.web.app.session.UserSession;

import java.util.HashMap;
import java.util.List;

@Component
public class LoginService {

    @Value("${app.username}")
    private String username;
    @Value("${app.password}")
    private String password;

    private final UserSession userSession;
    private final ProjectRepository projectRepository;

    @Autowired
    public LoginService(UserSession userSession, ProjectRepository projectRepository){
        this.userSession = userSession;
        this.projectRepository = projectRepository;
    }

    public boolean signInSuccessfully(String user, String password) {
        if (user.equals(this.username) && password.equals(this.password)){
            userSession.setProfessor(true);
            if (userSession.getGlobalReports() == null)
                userSession.setGlobalReports(new HashMap<>());
            if (projectRepository.findAll()!=null) {
                for (Project p : projectRepository.findAll()) {
                    if (p.getReports() != null) {
                        if (p.getReports().size() > 0) {
                            userSession.getGlobalReports().put(Long.toString(p.getId()), p.getReports());
                        }
                    }
                }
            }

            return true;
        }
        return false;
    }

}