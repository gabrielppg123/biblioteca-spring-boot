package br.com.gabrielppg.bibliotecaweb.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** Encaminha ao Resource Server o access token do usuario, nunca client_credentials. */
@Component
public class CadastroApiClient {

    private final RestClient api;

    public CadastroApiClient(@Value("${biblioteca.api.base-url}") String baseUrl) {
        this.api = RestClient.builder().baseUrl(baseUrl).build();
    }

    public void cadastrarAutor(String accessToken, String nome,
                               LocalDate nascimento, String nacionalidade) {
        api.post()
                .uri("/autores")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new AutorRequisicao(null, nome, nascimento, nacionalidade))
                .retrieve()
                .toBodilessEntity();
    }

    public void atualizarAutor(String accessToken, UUID id, String nome,
                               LocalDate nascimento, String nacionalidade) {
        api.put()
                .uri("/autores/{id}", id)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new AutorRequisicao(id, nome, nascimento, nacionalidade))
                .retrieve()
                .toBodilessEntity();
    }

    public void cadastrarLivro(String accessToken, String isbn, String titulo,
                               LocalDate publicacao, String genero,
                               BigDecimal preco, UUID idAutor) {
        api.post()
                .uri("/livros")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new LivroRequisicao(isbn, titulo, publicacao, genero, preco, idAutor))
                .retrieve()
                .toBodilessEntity();
    }

    public void atualizarLivro(String accessToken, UUID id, String isbn, String titulo,
                               LocalDate publicacao, String genero,
                               BigDecimal preco, UUID idAutor) {
        api.put()
                .uri("/livros/{id}", id)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new LivroRequisicao(isbn, titulo, publicacao, genero, preco, idAutor))
                .retrieve()
                .toBodilessEntity();
    }

    public void deletarLivro(String accessToken, UUID id) {
        api.delete()
                .uri("/livros/{id}", id)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .toBodilessEntity();
    }

    public void deletarAutor(String accessToken, UUID id) {
        api.delete()
                .uri("/autores/{id}", id)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .toBodilessEntity();
    }


    private record AutorRequisicao(UUID id, String nome,
                                  LocalDate dataNascimento, String nacionalidade) {}

    private record LivroRequisicao(String isbn, String titulo,
                                  LocalDate dataPublicacao, String genero,
                                  BigDecimal preco, UUID idAutor) {}
}
