package br.com.gabrielppg.authorizationserver.controller;

import br.com.gabrielppg.authorizationserver.controller.dto.UsuarioDTO;
import br.com.gabrielppg.authorizationserver.controller.mappers.UsuarioMapper;
import br.com.gabrielppg.authorizationserver.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {
    private final UsuarioService service;
    private final UsuarioMapper mapper;

    @PostMapping
    @PreAuthorize("hasAuthority('GERENTE')")
    @ResponseStatus(HttpStatus.CREATED)
    public void salvar(@RequestBody @Valid UsuarioDTO dto) {
        var usuario = mapper.toEntity(dto);
        service.salvar(usuario);
    }
}
