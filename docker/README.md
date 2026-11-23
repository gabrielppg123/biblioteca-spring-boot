# Biblioteca — Docker Compose local (3 aplicacoes, 2 bancos)

Esta configuracao **nao altera seu PostgreSQL antigo**. O Compose cria um servidor PostgreSQL
separado, com dois bancos (`authorization_db` e `biblioteca_db`) e dados num volume isolado.
Para preservar os usuarios, clientes OAuth2, livros e autores, restaure COPIAS de seus bancos
existentes antes de iniciar os 3 aplicativos. A Web nao tem banco proprio.

## Pre-requisitos

- Docker Desktop iniciado no Windows, com Docker Compose v2.
- As portas 9000, 8080 e 8081 livres (pare os 3 apps iniciados no IntelliJ).
- PostgreSQL antigo ainda funcionando para gerar os backups. **Nao apague o container antigo**.
- O Client Secret em texto original do cliente usado pelo `biblioteca-web`, e credenciais Google
  ja configuradas no Authorization Server. Segredos no banco estao em BCrypt, nao sao recuperaveis.
- Verificar em `authorization_db.client` que o cliente interativo possui a Redirect URI
  `http://localhost:8081/login/oauth2/code/biblioteca` e scope `openid`.

## 1. Preparar o arquivo de ambiente

Na pasta `Apps`, em Git Bash:

```bash
cp docker/.env.example docker/.env
```

Edite `docker/.env` com seus valores locais. Esse arquivo fica ignorado pelo Git por
`docker/.gitignore`. Nao cole seus segredos em mensagens nem em commits.

O campo `POSTGRES_PASSWORD` e a senha do NOVO PostgreSQL do Compose; pode ser diferente da
senha do container atual. Use o `client_id` e o segredo do cliente que ja funciona no `biblioteca-web`.
Se o segredo original foi perdido, use a interface de rotacao de segredo **com cautela**, pois
alterar um cliente usado em producao desconecta outras instancias que ainda usam o segredo antigo.

Execute uma verificacao da configuracao sem imprimir as variaveis:

```bash
docker compose --env-file docker/.env -f docker/compose.yaml config --quiet
git check-ignore docker/.env
```

## 2. Fazer backup dos bancos ANTIGOS

Confira o nome do seu container PostgreSQL atual:

```bash
docker ps --format 'table {{.Names}}\t{{.Ports}}'
```

Os exemplos a seguir usam o nome `bibliotecadb`. Substitua se o seu container tiver outro nome.
Os bancos esperados sao `authorization_db` e `biblioteca_db`.

```bash
mkdir -p docker/backups

docker exec bibliotecadb pg_dump -U postgres -Fc -d authorization_db > docker/backups/authorization_db.dump
docker exec bibliotecadb pg_dump -U postgres -Fc -d biblioteca_db > docker/backups/biblioteca_db.dump

ls -lh docker/backups/*.dump
```

**Nao prossiga** se um comando retornar erro ou um arquivo ficar vazio. Os arquivos `.dump`
contem DADOS REAIS, sao ignorados pelo Git, e devem ser guardados com cuidado.

## 3. Criar o NOVO PostgreSQL pelo Compose

```bash
docker compose --env-file docker/.env -f docker/compose.yaml up -d postgres

docker compose --env-file docker/.env -f docker/compose.yaml ps
```

O PostgreSQL novo tem porta **15433 no host**, para nao conflitar com o PostgreSQL antigo
na porta 5432. Dentro da rede Docker, as aplicacoes se conectam a `postgres:5432`.
O init script cria `biblioteca_db` apenas quando o volume eh novo; `authorization_db`
e criado pelo `POSTGRES_DB`.

## 4. Restaurar COPIAS dentro dos bancos novos

Somente depois de confirmar os backups:

```bash
docker compose --env-file docker/.env -f docker/compose.yaml exec -T postgres \
  pg_restore --exit-on-error --no-owner --no-acl -U postgres -d authorization_db \
  < docker/backups/authorization_db.dump

docker compose --env-file docker/.env -f docker/compose.yaml exec -T postgres \
  pg_restore --exit-on-error --no-owner --no-acl -U postgres -d biblioteca_db \
  < docker/backups/biblioteca_db.dump
```

Esses comandos RESTAURAM nos bancos NOVOS, nao no container `bibliotecadb` antigo.
**Nao rode duas vezes no mesmo banco** sem revisar: as tabelas ja existirao e a importacao falhara.
Nao use `docker compose down -v`: essa opcao remove o volume e todos os dados novos.

Confira as tabelas sem imprimir senhas ou hashes:

```bash
docker compose --env-file docker/.env -f docker/compose.yaml exec postgres \
  psql -U postgres -d authorization_db -c 'SELECT COUNT(*) AS usuarios FROM usuario;'

docker compose --env-file docker/.env -f docker/compose.yaml exec postgres \
  psql -U postgres -d biblioteca_db -c 'SELECT COUNT(*) AS autores FROM autor;'
```

## 5. Subir os 3 sistemas

Pare primeiro os aplicativos executados pelo IntelliJ para liberar 9000, 8080 e 8081.
Depois:

```bash
docker compose --env-file docker/.env -f docker/compose.yaml up -d --build
docker compose --env-file docker/.env -f docker/compose.yaml ps
```

Acompanhe os logs de inicializacao (sem divulgar logs que contenham dados sensiveis):

```bash
docker compose --env-file docker/.env -f docker/compose.yaml logs --tail=80 authorization-server
docker compose --env-file docker/.env -f docker/compose.yaml logs --tail=80 biblioteca-api
docker compose --env-file docker/.env -f docker/compose.yaml logs --tail=80 biblioteca-web
```

## 6. Validar

- Web: http://localhost:8081 (login local + Google, consultas e edicoes por perfil)
- Administracao/Auth: http://localhost:9000/admin
- API: http://localhost:8080/swagger-ui/index.html
- Discovery: http://localhost:9000/.well-known/openid-configuration

Teste com o GERENTE e com o OPERADOR. Confira tambem o Postman, Authorization Code + PKCE,
consultas API e Client Credentials. Nao troque o `client_id` nem a Redirect URI somente por causa do Docker.

## Por que existe `DockerOAuth2ClientConfiguration`?

No browser, o issuer OIDC real continua `http://localhost:9000`. Dentro dos containers,
`localhost` apontaria para o proprio container, entao o Web usa:

- `authorization-uri=http://localhost:9000/oauth2/authorize` (navegador);
- `token-uri=http://authorization-server:9000/oauth2/token` (servidor);
- `jwk-set-uri=http://authorization-server:9000/oauth2/jwks` (servidor);
- `issuer-uri=http://localhost:9000` (validacao do emissor dos ID Tokens).

A configuracao so e ativada com o perfil `docker`. No IntelliJ, sua configuracao normal
com OpenID discovery permanece inalterada. A API tambem consulta JWKS pelo DNS interno,
mas valida o issuer publico `localhost:9000`, preservando a seguranca.

## Parar sem perder dados

```bash
docker compose --env-file docker/.env -f docker/compose.yaml down
```

**Evite `down -v`**, que remove o volume PostgreSQL deste ambiente.

## Limitacoes e proximas etapas

- Esta configuracao e para **desenvolvimento local**, nao para expor na internet.
  Em producao usaremos dominio, HTTPS, secrets externos e endpoints publicos permanentes.
- O Authorization Server gera chaves RSA novas a cada reinicio; tokens anteriores
  podem deixar de ser validos. Persistir/gerenciar chaves e melhoria separada para producao.
- `-DskipTests` no Dockerfile reduz tempo de build: os testes seguem exigidos no GitHub Actions.
- O PostgreSQL novo nao e inicializado com contas e clientes ficticios. Os dumps preservam
  exatamente os dados que voce ja validou. O Compose evita apagar, modificar ou migrar o
  banco original silenciosamente.
- No Docker local, variaveis de ambiente sao acessiveis a quem controla o daemon Docker;
  nao use essa configuracao como modelo definitivo de gerencia de segredos na AWS.
