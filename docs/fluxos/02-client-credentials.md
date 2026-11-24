# Fluxo 02 — Integração máquina a máquina

```mermaid
sequenceDiagram
    participant Integracao as Cliente de integração
    participant Auth as Authorization Server :9000
    participant API as Biblioteca API :8080
    participant DB as biblioteca_db
    Integracao->>Auth: POST /oauth2/token (client_credentials)
    Note right of Integracao: Client ID + Client Secret, scope biblioteca.read
    Auth->>Auth: Autentica o cliente e verifica escopos
    Auth-->>Integracao: Access Token (aud=biblioteca-api)
    Integracao->>API: GET /livros (Bearer JWT)
    API->>Auth: Consulta JWKS (cache)
    API->>API: Valida assinatura, iss, aud, expiração e scope
    API->>DB: Consulta livros
    DB-->>API: Dados
    API-->>Integracao: 200 OK
```

- `SCOPE_biblioteca.read` autoriza consultas GET de livros e autores.
- Cadastro, edição e exclusão exigem autoridades humanas: `OPERADOR` ou `GERENTE`, conforme o recurso.
- O conversor JWT não concede roles humanas a tokens de clientes.
- O Authorization Server diferencia clientes `INTERATIVO` (Authorization Code + Refresh Token, com PKCE) de clientes `INTEGRACAO` (Client Credentials).
