package rater.web.app.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AppController {

    @GetMapping("/")
    public String prueba(){
        return "app";
    }
}
