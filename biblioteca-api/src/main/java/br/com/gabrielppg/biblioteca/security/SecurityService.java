package br.com.gabrielppg.biblioteca.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** A API lê a identidade validada no JWT, sem consultar a tabela usuario. */
@Component
public class SecurityService {
    public UUID obterIdUsuarioLogado() {
        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();
        if (!(autenticacao instanceof JwtAuthenticationToken jwtAuth)) {
            throw new AccessDeniedException("Identidade JWT de usuário necessária");
        }
        String tipo = jwtAuth.getToken().getClaimAsString("tipo_principal");
        String id = jwtAuth.getToken().getClaimAsString("usuario_id");
        if (!"USUARIO".equals(tipo) || id == null || id.isBlank()) {
            throw new AccessDeniedException("Token não representa um usuário");
        }
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            throw new AccessDeniedException("UUID de usuário inválido");
        }
    }
}
