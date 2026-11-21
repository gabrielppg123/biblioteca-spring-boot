package br.com.gabrielppg.authorizationserver.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class LoginViewController {
    @GetMapping("/login")
    public String login() { return "login"; }

    @GetMapping("/")
    @ResponseBody
    public String paginaInicial(Authentication authentication) {
        return "Authorization Server - autenticado como " + authentication.getName();
    }
}
