# Biblioteca Web — cadastro de autor e livro

Entrega incremental baseada na estrutura do projeto `biblioteca-web` do pacote de migração de 21/11/2026. **So altera arquivos da biblioteca-web**, nao substitui `SegurancaWebConfiguration.java`, `application.yml`, API ou Authorization Server.

## Aplicacao

1. Faca um backup/commit do `biblioteca-web` atual.
2. Copie o conteudo de `biblioteca-web/src` deste ZIP para a mesma pasta no projeto do IntelliJ, preservando a hierarquia. Confirme sobrescrita **apenas** para `templates/livros.html`, `templates/autores.html` e `static/css/estilo.css`. Os demais arquivos sao novos.
3. Nao sobrescreva seu projeto inteiro: ele ja contem correcoes locais de OAuth2 e URL da API.
4. Confira as variaveis atuais no IntelliJ: `BIBLIOTECA_AUTH_ISSUER=http://localhost:9000`, `BIBLIOTECA_API_URL=http://localhost:8080`, alem de client ID e secret originais. Nao mexer em roles/scopes.
5. Reinicie **somente** a `biblioteca-web` (`:8081`) apos compilar.

## Fluxo para popular o banco vazio

1. Entrar em http://localhost:8081 com uma conta que tenha role `GERENTE` no access token.
2. Acessar `http://localhost:8081/autores/novo` e cadastrar um autor. `POST /autores` na API exige `GERENTE`.
3. Apos salvar o autor, a Web redireciona para `http://localhost:8081/livros/novo`.
4. Escolher o autor e preencher os campos. `POST /livros` exige `GERENTE` ou `OPERADOR`.
5. O salvamento redireciona para `/livros`, onde o livro deve aparecer na consulta.

## Regras e seguranca

- Mantem o login local/Google existente e o CSRF no formulario Web.
- Usa o access token **do usuario logado** no back-end da Web, nao um token de `client_credentials`.
- Nunca envia token ao browser; API continua validando assinatura, audiencia, issuer e autoridades.
- Autor: nome, nacionalidade e nascimento anterior a hoje. Livro: titulo, ISBN, publicacao anterior a hoje, genero, autor; preco obrigatorio para livros a partir de 2020 (seguindo validador existente).
- A API valida novamente tudo e protege o cadastro com `@PreAuthorize`.
- A interface pode ser aberta por usuarios autenticados sem a role necessaria; a gravacao sera negada com 403 pela API e a Web mostrara mensagem. Visibilidade da UI por role e uma melhoria posterior, sem substituir a seguranca da API.

## Diagnosticos e limitacoes

- HTTP 403 ao gravar autor: entrar como GERENTE. HTTP 403 ao gravar livro: entrar como GERENTE ou OPERADOR.
- Se o cadastro retorna HTTP 500, leia log da Biblioteca API. Se ainda existir FK `id_usuario` apontando para tabela `usuario` compartilhada, nao remova a FK sem planejamento de migracao; a separacao de bancos sera realizada depois, com backup e esquema independente.
- Nao foi possivel executar compilacao Maven neste ambiente; arquivos conferidos por leitura e verificacoes estruturais. Se ocorrer incompatibilidade no projeto local, envie erro e ajustamos sem alterar configuracoes que funcionam.
