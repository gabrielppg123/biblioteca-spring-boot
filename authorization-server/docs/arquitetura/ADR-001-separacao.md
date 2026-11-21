# ADR-001 — Separar autenticação e recursos de negócio

## Decisão
Criar Authorization Server dedicado na porta local 9000, responsável por autenticar pessoas (senha/Google), manter clientes OAuth2, publicar JWKs e emitir tokens. Posteriormente, a Biblioteca API na porta 8080 será somente Resource Server, e a Biblioteca Web na porta 8081 será cliente OAuth2 confidencial.

## Por que
Uma única aplicação acumulava login por sessão, autenticação federada, emissão e validação de JWT, CRUD de livros e autorização de clientes, dificultando o entendimento e a manutenção.

## Estrutura de implantação
- Etapa 1: novos processos separados, banco **temporariamente compartilhado**, Web ainda conectada ao emissor antigo
- Etapa 2: redirecionar a Web para o novo issuer (após configurar cliente OAuth2 e callbacks)
- Etapa 3: API Resource Server valida `issuer`, assinatura, `aud` e permissões sem depender de sessão, e a identidade se relaciona por UUID
- Etapa 4: separar tabelas `usuario` e `client` em banco de identidade próprio, preservar UUIDs existentes; planejar migração das FKs do domínio

## Limitações temporárias
- `OAuth2AuthorizationService` usa armazenamento efêmero; códigos/tokens/autorizações não sobrevivem a reinício sem solução posterior.
- RSA é gerada na inicialização para este projeto de demonstração; tokens anteriores deixam de ser verificáveis após reiniciar.
- O registro de clientes atual permite múltiplos grants indiscriminadamente: separar clientes humanos e automações posteriormente.
- A URI externa e HTTPS devem ser revistas no deploy AWS; a configuração atual é **apenas local**.
