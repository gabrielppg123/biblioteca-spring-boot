# Biblioteca Web — Interface MVC e cliente OAuth2

Aplicação Spring Boot com Spring MVC e Thymeleaf, disponível na porta **8081**. Exibe formulários e páginas HTML para consulta e manutenção do acervo da Biblioteca.

## Responsabilidades

- Redirecionar o navegador ao Authorization Server para autenticação OAuth2/OIDC.
- Utilizar Authorization Code com PKCE e um cliente confidencial cadastrado no servidor de autorização.
- Enviar o access token do usuário à Biblioteca API nas chamadas realizadas pelo servidor Web.
- Aplicar as respostas de autorização e validação às telas.
- Manter a sessão do navegador em cookie `BIBLIOTECA_WEB_SESSION`.

A aplicação Web **não utiliza banco de dados próprio**. O login local é gerenciado pelo Authorization Server; o login Google é opcional.

## Como executar

Utilize exclusivamente o guia **[Instalação local](../docs/INSTALACAO_LOCAL.md)** para iniciar o sistema completo. O procedimento prepara automaticamente um cliente OAuth2 com o ID `biblioteca-web-local` e uma senha definida em `docker/.env`.

O cliente interativo usa o redirecionamento:

`http://localhost:8081/login/oauth2/code/biblioteca`

## Configuração

Em uma inicialização manual fora do Docker, a aplicação utiliza variáveis de ambiente de `application.yml`, como:

```dotenv
BIBLIOTECA_AUTH_ISSUER=http://localhost:9000
BIBLIOTECA_API_URL=http://localhost:8080
BIBLIOTECA_WEB_CLIENT_ID=ID_DE_CLIENTE_CADASTRADO
BIBLIOTECA_WEB_CLIENT_SECRET=SEGREDO_DEFINIDO_LOCALMENTE
```

Os valores acima são ilustrativos, não credenciais válidas. A configuração de URLs de serviços internos do Docker Compose é feita automaticamente pelo perfil `docker`.

Para visualizar o fluxo de autenticação, consulte o [diagrama de login e consulta](../docs/fluxos/01-login-consulta-com-3-aplicacoes.md).
