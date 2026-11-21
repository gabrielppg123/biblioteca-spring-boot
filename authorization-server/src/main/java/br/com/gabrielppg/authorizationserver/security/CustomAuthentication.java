package br.com.gabrielppg.authorizationserver.security;

import br.com.gabrielppg.authorizationserver.model.Usuario;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.FactorGrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.ArrayList;
import java.util.List;
import java.util.Collection;

@RequiredArgsConstructor
@Getter
public class CustomAuthentication implements Authentication
{

    private final Usuario usuario;
    private final FactorGrantedAuthority fatorAutenticacao;

    public CustomAuthentication(Usuario usuario) {
        this(usuario, null);
    }

    @Override
    public Collection<GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> autoridades = new ArrayList<>();

        for (String role : usuario.getRoles()) {
            autoridades.add(new SimpleGrantedAuthority(role));
        }

        if (fatorAutenticacao != null) {
            autoridades.add(fatorAutenticacao);
        }

        return autoridades;
    }

    @Override
    public @Nullable Object getCredentials() {
        return null;
    }

    @Override
    public @Nullable Object getDetails() {
        return usuario;
    }

    @Override
    public @Nullable Object getPrincipal() {
        return usuario;
    }

    @Override
    public boolean isAuthenticated() {
        return true;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {

    }

    @Override
    public String getName() {
        return usuario.getLogin();
    }
}
