package br.com.gabrielppg.bibliotecaweb.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestCustomizers;
import org.springframework.security.web.SecurityFilterChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;

@Configuration
public class SegurancaWebConfiguration {

    private static final Logger log =
            LoggerFactory.getLogger(SegurancaWebConfiguration.class);

    @Bean
    SecurityFilterChain webSecurityFilterChain(
            HttpSecurity http, ClientRegistrationRepository clientRegistrationRepository) throws Exception {
        var resolver = new DefaultOAuth2AuthorizationRequestResolver(clientRegistrationRepository);
        // Habilita PKCE mesmo sendo um cliente confidencial (com client_secret).
        resolver.setAuthorizationRequestCustomizer(OAuth2AuthorizationRequestCustomizers.withPkce());
        return http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/", "/css/**", "/error").permitAll()
                        .anyRequest().authenticated()
                )
                // Faz login contra o Authorization Server da Biblioteca.
                // O browser conserva a sessão da aplicação WEB; não recebe o access token.

                .oauth2Login(login -> login
                        .authorizationEndpoint(endpoint ->
                                endpoint.authorizationRequestResolver(resolver))
                        .loginPage("/")
                        .defaultSuccessUrl("/livros", true)
                        .failureHandler((request, response, exception) -> {

                            String codigo = exception instanceof OAuth2AuthenticationException erro
                                    ? erro.getError().getErrorCode()
                                    : "authentication_failed";

                            Throwable causa = exception;

                            while (causa.getCause() != null && causa.getCause() != causa) {
                                causa = causa.getCause();
                            }

                            log.error(
                                    "Falha na autenticacao OAuth2: codigo={}, tipo={}, causa={}",
                                    codigo,
                                    exception.getClass().getSimpleName(),
                                    causa.getClass().getSimpleName()
                            );

                            response.sendRedirect(request.getContextPath() + "/?error");
                        })
                )

                .logout(logout -> logout
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                )
                .build();
    }
}
