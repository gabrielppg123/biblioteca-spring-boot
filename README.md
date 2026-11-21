# Migração controlada: Biblioteca API como Resource Server

## O que contém

- `biblioteca-api/`: **novo projeto** Resource Server Java 21 / Spring Boot 4.1.2. Porta **8082** para testar sem encostar na Biblioteca original (8080).
- `authorization-server/`: cópia da etapa 1 na porta **9000**, alterada **somente** para incluir `aud=biblioteca-api` nos ACCESS tokens. O claim `usuario_id` já existia nessa versão.
- `biblioteca-web/`: cópia base da Web na porta **8081**, apontando para **9000** para login e **8082** para API; mantém cookie único e scope `openid`.
- `patches/AuthorizationServerConfiguration.patch`: diferença mínima para aplicar ao servidor 9000 que você já testou.
- `docs/fluxos/`: 3 diagramas Mermaid editáveis no Obsidian.

## Sem risco para o seu sistema funcional

A aplicação **original :8080 não deve ser alterada nem desligada antes do teste**. Execute uma cópia nova da API em `:8082`. A Web atual pode continuar funcionando com `:8080` até você decidir trocar suas variáveis de ambiente.

## Passo 1 — Authorization Server (9000)

1. Abra **seu** Authorization Server funcional.
2. Em `AuthorizationServerConfiguration.tokenCustomizer()`, adicione no início do lambda, após `var principal = context.getPrincipal();`:

   ```java
   if (OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
       context.getClaims().audience(List.of("biblioteca-api"));
   }
   ```

3. Confirme que o mesmo customizer **já** preenche `usuario_id` no ACCESS_TOKEN de usuário:

   ```java
   .claim("usuario_id", authentication.getUsuario().getId().toString());
   ```

4. Reinicie somente o Authorization Server. Tokens obtidos antes da mudança não têm a nova audiência: faça **novo login** para obter outro access token.
5. O `id_token` OIDC **não recebe** `aud=biblioteca-api` neste patch. Ele segue destinado ao client web.

> Na sua máquina, prefira o `patches/AuthorizationServerConfiguration.patch` a substituir o projeto inteiro. Você pode ter alterações locais não incluídas aqui.

## Passo 2 — Biblioteca API nova (8082)

1. Abra `biblioteca-api/` como projeto Maven separado no IntelliJ.
2. Use as credenciais **existentes** do PostgreSQL na configuração de execução do IntelliJ (não salvar no Git):

   ```dotenv
   DATASOURCE_URL=jdbc:postgresql://localhost:5432/SEU_BANCO
   DATASOURCE_USERNAME=SEU_USUARIO
   DATASOURCE_PASSWORD=SUA_SENHA
   AUTH_SERVER_ISSUER=http://localhost:9000
   AUTH_SERVER_JWKS_URI=http://localhost:9000/oauth2/jwks
   ```

3. Inicie `Inicio.java`. **Confira `http://localhost:9092/actuator/health` (Actuator separado).**
4. Sem token, `GET http://localhost:8082/livros` deve dar **401**.
5. A API nova **não** disponibiliza `/login`, `/oauth2/token`, `/usuarios`, `/clients` nem `/oauth2/authorization/google`.

## Passo 3 — Biblioteca Web (8081)

No projeto **Web que já está funcionando** (não é obrigatório substituí-lo pelo diretório `biblioteca-web/`), configure:

```dotenv
BIBLIOTECA_AUTH_ISSUER=http://localhost:9000
BIBLIOTECA_API_URL=http://localhost:8082
BIBLIOTECA_WEB_CLIENT_ID=biblioteca-web
BIBLIOTECA_WEB_CLIENT_SECRET=SEU_SEGREDO_ORIGINAL
```

Verifique em `src/main/resources/application.yml`:

- `scope: openid` (o cliente `biblioteca-web` foi cadastrado somente com `openid`).
- `server.servlet.session.cookie.name: BIBLIOTECA_WEB_SESSION` para não colidir com o cookie do servidor de identidade.
- O callback continua `http://localhost:8081/login/oauth2/code/biblioteca`; **não** precisa mudar o cadastro do cliente OAuth2.

Reinicie a Web. Abra uma **janela anônima**, entre com login local e depois Google. Acesse `/livros` e `/autores`.

## Passo 4 — Testes esperados

| Ação | Resultado esperado |
|---|---|
| `GET /livros` na API 8082 sem token | 401 |
| Access token novo da porta 9000 com `aud=biblioteca-api` e usuário `OPERADOR` | 200 em GET livros/autores |
| Token com `iss` diferente ou assinatura de :8080 | 401 |
| Token válido de outra audiência | 401 |
| Token client_credentials sem scope `biblioteca.read` | 403 em GET (token validado, acesso negado) |
| Token client_credentials com scope `biblioteca.read` | 200 em GET; 403 em POST/PUT/DELETE |
| Login local e Google pela Web 8081 apontando para 9000 | Consultas funcionam |
| POST /autores como GERENTE | 201, `id_usuario` com UUID do JWT |
| POST /autores como OPERADOR | 403 |

> O teste de integração de máquina requer **um cliente OAuth2 cadastrado com `biblioteca.read`**. `client_credentials` de um cliente que só tem `openid` não basta. Não reutilize o cliente da Web para integrações.

## PostgreSQL: escolha segura para esta etapa

O Authorization Server e a API **ainda compartilham o banco existente nesta fase** para manter os dados e não quebrar as FKs SQL já presentes na tabela `autor` e `livro`. Porém, o código da nova API **não contém** entidade `Usuario` e **não consulta** a tabela `usuario` para autenticação.

O arquivo `biblioteca-api/database/schema.sql` descreve a estrutura **para uma base nova independente**, sem a FK com `usuario`. **Não execute esse script agora no banco atual**. O script `migracao_futura_remover_fk_usuario.sql` é apenas para a futura separação física, quando teremos backup e plano de migração de dados.

## Testes e build

Dentro de `biblioteca-api/` no Windows:

```powershell
.\mvnw.cmd clean test
```

O ZIP inclui `.mvn/wrapper/maven-wrapper.properties` ausente do RAR original, para o wrapper Maven funcionar. O teste Testcontainers precisa do Docker Desktop em execução. **O código não foi compilado nem testado com Maven neste ambiente.**

## Rollback

Se a Web não autenticar ou consultar, restaure suas variáveis originais `BIBLIOTECA_AUTH_ISSUER=http://localhost:8080` e `BIBLIOTECA_API_URL=http://localhost:8080`, reinicie a Web e use a Biblioteca original. Não exclua a versão anterior enquanto estiver testando.

## Observações técnicas para entrevista

- O Authorization Server é o provedor de identidade e emissor de JWTs.
- A Biblioteca API é um Resource Server stateless: valida RS256 via JWKS + `iss` + `aud` + tempos de validade.
- A Web é o cliente OAuth2 que guarda os tokens do lado servidor e usa cookie de sessão no navegador.
- Roles humanas `GERENTE`/`OPERADOR` são separadas de scopes de aplicações (`biblioteca.read`).
- `id_usuario` permanece como referência de auditoria, não como relacionamento JPA cruzado.
- Limitação didática: chaves RSA são geradas novamente a cada reinício do Authorization Server, invalidando tokens anteriores. No projeto Nexus Commerce será tratada persistência/rotação das chaves.
- Limitação pendente: `RegisteredClientRepository` atual ainda atribui os mesmos tipos de grant para todos os clientes. Refinar isso em etapa posterior.
