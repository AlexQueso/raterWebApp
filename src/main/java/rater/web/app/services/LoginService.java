package rater.web.app.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import rater.web.app.session.UserSession;

import java.util.HashMap;

@Component
public class LoginService {

    private final UserSession userSession;

    @Autowired
    public LoginService(UserSession userSession){
        this.userSession = userSession;
    }

    public boolean signInSuccessfully(String user, String password) {
        if (user.equals("q") && password.equals("q")){
            userSession.setProfessor(true);
            userSession.setGlobalReports(new HashMap<>());
            userSession.setIndividualReports(new HashMap<>());
            return true;
        }
        return false;
    }
}
