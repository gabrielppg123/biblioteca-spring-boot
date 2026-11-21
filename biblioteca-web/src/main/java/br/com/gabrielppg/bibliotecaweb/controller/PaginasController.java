package br.com.gabrielppg.bibliotecaweb.controller;

import br.com.gabrielppg.bibliotecaweb.client.BibliotecaApiClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.ResourceAccessException;

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
            @RequestParam(required = false) String titulo,
            @RequestParam(required = false) String isbn,
            @RequestParam(required = false) String nomeAutor,
            @RequestParam(defaultValue = "0") int pagina,
            Model modelo) {
        int paginaValidada = Math.max(0, pagina);
        modelo.addAttribute("titulo", titulo);
        modelo.addAttribute("isbn", isbn);
        modelo.addAttribute("nomeAutor", nomeAutor);
        modelo.addAttribute("paginaAnterior", Math.max(0, paginaValidada - 1));
        modelo.addAttribute("paginaSeguinte", paginaValidada + 1);

        try {
            var resultado = api.consultarLivros(
                    cliente.getAccessToken().getTokenValue(), titulo, isbn, nomeAutor, paginaValidada);
            modelo.addAttribute("resultado", resultado);
        } catch (RestClientResponseException ex) {
            modelo.addAttribute("erro", mensagemErro(ex.getStatusCode().value()));
        } catch (ResourceAccessException ex) {
            modelo.addAttribute("erro", "A API da Biblioteca não está acessível na porta 8082.");
        }
        return "livros";
    }

    @GetMapping("/autores")
    public String autores(
            @RegisteredOAuth2AuthorizedClient("biblioteca") OAuth2AuthorizedClient cliente,
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String nacionalidade,
            Model modelo) {
        modelo.addAttribute("nome", nome);
        modelo.addAttribute("nacionalidade", nacionalidade);
        try {
            modelo.addAttribute("autores", api.consultarAutores(
                    cliente.getAccessToken().getTokenValue(), nome, nacionalidade));
        } catch (RestClientResponseException ex) {
            modelo.addAttribute("erro", mensagemErro(ex.getStatusCode().value()));
        } catch (ResourceAccessException ex) {
            modelo.addAttribute("erro", "A API da Biblioteca não está acessível na porta 8082.");
        }
        return "autores";
    }

    private String mensagemErro(int status) {
        return switch (status) {
            case 401 -> "O token foi recusado pela API. Entre novamente e confira o emissor do JWT.";
            case 403 -> "Sua conta não tem permissão para realizar esta consulta.";
            default -> "A API respondeu com HTTP " + status + ". Consulte os logs do servidor.";
        };
    }
}
