# Fluxo 03 — Authorization Server isolado (etapa 1)

```mermaid
flowchart TD
    H["Usuário / navegador"] --> AS["Authorization Server :9000"]
    AS --> Escolha{"Forma de login"}
    Escolha -->|Senha| Local["CustomAuthenticationProvider"]
    Escolha -->|Google| Social["Google OIDC → LoginSocialSuccessHandler"]
    Local --> Usuario["UsuarioService → PostgreSQL: usuario"]
    Social --> Usuario
    Usuario --> Sessao["Sessão no Auth Server"]
    Sessao --> Authorize["/oauth2/authorize: Authorization Code + PKCE"]
    Authorize --> Token["/oauth2/token"]
    Token --> Assinatura["JWK RSA → access_token / id_token"]
    AsR["Biblioteca original :8080"] -.- X["Ainda não aponta para :9000"]
    Web["Biblioteca Web :8081"] -.- X
```

Neste checkpoint, a autenticação no `:9000` é independente **como processo**, mas usa temporariamente as tabelas `usuario` e `client` do PostgreSQL antigo. A Web continua usando `:8080` até a próxima etapa. Não trocar o issuer antes de preparar a Biblioteca API.
