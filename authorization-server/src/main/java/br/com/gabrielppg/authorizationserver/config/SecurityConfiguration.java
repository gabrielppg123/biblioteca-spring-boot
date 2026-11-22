package br.com.gabrielppg.authorizationserver.config;

import br.com.gabrielppg.authorizationserver.security.LoginSocialSuccessHandler;
import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
public class SecurityConfiguration {
    // A cadeia @Order(1) do Authorization Server gerencia /oauth2/* e OIDC.
    // Esta cadeia cuida apenas do login por sessão (local ou Google).
    @Bean
    @Order(2)
    SecurityFilterChain loginSecurityFilterChain(HttpSecurity http, LoginSocialSuccessHandler googleHandler) throws Exception {
        return http
            .csrf(Customizer.withDefaults())
            .formLogin(form -> form.loginPage("/login").permitAll())
            .oauth2Login(oauth2 -> oauth2.loginPage("/login").successHandler(googleHandler))
            .authorizeHttpRequests(auth -> auth
                .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                    .requestMatchers("/login", "/oauth2/authorization/google",
                            "/login/oauth2/code/google", "/actuator/health", "/actuator/info",
                            "/.well-known/appspecific/**")
                .permitAll()
                .anyRequest().authenticated())
            .build();
    }

    @Bean
    GrantedAuthorityDefaults grantedAuthorityDefaults() {
        // O banco atual já utiliza autoridades GERENTE e OPERADOR sem ROLE_.
        return new GrantedAuthorityDefaults("");
    }
}
