
package br.com.gabrielppg.biblioteca.service;

import br.com.gabrielppg.biblioteca.exception.OperacaoNaoPermitidaException;
import br.com.gabrielppg.biblioteca.model.Autor;
import br.com.gabrielppg.biblioteca.repository.AutorRepository;
import br.com.gabrielppg.biblioteca.repository.LivroRepository;
import br.com.gabrielppg.biblioteca.security.SecurityService;
import br.com.gabrielppg.biblioteca.validator.AutorValidator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import br.com.gabrielppg.biblioteca.exception.RegistroNaoEncontradoException;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class AutorServiceTest {

    @Mock
    private AutorRepository repository;

    @Mock
    private AutorValidator validator;

    @Mock
    private LivroRepository livroRepository;

    @Mock
    private SecurityService securityService;

    @InjectMocks
    private AutorService service;

    @Test
    void deveAssociarUsuarioLogadoAoSalvarAutor() {
        Autor autor = new Autor();
        UUID usuarioId = UUID.randomUUID();

        when(securityService.obterIdUsuarioLogado())
                .thenReturn(usuarioId);

        when(repository.save(autor))
                .thenReturn(autor);

        Autor resultado = service.salvar(autor);

        assertEquals(usuarioId, resultado.getIdUsuario());

        verify(validator).validar(autor);
        verify(repository).save(autor);
    }

    @Test
    void deveImpedirExclusaoDeAutorComLivros() {
        Autor autor = new Autor();

        when(livroRepository.existsByAutor(autor))
                .thenReturn(true);

        assertThrows(
                OperacaoNaoPermitidaException.class,
                () -> service.deletar(autor)
        );

        verify(repository, never()).delete(any(Autor.class));
    }

    @Test
    void devePermitirExclusaoDeAutorSemLivros() {
        Autor autor = new Autor();

        when(livroRepository.existsByAutor(autor))
                .thenReturn(false);

        service.deletar(autor);

        verify(repository).delete(autor);
    }


    @Test
    void deveImpedirAtualizacaoDeAutorSemId() {
        Autor dados = new Autor();

        assertThrows(
                IllegalArgumentException.class,
                () -> service.atualizar(null, dados)
        );

        verifyNoInteractions(repository, validator);
    }


    @Test
    void deveAtualizarAutorPreservandoUsuario() {
        UUID id = UUID.randomUUID();

        UUID usuarioOriginal = UUID.randomUUID();

        Autor existente = new Autor();
        existente.setId(id);
        existente.setNome("Nome Antigo");
        existente.setIdUsuario(usuarioOriginal);

        Autor dados = new Autor();
        dados.setNome("Nome Atualizado");
        dados.setNacionalidade("Brasileiro");
        dados.setDataNascimento(LocalDate.of(1990, 1, 1));

        when(repository.findById(id))
                .thenReturn(Optional.of(existente));

        service.atualizar(id, dados);

        assertEquals("Nome Atualizado", existente.getNome());
        assertEquals("Brasileiro", existente.getNacionalidade());

        assertEquals(
                LocalDate.of(1990, 1, 1),
                existente.getDataNascimento()
        );

        assertEquals(id, existente.getId());
        assertEquals(usuarioOriginal, existente.getIdUsuario());

        verify(validator).validar(existente);
        verify(repository).save(existente);
    }

    @Test
    void deveImpedirAtualizacaoDeAutorInexistente() {
        UUID id = UUID.randomUUID();
        Autor dados = new Autor();

        when(repository.findById(id))
                .thenReturn(Optional.empty());

        assertThrows(
                RegistroNaoEncontradoException.class,
                () -> service.atualizar(id, dados)
        );

        verify(repository, never()).save(any());
        verifyNoInteractions(validator);
    }


}
