# Docker Compose — referência de operação local

Este documento descreve como operar, diagnosticar e fazer backup do ambiente Docker da Biblioteca. **Não é um guia independente de instalação.** Para criar um ambiente do zero, use [Instalação local](../docs/INSTALACAO_LOCAL.md).

Todos os comandos abaixo devem ser executados **na raiz do repositório**, com Git Bash no Windows e `docker/.env` já configurado. Os blocos `bash` possuem um comando por vez.

## Componentes

| Serviço Compose | Porta no computador | Finalidade |
| --- | --- | --- |
| `postgres` | `127.0.0.1:15433` | Bancos `authorization_db` e `biblioteca_db` |
| `authorization-server` | `127.0.0.1:9000` | Login e tokens OAuth2/OIDC |
| `biblioteca-api` | `127.0.0.1:8080` | API REST |
| `biblioteca-web` | `127.0.0.1:8081` | Interface HTML |

O Compose utiliza uma rede interna para comunicação entre serviços. O projeto Compose `biblioteca-clone` mantém rede e volume separados de outros projetos, mas as portas publicadas no Windows precisam estar livres.

## Consultar o ambiente

**1. Ver os contêineres do projeto:**

```bash
docker compose -p biblioteca-clone --env-file docker/.env -f docker/compose.yaml ps
```

**2. Listar todos os contêineres em execução e suas portas:**

```bash
docker ps --format "table {{.Names}}\t{{.Ports}}"
```

## Consultar logs

Os logs podem conter informações sensíveis. Revise o conteúdo antes de compartilhar.

**1. PostgreSQL:**

```bash
docker compose -p biblioteca-clone --env-file docker/.env -f docker/compose.yaml logs --tail=80 postgres
```

**2. Authorization Server:**

```bash
docker compose -p biblioteca-clone --env-file docker/.env -f docker/compose.yaml logs --tail=80 authorization-server
```

**3. API REST:**

```bash
docker compose -p biblioteca-clone --env-file docker/.env -f docker/compose.yaml logs --tail=80 biblioteca-api
```

**4. Interface Web:**

```bash
docker compose -p biblioteca-clone --env-file docker/.env -f docker/compose.yaml logs --tail=80 biblioteca-web
```

## Encerrar sem apagar os bancos

**Comando — Parar os serviços mantendo os volumes:**

```bash
docker compose -p biblioteca-clone --env-file docker/.env -f docker/compose.yaml down
```

Os dados do PostgreSQL permanecem no volume `biblioteca-clone_postgres_data`. **Não execute `down -v` ou `docker volume prune`** em ambientes que contenham dados a preservar.

## Fazer backup dos bancos existentes

Esta operação é opcional e pressupõe que o PostgreSQL do projeto esteja em execução. Os backups podem conter dados pessoais e credenciais protegidas por hash; guarde-os com acesso restrito e não os publique no Git.

**Comando 1 — Criar a pasta local de backups:**

```bash
mkdir -p docker/backups
```

**Comando 2 — Exportar o banco de autenticação:**

```bash
docker compose -p biblioteca-clone --env-file docker/.env -f docker/compose.yaml exec -T postgres pg_dump -U postgres -Fc -d authorization_db > docker/backups/authorization_db.dump
```

**Comando 3 — Exportar o banco da API:**

```bash
docker compose -p biblioteca-clone --env-file docker/.env -f docker/compose.yaml exec -T postgres pg_dump -U postgres -Fc -d biblioteca_db > docker/backups/biblioteca_db.dump
```

**Comando 4 — Conferir se os arquivos foram criados:**

```bash
ls -lh docker/backups/*.dump
```

Confirme que os arquivos têm tamanho maior que zero. O diretório `docker/backups/` é ignorado pelo Git.

### Restauração de backups

**A restauração não faz parte da instalação limpa.** O PostgreSQL executa os scripts de inicialização automaticamente na criação de um volume vazio; restaurar um dump sobre tabelas já criadas pode causar erros ou sobrescrever dados. Uma recuperação exige validar a versão do dump, utilizar bancos de destino apropriados e verificar a integridade antes de substituir qualquer dado. Por esse motivo, não há um comando genérico de restauração neste guia.

## Comunicação entre os serviços

No navegador, o Authorization Server utiliza o endereço público local `http://localhost:9000`. Dentro dos contêineres, as chamadas servidor a servidor usam o DNS da rede Docker, por exemplo `http://authorization-server:9000`.

A aplicação Web ativa uma configuração OAuth2 específica do perfil `docker` para separar URLs acessíveis pelo navegador das URLs internas de troca de tokens e JWKS. A API utiliza JWKS pelo endereço interno, validando o emissor definido no token.

## Limites do ambiente

Este Compose publica portas apenas em `127.0.0.1` e foi projetado para demonstração local. Uma implantação pública requer HTTPS, gestão de segredos, configuração de domínios e persistência/rotação de chaves de assinatura JWT. Os Dockerfiles não substituem o workflow de testes Maven disponível em `.github/workflows/ci.yml`.
