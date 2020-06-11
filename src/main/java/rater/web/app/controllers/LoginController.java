package rater.web.app.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import rater.web.app.classes.Breadcrumb;
import rater.web.app.services.LoginService;
import rater.web.app.utils.Utils;

import javax.servlet.http.HttpSession;
import java.util.LinkedList;

@Controller
public class LoginController {

    public final LoginService loginService;

    @Autowired
    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    /**
     * Login page
     */
    @GetMapping("/login")
    public String goToLogIn(Model model){
        model.addAttribute("login", true);

        //breadcrumb
        LinkedList<Breadcrumb> breadcrumbs = new LinkedList<>();
        breadcrumbs.add(new Breadcrumb("Inicio", "/"));
        model.addAttribute("breadcrumb-list", breadcrumbs);
        model.addAttribute("breadcrumb-active", "Iniciar Sesión");
        return "app";
    }

    /**
     * sign in form, check credentials
     * @param user user
     * @param password password
     * @return redirects to main page
     */
    @PostMapping(value = "/signing-in")
    public String signIn(Model model, @RequestParam String user, @RequestParam String password) {
        if (loginService.signInSuccessfully(user, password)){
            return Utils.redirectTo("/");
        }

        return Utils.redirectTo("/login");
    }

    /**
     * Log out
     * @param session httpsession
     * @return redirects to main page
     */
    @GetMapping("/log-out")
    public String logOut(HttpSession session){
        session.invalidate();
        return Utils.redirectTo("/");
    }

}