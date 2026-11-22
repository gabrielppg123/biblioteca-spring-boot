
package br.com.gabrielppg.bibliotecaweb.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestCustomizers;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Configuration
public class SegurancaWebConfiguration {

    private static final Logger log =
            LoggerFactory.getLogger(SegurancaWebConfiguration.class);

    @Bean
    GrantedAuthoritiesMapper userAuthoritiesMapper() {
        return authorities -> {

            Set<GrantedAuthority> autoridadesMapeadas =
                    new HashSet<>(authorities);

            for (GrantedAuthority authority : authorities) {

                if (authority instanceof OidcUserAuthority autoridadeOidc) {

                    Object claim = autoridadeOidc.getIdToken()
                            .getClaim("authorities");

                    if (claim instanceof Collection<?> papeis) {

                        for (Object papel : papeis) {

                            if (papel instanceof String nome
                                    && (nome.equals("GERENTE")
                                    || nome.equals("OPERADOR"))) {

                                autoridadesMapeadas.add(
                                        new SimpleGrantedAuthority(nome)
                                );
                            }
                        }
                    }
                }
            }

            return autoridadesMapeadas;
        };
    }

    @Bean
    SecurityFilterChain webSecurityFilterChain(
            HttpSecurity http,
            ClientRegistrationRepository clientRegistrationRepository,
            GrantedAuthoritiesMapper userAuthoritiesMapper) throws Exception {

        var resolver = new DefaultOAuth2AuthorizationRequestResolver(
                clientRegistrationRepository
        );

        // Habilita PKCE mesmo sendo um cliente confidencial (com client_secret).
        resolver.setAuthorizationRequestCustomizer(
                OAuth2AuthorizationRequestCustomizers.withPkce()
        );

        return http

                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/", "/css/**", "/error").permitAll()

                        // Operações de gerenciamento de autores: somente GERENTE.
                        .requestMatchers(
                                "/autores/novo",
                                "/autores/*/editar",
                                "/autores/*/excluir"
                        ).hasAuthority("GERENTE")

                        .anyRequest().authenticated()
                )

                // Faz login contra o Authorization Server da Biblioteca.
                // O browser conserva a sessão da aplicação WEB; não recebe o access token.
                .oauth2Login(login -> login

                        .authorizationEndpoint(endpoint ->
                                endpoint.authorizationRequestResolver(resolver))

                        // Lê as autoridades do ID Token OIDC.
                        .userInfoEndpoint(userInfo ->
                                userInfo.userAuthoritiesMapper(userAuthoritiesMapper))

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
