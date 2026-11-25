# ADR-001 — Autenticação e API de negócio em aplicações separadas

**Status:** adotada.

## Contexto

A autenticação OAuth2/OpenID Connect, a emissão de tokens e o gerenciamento de identidades têm responsabilidades diferentes das operações de domínio sobre autores e livros. Separar essas responsabilidades reduz o acoplamento entre a camada de identidade e as regras de negócio da Biblioteca.

## Decisão

A arquitetura contém três aplicações:

| Aplicação | Responsabilidade | Porta local |
| --- | --- | --- |
| `authorization-server` | Identidades, clientes OAuth2, login e emissão de tokens | `9000` |
| `biblioteca-api` | Resource Server JWT e operações REST de autores e livros | `8080` |
| `biblioteca-web` | Interface MVC e cliente OAuth2/OIDC | `8081` |

O Authorization Server usa o banco `authorization_db`; a API usa `biblioteca_db`. A Web não possui banco de dados próprio. A API mantém o UUID do usuário autenticado como valor de auditoria, sem referência JPA entre os bancos de identidade e negócio.

O Authorization Server diferencia clientes `INTERATIVO` e `INTEGRACAO`: clientes interativos utilizam Authorization Code com PKCE e Refresh Token; clientes de integração utilizam Client Credentials. A API valida os tokens JWT por assinatura, emissor, audiência e expiração, sem realizar login.

## Consequências

- Autenticação e dados de negócio podem evoluir separadamente.
- O servidor de autorização torna-se uma dependência explícita para login e descoberta de chaves JWKS.
- A integração OAuth2 precisa considerar URLs de navegador e endereços internos da rede Docker.
- Um ambiente público exige gestão de segredos, HTTPS e chaves persistentes.

Veja a [visão geral](../../README.md), o [guia de instalação](../INSTALACAO_LOCAL.md) e o [fluxo de login](../fluxos/01-login-consulta-com-3-aplicacoes.md).
