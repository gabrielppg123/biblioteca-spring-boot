# Fluxo 01 — Login e consulta com três aplicações

O navegador inicia a autenticação pela Biblioteca Web. O Authorization Server autentica o usuário e emite tokens; a Web utiliza o access token para consultar a API. Login Google é opcional.

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
    Usuario->>Auth: Login com senha ou Google
    opt Login Google
        Auth->>Google: Autenticação OIDC
        Google-->>Auth: Identidade validada
    end
    Auth->>Identidade: Localiza ou cadastra identidade
    Auth-->>Web: Authorization Code
    Web->>Auth: Troca code por tokens (PKCE)
    Auth-->>Web: Access Token + ID Token
    Web->>API: GET /livros (Bearer JWT)
    API->>Auth: Consulta JWKS (com cache)
    Note over API: Valida assinatura, iss, aud, validade e authorities
    API->>Acervo: Consulta livros e autores
    Acervo-->>API: Dados
    API-->>Web: JSON paginado
    Web-->>Usuario: Página HTML
```

O Authorization Server gerencia o login e a sessão de autenticação. A Web mantém tokens no lado servidor; o navegador utiliza um cookie de sessão. A API valida os JWTs localmente com chaves obtidas por JWKS, sem solicitar introspecção a cada requisição.

Para executar o fluxo em um computador, consulte [Instalação local](../INSTALACAO_LOCAL.md).
