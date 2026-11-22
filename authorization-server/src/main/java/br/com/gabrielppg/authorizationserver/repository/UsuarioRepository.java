package br.com.gabrielppg.authorizationserver.repository;

import br.com.gabrielppg.authorizationserver.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {
    Usuario findByLogin(String login);

    Usuario findByEmail(String email);

    Usuario findByGoogleSub(String googleSub);

    Usuario findByLoginIgnoreCase(String login);

    Usuario findByEmailIgnoreCase(String email);

    List<Usuario> findByLoginContainingIgnoreCaseOrEmailContainingIgnoreCaseOrderByLoginAsc(
            String login, String email
    );

    // Consulta nativa: roles é um array varchar[] do PostgreSQL.
    @org.springframework.data.jpa.repository.Query(value = """
            SELECT COUNT(*) FROM usuario
            WHERE ativo = TRUE AND 'GERENTE' = ANY(roles)
            """, nativeQuery = true)
    long contarGerentesAtivos();
}
