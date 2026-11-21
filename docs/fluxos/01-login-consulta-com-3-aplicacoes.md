# Fluxo 01: Login e consulta com três aplicações

```mermaid
sequenceDiagram
    actor Usuario
    participant Web as Biblioteca Web :8081
    participant Auth as Authorization Server :9000
    participant Google as Google (opcional)
    participant API as Biblioteca API :8082
    participant DB as PostgreSQL (fase transitória)

    Usuario->>Web: Abre /livros
    Web-->>Usuario: Redireciona para login no AS
    Usuario->>Auth: Digita senha OU escolhe Google
    opt Login Google
        Auth->>Google: Autenticação OIDC
        Google-->>Auth: Identidade validada
    end
    Auth->>DB: Consulta/cria usuário
    Auth-->>Web: Authorization Code (redirect 8081)
    Web->>Auth: POST /oauth2/token (+ PKCE e client_secret)
    Auth-->>Web: Access Token + ID Token
    Web->>API: GET /livros + Bearer access_token
    API->>Auth: GET /oauth2/jwks (chaves públicas, com cache)
    Note over API: Verifica assinatura, iss, aud, validade e authorities
    API->>DB: Consulta livros e autores
    DB-->>API: Resultado
    API-->>Web: JSON paginado
    Web-->>Usuario: Página HTML
```

> Observação: a Biblioteca Web armazena tokens no lado servidor; o navegador usa um cookie de sessão.
> O JWT não é validado consultando o Authorization Server a cada requisição: as chaves JWKS são obtidas e armazenadas em cache pelo decoder.
