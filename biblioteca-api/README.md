# Biblioteca API — Resource Server

Esta é a versão refatorada da aplicação `biblioteca_21_11.rar`, para executar na porta **8082** como Resource Server independente.

- Recebe JWTs emitidos pelo Authorization Server :9000 e validados via JWKS, emissor e audiência.
- Endpoints de autores e livros continuam com os mesmos DTOs/contratos e paginação.
- `usuario` / `client` / login / emissão de token foram removidos da API.
- O UUID de auditoria (`id_usuario`) é obtido do claim `usuario_id` no JWT.
- `GET` de livros/autores aceita roles humanas ou `SCOPE_biblioteca.read`. A escrita exige role humana.

**Leia o README.md da pasta acima antes de integrar.** Não substitua o banco de dados agora.
