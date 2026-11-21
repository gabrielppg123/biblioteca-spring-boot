package br.com.gabrielppg.bibliotecaweb.client;

import br.com.gabrielppg.bibliotecaweb.dto.AutorDTO;
import br.com.gabrielppg.bibliotecaweb.dto.PaginaLivros;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class BibliotecaApiClient {

    private final RestClient api;

    public BibliotecaApiClient(@Value("${biblioteca.api.base-url}") String baseUrl) {
        this.api = RestClient.builder().baseUrl(baseUrl).build();
    }

    public PaginaLivros consultarLivros(
            String accessToken, String titulo, String isbn, String nomeAutor, int pagina) {
        return api.get()
                .uri(builder -> {
                    builder.path("/livros")
                            .queryParam("pagina", pagina)
                            .queryParam("tamanho-pagina", 10);
                    if (titulo != null && !titulo.isBlank()) {
                        builder.queryParam("titulo", titulo.trim());
                    }
                    if (isbn != null && !isbn.isBlank()) {
                        builder.queryParam("isbn", isbn.trim());
                    }
                    if (nomeAutor != null && !nomeAutor.isBlank()) {
                        builder.queryParam("nome-autor", nomeAutor.trim());
                    }
                    return builder.build();
                })
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .body(new ParameterizedTypeReference<PaginaLivros>() {});
    }

    public List<AutorDTO> consultarAutores(String accessToken, String nome, String nacionalidade) {
        return api.get()
                .uri(builder -> {
                    builder.path("/autores");
                    if (nome != null && !nome.isBlank()) {
                        builder.queryParam("nome", nome.trim());
                    }
                    if (nacionalidade != null && !nacionalidade.isBlank()) {
                        builder.queryParam("nacionalidade", nacionalidade.trim());
                    }
                    return builder.build();
                })
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .body(new ParameterizedTypeReference<List<AutorDTO>>() {});
    }
}
