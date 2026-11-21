package br.com.gabrielppg.authorizationserver.controller;

import br.com.gabrielppg.authorizationserver.model.Client;
import br.com.gabrielppg.authorizationserver.service.ClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/clients")
@RequiredArgsConstructor
@Slf4j
public class ClientController {

    private final ClientService service;

    @PostMapping
    @PreAuthorize("hasRole('GERENTE')")
    @ResponseStatus(HttpStatus.CREATED)
    public void salvar (@RequestBody Client client){
        log.info("Registrando novo client: {} com scope: {}", client.getClientId(), client.getScope());
        service.salvar(client);
    }

}
