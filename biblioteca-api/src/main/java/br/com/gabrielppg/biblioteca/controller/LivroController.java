package br.com.gabrielppg.biblioteca.controller;

import br.com.gabrielppg.biblioteca.controller.dto.CadastroLivroDTO;
import br.com.gabrielppg.biblioteca.controller.dto.PesquisaLivroDTO;
import br.com.gabrielppg.biblioteca.controller.mappers.LivroMapper;
import br.com.gabrielppg.biblioteca.model.GeneroLivro;
import br.com.gabrielppg.biblioteca.model.Livro;
import br.com.gabrielppg.biblioteca.service.LivroService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import br.com.gabrielppg.biblioteca.controller.dto.PaginaResposta;

import java.util.UUID;

@RestController
@RequestMapping("livros")
@RequiredArgsConstructor
public class LivroController implements GenericController {
    private final LivroService service;
    private final LivroMapper mapper;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('OPERADOR', 'GERENTE')")
    public ResponseEntity<Void> salvar(
            @RequestBody @Valid CadastroLivroDTO dto) {

        Livro livro = mapper.toEntity(dto);

        service.salvar(livro, dto.idAutor());

        return ResponseEntity
                .created(gerarHeaderLocation(livro.getId()))
                .build();
    }


    @GetMapping("{id}")
    @PreAuthorize("hasAnyAuthority('OPERADOR', 'GERENTE', 'SCOPE_biblioteca.read')")
    public ResponseEntity<PesquisaLivroDTO> obterDetalhes(@PathVariable("id") UUID id){
        return service.obterPorId(id)
                .map(livro -> {
                    var dto = mapper.toDTO(livro);
                    return ResponseEntity.ok(dto);
                }).orElseGet( () -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("{id}")
    @PreAuthorize("hasAnyAuthority('OPERADOR', 'GERENTE')")
    public ResponseEntity<Object> deletar(@PathVariable("id") UUID id){
        return service.obterPorId(id)
                .map(livro -> {
                    service.deletar(livro);
                    return ResponseEntity.noContent().build();
                }).orElseGet( () -> ResponseEntity.notFound().build());
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('OPERADOR', 'GERENTE', 'SCOPE_biblioteca.read')")
    public ResponseEntity<PaginaResposta<PesquisaLivroDTO>> pesquisa(
            @RequestParam(value="isbn", required = false) String isbn,
            @RequestParam(value="titulo", required = false) String titulo,
            @RequestParam(value="nome-autor", required = false) String nomeAutor,
            @RequestParam(value="genero", required = false) GeneroLivro genero,
            @RequestParam(value="ano-publicacao", required = false) Integer anoPublicacao,
            @RequestParam(value="pagina", defaultValue = "0") Integer pagina,
            @RequestParam(value="tamanho-pagina", defaultValue = "10") Integer tamanhoPagina
            ){

        Page<Livro> paginaResultado = service.pesquisa(isbn, titulo, nomeAutor, genero, anoPublicacao,pagina,tamanhoPagina);

        Page<PesquisaLivroDTO> resultado = paginaResultado.map(mapper::toDTO);

        return ResponseEntity.ok(PaginaResposta.de(resultado));
    }

    @PutMapping("{id}")
    @PreAuthorize("hasAnyAuthority('OPERADOR', 'GERENTE')")
    public ResponseEntity<Void> atualizar(
            @PathVariable UUID id,
            @RequestBody @Valid CadastroLivroDTO dto) {

        Livro dados = mapper.toEntity(dto);

        service.atualizar(id, dados, dto.idAutor());

        return ResponseEntity.noContent().build();
    }

}
