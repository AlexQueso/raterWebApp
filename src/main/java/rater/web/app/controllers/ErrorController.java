package rater.web.app.controllers;

import org.springframework.stereotype.Controller;
import rater.web.app.utils.Utils;

@Controller
public class ErrorController implements org.springframework.boot.web.servlet.error.ErrorController {

    @Override
    public String getErrorPath() {
        return Utils.redirectTo("/");
    }
}
