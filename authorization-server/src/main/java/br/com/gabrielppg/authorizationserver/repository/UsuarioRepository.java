package br.com.gabrielppg.authorizationserver.repository;

import br.com.gabrielppg.authorizationserver.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {
    Usuario findByLogin(String login);

    Usuario findByEmail(String email);

    Usuario findByGoogleSub(String googleSub);
}
