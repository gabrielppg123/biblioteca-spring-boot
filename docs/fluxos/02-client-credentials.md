# Fluxo 02 — Integração máquina a máquina (Client Credentials)

Este fluxo é utilizado por clientes de integração autorizados a consultar a API, sem autenticação interativa de um usuário.

```mermaid
sequenceDiagram
    participant Integracao as Cliente de integração
    participant Auth as Authorization Server :9000
    participant API as Biblioteca API :8080
    participant DB as biblioteca_db
    Integracao->>Auth: POST /oauth2/token (client_credentials)
    Note right of Integracao: Client ID, Client Secret, scope biblioteca.read
    Auth->>Auth: Autentica cliente e verifica escopos
    Auth-->>Integracao: Access Token (aud=biblioteca-api)
    Integracao->>API: GET /livros (Bearer JWT)
    API->>Auth: Consulta JWKS (cache)
    API->>API: Valida assinatura, iss, aud, expiração e escopo
    API->>DB: Consulta livros
    DB-->>API: Dados
    API-->>Integracao: 200 OK
```

- `SCOPE_biblioteca.read` permite consultas aos livros e autores.
- Cadastro, edição e exclusão exigem autoridades humanas de `OPERADOR` ou `GERENTE`, conforme o recurso.
- Tokens emitidos pelo fluxo Client Credentials não recebem roles humanas.
- O cliente OAuth2 criado automaticamente na instalação local é do tipo `INTERATIVO`, para a Biblioteca Web. Para testar Client Credentials, é necessário cadastrar separadamente um cliente do tipo `INTEGRACAO` no Authorization Server.

Veja a [instalação local](../INSTALACAO_LOCAL.md) e a [descrição do Authorization Server](../../authorization-server/README.md).
