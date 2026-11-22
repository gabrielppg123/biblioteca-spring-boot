package br.com.gabrielppg.authorizationserver.security;

import br.com.gabrielppg.authorizationserver.model.Client;
import br.com.gabrielppg.authorizationserver.model.TipoCliente;
import br.com.gabrielppg.authorizationserver.service.ClientService;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomRegisteredClientRepositoryTest {

    private final ClientService service = mock(ClientService.class);
    private final CustomRegisteredClientRepository repository =
            new CustomRegisteredClientRepository(service, TokenSettings.builder().build());

    private Client cliente(boolean ativo) {
        Client c = new Client();
        c.setId(UUID.randomUUID());
        c.setClientId("cliente-interativo");
        c.setClientSecret("$2a$10$exemplo-hash");
        c.setTipoCliente(TipoCliente.INTERATIVO);
        c.setScope("openid biblioteca.read");
        c.setRedirectURI("http://localhost:8081/login/oauth2/code/biblioteca");
        c.setAtivo(ativo);
        return c;
    }

    @Test
    void clienteInativoNaoPodeSerResolvidoPorClientId() {
        when(service.obterPorClientId("cliente-interativo")).thenReturn(cliente(false));
        assertNull(repository.findByClientId("cliente-interativo"));
    }

    @Test
    void clienteInativoNaoPodeSerResolvidoPorUUID() {
        Client c = cliente(false);
        when(service.obterPorId(c.getId().toString())).thenReturn(c);
        assertNull(repository.findById(c.getId().toString()));
        verify(service, never()).obterPorClientId(anyString());
    }

    @Test
    void clienteAtivoMantemAuthorizationCodePkceERefreshToken() {
        Client c = cliente(true);
        when(service.obterPorClientId(c.getClientId())).thenReturn(c);
        var registrado = repository.findByClientId(c.getClientId());
        assertNotNull(registrado);
        assertEquals(c.getClientId(), registrado.getClientId());
        assertEquals(c.getClientSecret(), registrado.getClientSecret());
        assertTrue(registrado.getAuthorizationGrantTypes().contains(AuthorizationGrantType.AUTHORIZATION_CODE));
        assertTrue(registrado.getAuthorizationGrantTypes().contains(AuthorizationGrantType.REFRESH_TOKEN));
        assertTrue(registrado.getClientSettings().isRequireProofKey());
        assertTrue(registrado.getScopes().contains("openid"));
    }

    @Test
    void integracaoAtivaContinuaUsandoClientCredentials() {
        Client c = cliente(true);
        c.setTipoCliente(TipoCliente.INTEGRACAO);
        c.setRedirectURI(null);
        c.setScope("biblioteca.read");
        when(service.obterPorClientId(c.getClientId())).thenReturn(c);
        var registrado = repository.findByClientId(c.getClientId());
        assertNotNull(registrado);
        assertEquals(1, registrado.getAuthorizationGrantTypes().size());
        assertTrue(registrado.getAuthorizationGrantTypes().contains(AuthorizationGrantType.CLIENT_CREDENTIALS));
        assertFalse(registrado.getClientSettings().isRequireProofKey());
    }
}
