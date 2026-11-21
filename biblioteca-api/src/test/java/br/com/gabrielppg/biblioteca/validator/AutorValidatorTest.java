
package br.com.gabrielppg.biblioteca.validator;

import br.com.gabrielppg.biblioteca.exception.RegistroDuplicadoException;
import br.com.gabrielppg.biblioteca.model.Autor;
import br.com.gabrielppg.biblioteca.repository.AutorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AutorValidatorTest {

    @Mock
    private AutorRepository repository;

    @InjectMocks
    private AutorValidator validator;

    @Test
    void permiteCadastroQuandoNaoHaDuplicidade() {
        Autor autor = criarAutor(null);

        when(repository.findByNomeAndNacionalidadeAndDataNascimento(
                autor.getNome(),
                autor.getNacionalidade(),
                autor.getDataNascimento()
        )).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> validator.validar(autor));
    }

    @Test
    void permiteAtualizacaoQuandoNaoHaOutroAutorIgual() {
        Autor autor = criarAutor(UUID.randomUUID());

        when(repository.findByNomeAndNacionalidadeAndDataNascimento(
                autor.getNome(),
                autor.getNacionalidade(),
                autor.getDataNascimento()
        )).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> validator.validar(autor));
    }

    @Test
    void permiteAtualizarMesmoAutorSemConsiderarDuplicado() {
        Autor autor = criarAutor(UUID.randomUUID());

        when(repository.findByNomeAndNacionalidadeAndDataNascimento(
                autor.getNome(),
                autor.getNacionalidade(),
                autor.getDataNascimento()
        )).thenReturn(Optional.of(autor));

        assertDoesNotThrow(() -> validator.validar(autor));
    }

    @Test
    void bloqueiaAtualizacaoComOutroAutorJaExistente() {
        Autor autor = criarAutor(UUID.randomUUID());
        Autor outro = criarAutor(UUID.randomUUID());

        when(repository.findByNomeAndNacionalidadeAndDataNascimento(
                autor.getNome(),
                autor.getNacionalidade(),
                autor.getDataNascimento()
        )).thenReturn(Optional.of(outro));

        assertThrows(
                RegistroDuplicadoException.class,
                () -> validator.validar(autor)
        );
    }

    private Autor criarAutor(UUID id) {
        Autor autor = new Autor();
        autor.setId(id);
        autor.setNome("Autor de Teste");
        autor.setNacionalidade("Brasileiro");
        autor.setDataNascimento(LocalDate.of(1990, 5, 10));

        return autor;
    }
}
