
package br.com.gabrielppg.biblioteca.repository;

import br.com.gabrielppg.biblioteca.model.Autor;
import br.com.gabrielppg.biblioteca.model.GeneroLivro;
import br.com.gabrielppg.biblioteca.model.Livro;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
@Testcontainers
class LivroRepositoryIntegracaoTest {

    @Container
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:16.3");

    @DynamicPropertySource
    static void configurarBanco(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",
                postgres::getJdbcUrl);

        registry.add("spring.datasource.username",
                postgres::getUsername);

        registry.add("spring.datasource.password",
                postgres::getPassword);
    }

    @Autowired
    private LivroRepository livroRepository;

    @Autowired
    private AutorRepository autorRepository;

    @Test
    void devePersistirLivroComAutorNoPostgresql() {

        UUID usuarioId = UUID.randomUUID();

        Autor autor = new Autor();
        autor.setNome("Machado de Assis");
        autor.setNacionalidade("Brasileiro");
        autor.setDataNascimento(
                LocalDate.of(1839, 6, 21)
        );
        autor.setIdUsuario(usuarioId);

        autorRepository.saveAndFlush(autor);

        Livro livro = new Livro();
        livro.setTitulo("Dom Casmurro");
        livro.setIsbn("9788535914849");
        livro.setGenero(GeneroLivro.FICCAO);
        livro.setDataPublicacao(
                LocalDate.of(1899, 1, 1)
        );
        livro.setPreco(new BigDecimal("39.90"));
        livro.setAutor(autor);
        livro.setIdUsuario(usuarioId);

        livroRepository.saveAndFlush(livro);

        var resultado = livroRepository.findByIsbn(
                "9788535914849"
        );

        assertTrue(resultado.isPresent());

        assertEquals(
                "Dom Casmurro",
                resultado.orElseThrow().getTitulo()
        );

        assertEquals(
                autor.getId(),
                resultado.orElseThrow().getAutor().getId()
        );
    }
}