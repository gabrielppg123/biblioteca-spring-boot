# Biblioteca Web — OAuth2 Client e interface MVC

Aplicação Java 21 / Spring Boot 4.1.2 com Spring MVC e Thymeleaf, executada localmente na **porta 8081**.

- Autenticação via Authorization Server (`http://localhost:9000`) com OAuth2 Authorization Code + PKCE e OIDC.
- Login local e Google são tratados pelo Authorization Server, não diretamente pela Web.
- Consumo da Biblioteca API (`http://localhost:8080`) com access token do usuário no lado servidor.
- Cookie de sessão separado: `BIBLIOTECA_WEB_SESSION`.
- Não utiliza banco de dados próprio.

## Configuração local

Defina as variáveis de ambiente de acordo com `.env.example`, sem registrar segredos no Git:

```dotenv
BIBLIOTECA_AUTH_ISSUER=http://localhost:9000
BIBLIOTECA_API_URL=http://localhost:8080
BIBLIOTECA_WEB_CLIENT_ID=SEU_CLIENT_ID_INTERATIVO
BIBLIOTECA_WEB_CLIENT_SECRET=SEU_SEGREDO_ORIGINAL
```

O cliente OAuth2 precisa existir previamente no banco `authorization_db`, com escopo `openid` e callback `http://localhost:8081/login/oauth2/code/biblioteca`.

Para executar a HML com Docker Compose, utilize as instruções do [guia Docker](../docker/README.md). Nesse perfil, as comunicações internas usam nomes de serviços Docker, mantendo as URLs de navegador em `localhost`.
