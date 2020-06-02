package rater.web.app.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import rater.web.app.session.UserSession;

import java.util.HashMap;

@Component
public class LoginService {

    @Value("${app.username}")
    private String username;
    @Value("${app.password}")
    private String password;

    private final UserSession userSession;

    @Autowired
    public LoginService(UserSession userSession){
        this.userSession = userSession;
    }

    public boolean signInSuccessfully(String user, String password) {
        if (user.equals(this.username) && password.equals(this.password)){
            userSession.setProfessor(true);
            userSession.setGlobalReports(new HashMap<>());
            return true;
        }
        return false;
    }
}
