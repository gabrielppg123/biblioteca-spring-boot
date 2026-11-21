# Biblioteca Web — Integração com Authorization Server separado

Nesta entrega, a Web roda na porta `8081` e realiza login via Authorization Server `9000`, usando `Authorization Code + PKCE`. Consulta a Biblioteca API Resource Server na porta `8082` via JWT Bearer no servidor, não no navegador.

## Configuração e ordem de execução

1. Inicie o Authorization Server `9000`, **após aplicar o patch de audiência dos JWTs**.
2. Inicie a Biblioteca API `8082`, apontando para JWKS de `9000`.
3. Inicie a Web `8081` com variáveis:

```text
BIBLIOTECA_AUTH_ISSUER=http://localhost:9000
BIBLIOTECA_API_URL=http://localhost:8082
BIBLIOTECA_WEB_CLIENT_ID=biblioteca-web
BIBLIOTECA_WEB_CLIENT_SECRET=SEU_SEGREDO_ORIGINAL
```

O callback cadastrado na tabela `client` permanece `http://localhost:8081/login/oauth2/code/biblioteca`. Scope `openid` e cookie `BIBLIOTECA_WEB_SESSION` já estão configurados no `application.yml` deste pacote.

Abra uma janela anônima do navegador e acesse `http://localhost:8081`. Teste login local e Google e navegue em livros e autores. O Google é integrado **somente ao Authorization Server**, pelo callback `http://localhost:9000/login/oauth2/code/google`, já validado no seu Google Cloud.

> Se preferir atualizar seu projeto Web atual, basta ajustar as variáveis e conferir o cookie/scope, sem substituir o código. Leia o README da raiz do pacote.
