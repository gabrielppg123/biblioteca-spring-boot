# ADR-001 — Separação entre autenticação e API de negócio

**Estado: implementada.** Este registro documenta uma decisão arquitetural já executada.

## Contexto

A aplicação inicial reunia login por sessão, autenticação federada, emissão de tokens, CRUD de livros/autores e validação de JWT no mesmo processo.

## Decisão implementada

Separar três aplicações:

- `authorization-server` (porta `9000`): usuários, clientes OAuth2, login local e Google, OAuth2/OIDC e assinatura de tokens.
- `biblioteca-api` (porta `8080`): API REST stateless, Resource Server e validação de JWT (JWKS, issuer, audience e expiração).
- `biblioteca-web` (porta `8081`): cliente OAuth2 com Spring MVC e Thymeleaf.

Os dados de identidade ficam em `authorization_db`; autores e livros ficam em `biblioteca_db`. A API registra `usuario_id` como UUID de auditoria, sem dependência de uma entidade JPA `Usuario`.

Clientes `INTERATIVO` e `INTEGRACAO` têm grants distintos: Authorization Code/Refresh Token e Client Credentials, respectivamente. PKCE é exigido para clientes interativos.

## Evoluções para implantação pública

- Persistir/gerenciar as chaves de assinatura JWT, atualmente geradas ao iniciar o Authorization Server.
- Configurar issuer e redirecionamentos com domínio e HTTPS.
- Definir persistência das autorizações OAuth2, políticas de sessão e segredos para a infraestrutura de implantação.

Veja o [README principal](../../../README.md) para o estado atual do projeto e execução em DEV/HML.
