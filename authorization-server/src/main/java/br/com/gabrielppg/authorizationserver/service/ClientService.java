package br.com.gabrielppg.authorizationserver.service;

import br.com.gabrielppg.authorizationserver.model.Client;
import br.com.gabrielppg.authorizationserver.model.TipoCliente;
import br.com.gabrielppg.authorizationserver.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository repository;
    private final PasswordEncoder encoder;

    // O Client ID é estável: as integrações dependem dele.
    @Transactional
    public Client salvar(Client client) {
        if (client == null) {
            throw new IllegalArgumentException("Cliente não informado.");
        }
        validarClientId(client.getClientId());
        validarSegredo(client.getClientSecret());
        validarConfiguracao(client.getTipoCliente(), client.getScope(), client.getRedirectURI());

        String clientId = client.getClientId().trim();
        if (repository.findByClientId(clientId) != null) {
            throw new IllegalArgumentException("Já existe um cliente com esse Client ID.");
        }
        client.setClientId(clientId);
        aplicarConfiguracao(client, client.getTipoCliente(), client.getScope(), client.getRedirectURI());
        client.setClientSecret(encoder.encode(client.getClientSecret()));
        client.setAtivo(true);
        return repository.save(client);
    }

    @Transactional(readOnly = true)
    public List<Client> pesquisar(String busca) {
        String termo = busca == null ? "" : busca.trim();
        if (termo.isEmpty()) {
            return repository.findAll(Sort.by("clientId"));
        }
        try {
            UUID id = UUID.fromString(termo);
            return repository.findById(id).map(List::of).orElseGet(List::of);
        } catch (IllegalArgumentException ex) {
            return repository.findByClientIdContainingIgnoreCaseOrderByClientIdAsc(termo);
        }
    }

    @Transactional(readOnly = true)
    public Client obterPorClientId(String clientId) {
        return repository.findByClientId(clientId);
    }

    @Transactional(readOnly = true)
    public Client obterPorId(String id) {
        return repository.findById(UUID.fromString(id)).orElse(null);
    }

    @Transactional(readOnly = true)
    public Client obterPorId(UUID id) {
        return repository.findById(id).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<Client> listarTodos() {
        return repository.findAll();
    }

    // Não recebe Client Secret e não reprocessa o hash existente.
    @Transactional
    public void atualizar(UUID id, TipoCliente tipo, String scopes, String redirectURI) {
        Client client = obrigatorio(id);
        validarConfiguracao(tipo, scopes, redirectURI);
        aplicarConfiguracao(client, tipo, scopes, redirectURI);
        repository.save(client);
    }

    // Gera segredo aleatório no servidor. O valor em texto puro só sai no retorno deste método.
    @Transactional
    public String renovarSegredo(UUID id, String confirmacao) {
        Client client = obrigatorio(id);
        validarConfirmacao(client, confirmacao);
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        String novoSegredo = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        client.setClientSecret(encoder.encode(novoSegredo));
        repository.save(client);
        return novoSegredo;
    }

    // Permite que o administrador escolha um segredo, sem expor o valor em logs ou URLs.
    // A confirmação do Client ID e das duas entradas acontece antes de persistir.
    @Transactional
    public void definirSegredo(UUID id, String confirmacao, String novoSegredo,
                              String confirmacaoNovoSegredo) {
        Client client = obrigatorio(id);
        validarConfirmacao(client, confirmacao);

        if (novoSegredo == null || novoSegredo.length() < 16) {
            throw new IllegalArgumentException("O Client Secret deve ter pelo menos 16 caracteres.");
        }
        validarSegredo(novoSegredo); // Limite de 72 bytes do BCrypt, inclusive UTF-8.

        if (!novoSegredo.equals(confirmacaoNovoSegredo)) {
            throw new IllegalArgumentException("A confirmação do novo Client Secret não confere.");
        }
        if (encoder.matches(novoSegredo, client.getClientSecret())) {
            throw new IllegalArgumentException("Escolha um Client Secret diferente do atual.");
        }

        client.setClientSecret(encoder.encode(novoSegredo));
        repository.save(client);
    }

    @Transactional
    public void alterarAtivacao(UUID id, boolean ativo, String confirmacao) {
        Client client = obrigatorio(id);
        validarConfirmacao(client, confirmacao);
        client.setAtivo(ativo);
        repository.save(client);
    }

    private Client obrigatorio(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente OAuth2 não encontrado."));
    }

    private void validarConfirmacao(Client client, String confirmacao) {
        if (confirmacao == null || !client.getClientId().equals(confirmacao.trim())) {
            throw new IllegalArgumentException("Digite o Client ID exatamente como exibido para confirmar a operação.");
        }
    }

    private void aplicarConfiguracao(Client client, TipoCliente tipo, String scopes, String redirectURI) {
        client.setTipoCliente(tipo);
        client.setScope(scopes.trim().replaceAll("\\s+", " "));
        client.setRedirectURI(tipo == TipoCliente.INTERATIVO ? redirectURI.trim() : null);
    }

    private void validarClientId(String clientId) {
        if (clientId == null || !clientId.trim().matches("[A-Za-z0-9][A-Za-z0-9._-]{2,99}")) {
            throw new IllegalArgumentException("Client ID inválido (3 a 100 caracteres). ");
        }
    }

    private void validarSegredo(String segredo) {
        if (segredo == null || segredo.isBlank()) {
            throw new IllegalArgumentException("Client Secret é obrigatório.");
        }
        if (segredo.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException("O Client Secret não pode ultrapassar 72 bytes, limite do BCrypt.");
        }
    }

    private void validarConfiguracao(TipoCliente tipo, String scopes, String redirectURI) {
        if (tipo == null) {
            throw new IllegalArgumentException("Selecione o tipo do cliente.");
        }
        if (scopes == null || scopes.isBlank() || scopes.trim().length() > 500) {
            throw new IllegalArgumentException("Informe scopes válidos (até 500 caracteres).");
        }
        if (tipo == TipoCliente.INTERATIVO) {
            validarRedirectURI(redirectURI);
        }
    }

    private void validarRedirectURI(String redirectURI) {
        if (redirectURI == null || redirectURI.isBlank() || redirectURI.trim().length() > 500) {
            throw new IllegalArgumentException("Cliente interativo precisa de uma Redirect URI válida (até 500 caracteres).");
        }
        try {
            URI uri = URI.create(redirectURI.trim());
            if (!uri.isAbsolute() || uri.getHost() == null || uri.getFragment() != null
                    || uri.getUserInfo() != null || !("http".equalsIgnoreCase(uri.getScheme())
                    || "https".equalsIgnoreCase(uri.getScheme()))) {
                throw new IllegalArgumentException();
            }
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Informe uma Redirect URI HTTP/HTTPS válida, sem fragmento.");
        }
    }
}
