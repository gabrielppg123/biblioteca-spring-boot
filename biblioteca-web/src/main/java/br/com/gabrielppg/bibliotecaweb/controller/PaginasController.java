
package br.com.gabrielppg.bibliotecaweb.controller;

import br.com.gabrielppg.bibliotecaweb.client.BibliotecaApiClient;
import br.com.gabrielppg.bibliotecaweb.dto.PaginaLivros;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.ResourceAccessException;

import java.util.List;
import java.util.UUID;

@Controller
public class PaginasController {

    private final BibliotecaApiClient api;

    public PaginasController(BibliotecaApiClient api) {
        this.api = api;
    }

    @GetMapping("/")
    public String inicio() {
        return "inicio";
    }

    @GetMapping("/livros")
    public String livros(
            @RegisteredOAuth2AuthorizedClient("biblioteca") OAuth2AuthorizedClient cliente,
            @RequestParam(required = false) String id,
            @RequestParam(required = false) String titulo,
            @RequestParam(required = false) String isbn,
            @RequestParam(required = false) String nomeAutor,
            @RequestParam(defaultValue = "0") int pagina,
            Model modelo) {

        int paginaValidada = Math.max(0, pagina);

        modelo.addAttribute("id", id);
        modelo.addAttribute("titulo", titulo);
        modelo.addAttribute("isbn", isbn);
        modelo.addAttribute("nomeAutor", nomeAutor);
        modelo.addAttribute("paginaAnterior", Math.max(0, paginaValidada - 1));
        modelo.addAttribute("paginaSeguinte", paginaValidada + 1);

        String accessToken = cliente.getAccessToken().getTokenValue();

        // Pesquisa individual por ID
        if (id != null && !id.isBlank()) {
            UUID livroId;

            try {
                livroId = UUID.fromString(id.trim());
            } catch (IllegalArgumentException ex) {
                modelo.addAttribute("resultado", paginaVazia());
                modelo.addAttribute("erro", "Informe um UUID válido para pesquisar o livro.");
                return "livros";
            }

            try {
                var livro = api.consultarLivroPorId(accessToken, livroId);

                modelo.addAttribute("resultado",
                        new PaginaLivros(List.of(livro), 0, 1, 1, true, true));

            } catch (RestClientResponseException ex) {
                modelo.addAttribute("resultado", paginaVazia());

                if (ex.getStatusCode().value() == 404) {
                    modelo.addAttribute("erro", "Nenhum livro encontrado com o ID informado.");
                } else {
                    modelo.addAttribute("erro", mensagemErro(ex.getStatusCode().value()));
                }

            } catch (ResourceAccessException ex) {
                modelo.addAttribute("resultado", paginaVazia());
                modelo.addAttribute("erro", "Não foi possível acessar a API da Biblioteca.");
            }

            return "livros";
        }

        // Pesquisa normal com filtros e paginação
        try {
            var resultado = api.consultarLivros(
                    accessToken, titulo, isbn, nomeAutor, paginaValidada);

            modelo.addAttribute("resultado", resultado);

        } catch (RestClientResponseException ex) {
            modelo.addAttribute("erro", mensagemErro(ex.getStatusCode().value()));

        } catch (ResourceAccessException ex) {
            modelo.addAttribute("erro", "Não foi possível acessar a API da Biblioteca.");
        }

        return "livros";
    }

    @GetMapping("/autores")
    public String autores(
            @RegisteredOAuth2AuthorizedClient("biblioteca") OAuth2AuthorizedClient cliente,
            @RequestParam(required = false) String id,
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String nacionalidade,
            Model modelo) {

        modelo.addAttribute("id", id);
        modelo.addAttribute("nome", nome);
        modelo.addAttribute("nacionalidade", nacionalidade);

        String accessToken = cliente.getAccessToken().getTokenValue();

        // Pesquisa individual por ID
        if (id != null && !id.isBlank()) {
            UUID autorId;

            try {
                autorId = UUID.fromString(id.trim());
            } catch (IllegalArgumentException ex) {
                modelo.addAttribute("autores", List.of());
                modelo.addAttribute("erro", "Informe um UUID válido para pesquisar o autor.");
                return "autores";
            }

            try {
                var autor = api.consultarAutorPorId(accessToken, autorId);
                modelo.addAttribute("autores", List.of(autor));

            } catch (RestClientResponseException ex) {
                modelo.addAttribute("autores", List.of());

                if (ex.getStatusCode().value() == 404) {
                    modelo.addAttribute("erro", "Nenhum autor encontrado com o ID informado.");
                } else {
                    modelo.addAttribute("erro", mensagemErro(ex.getStatusCode().value()));
                }

            } catch (ResourceAccessException ex) {
                modelo.addAttribute("autores", List.of());
                modelo.addAttribute("erro", "Não foi possível acessar a API da Biblioteca.");
            }

            return "autores";
        }

        // Pesquisa normal com filtros
        try {
            modelo.addAttribute("autores",
                    api.consultarAutores(accessToken, nome, nacionalidade));

        } catch (RestClientResponseException ex) {
            modelo.addAttribute("erro", mensagemErro(ex.getStatusCode().value()));

        } catch (ResourceAccessException ex) {
            modelo.addAttribute("erro", "Não foi possível acessar a API da Biblioteca.");
        }

        return "autores";
    }

    private static PaginaLivros paginaVazia() {
        return new PaginaLivros(List.of(), 0, 0, 0, true, true);
    }

    private String mensagemErro(int status) {
        return switch (status) {
            case 401 -> "O token foi recusado pela API. Entre novamente e confira o emissor do JWT.";
            case 403 -> "Sua conta não tem permissão para realizar esta consulta.";
            default -> "A API respondeu com HTTP " + status + ". Consulte os logs do servidor.";
        };
    }
}
