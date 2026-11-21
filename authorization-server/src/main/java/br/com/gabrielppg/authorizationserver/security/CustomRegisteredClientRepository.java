package br.com.gabrielppg.authorizationserver.security;

import br.com.gabrielppg.authorizationserver.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomRegisteredClientRepository implements RegisteredClientRepository {

    private final ClientService clientService;
    private final TokenSettings tokenSettings;
    private final ClientSettings clientSettings;

    @Override
    public void save(RegisteredClient registeredClient) {
        throw new UnsupportedOperationException(
                "O cadastro de clientes OAuth2 deve ser realizado pelo ClientService."
        );
    }

    @Override
    public RegisteredClient findById(String id) {

        var client = clientService.obterPorId(id);

        if (client == null) {
            return null;
        }

        return findByClientId(client.getClientId());
    }

    @Override
    public RegisteredClient findByClientId(String clientId) {
        var client = clientService.obterPorClientId(clientId);
        if (client == null) return null;

        return RegisteredClient
                .withId(client.getId().toString())
                .clientId(client.getClientId())
                .clientSecret(client.getClientSecret())
                .redirectUri(client.getRedirectURI())
                .scopes(scopes -> {
                            for (String scope : client.getScope().trim().split("\\s+")) {
                                scopes.add(scope);
                            }
                })
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN) //Só quando estou autenticado. Não funciona com client_credentials. Ele renova o AccessToken
                .tokenSettings(tokenSettings)
                .clientSettings(clientSettings)
                .build();
    }
}
