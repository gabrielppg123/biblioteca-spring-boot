package br.com.gabrielppg.authorizationserver.controller.admin;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasAuthority('GERENTE')")
public class AdminController {

    @GetMapping
    public String inicio() {
        return "admin/inicio";
    }
}
