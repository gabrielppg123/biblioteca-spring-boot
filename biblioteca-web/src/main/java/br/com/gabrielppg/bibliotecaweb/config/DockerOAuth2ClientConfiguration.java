package br.com.gabrielppg.bibliotecaweb.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames;

/**
 * Docker Desktop: o navegador usa localhost, enquanto as chamadas servidor-servidor
 * usam o DNS da rede Compose. O emissor continua localhost para validar os JWTs.
 * Fora do perfil 'docker', a configuração OIDC original no application.yml permanece.
 */
@Configuration
@Profile("docker")
public class DockerOAuth2ClientConfiguration {

    @Bean
    ClientRegistrationRepository dockerClientRegistrationRepository(
            @Value("${BIBLIOTECA_WEB_CLIENT_ID}") String clientId,
            @Value("${BIBLIOTECA_WEB_CLIENT_SECRET}") String clientSecret,
            @Value("${BIBLIOTECA_AUTH_ISSUER:http://localhost:9000}") String issuer) {

        ClientRegistration registration = ClientRegistration.withRegistrationId("biblioteca")
                .clientId(clientId)
                .clientSecret(clientSecret)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .scope("openid")
                // Front-channel: URL aberta no navegador do usuario.
                .authorizationUri("http://localhost:9000/oauth2/authorize")
                // Back-channel: URLs acessadas somente pelos containers.
                .tokenUri("http://authorization-server:9000/oauth2/token")
                .jwkSetUri("http://authorization-server:9000/oauth2/jwks")
                .userInfoUri("http://authorization-server:9000/oauth2/userinfo")
                .userNameAttributeName(IdTokenClaimNames.SUB)
                // Validacao do issuer deve usar o valor REAL dos JWTs: localhost:9000.
                .issuerUri(issuer)
                .build();

        return new InMemoryClientRegistrationRepository(registration);
    }
}
