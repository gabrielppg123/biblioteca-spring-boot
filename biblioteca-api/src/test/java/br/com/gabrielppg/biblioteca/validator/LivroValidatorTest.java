
package br.com.gabrielppg.biblioteca.validator;

import br.com.gabrielppg.biblioteca.exception.CampoInvalidoException;
import br.com.gabrielppg.biblioteca.exception.RegistroDuplicadoException;
import br.com.gabrielppg.biblioteca.model.Livro;
import br.com.gabrielppg.biblioteca.repository.LivroRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LivroValidatorTest {

    @Mock
    private LivroRepository repository;

    @InjectMocks
    private LivroValidator validator;

    @Test
    void devePermitirCadastrarLivroValido() {
        Livro livro = criarLivro(2024, BigDecimal.valueOf(50));

        when(repository.findByIsbn(livro.getIsbn()))
                .thenReturn(Optional.empty());

        assertDoesNotThrow(() -> validator.validar(livro));
    }

    @Test
    void deveBloquearIsbnDuplicado() {
        Livro livro = criarLivro(2024, BigDecimal.valueOf(50));
        Livro existente = criarLivro(2023, BigDecimal.valueOf(40));

        when(repository.findByIsbn(livro.getIsbn()))
                .thenReturn(Optional.of(existente));

        assertThrows(
                RegistroDuplicadoException.class,
                () -> validator.validar(livro)
        );
    }

    @Test
    void deveExigirPrecoParaLivroPublicadoAPartirDe2020() {
        Livro livro = criarLivro(2020, null);

        when(repository.findByIsbn(livro.getIsbn()))
                .thenReturn(Optional.empty());

        CampoInvalidoException exception = assertThrows(
                CampoInvalidoException.class,
                () -> validator.validar(livro)
        );

        assertEquals("preço", exception.getCampo());
    }

    @Test
    void devePermitirLivroAnteriorA2020SemPreco() {
        Livro livro = criarLivro(2019, null);

        when(repository.findByIsbn(livro.getIsbn()))
                .thenReturn(Optional.empty());

        assertDoesNotThrow(() -> validator.validar(livro));
    }

    @Test
    void devePermitirAtualizarLivroComMesmoIsbn() {
        UUID id = UUID.randomUUID();

        Livro livro = criarLivro(2024, BigDecimal.valueOf(50));
        livro.setId(id);

        Livro existente = criarLivro(2024, BigDecimal.valueOf(50));
        existente.setId(id);

        when(repository.findByIsbn(livro.getIsbn()))
                .thenReturn(Optional.of(existente));

        assertDoesNotThrow(() -> validator.validar(livro));
    }

    @Test
    void deveBloquearAtualizacaoComIsbnDeOutroLivro() {
        Livro livro = criarLivro(2024, BigDecimal.valueOf(50));
        livro.setId(UUID.randomUUID());

        Livro existente = criarLivro(2023, BigDecimal.valueOf(40));
        existente.setId(UUID.randomUUID());

        when(repository.findByIsbn(livro.getIsbn()))
                .thenReturn(Optional.of(existente));

        assertThrows(
                RegistroDuplicadoException.class,
                () -> validator.validar(livro)
        );
    }

    private Livro criarLivro(int ano, BigDecimal preco) {
        Livro livro = new Livro();
        livro.setTitulo("Livro de Teste");
        livro.setIsbn("9780134685991");
        livro.setDataPublicacao(LocalDate.of(ano, 1, 1));
        livro.setPreco(preco);

        return livro;
    }
}
