package br.com.gabrielppg.authorizationserver.repository;

import br.com.gabrielppg.authorizationserver.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ClientRepository extends JpaRepository<Client, UUID> {
    Client findByClientId(String clientId);

    List<Client> findByClientIdContainingIgnoreCaseOrderByClientIdAsc(String clientId);
}
