package br.com.gabrielppg.authorizationserver.security;

import br.com.gabrielppg.authorizationserver.model.Usuario;
import br.com.gabrielppg.authorizationserver.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class CustomAuthenticationProviderTest {

    @Test
    void deveBloquearUsuarioDesativadoAntesDeConferirSenha() {
        UsuarioService service = mock(UsuarioService.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        CustomAuthenticationProvider provider = new CustomAuthenticationProvider(service, encoder);
        Usuario inativo = new Usuario();
        inativo.setLogin("desativado");
        inativo.setAtivo(false);
        when(service.obterPorLogin("desativado")).thenReturn(inativo);

        var tentativa = UsernamePasswordAuthenticationToken.unauthenticated("desativado", "senha");
        assertThrows(UsernameNotFoundException.class, () -> provider.authenticate(tentativa));
        verifyNoInteractions(encoder);
    }
}
