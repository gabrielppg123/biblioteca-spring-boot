package br.com.gabrielppg.biblioteca.security;

import br.com.gabrielppg.biblioteca.config.SecurityConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ResourceServerJwtTest {
    private final SecurityConfiguration config = new SecurityConfiguration();

    private Jwt token(String tipoPrincipal, String usuarioId, List<String> roles, String scope) {
        var builder = Jwt.withTokenValue("simulado")
                .header("alg", "RS256")
                .subject("gabriel")
                .claim("tipo_principal", tipoPrincipal)
                .claim("authorities", roles)
                .claim("scope", scope)
                .audience(List.of("biblioteca-api"));
        if (usuarioId != null) {
            builder.claim("usuario_id", usuarioId);
        }
        return builder.build();
    }

    @AfterEach
    void limparContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void usuarioComUuidRecebeRolesPermitidas() {
        var jwt = token("USUARIO", UUID.randomUUID().toString(),
                List.of("GERENTE", "FACTOR_PASSWORD"), "openid");
        var auth = config.jwtAuthenticationConverter().convert(jwt);
        assertNotNull(auth);
        assertTrue(auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("GERENTE")));
        assertFalse(auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("FACTOR_PASSWORD")));
    }

    @Test
    void tokenDeClientNaoHerdaraRolesHumanas() {
        var jwt = token("CLIENTE", null, List.of("GERENTE"), "biblioteca.read");
        var auth = config.jwtAuthenticationConverter().convert(jwt);
        assertNotNull(auth);
        assertFalse(auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("GERENTE")));
        assertTrue(auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("SCOPE_biblioteca.read")));
    }

    @Test
    void tokenDeUsuarioSemUuidNaoRecebeRole() {
        var jwt = token("USUARIO", null, List.of("GERENTE"), "openid");
        var auth = config.jwtAuthenticationConverter().convert(jwt);
        assertNotNull(auth);
        assertFalse(auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("GERENTE")));
    }

    @Test
    void auditoriaUsaUuidDoJwt() {
        UUID id = UUID.randomUUID();
        var jwt = token("USUARIO", id.toString(), List.of("GERENTE"), "openid");
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
        assertEquals(id, new SecurityService().obterIdUsuarioLogado());
    }

    @Test
    void clienteNaoPodeFornecerIdDeAuditoria() {
        var jwt = token("CLIENTE", null, List.of(), "biblioteca.read");
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
        assertThrows(AccessDeniedException.class,
                () -> new SecurityService().obterIdUsuarioLogado());
    }

    @Test
    void validaAudienciaObrigatoria() {
        var validator = new AudienceValidator("biblioteca-api");
        var jwt = token("USUARIO", UUID.randomUUID().toString(), List.of("OPERADOR"), "openid");
        assertFalse(validator.validate(jwt).hasErrors());
        var outro = Jwt.withTokenValue("simulado").header("alg", "RS256")
                .subject("gabriel").audience(List.of("outro-servico")).build();
        assertTrue(validator.validate(outro).hasErrors());
    }
}
