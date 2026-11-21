# Fluxo 02: Integração máquina-a-máquina

```mermaid
sequenceDiagram
    participant Integracao as Aplicação cliente
    participant Auth as Authorization Server :9000
    participant API as Biblioteca API :8082
    participant DB as PostgreSQL
    Integracao->>Auth: POST /oauth2/token
grant_type=client_credentials
Basic client_id:secret
scope=biblioteca.read
    Auth->>Auth: Verifica cliente e escopo cadastrado
    Auth-->>Integracao: Access Token (aud=biblioteca-api)
    Integracao->>API: GET /livros
Authorization: Bearer JWT
    API->>Auth: Busca JWKS (cache)
    API->>API: Verifica assinatura, iss, aud, exp, scope
    API->>DB: SELECT livros
    DB-->>API: Livros
    API-->>Integracao: 200 OK
```

- Apenas endpoints GET reconhecem `SCOPE_biblioteca.read`.
- Cadastro, edição e remoção exigem roles de pessoa (`OPERADOR` ou `GERENTE`).
- Tokens de cliente não recebem roles humanas.
- Na estrutura atual, a configuração de grants ainda é igual para todos os clientes OAuth2; a distinção por grant deverá ser refinada no Authorization Server.
