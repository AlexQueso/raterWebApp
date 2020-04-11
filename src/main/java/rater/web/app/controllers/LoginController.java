package rater.web.app.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import rater.web.app.services.LoginService;
import rater.web.app.utils.Utils;

import javax.servlet.http.HttpSession;

@Controller
public class LoginController {

    public final LoginService loginService;

    @Autowired
    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    @PostMapping(value = "/signing_in")
    public String signIn(Model model, @RequestParam String user, @RequestParam String password) {
        if (loginService.signInSuccessfully(user, password)){
            return Utils.redirectTo("/");
        }

        return "login";
    }

    @GetMapping("/log-out")
    public String logOut(HttpSession session){
        session.invalidate();
        return Utils.redirectTo("/");
    }
}
