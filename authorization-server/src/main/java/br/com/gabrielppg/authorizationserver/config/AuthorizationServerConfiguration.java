package br.com.gabrielppg.authorizationserver.config;

import br.com.gabrielppg.authorizationserver.security.CustomAuthentication;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Configuration
@EnableWebSecurity
public class AuthorizationServerConfiguration {

    @Bean
    @Order(1)
    public SecurityFilterChain authServerSecurityFilterChain(
            HttpSecurity http) throws Exception {

        http.oauth2AuthorizationServer(authorizationServer -> {
            http.securityMatcher(authorizationServer.getEndpointsMatcher());
            authorizationServer.oidc(Customizer.withDefaults());
        });

        http.authorizeHttpRequests(auth ->
                auth.anyRequest().authenticated()
        );

        http.exceptionHandling(ex ->
                ex.defaultAuthenticationEntryPointFor(
                        new LoginUrlAuthenticationEntryPoint("/login"),
                        new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
                )
        );

        http.oauth2ResourceServer(oauth2 ->
                oauth2.jwt(Customizer.withDefaults())
        );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    public TokenSettings tokenSettings() {
        return TokenSettings.builder()
                .accessTokenFormat(OAuth2TokenFormat.SELF_CONTAINED)
                .accessTokenTimeToLive(Duration.ofMinutes(60))
                .refreshTokenTimeToLive(Duration.ofMinutes(90)) //+30 minutos ativo
                .build();
    }

    @Bean
    public ClientSettings clientSettings(){
        return ClientSettings.builder()
                .requireAuthorizationConsent(false)
                .build();
    }

    // Gera um par de chaves RSA para assinatura dos JWTs.
    // A chave privada assina os tokens.
    // A chave pública permite verificar suas assinaturas.
    // As chaves são geradas novamente a cada inicialização.
    // Implementação intencional para este projeto demonstrativo.
    @Bean
    public JWKSource<SecurityContext> jwkSource() throws Exception {
        RSAKey rsaKey = gerarChaveRSA();

        JWKSet jwkSet = new JWKSet(rsaKey);

        return new ImmutableJWKSet<>(jwkSet);
    }

    private RSAKey gerarChaveRSA() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);

        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        RSAPublicKey chavePublica = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey chavePrivada = (RSAPrivateKey) keyPair.getPrivate();

        return new RSAKey.Builder(chavePublica)
                .privateKey(chavePrivada)
                .keyID(UUID.randomUUID().toString())
                .build();
    }

    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource){
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    @Bean
    public AuthorizationServerSettings authorizationServerSettings(
            @Value("${AUTH_SERVER_ISSUER:http://localhost:9000}") String issuer){
        return AuthorizationServerSettings
                .builder()
                .issuer(issuer)
                //Obter Token
                .tokenEndpoint("/oauth2/token")
                //Obter Informações do Token
                .tokenIntrospectionEndpoint("/oauth2/introspect")
                //Revogar o Token
                .tokenRevocationEndpoint("/oauth2/revoke")
                .authorizationEndpoint("/oauth2/authorize")
                //Informações do Usuário
                .oidcUserInfoEndpoint("/oauth2/userinfo")
                //Obter a chave pública para verificar a assinatura do token
                .jwkSetEndpoint("/oauth2/jwks")
                //Logout
                .oidcLogoutEndpoint("/oauth2/logout")
                .build();
    }

    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> tokenCustomizer() {
        return context -> {
            var principal = context.getPrincipal();
            OAuth2TokenType tipoToken = context.getTokenType();

            // Todos os access tokens deste AS são destinados à Biblioteca API.
            // O ID Token OIDC não deve receber esta audiência.
            if (OAuth2TokenType.ACCESS_TOKEN.equals(tipoToken)) {
                context.getClaims().audience(List.of("biblioteca-api"));
            }

            if (principal instanceof CustomAuthentication authentication) {

                List<String> authoritiesList = authentication.getAuthorities()
                        .stream()
                        .map(GrantedAuthority::getAuthority)
                        .toList();

                if (OAuth2TokenType.ACCESS_TOKEN.equals(tipoToken)) {

                    // Claims destinados à Biblioteca API.
                    context.getClaims()
                            .claim("authorities", authoritiesList)
                            .claim("tipo_principal", "USUARIO")
                            .claim("usuario_id", authentication.getUsuario().getId().toString());

                    String email = authentication.getUsuario().getEmail();

                    if (email != null && !email.isBlank()) {
                        context.getClaims().claim("email", email);
                    }

                } else if (OidcParameterNames.ID_TOKEN.equals(tipoToken.getValue())) {

                    // Autoridades disponibilizadas para a aplicação WEB.
                    context.getClaims()
                            .claim("authorities", authoritiesList);
                }
            }
        };
    }

}
