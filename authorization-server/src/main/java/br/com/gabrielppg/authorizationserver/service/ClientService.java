package br.com.gabrielppg.authorizationserver.service;

import br.com.gabrielppg.authorizationserver.model.Client;
import br.com.gabrielppg.authorizationserver.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository repository;
    private final PasswordEncoder encoder;

    public Client salvar (Client client){
        var senhaCriptografada = encoder.encode(client.getClientSecret());
        client.setClientSecret(senhaCriptografada);
        return repository.save(client);
    }

    public Client obterPorClientId (String clientId){
        return repository.findByClientId(clientId);
    }

    public Client obterPorId(String id) {
        return repository.findById(UUID.fromString(id))
                .orElse(null);
    }

}
