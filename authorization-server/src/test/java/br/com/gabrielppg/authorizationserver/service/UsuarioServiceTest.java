package br.com.gabrielppg.authorizationserver.service;

import br.com.gabrielppg.authorizationserver.model.Usuario;
import br.com.gabrielppg.authorizationserver.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UsuarioServiceTest {

    private final UsuarioRepository repository = mock(UsuarioRepository.class);
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(4);
    private final UsuarioService service = new UsuarioService(repository, encoder);

    private Usuario usuario(String papel) {
        Usuario u = new Usuario();
        u.setId(UUID.randomUUID());
        u.setLogin("teste");
        u.setEmail("teste@example.org");
        u.setRoles(List.of(papel));
        u.setSenha(encoder.encode("SenhaBemForte123"));
        return u;
    }

    @Test
    void cadastroDeveCodificarSenha() {
        Usuario usuario = new Usuario();
        usuario.setLogin("novo");
        usuario.setEmail("novo@example.org");
        usuario.setRoles(List.of("OPERADOR"));
        usuario.setSenha("SenhaBemForte123");

        service.salvar(usuario);

        assertTrue(encoder.matches("SenhaBemForte123", usuario.getSenha()));
        assertNotEquals("SenhaBemForte123", usuario.getSenha());
        assertTrue(usuario.isAtivo());
        verify(repository).save(usuario);
    }

    @Test
    void edicaoDeDadosDevePreservarHashDaSenha() {
        Usuario u = usuario("OPERADOR");
        String hashOriginal = u.getSenha();
        when(repository.findById(u.getId())).thenReturn(Optional.of(u));

        service.atualizarDados(u.getId(), "novo-login", "novo@example.org", "GERENTE", UUID.randomUUID());

        assertEquals("novo-login", u.getLogin());
        assertEquals(List.of("GERENTE"), u.getRoles());
        assertEquals(hashOriginal, u.getSenha());
        verify(repository).save(u);
    }

    @Test
    void trocaDeSenhaDeveGerarNovoHashValido() {
        Usuario u = usuario("OPERADOR");
        String hashAntigo = u.getSenha();
        when(repository.findById(u.getId())).thenReturn(Optional.of(u));

        service.redefinirSenha(u.getId(), "NovaSenhaMuitoForte123");

        assertNotEquals(hashAntigo, u.getSenha());
        assertTrue(encoder.matches("NovaSenhaMuitoForte123", u.getSenha()));
        assertFalse(encoder.matches("SenhaBemForte123", u.getSenha()));
    }

    @Test
    void contaExclusivamenteGoogleNaoDeveReceberSenhaPorAcidente() {
        Usuario u = usuario("OPERADOR");
        u.setSenha(null);
        u.setGoogleSub("google-abc");
        when(repository.findById(u.getId())).thenReturn(Optional.of(u));

        assertThrows(IllegalArgumentException.class,
                () -> service.redefinirSenha(u.getId(), "NovaSenhaMuitoForte123"));
        verify(repository, never()).save(any());
    }

    @Test
    void naoPodeDesativarAPropriaConta() {
        Usuario gerente = usuario("GERENTE");
        when(repository.findById(gerente.getId())).thenReturn(Optional.of(gerente));

        assertThrows(IllegalArgumentException.class,
                () -> service.alterarAtivacao(gerente.getId(), false, gerente.getId()));
        assertTrue(gerente.isAtivo());
    }

    @Test
    void naoPodeRetirarProprioPapelGerente() {
        Usuario gerente = usuario("GERENTE");
        when(repository.findById(gerente.getId())).thenReturn(Optional.of(gerente));

        assertThrows(IllegalArgumentException.class,
                () -> service.atualizarDados(gerente.getId(), "teste", "teste@example.org",
                        "OPERADOR", gerente.getId()));
        assertEquals(List.of("GERENTE"), gerente.getRoles());
    }

    @Test
    void naoPodeDesativarUltimoGerenteAtivo() {
        Usuario gerente = usuario("GERENTE");
        when(repository.findById(gerente.getId())).thenReturn(Optional.of(gerente));
        when(repository.contarGerentesAtivos()).thenReturn(1L);

        assertThrows(IllegalArgumentException.class,
                () -> service.alterarAtivacao(gerente.getId(), false, UUID.randomUUID()));
        assertTrue(gerente.isAtivo());
    }

    @Test
    void deveDesativarEReativarOperador() {
        Usuario operador = usuario("OPERADOR");
        when(repository.findById(operador.getId())).thenReturn(Optional.of(operador));
        UUID admin = UUID.randomUUID();

        service.alterarAtivacao(operador.getId(), false, admin);
        assertFalse(operador.isAtivo());
        service.alterarAtivacao(operador.getId(), true, admin);
        assertTrue(operador.isAtivo());
        verify(repository, times(2)).save(operador);
    }

    @Test
    void deveImpedirLoginDuplicadoMesmoComMaiusculas() {
        Usuario novo = new Usuario();
        novo.setLogin("TesTe");
        novo.setEmail("novo@example.org");
        novo.setSenha("SenhaBemForte123");
        novo.setRoles(List.of("OPERADOR"));
        when(repository.findByLoginIgnoreCase("TesTe")).thenReturn(usuario("OPERADOR"));

        assertThrows(IllegalArgumentException.class, () -> service.salvar(novo));
        verify(repository, never()).save(any());
    }

    @Test
    void buscaPorUuidRetornaResultadoExato() {
        Usuario u = usuario("OPERADOR");
        when(repository.findById(u.getId())).thenReturn(Optional.of(u));
        assertEquals(List.of(u), service.pesquisar(u.getId().toString()));
        verify(repository, never()).findByLoginContainingIgnoreCaseOrEmailContainingIgnoreCaseOrderByLoginAsc(any(), any());
    }
}
