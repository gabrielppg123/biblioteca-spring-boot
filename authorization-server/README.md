# Authorization Server 9000 — ajuste de audiência

Esta cópia inclui o Authorization Server que você validou com login local, Google e Client Credentials. A única alteração funcional desta integração é `aud=biblioteca-api` nos ACCESS_TOKENs, por meio do `OAuth2TokenCustomizer`.

**Recomendação:** em seu projeto local já funcional, aplique apenas o patch disponível em `../patches/AuthorizationServerConfiguration.patch`; não é necessário substituir todo o projeto ou recadastrar os clientes.

O claim `usuario_id` já estava presente no seu Authorization Server original. É usado para auditoria de cadastros de autores e livros no Resource Server, sem acesso à entidade `Usuario`.

Os endpoints OAuth2/OIDC continuam em `http://localhost:9000`; callback Google `http://localhost:9000/login/oauth2/code/google`.

Não altere as credenciais de banco/Google e não publique segredos em repositórios.
