# Biblioteca — área administrativa, etapa 1

Pacote incremental para o `authorization-server` existente (Spring Boot 4.1.2 / Spring Security 7.1.2).

## Conteúdo

- `/admin` — painel inicial exclusivo para GERENTE.
- `/admin/usuarios` — lista usuários locais e Google sem expor senhas/hashes/Google sub.
- `/admin/usuarios/novo` — cadastro de usuários locais com validações, bcrypt pelo UsuarioService existente, papel GERENTE ou OPERADOR.
- `/admin/clientes` — listagem de clientes OAuth2, **sem** revelar client secrets ou hashes.
- `/css/admin.css` — estilo das páginas.

## Como aplicar

1. Faça backup/commit da versão funcional.
2. Extraia o ZIP. Copie o conteúdo de `authorization-server/src/main` para o mesmo diretório da sua aplicação, preservando a estrutura de pacotes. **São apenas arquivos novos**, nenhum arquivo existente é substituído.
3. **Não altere** `SecurityConfiguration`, `AuthorizationServerConfiguration`, `application.yml` nem o banco.
4. Reinicie somente o `authorization-server` na porta 9000.
5. Em janela anônima, faça login como usuário local GERENTE em http://localhost:9000/login e abra http://localhost:9000/admin.
6. Consulte as duas listagens e cadastre um usuário local OPERADOR para teste. Use um login e email novos. O usuário novo deverá autenticar via Biblioteca Web.
7. Para segurança, verifique que um login Google/OPERADOR recebe 403 ao acessar `/admin`, `/admin/usuarios` e `/admin/clientes`. Envie POST `/admin/usuarios` sem CSRF e confirme que é bloqueado. Não faça teste de permissão usando uma sessão já autenticada como GERENTE.

## O que é intencionalmente excluído nesta etapa

- Cadastro/edição/remoção de clientes: há uma limitação no `CustomRegisteredClientRepository`, que habilita `authorization_code`, `client_credentials` e `refresh_token` para todos os clientes. Na etapa 2 vamos adicionar um tipo/grants explícitos, migrar os registros existentes de forma controlada e só então permitir novos clientes com uma configuração segura.
- Alteração de senha, exclusão de usuário, gerenciamento de roles de usuários existentes: exigem controles adicionais de reautenticação/auditoria. Não são necessários para o MVP.
- Não modifica endpoints de autenticação, cookies, Google, JWKS, JWT, banco de dados, Docker ou AWS.

## Notas de segurança

- Área protegida por `@PreAuthorize("hasAuthority('GERENTE')")` nos métodos do controller; a aplicação já tem `@EnableMethodSecurity` ativo.
- CSRF continua habilitado na cadeia de login; formulário inclui o token explicitamente.
- Dados da tela são DTOs resumidos; nunca passamos a entidade Client completa para a view.
- Não armazene/registre senhas em logs.
- Antes de publicar na AWS, restrinja o acesso à área administrativa (por exemplo, VPN ou autenticação adicional/MFA), mantenha HTTPS e realize testes de autorização.

## Validação

Este pacote foi montado a partir da cópia do código do Authorization Server fornecida anteriormente. O build Maven e a integração com sua aplicação não foram executados neste ambiente. Valide por etapas no IntelliJ e reporte qualquer erro.
