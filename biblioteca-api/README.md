# Biblioteca API — Resource Server JWT

API REST para consulta e manutenção de autores e livros, implementada em Java 21 e Spring Boot 4.1.2. Utiliza a porta **8080** e o banco PostgreSQL `biblioteca_db`.

## Responsabilidades

- Expor endpoints REST de autores (`/autores`) e livros (`/livros`).
- Oferecer filtros, paginação e operações de cadastro, atualização e exclusão.
- Validar assinatura JWT (JWKS), emissor, audiência `biblioteca-api` e validade dos tokens.
- Aplicar permissões: `GERENTE` administra autores; `GERENTE` e `OPERADOR` administram livros; escopo `biblioteca.read` permite consultas.
- Registrar o UUID do usuário que cadastrou cada autor ou livro, sem depender de uma entidade JPA de usuário neste banco.

## Como executar

Utilize o guia **[Instalação local](../docs/INSTALACAO_LOCAL.md)**. O Docker Compose inicializa o banco `biblioteca_db` e a estrutura de tabelas a partir de [`database/schema.sql`](database/schema.sql).

A API requer um Authorization Server disponível em `http://localhost:9000` para validação dos tokens. As URLs internas do Docker são configuradas no Compose.

## Documentação da API

Com o ambiente iniciado, abra a interface Swagger em:

`http://localhost:8080/swagger-ui/index.html`

## Testes

Na raiz do repositório, com Java 21 e Maven instalados e o Docker Engine disponível para o Testcontainers, execute:

```bash
mvn -f biblioteca-api/pom.xml clean verify
```

Consulte os [fluxos de autorização](../docs/fluxos/02-client-credentials.md) e [identificação do responsável pelo cadastro](../docs/fluxos/03-auditoria-de-cadastros.md) para mais detalhes.
