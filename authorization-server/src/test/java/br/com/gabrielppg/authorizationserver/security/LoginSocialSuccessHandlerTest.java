package br.com.gabrielppg.authorizationserver.security;

import br.com.gabrielppg.authorizationserver.model.Usuario;
import br.com.gabrielppg.authorizationserver.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import static org.mockito.Mockito.*;

class LoginSocialSuccessHandlerTest {

    @Test
    void deveRejeitarLoginGoogleDeContaDesativada() throws Exception {
        UsuarioService service = mock(UsuarioService.class);
        LoginSocialSuccessHandler handler = new LoginSocialSuccessHandler(service);
        OAuth2AuthenticationToken token = mock(OAuth2AuthenticationToken.class);
        OidcUser principal = mock(OidcUser.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession sessao = mock(HttpSession.class);

        Usuario inativo = new Usuario();
        inativo.setLogin("google-inativo");
        inativo.setGoogleSub("google-sub-teste");
        inativo.setAtivo(false);

        when(token.getAuthorizedClientRegistrationId()).thenReturn("google");
        when(token.getPrincipal()).thenReturn(principal);
        when(principal.getSubject()).thenReturn("google-sub-teste");
        when(service.obterPorGoogleSub("google-sub-teste")).thenReturn(inativo);
        when(request.getSession(false)).thenReturn(sessao);
        when(request.getContextPath()).thenReturn("");

        handler.onAuthenticationSuccess(request, response, token);

        verify(sessao).invalidate();
        verify(response).sendRedirect("/login?error");
        verify(service, never()).salvar(any());
    }
}
