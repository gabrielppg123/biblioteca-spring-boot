
package br.com.gabrielppg.authorizationserver.security;

import br.com.gabrielppg.authorizationserver.model.Client;
import br.com.gabrielppg.authorizationserver.model.TipoCliente;
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

    @Override
    public void save(RegisteredClient registeredClient) {
        throw new UnsupportedOperationException(
                "O cadastro de clientes OAuth2 deve ser realizado pelo ClientService."
        );
    }

    @Override
    public RegisteredClient findById(String id) {
        var client = clientService.obterPorId(id);

        if (client == null || !client.isAtivo()) {
            return null;
        }

        return findByClientId(client.getClientId());
    }

    @Override
    public RegisteredClient findByClientId(String clientId) {

        var client = clientService.obterPorClientId(clientId);

        if (client == null || !client.isAtivo()) {
            return null;
        }

        var registeredClient = RegisteredClient
                .withId(client.getId().toString())
                .clientId(client.getClientId())
                .clientSecret(client.getClientSecret())
                .clientAuthenticationMethod(
                        ClientAuthenticationMethod.CLIENT_SECRET_BASIC
                )
                .tokenSettings(tokenSettings)
                .clientSettings(criarConfiguracoesCliente(client));

        if (client.getScope() != null && !client.getScope().isBlank()) {
            for (String scope : client.getScope().trim().split("\\s+")) {
                registeredClient.scope(scope);
            }
        }

        switch (client.getTipoCliente()) {

            case INTERATIVO -> {

                if (client.getRedirectURI() == null ||
                        client.getRedirectURI().isBlank()) {

                    throw new IllegalStateException(
                            "Cliente INTERATIVO precisa de uma Redirect URI: "
                                    + client.getClientId()
                    );
                }

                registeredClient
                        .redirectUri(client.getRedirectURI())
                        .authorizationGrantType(
                                AuthorizationGrantType.AUTHORIZATION_CODE
                        )
                        .authorizationGrantType(
                                AuthorizationGrantType.REFRESH_TOKEN
                        );
            }

            case INTEGRACAO -> {

                registeredClient.authorizationGrantType(
                        AuthorizationGrantType.CLIENT_CREDENTIALS
                );
            }
        }

        return registeredClient.build();
    }

    private ClientSettings criarConfiguracoesCliente(Client client) {

        boolean exigirPkce =
                client.getTipoCliente() == TipoCliente.INTERATIVO;

        return ClientSettings.builder()
                .requireAuthorizationConsent(false)
                .requireProofKey(exigirPkce)
                .build();
    }
}
