# Documentação da Biblioteca

Este índice organiza a documentação por objetivo. **Para instalar e executar o projeto, basta seguir um único roteiro.** Os demais arquivos explicam responsabilidades, arquitetura e operação.

## Ordem de leitura para executar a aplicação

1. **[README principal](../README.md):** apresentação do projeto e seus componentes.
2. **[Instalação local](INSTALACAO_LOCAL.md):** sequência completa de clonagem, configuração, criação dos bancos, inicialização e teste funcional.

Não é necessário ler outro documento para concluir a instalação limpa.

## Para compreender a implementação

| Documento | Conteúdo |
| --- | --- |
| [Authorization Server](../authorization-server/README.md) | OAuth2, OpenID Connect, login e clientes |
| [Biblioteca API](../biblioteca-api/README.md) | REST, JWT, permissões e persistência |
| [Biblioteca Web](../biblioteca-web/README.md) | Interface MVC e cliente OAuth2 |
| [Decisão arquitetural ADR-001](arquitetura/ADR-001-separacao.md) | Separação das aplicações e dos bancos |

## Diagramas

1. [Login, emissão de token e consulta à API](fluxos/01-login-consulta-com-3-aplicacoes.md)
2. [Integração máquina a máquina — Client Credentials](fluxos/02-client-credentials.md)
3. [Identificação de quem cadastrou autores e livros](fluxos/03-auditoria-de-cadastros.md)

## Para operar ou diagnosticar o Docker

Consulte o [guia de operação Docker](../docker/README.md) para entender portas, volumes, logs, backup e solução de problemas. Ele é **uma referência complementar**, não um segundo tutorial de instalação.

## Convenções dos procedimentos

- Todos os comandos de terminal pressupõem execução na **raiz do repositório**, salvo indicação explícita.
- Cada bloco `bash` contém **apenas um comando**, que deve ser executado separadamente.
- Comandos opcionais são identificados como opcionais e acompanhados da condição de uso.
- Valores privados devem permanecer em `docker/.env`, fora do Git.
- Os passos de instalação utilizam um banco inicial vazio e não dependem de dados de outro ambiente.
