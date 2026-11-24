# Fluxo 01 — Login e consulta com três aplicações

```mermaid
sequenceDiagram
    actor Usuario
    participant Web as Biblioteca Web :8081
    participant Auth as Authorization Server :9000
    participant Google as Google (opcional)
    participant Identidade as authorization_db
    participant API as Biblioteca API :8080
    participant Acervo as biblioteca_db

    Usuario->>Web: Abre /livros
    Web-->>Usuario: Redireciona para autenticação
    Usuario->>Auth: Senha local ou login Google
    opt Login Google
        Auth->>Google: Autenticação OIDC
        Google-->>Auth: Identidade validada
    end
    Auth->>Identidade: Consulta/cria usuário
    Auth-->>Web: Authorization Code (redirect :8081)
    Web->>Auth: Troca code por tokens (PKCE + client secret)
    Auth-->>Web: Access Token + ID Token
    Web->>API: GET /livros com Bearer JWT
    API->>Auth: Consulta JWKS (com cache)
    Note over API: Valida assinatura, iss, aud, expiração e authorities
    API->>Acervo: Consulta livros e autores
    Acervo-->>API: Dados
    API-->>Web: JSON paginado
    Web-->>Usuario: Página HTML
```

A Biblioteca Web mantém os tokens do lado servidor; o navegador utiliza cookie de sessão. O Resource Server não solicita introspecção do token a cada requisição: utiliza a validação local com chaves obtidas por JWKS.
