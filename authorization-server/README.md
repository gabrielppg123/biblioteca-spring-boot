# Authorization Server — OAuth2 e OpenID Connect

Aplicação Spring Boot responsável por autenticação de usuários, emissão de tokens e gestão de clientes OAuth2. Utiliza a porta **9000** e o banco PostgreSQL `authorization_db`.

## Responsabilidades

- Login com usuário e senha; login Google opcional mediante configuração de credenciais próprias.
- Emissão de tokens JWT por meio do Spring Authorization Server.
- Authorization Code com PKCE, Refresh Token e OpenID Connect para clientes interativos.
- Client Credentials para clientes de integração.
- Cadastro e administração de usuários e clientes OAuth2.
- Disponibilização de chaves públicas por JWKS para a API.

## Endpoints

| Recurso | Endereço |
| --- | --- |
| Login | `http://localhost:9000/login` |
| Administração | `http://localhost:9000/admin` |
| Descoberta OpenID Connect | `http://localhost:9000/.well-known/openid-configuration` |
| Emissão de tokens | `http://localhost:9000/oauth2/token` |
| Chaves públicas JWKS | `http://localhost:9000/oauth2/jwks` |

## Como executar

Siga **[Instalação local](../docs/INSTALACAO_LOCAL.md)** para preparar todas as dependências e iniciar as três aplicações pelo Docker Compose. O guia cria o banco e um usuário `gerente-demo` para o primeiro acesso, com senha definida no arquivo local `docker/.env`.

Não é necessário criar um usuário Google para testar o login local. Os parâmetros da aplicação estão em `src/main/resources/application.yml`; os valores sensíveis são definidos fora do controle de versão.

## Considerações de segurança

- Usuários e clientes OAuth2 são armazenados no banco próprio do servidor.
- A API recebe JWTs, mas não realiza autenticação por senha nem emite tokens.
- No ambiente Docker local, as chaves de assinatura RSA são geradas durante a inicialização. Após reiniciar o Authorization Server, tokens emitidos anteriormente podem deixar de ser válidos; faça novo login.
- O ambiente local não fornece os requisitos de implantação pública, como HTTPS e gerenciamento persistente de chaves.

Para entender a separação de responsabilidades, consulte a [decisão arquitetural](../docs/arquitetura/ADR-001-separacao.md) e o [fluxo de autenticação](../docs/fluxos/01-login-consulta-com-3-aplicacoes.md).
