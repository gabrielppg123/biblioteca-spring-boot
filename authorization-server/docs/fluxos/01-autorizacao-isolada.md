# Fluxo — Autenticação centralizada (arquitetura atual)

```mermaid
flowchart TD
    H["Usuário / navegador"] --> W["Biblioteca Web :8081"]
    W --> AS["Authorization Server :9000"]
    AS --> Escolha{"Login local ou Google?"}
    Escolha -->|Local| Local["CustomAuthenticationProvider"]
    Escolha -->|Google| Social["LoginSocialSuccessHandler"]
    Local --> Usuario["UsuarioService / authorization_db"]
    Social --> Usuario
    Usuario --> Sessao["Sessão de autenticação no Authorization Server"]
    Sessao --> Token["Authorization Code + PKCE / tokens OAuth2-OIDC"]
    Token --> W
    W -->|"Access token JWT"| API["Biblioteca API :8080"]
    API --> Dados["biblioteca_db"]
    API -.->|"JWKS / issuer / audience"| AS
```

O Authorization Server gerencia identidade e clientes OAuth2, enquanto a API gerencia o domínio Biblioteca. A Web não armazena dados de negócio em banco próprio.
