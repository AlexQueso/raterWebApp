package rater.web.app.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import rater.web.app.session.UserSession;

import java.util.HashMap;

@Component
public class AppService {

    private final UserSession userSession;

    @Autowired
    public AppService(UserSession userSession) {
        this.userSession = userSession;
    }

    public boolean userIsProfessor() {
        return userSession.isProfessor();
    }

    public void homeOverview() {
        boolean b = userSession.isNewUser();
        if (userSession.isNewUser())
            userSession.setNewUser(false);

        //todo get projects from data base to show them up in the home page
        //return List<Project> practicas
    }
}
