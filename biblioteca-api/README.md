# Biblioteca API — Resource Server

API REST Java 21 / Spring Boot 4.1.2 para gestão de livros e autores.

- **Porta local:** `8080` (DEV e HML; veja o README da raiz).
- **Banco:** `biblioteca_db`, independente do banco do Authorization Server.
- **Autenticação:** Bearer JWT emitido pelo Authorization Server na porta `9000`.
- **Validação:** assinatura via JWKS, emissor, audiência `biblioteca-api` e validade.
- **Autorização:** `GERENTE` gerencia autores; `GERENTE` e `OPERADOR` gerenciam livros; `biblioteca.read` permite somente consultas.
- **Auditoria:** UUID do usuário extraído do token, sem relacionamento JPA com a entidade de identidade.

Execute as verificações com `mvn clean verify` nesta pasta. O teste de integração com PostgreSQL utiliza Testcontainers e precisa do Docker disponível.

Para iniciar as três aplicações, consulte o [README principal](../README.md) e o [guia Docker](../docker/README.md). O ambiente HML ainda utiliza a restauração de bases previamente preparadas; o repositório não contém dados privados nem segredos.
