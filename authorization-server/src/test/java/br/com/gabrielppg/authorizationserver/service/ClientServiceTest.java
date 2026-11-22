package br.com.gabrielppg.authorizationserver.service;

import br.com.gabrielppg.authorizationserver.model.Client;
import br.com.gabrielppg.authorizationserver.model.TipoCliente;
import br.com.gabrielppg.authorizationserver.repository.ClientRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ClientServiceTest {

    private final ClientRepository repository = mock(ClientRepository.class);
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(4);
    private final ClientService service = new ClientService(repository, encoder);

    private Client cliente() {
        Client c = new Client();
        c.setId(UUID.randomUUID());
        c.setClientId("cliente-interativo");
        c.setClientSecret(encoder.encode("SegredoAnterior123"));
        c.setTipoCliente(TipoCliente.INTERATIVO);
        c.setScope("openid biblioteca.read");
        c.setRedirectURI("http://localhost:8081/login/oauth2/code/biblioteca");
        return c;
    }

    @Test
    void cadastroPreservaNomeEAplicaBcrypt() {
        Client novo = cliente();
        novo.setId(null);
        novo.setClientId("  cliente-novo  ");
        novo.setClientSecret("SegredoNovo123");
        novo.setAtivo(false);
        service.salvar(novo);
        assertEquals("cliente-novo", novo.getClientId());
        assertTrue(encoder.matches("SegredoNovo123", novo.getClientSecret()));
        assertTrue(novo.isAtivo());
        verify(repository).save(novo);
    }

    @Test
    void cadastroRecusaClientIdDuplicado() {
        Client novo = cliente();
        novo.setClientSecret("SegredoNovo123");
        when(repository.findByClientId("cliente-interativo")).thenReturn(cliente());
        assertThrows(IllegalArgumentException.class, () -> service.salvar(novo));
        verify(repository, never()).save(any());
    }

    @Test
    void cadastroRecusaSegredoQueExcedeLimiteEmBytes() {
        Client novo = cliente();
        novo.setClientSecret("é".repeat(40)); // 80 bytes UTF-8
        assertThrows(IllegalArgumentException.class, () -> service.salvar(novo));
        verify(repository, never()).save(any());
    }

    @Test
    void editarNaoMudaClientIdNemRecodificaSegredo() {
        Client c = cliente();
        String hash = c.getClientSecret();
        when(repository.findById(c.getId())).thenReturn(Optional.of(c));

        service.atualizar(c.getId(), TipoCliente.INTERATIVO, "openid  profile",
                "https://exemplo.org/callback");

        assertEquals("cliente-interativo", c.getClientId());
        assertEquals(hash, c.getClientSecret());
        assertEquals("openid profile", c.getScope());
        assertEquals("https://exemplo.org/callback", c.getRedirectURI());
        verify(repository).save(c);
    }

    @Test
    void editarParaIntegracaoRemoveRedirectUriMasMantemSecret() {
        Client c = cliente();
        String hash = c.getClientSecret();
        when(repository.findById(c.getId())).thenReturn(Optional.of(c));
        service.atualizar(c.getId(), TipoCliente.INTEGRACAO, "biblioteca.read", null);
        assertNull(c.getRedirectURI());
        assertEquals(TipoCliente.INTEGRACAO, c.getTipoCliente());
        assertEquals(hash, c.getClientSecret());
    }

    @Test
    void editarRecusaRedirectUriComFragmento() {
        Client c = cliente();
        when(repository.findById(c.getId())).thenReturn(Optional.of(c));
        assertThrows(IllegalArgumentException.class, () ->
                service.atualizar(c.getId(), TipoCliente.INTERATIVO, "openid", "https://exemplo.org/#fragmento"));
        verify(repository, never()).save(any());
    }

    @Test
    void renovarSegredoGeraValorAleatorioEArmazenaSomenteHash() {
        Client c = cliente();
        String hashAnterior = c.getClientSecret();
        when(repository.findById(c.getId())).thenReturn(Optional.of(c));
        String segredo = service.renovarSegredo(c.getId(), c.getClientId());
        assertEquals(43, segredo.length()); // 32 bytes em Base64 URL sem padding
        assertNotEquals(hashAnterior, c.getClientSecret());
        assertTrue(encoder.matches(segredo, c.getClientSecret()));
        assertFalse(encoder.matches("SegredoAnterior123", c.getClientSecret()));
        assertNotEquals(segredo, c.getClientSecret());
        verify(repository).save(c);
    }

    @Test
    void renovarSegredoSemConfirmacaoNaoAlteraHash() {
        Client c = cliente();
        String hashOriginal = c.getClientSecret();
        when(repository.findById(c.getId())).thenReturn(Optional.of(c));
        assertThrows(IllegalArgumentException.class, () -> service.renovarSegredo(c.getId(), "outro-cliente"));
        assertEquals(hashOriginal, c.getClientSecret());
        verify(repository, never()).save(any());
    }

    @Test
    void definirSegredoEscolhidoArmazenaSomenteHash() {
        Client c = cliente();
        String antigo = c.getClientSecret();
        when(repository.findById(c.getId())).thenReturn(Optional.of(c));
        String escolhido = "MinhaSenhaSecreta-2026!";

        service.definirSegredo(c.getId(), c.getClientId(), escolhido, escolhido);

        assertNotEquals(antigo, c.getClientSecret());
        assertNotEquals(escolhido, c.getClientSecret());
        assertTrue(encoder.matches(escolhido, c.getClientSecret()));
        assertFalse(encoder.matches("SegredoAnterior123", c.getClientSecret()));
        verify(repository).save(c);
    }

    @Test
    void definirSegredoRecusaConfirmacaoDiferenteSemSalvar() {
        Client c = cliente();
        String antigo = c.getClientSecret();
        when(repository.findById(c.getId())).thenReturn(Optional.of(c));

        assertThrows(IllegalArgumentException.class, () ->
                service.definirSegredo(c.getId(), c.getClientId(),
                        "UmSegredoSuficientementeLongo", "OutroSegredoSuficientementeLongo"));
        assertEquals(antigo, c.getClientSecret());
        verify(repository, never()).save(any());
    }

    @Test
    void definirSegredoRecusaClientIdIncorreto() {
        Client c = cliente();
        String antigo = c.getClientSecret();
        when(repository.findById(c.getId())).thenReturn(Optional.of(c));
        assertThrows(IllegalArgumentException.class, () ->
                service.definirSegredo(c.getId(), "id-errado", "MeuSegredoBemLongo123!", "MeuSegredoBemLongo123!"));
        assertEquals(antigo, c.getClientSecret());
        verify(repository, never()).save(any());
    }

    @Test
    void definirSegredoRecusaSegredoMuitoCurto() {
        Client c = cliente();
        String antigo = c.getClientSecret();
        when(repository.findById(c.getId())).thenReturn(Optional.of(c));
        assertThrows(IllegalArgumentException.class, () ->
                service.definirSegredo(c.getId(), c.getClientId(), "curta", "curta"));
        assertEquals(antigo, c.getClientSecret());
        verify(repository, never()).save(any());
    }

    @Test
    void definirSegredoRecusaMaisDe72BytesEmUtf8() {
        Client c = cliente();
        String antigo = c.getClientSecret();
        when(repository.findById(c.getId())).thenReturn(Optional.of(c));
        assertThrows(IllegalArgumentException.class, () ->
                service.definirSegredo(c.getId(), c.getClientId(), "é".repeat(40), "é".repeat(40)));
        assertEquals(antigo, c.getClientSecret());
        verify(repository, never()).save(any());
    }

    @Test
    void definirSegredoRecusaReutilizarMesmoSegredo() {
        Client c = cliente();
        String antigo = c.getClientSecret();
        when(repository.findById(c.getId())).thenReturn(Optional.of(c));
        assertThrows(IllegalArgumentException.class, () ->
                service.definirSegredo(c.getId(), c.getClientId(), "SegredoAnterior123", "SegredoAnterior123"));
        assertEquals(antigo, c.getClientSecret());
        verify(repository, never()).save(any());
    }

    @Test
    void desativarEReativarExigeConfirmacaoEAtualizaStatus() {
        Client c = cliente();
        when(repository.findById(c.getId())).thenReturn(Optional.of(c));
        assertThrows(IllegalArgumentException.class, () -> service.alterarAtivacao(c.getId(), false, "errado"));
        assertTrue(c.isAtivo());
        service.alterarAtivacao(c.getId(), false, c.getClientId());
        assertFalse(c.isAtivo());
        service.alterarAtivacao(c.getId(), true, c.getClientId());
        assertTrue(c.isAtivo());
        verify(repository, times(2)).save(c);
    }

    @Test
    void pesquisaPorUUIDRetornaSomenteRegistroExato() {
        Client c = cliente();
        when(repository.findById(c.getId())).thenReturn(Optional.of(c));
        assertEquals(List.of(c), service.pesquisar(c.getId().toString()));
        verify(repository, never()).findByClientIdContainingIgnoreCaseOrderByClientIdAsc(anyString());
    }

    @Test
    void pesquisaPorClientIdParcialIgnoraMaiusculas() {
        Client c = cliente();
        when(repository.findByClientIdContainingIgnoreCaseOrderByClientIdAsc("INTERATIVO"))
                .thenReturn(List.of(c));
        assertEquals(List.of(c), service.pesquisar("INTERATIVO"));
    }
}
