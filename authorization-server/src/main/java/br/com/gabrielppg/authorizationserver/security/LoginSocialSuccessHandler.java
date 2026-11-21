package br.com.gabrielppg.authorizationserver.security;

import br.com.gabrielppg.authorizationserver.model.Usuario;
import br.com.gabrielppg.authorizationserver.service.UsuarioService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.FactorGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LoginSocialSuccessHandler
        extends SavedRequestAwareAuthenticationSuccessHandler {

    private final UsuarioService usuarioService;

    private final SecurityContextRepository repositorioContexto =
            new HttpSessionSecurityContextRepository();

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        if (!(authentication instanceof OAuth2AuthenticationToken token)
                || !"google".equals(token.getAuthorizedClientRegistrationId())
                || !(token.getPrincipal() instanceof OidcUser principal)) {

            throw new IllegalStateException(
                    "Autenticação Google inválida."
            );
        }

        // Identificador único e estável da conta Google
        String googleSub = principal.getSubject();

        if (googleSub == null || googleSub.isBlank()) {
            throw new IllegalStateException(
                    "Identificador Google não informado."
            );
        }

        // Busca exclusivamente pelo identificador Google
        Usuario usuario = usuarioService.obterPorGoogleSub(googleSub);

        if (usuario == null) {

            usuario = new Usuario();

            usuario.setGoogleSub(googleSub);
            usuario.setLogin(gerarLoginInterno());
            usuario.setEmail(null);
            usuario.setSenha(null);
            usuario.setRoles(List.of("OPERADOR"));

            usuarioService.salvar(usuario);
        }

        // Converte a identidade Google para a autenticação interna
        CustomAuthentication autenticacaoInterna =
                new CustomAuthentication(
                        usuario,
                        FactorGrantedAuthority.fromAuthority(
                                FactorGrantedAuthority.AUTHORIZATION_CODE_AUTHORITY
                        )
                );

        SecurityContext contexto =
                SecurityContextHolder.createEmptyContext();

        contexto.setAuthentication(autenticacaoInterna);

        SecurityContextHolder.setContext(contexto);

        // Persiste a autenticação interna na sessão
        repositorioContexto.saveContext(
                contexto,
                request,
                response
        );

        super.onAuthenticationSuccess(
                request,
                response,
                autenticacaoInterna
        );
    }

    private String gerarLoginInterno() {
        return "g_" + UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 18);
    }
}
