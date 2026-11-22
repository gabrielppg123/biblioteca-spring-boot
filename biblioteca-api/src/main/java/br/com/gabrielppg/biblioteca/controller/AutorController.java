package br.com.gabrielppg.biblioteca.controller;

import br.com.gabrielppg.biblioteca.controller.dto.AutorDTO;
import br.com.gabrielppg.biblioteca.controller.mappers.AutorMapper;
import br.com.gabrielppg.biblioteca.model.Autor;
import br.com.gabrielppg.biblioteca.service.AutorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/autores")
@RequiredArgsConstructor
@Tag(name="Autores")
@Slf4j
public class AutorController implements GenericController {

    private final AutorService service;
    private final AutorMapper mapper;

    @PostMapping
    @PreAuthorize("hasAuthority('GERENTE')")
    @Operation(summary="Salvar", description = "Cadastrar novo autor")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cadastrado com sucesso."),
            @ApiResponse(responseCode = "422", description = "Erro de validação."),
            @ApiResponse(responseCode = "409", description = "Autor já cadastrado.")
    })
    public ResponseEntity<Void> salvar(@RequestBody @Valid AutorDTO dto
    ){
        log.info("Cadastrando novo autor: {}", dto.nome());
        Autor autor = mapper.toEntity(dto);
        service.salvar(autor);
        URI location = gerarHeaderLocation(autor.getId());
        return ResponseEntity.created(location).build();
    }

    @GetMapping("{id}")
    @PreAuthorize("hasAnyAuthority('OPERADOR', 'GERENTE', 'SCOPE_biblioteca.read')")
    @Operation(summary="Obter Detalhes", description = "Retorna os dados do autor pelo ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Autor encontrado."),
            @ApiResponse(responseCode = "404", description = "Autor não encontrado.")
    })
    public ResponseEntity<AutorDTO> obterDetalhes(@PathVariable("id") UUID id) {
        return service.obterAutorPeloId(id)
                .map(autor -> ResponseEntity.ok(mapper.toDTO(autor)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("{id}")
    @PreAuthorize("hasAuthority('GERENTE')")
    @Operation(summary="Deletar", description = "Deleta um autor pelo ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Deletado com sucesso."),
            @ApiResponse(responseCode = "404", description = "Autor não encontrado."),
            @ApiResponse(responseCode = "400", description = "Autor possui livro(s) cadastrado(s).")
    })
    public ResponseEntity<Void> deletar(@PathVariable("id") UUID id){
        log.info("Deletando autor de id: {}", id);
        Optional<Autor> autor = service.obterAutorPeloId(id);
        if (autor.isEmpty()) {
            return ResponseEntity.notFound().build();
        } else {
            service.deletar(autor.get());
            return ResponseEntity.noContent().build();
        }
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('OPERADOR', 'GERENTE', 'SCOPE_biblioteca.read')")
    @Operation(summary="Pesquisar", description = "Realiza pesquisa de autores por parâmetros.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pesquisa realizadas com sucesso.")
    })
    public ResponseEntity<List<AutorDTO>> pesquisar(
            @RequestParam(value = "nome", required = false) String nome,
            @RequestParam(value = "nacionalidade", required = false) String nacionalidade
    ){
        List<Autor> resultado = service.pesquisaByExample(nome, nacionalidade);

        List<AutorDTO> lista = resultado.
                stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(lista);
    }

    @PutMapping("{id}")
    @PreAuthorize("hasAuthority('GERENTE')")
    @Operation(
            summary = "Atualizar",
            description = "Atualiza um autor pelo ID."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Atualizado com sucesso."
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Autor não encontrado."
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Autor já cadastrado."
            )
    })
    public ResponseEntity<Void> atualizar(
            @PathVariable("id") UUID id,
            @RequestBody @Valid AutorDTO dto) {

        Autor dados = mapper.toEntity(dto);

        service.atualizar(id, dados);

        return ResponseEntity.noContent().build();
    }

}
