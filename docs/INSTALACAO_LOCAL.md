# Instalação local — Docker Compose

Este guia apresenta todos os passos para executar a Biblioteca a partir de um clone do repositório em **Windows com Git Bash e Docker Desktop**. O Docker constrói e inicia as três aplicações Java e um PostgreSQL com dois bancos distintos.

**Não é necessário instalar Java, Maven ou configurar o Google** para usar este procedimento. O login local de demonstração é criado automaticamente durante a primeira inicialização de um banco novo.

**Local dos comandos:** abra o Git Bash no diretório em que pretende clonar o projeto. Depois da etapa 1, execute os comandos seguintes na **raiz do repositório clonado**. Os comandos estão separados; execute **um bloco por vez** e confira o resultado antes de avançar.

> O Compose é destinado ao uso local. Não exponha esse ambiente diretamente na internet.

## 1. Baixar o código-fonte

**Comando 1.1 — Clonar o repositório:**

```bash
git clone https://github.com/gabrielppg123/biblioteca-spring-boot.git
```

**Resultado esperado:** uma nova pasta `biblioteca-spring-boot` é criada. Se o repositório exigir autenticação, siga as instruções do Git.

**Comando 1.2 — Entrar na pasta:**

```bash
cd biblioteca-spring-boot
```

**Resultado esperado:** o terminal está na raiz do repositório, onde se encontram `README.md`, `docker/` e as três aplicações.

**Comando 1.3 — Conferir o estado do clone:**

```bash
git status --short
```

**Resultado esperado:** nenhuma linha, indicando que não existem alterações locais.

> Se o repositório já estiver clonado, entre na pasta correspondente e continue a partir da etapa 2. Não é necessário repetir a clonagem.

## 2. Configurar credenciais locais

**Comando 2.1 — Criar o arquivo de ambiente a partir do modelo:**

```bash
cp docker/.env.example docker/.env
```

**Comando 2.2 — Editar o arquivo no Bloco de Notas:**

```bash
notepad docker/.env
```

Substitua os valores de demonstração por **três valores diferentes, aleatórios e fortes**:

| Variável | Uso | Requisito |
| --- | --- | --- |
| `POSTGRES_PASSWORD` | Senha do PostgreSQL local | Senha forte |
| `BOOTSTRAP_ADMIN_PASSWORD` | Senha do login `gerente-demo` | 12 a 72 bytes UTF-8 |
| `BIBLIOTECA_WEB_CLIENT_SECRET` | Segredo do cliente OAuth2 interativo | 16 a 72 bytes UTF-8 |

Para facilitar a leitura do arquivo `.env`, use apenas letras e números nesses valores e evite caracteres de sintaxe como `#`, `$` e aspas. Preserve `BIBLIOTECA_WEB_CLIENT_ID=biblioteca-web-local`.

As variáveis `GOOGLE_CLIENT_ID` e `GOOGLE_CLIENT_SECRET` podem manter os valores fictícios do arquivo de exemplo: o login por Google não fará parte do teste inicial. Salve o arquivo.

**Não inclua `docker/.env` em commits, capturas de tela ou mensagens.**

## 3. Validar as configurações

**Comando 3.1 — Verificar a configuração do Compose sem exibir segredos:**

```bash
docker compose -p biblioteca-clone --env-file docker/.env -f docker/compose.yaml config --quiet
```

**Resultado esperado:** o comando termina sem mensagens de erro.

**Comando 3.2 — Confirmar que o arquivo de credenciais está ignorado pelo Git:**

```bash
git check-ignore docker/.env
```

**Resultado esperado:** `docker/.env`.

## 4. Verificar disponibilidade das portas

O ambiente publica as portas `15433` (PostgreSQL), `9000` (Authorization Server), `8080` (API) e `8081` (Web), todas somente para acesso local.

**Comando 4.1 — Listar contêineres ativos e portas publicadas:**

```bash
docker ps --format "table {{.Names}}\t{{.Ports}}"
```

**Resultado esperado:** nenhum contêiner deve estar utilizando as portas `15433`, `9000`, `8080` ou `8081`.

**Somente se alguma porta estiver ocupada:** identifique o contêiner responsável e, caso possa interrompê-lo com segurança, execute o comando opcional abaixo, substituindo `NOME_DO_CONTAINER` pelo nome apresentado em `docker ps`.

**Comando 4.2 — Opcional: parar um contêiner identificado:**

```bash
docker stop NOME_DO_CONTAINER
```

Esse comando interrompe somente o contêiner indicado e **não exclui os dados nem os volumes**. Caso a porta seja usada por outro processo do Windows, encerre o processo pela ferramenta correspondente. Não interrompa contêineres de terceiros sem confirmar sua finalidade.

> O parâmetro `-p biblioteca-clone` cria redes e volumes separados de outros projetos Compose, mas **não altera as portas do computador**. Portanto, conflitos de porta devem ser resolvidos antes de iniciar o ambiente.

## 5. Inicializar o PostgreSQL

**Comando 5.1 — Criar e iniciar o banco PostgreSQL:**

```bash
docker compose -p biblioteca-clone --env-file docker/.env -f docker/compose.yaml up -d postgres
```

**Resultado esperado:** o contêiner PostgreSQL é iniciado. Na primeira criação de um volume vazio, os scripts em `docker/postgres/` preparam `authorization_db`, `biblioteca_db`, as tabelas, o usuário `gerente-demo` e o cliente OAuth2 `biblioteca-web-local`.

**Comando 5.2 — Conferir o serviço:**

```bash
docker compose -p biblioteca-clone --env-file docker/.env -f docker/compose.yaml ps
```

**Resultado esperado:** o serviço `postgres` aparece em execução, preferencialmente como `healthy` após a inicialização.

## 6. Conferir a criação dos dados iniciais

Execute cada consulta separadamente. Elas apenas leem informações do banco; não exibem senhas nem hashes.

**Comando 6.1 — Consultar a quantidade de usuários no Authorization Server:**

```bash
docker compose -p biblioteca-clone --env-file docker/.env -f docker/compose.yaml exec -T postgres psql -U postgres -d authorization_db -c 'SELECT COUNT(*) AS usuarios FROM usuario;'
```

**Resultado esperado:** `usuarios = 1`.

**Comando 6.2 — Consultar os clientes OAuth2 cadastrados:**

```bash
docker compose -p biblioteca-clone --env-file docker/.env -f docker/compose.yaml exec -T postgres psql -U postgres -d authorization_db -c 'SELECT COUNT(*) AS clientes FROM client;'
```

**Resultado esperado:** `clientes = 1`.

**Comando 6.3 — Confirmar a criação da tabela de autores:**

```bash
docker compose -p biblioteca-clone --env-file docker/.env -f docker/compose.yaml exec -T postgres psql -U postgres -d biblioteca_db -c 'SELECT COUNT(*) AS autores FROM autor;'
```

**Resultado esperado:** `autores = 0`, pois o banco inicial não contém registros de acervo.

Se alguma consulta falhar, consulte a seção *Diagnóstico* antes de prosseguir.

## 7. Construir e iniciar as aplicações

**Comando 7.1 — Construir as imagens e iniciar os serviços:**

```bash
docker compose -p biblioteca-clone --env-file docker/.env -f docker/compose.yaml up -d --build
```

O primeiro build pode precisar baixar imagens e dependências Maven. Aguarde a conclusão dos comandos no terminal e verifique se não houve falhas.

**Comando 7.2 — Consultar o estado dos serviços:**

```bash
docker compose -p biblioteca-clone --env-file docker/.env -f docker/compose.yaml ps
```

**Resultado esperado:** quatro serviços executando: `postgres`, `authorization-server`, `biblioteca-api` e `biblioteca-web`.

## 8. Testar no navegador

1. Abra **`http://localhost:8081`**.
2. Escolha o login local e entre com o usuário **`gerente-demo`** e a senha definida em `BOOTSTRAP_ADMIN_PASSWORD`.
3. Acesse a listagem de autores; cadastre um autor.
4. Acesse a listagem de livros; cadastre um livro associado ao autor.
5. Confirme que os registros aparecem nas consultas.
6. Abra **`http://localhost:9000/admin`** para visualizar a administração do Authorization Server.
7. Para explorar a API, consulte **`http://localhost:8080/swagger-ui/index.html`** e a descoberta OIDC em **`http://localhost:9000/.well-known/openid-configuration`**.

**Resultado esperado:** autenticação local, consultas e operações autorizadas funcionando na interface Web.

O login Google requer configuração externa e é opcional. O ambiente inicial inclui um cliente OAuth2 interativo para a Web; um cliente `INTEGRACAO` precisa ser cadastrado separadamente para testes de Client Credentials.

## 9. Encerrar os serviços sem apagar dados

**Comando 9.1 — Parar e remover somente os contêineres e a rede do projeto Compose:**

```bash
docker compose -p biblioteca-clone --env-file docker/.env -f docker/compose.yaml down
```

**Resultado esperado:** os serviços param e o volume PostgreSQL `biblioteca-clone_postgres_data` é preservado.

**Não utilize `down -v` nem `docker volume prune`:** esses comandos podem remover dados persistentes.

## Diagnóstico — somente se houver falhas

### Conflito de porta

Erro típico: `Bind for 127.0.0.1:15433 failed: port is already allocated`.

**Solução:** repita o comando 4.1, identifique o serviço que ocupa a porta e libere-a de forma controlada. Depois, repita **somente o comando 5.1**. A criação da rede ou do volume pode ter ocorrido mesmo que a inicialização tenha sido interrompida.

### PostgreSQL ou tabelas não disponíveis

**Comando D.1 — Mostrar as últimas mensagens do PostgreSQL:**

```bash
docker compose -p biblioteca-clone --env-file docker/.env -f docker/compose.yaml logs --tail=80 postgres
```

Os scripts SQL são executados apenas quando o volume de dados é criado vazio. Alterar `docker/.env` posteriormente **não altera** automaticamente as senhas e os registros persistidos.

### Falha de inicialização das aplicações

**Comando D.2 — Logs do Authorization Server:**

```bash
docker compose -p biblioteca-clone --env-file docker/.env -f docker/compose.yaml logs --tail=80 authorization-server
```

**Comando D.3 — Logs da API:**

```bash
docker compose -p biblioteca-clone --env-file docker/.env -f docker/compose.yaml logs --tail=80 biblioteca-api
```

**Comando D.4 — Logs da Web:**

```bash
docker compose -p biblioteca-clone --env-file docker/.env -f docker/compose.yaml logs --tail=80 biblioteca-web
```

Revise os logs antes de compartilhá-los, pois podem conter informações sensíveis.

### Login ou token recusado

Confirme que o ID `biblioteca-web-local` e o segredo configurado em `docker/.env` correspondem aos dados usados na **primeira inicialização** do banco. Se o Authorization Server reiniciar, novos tokens poderão ser necessários, pois as chaves RSA do ambiente local são geradas na inicialização.

## Referências adicionais

- [Visão geral do projeto](../README.md)
- [Índice da documentação](README.md)
- [Operação Docker, logs e backup](../docker/README.md)
