
package br.com.gabrielppg.biblioteca.service;

import br.com.gabrielppg.biblioteca.exception.RegistroDuplicadoException;
import br.com.gabrielppg.biblioteca.exception.RegistroNaoEncontradoException;
import br.com.gabrielppg.biblioteca.model.Autor;
import br.com.gabrielppg.biblioteca.model.GeneroLivro;
import br.com.gabrielppg.biblioteca.model.Livro;
import br.com.gabrielppg.biblioteca.repository.AutorRepository;
import br.com.gabrielppg.biblioteca.repository.LivroRepository;
import br.com.gabrielppg.biblioteca.security.SecurityService;
import br.com.gabrielppg.biblioteca.validator.LivroValidator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LivroServiceTest {

    @Mock
    private LivroRepository repository;

    @Mock
    private AutorRepository autorRepository;

    @Mock
    private LivroValidator validator;

    @Mock
    private SecurityService securityService;

    @InjectMocks
    private LivroService service;

    @Test
    void deveSalvarLivroAssociandoAutorEUsuarioLogado() {
        // Arrange
        Livro livro = new Livro();
        Autor autor = new Autor();
        UUID usuarioId = UUID.randomUUID();
        UUID idAutor = UUID.randomUUID();

        autor.setId(idAutor);

        when(autorRepository.findById(idAutor))
                .thenReturn(Optional.of(autor));

        when(securityService.obterIdUsuarioLogado())
                .thenReturn(usuarioId);

        when(repository.save(livro))
                .thenReturn(livro);

        // Act
        Livro resultado = service.salvar(livro, idAutor);

        // Assert
        assertSame(livro, resultado);
        assertSame(autor, resultado.getAutor());
        assertEquals(usuarioId, resultado.getIdUsuario());

        verify(autorRepository).findById(idAutor);
        verify(validator).validar(livro);
        verify(repository).save(livro);
    }

    @Test
    void deveImpedirCadastroQuandoAutorNaoExiste() {
        // Arrange
        Livro livro = new Livro();
        UUID idAutor = UUID.randomUUID();

        when(autorRepository.findById(idAutor))
                .thenReturn(Optional.empty());

        // Act e Assert
        assertThrows(
                RegistroNaoEncontradoException.class,
                () -> service.salvar(livro, idAutor)
        );

        verify(repository, never()).save(any());
        verifyNoInteractions(validator, securityService);
    }

    @Test
    void naoDeveSalvarLivroQuandoValidacaoFalhar() {
        // Arrange
        Livro livro = new Livro();
        Autor autor = new Autor();
        UUID idAutor = UUID.randomUUID();

        when(autorRepository.findById(idAutor))
                .thenReturn(Optional.of(autor));

        doThrow(new RegistroDuplicadoException("ISBN já cadastrado"))
                .when(validator).validar(livro);

        // Act e Assert
        assertThrows(
                RegistroDuplicadoException.class,
                () -> service.salvar(livro, idAutor)
        );

        verify(repository, never()).save(any());
        verifyNoInteractions(securityService);
    }

    @Test
    void deveImpedirAtualizacaoDeLivroSemId() {
        // Arrange
        Livro dados = new Livro();
        UUID idAutor = UUID.randomUUID();

        // Act e Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> service.atualizar(null, dados, idAutor)
        );

        verifyNoInteractions(
                repository,
                autorRepository,
                validator,
                securityService
        );
    }

    @Test
    void deveAtualizarLivroPreservandoDadosOriginais() {
        // Arrange
        UUID idLivro = UUID.randomUUID();
        UUID idAutor = UUID.randomUUID();

        UUID usuarioOriginal = UUID.randomUUID();
        Autor autor = new Autor();
        autor.setId(idAutor);

        LocalDateTime dataCadastro =
                LocalDateTime.of(2025, 1, 10, 10, 0);

        Livro livroExistente = new Livro();
        livroExistente.setId(idLivro);
        livroExistente.setIdUsuario(usuarioOriginal);
        livroExistente.setDataCadastro(dataCadastro);
        livroExistente.setTitulo("Título Antigo");

        Livro dados = new Livro();
        dados.setTitulo("Título Atualizado");
        dados.setIsbn("9780134685991");
        dados.setDataPublicacao(
                LocalDate.of(2024, 5, 10)
        );
        dados.setGenero(GeneroLivro.FICCAO);
        dados.setPreco(new BigDecimal("59.90"));

        when(repository.findById(idLivro))
                .thenReturn(Optional.of(livroExistente));

        when(autorRepository.findById(idAutor))
                .thenReturn(Optional.of(autor));

        // Act
        service.atualizar(idLivro, dados, idAutor);

        // Assert
        assertEquals(idLivro, livroExistente.getId());

        assertEquals(
                "Título Atualizado",
                livroExistente.getTitulo()
        );

        assertEquals(
                "9780134685991",
                livroExistente.getIsbn()
        );

        assertEquals(
                LocalDate.of(2024, 5, 10),
                livroExistente.getDataPublicacao()
        );

        assertEquals(
                GeneroLivro.FICCAO,
                livroExistente.getGenero()
        );

        assertEquals(
                0,
                new BigDecimal("59.90")
                        .compareTo(livroExistente.getPreco())
        );

        assertSame(autor, livroExistente.getAutor());

        // Dados que não devem ser alterados
        assertEquals(
                usuarioOriginal,
                livroExistente.getIdUsuario()
        );

        assertEquals(
                dataCadastro,
                livroExistente.getDataCadastro()
        );

        verify(validator).validar(livroExistente);
        verify(repository).save(livroExistente);
    }

    @Test
    void deveImpedirAtualizacaoQuandoLivroNaoExiste() {
        // Arrange
        UUID idLivro = UUID.randomUUID();
        UUID idAutor = UUID.randomUUID();
        Livro dados = new Livro();

        when(repository.findById(idLivro))
                .thenReturn(Optional.empty());

        // Act e Assert
        assertThrows(
                RegistroNaoEncontradoException.class,
                () -> service.atualizar(idLivro, dados, idAutor)
        );

        verify(repository, never()).save(any());
        verifyNoInteractions(autorRepository, validator);
    }

    @Test
    void deveImpedirAtualizacaoQuandoAutorNaoExiste() {
        // Arrange
        UUID idLivro = UUID.randomUUID();
        UUID idAutor = UUID.randomUUID();

        Livro livroExistente = new Livro();
        livroExistente.setId(idLivro);

        Livro dados = new Livro();

        when(repository.findById(idLivro))
                .thenReturn(Optional.of(livroExistente));

        when(autorRepository.findById(idAutor))
                .thenReturn(Optional.empty());

        // Act e Assert
        assertThrows(
                RegistroNaoEncontradoException.class,
                () -> service.atualizar(idLivro, dados, idAutor)
        );

        verify(repository, never()).save(any());
        verifyNoInteractions(validator);
    }

    @Test
    void deveExcluirLivro() {
        // Arrange
        Livro livro = new Livro();
        livro.setId(UUID.randomUUID());

        // Act
        service.deletar(livro);

        // Assert
        verify(repository).delete(livro);
        verifyNoMoreInteractions(repository);
    }
}
