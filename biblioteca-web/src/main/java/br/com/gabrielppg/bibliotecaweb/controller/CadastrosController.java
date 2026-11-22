package br.com.gabrielppg.bibliotecaweb.controller;

import br.com.gabrielppg.bibliotecaweb.client.BibliotecaApiClient;
import br.com.gabrielppg.bibliotecaweb.client.CadastroApiClient;
import br.com.gabrielppg.bibliotecaweb.dto.NovoAutorForm;
import br.com.gabrielppg.bibliotecaweb.dto.NovoLivroForm;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;

@Controller
public class CadastrosController {

    private static final List<String> GENEROS = List.of(
            "FICCAO", "FANTASIA", "MISTERIO", "ROMANCE", "BIOGRAFIA", "CIENCIA");

    private final BibliotecaApiClient consultaApi;
    private final CadastroApiClient cadastroApi;

    public CadastrosController(BibliotecaApiClient consultaApi, CadastroApiClient cadastroApi) {
        this.consultaApi = consultaApi;
        this.cadastroApi = cadastroApi;
    }

    @GetMapping("/autores/novo")
    public String formularioAutor(Model model) {
        model.addAttribute("form", new NovoAutorForm());
        return "novo-autor";
    }

    @PostMapping("/autores/novo")
    public String cadastrarAutor(@ModelAttribute("form") NovoAutorForm form,
                                  @RegisteredOAuth2AuthorizedClient("biblioteca") OAuth2AuthorizedClient cliente,
                                  Model model, RedirectAttributes flash) {
        try {
            if (vazio(form.getNome()) || vazio(form.getNacionalidade())
                    || vazio(form.getDataNascimento())) {
                model.addAttribute("erro", "Preencha nome, nascimento e nacionalidade.");
                return "novo-autor";
            }
            LocalDate nascimento = dataAnteriorHoje(form.getDataNascimento(), "nascimento");
            cadastroApi.cadastrarAutor(cliente.getAccessToken().getTokenValue(),
                    form.getNome().trim(), nascimento, form.getNacionalidade().trim());
            flash.addFlashAttribute("sucesso", "Autor cadastrado! Agora voce pode cadastrar o livro.");
            return "redirect:/livros/novo";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("erro", ex.getMessage());
        } catch (RestClientResponseException ex) {
            model.addAttribute("erro", erroCadastro(ex, "autor"));
        } catch (ResourceAccessException ex) {
            model.addAttribute("erro", "Nao foi possivel acessar a API. Confira BIBLIOTECA_API_URL.");
        }
        return "novo-autor";
    }

    @GetMapping("/livros/novo")
    public String formularioLivro(@RegisteredOAuth2AuthorizedClient("biblioteca") OAuth2AuthorizedClient cliente,
                                  Model model) {
        model.addAttribute("form", new NovoLivroForm());
        preencherAutores(cliente, model);
        return "novo-livro";
    }

    @PostMapping("/livros/novo")
    public String cadastrarLivro(@ModelAttribute("form") NovoLivroForm form,
                                 @RegisteredOAuth2AuthorizedClient("biblioteca") OAuth2AuthorizedClient cliente,
                                 Model model, RedirectAttributes flash) {
        try {
            if (vazio(form.getIsbn()) || vazio(form.getTitulo())
                    || vazio(form.getDataPublicacao()) || vazio(form.getGenero())
                    || vazio(form.getIdAutor())) {
                throw new IllegalArgumentException("Preencha ISBN, titulo, publicacao, genero e autor.");
            }
            LocalDate publicacao = dataAnteriorHoje(form.getDataPublicacao(), "publicacao");
            if (!GENEROS.contains(form.getGenero())) {
                throw new IllegalArgumentException("Selecione um genero valido.");
            }
            UUID idAutor;
            try {
                idAutor = UUID.fromString(form.getIdAutor());
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("Selecione um autor valido.");
            }
            BigDecimal preco = null;
            if (!vazio(form.getPreco())) {
                try {
                    preco = new BigDecimal(form.getPreco().trim().replace(',', '.'));
                    if (preco.signum() < 0) {
                        throw new IllegalArgumentException("O preco nao pode ser negativo.");
                    }
                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException("Informe um preco valido (ex.: 39,90).");
                }
            }
            if (publicacao.getYear() >= 2020 && preco == null) {
                throw new IllegalArgumentException("Para livros publicados a partir de 2020, o preco e obrigatorio.");
            }
            cadastroApi.cadastrarLivro(cliente.getAccessToken().getTokenValue(),
                    form.getIsbn().trim(), form.getTitulo().trim(), publicacao,
                    form.getGenero(), preco, idAutor);
            flash.addFlashAttribute("sucesso", "Livro cadastrado com sucesso!");
            return "redirect:/livros";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("erro", ex.getMessage());
        } catch (RestClientResponseException ex) {
            model.addAttribute("erro", erroCadastro(ex, "livro"));
        } catch (ResourceAccessException ex) {
            model.addAttribute("erro", "Nao foi possivel acessar a API. Confira BIBLIOTECA_API_URL.");
        }
        preencherAutores(cliente, model);
        return "novo-livro";
    }

    @GetMapping("/livros/{id}/editar")
    public String formularioEdicaoLivro(
            @PathVariable UUID id,
            @RegisteredOAuth2AuthorizedClient("biblioteca") OAuth2AuthorizedClient cliente,
            Model model,
            RedirectAttributes flash) {

        try {
            var livro = consultaApi.consultarLivroPorId(
                    cliente.getAccessToken().getTokenValue(),
                    id
            );

            if (livro == null) {
                flash.addFlashAttribute("erro", "Livro não encontrado.");
                return "redirect:/livros";
            }

            NovoLivroForm form = new NovoLivroForm();

            form.setIsbn(livro.isbn());
            form.setTitulo(livro.titulo());
            form.setDataPublicacao(
                    livro.dataPublicacao() != null ? livro.dataPublicacao().toString() : ""
            );
            form.setGenero(
                    livro.genero() != null ? livro.genero().toString() : ""
            );
            form.setPreco(
                    livro.preco() != null ? livro.preco().toPlainString() : ""
            );
            form.setIdAutor(
                    livro.autor() != null && livro.autor().id() != null
                            ? livro.autor().id().toString()
                            : ""
            );

            model.addAttribute("form", form);
            model.addAttribute("idLivro", id);

            preencherAutores(cliente, model);

            return "editar-livro";

        } catch (RestClientResponseException ex) {

            String mensagem = switch (ex.getStatusCode().value()) {
                case 401 -> "Sua sessão expirou ou o token foi recusado pela API.";
                case 403 -> "Você não tem permissão para consultar este livro.";
                case 404 -> "Livro não encontrado.";
                default -> "Erro ao consultar o livro. HTTP " + ex.getStatusCode().value() + ".";
            };

            flash.addFlashAttribute("erro", mensagem);

        } catch (ResourceAccessException ex) {
            flash.addFlashAttribute(
                    "erro",
                    "Não foi possível acessar a API da Biblioteca."
            );
        }

        return "redirect:/livros";
    }

    @PostMapping("/livros/{id}/editar")
    public String atualizarLivro(
            @PathVariable UUID id,
            @ModelAttribute("form") NovoLivroForm form,
            @RegisteredOAuth2AuthorizedClient("biblioteca") OAuth2AuthorizedClient cliente,
            Model model,
            RedirectAttributes flash) {

        try {
            if (vazio(form.getIsbn()) || vazio(form.getTitulo())
                    || vazio(form.getDataPublicacao()) || vazio(form.getGenero())
                    || vazio(form.getIdAutor())) {
                throw new IllegalArgumentException(
                        "Preencha ISBN, título, publicação, gênero e autor."
                );
            }

            LocalDate publicacao = dataAnteriorHoje(form.getDataPublicacao(), "publicação");

            if (!GENEROS.contains(form.getGenero())) {
                throw new IllegalArgumentException("Selecione um gênero válido.");
            }

            UUID idAutor;

            try {
                idAutor = UUID.fromString(form.getIdAutor());
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("Selecione um autor válido.");
            }

            BigDecimal preco = null;

            if (!vazio(form.getPreco())) {
                try {
                    preco = new BigDecimal(form.getPreco().trim().replace(',', '.'));

                    if (preco.signum() < 0) {
                        throw new IllegalArgumentException("O preço não pode ser negativo.");
                    }

                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException(
                            "Informe um preço válido (ex.: 39,90)."
                    );
                }
            }

            if (publicacao.getYear() >= 2020 && preco == null) {
                throw new IllegalArgumentException(
                        "Para livros publicados a partir de 2020, o preço é obrigatório."
                );
            }

            cadastroApi.atualizarLivro(
                    cliente.getAccessToken().getTokenValue(),
                    id,
                    form.getIsbn().trim(),
                    form.getTitulo().trim(),
                    publicacao,
                    form.getGenero(),
                    preco,
                    idAutor
            );

            flash.addFlashAttribute("sucesso", "Livro atualizado com sucesso!");

            return "redirect:/livros";

        } catch (IllegalArgumentException ex) {

            model.addAttribute("erro", ex.getMessage());

        } catch (RestClientResponseException ex) {

            String mensagem = switch (ex.getStatusCode().value()) {
                case 400, 422 -> "Dados do livro rejeitados pela API. Confira os campos.";
                case 401 -> "Sua sessão expirou ou o token foi recusado pela API.";
                case 403 -> "Você não tem permissão para editar este livro.";
                case 404 -> "Livro não encontrado. Talvez tenha sido excluído.";
                case 409 -> "Já existe um livro cadastrado com esse ISBN.";
                default -> "Erro ao atualizar o livro. HTTP " + ex.getStatusCode().value() + ".";
            };

            model.addAttribute("erro", mensagem);

        } catch (ResourceAccessException ex) {

            model.addAttribute(
                    "erro",
                    "Não foi possível acessar a API da Biblioteca."
            );
        }

        model.addAttribute("idLivro", id);
        preencherAutores(cliente, model);

        return "editar-livro";
    }

    @PostMapping("/livros/{id}/excluir")
    public String excluirLivro(
            @PathVariable UUID id,
            @RegisteredOAuth2AuthorizedClient("biblioteca") OAuth2AuthorizedClient cliente,
            RedirectAttributes flash) {

        try {
            cadastroApi.deletarLivro(
                    cliente.getAccessToken().getTokenValue(),
                    id
            );

            flash.addFlashAttribute("sucesso", "Livro excluído com sucesso!");

        } catch (RestClientResponseException ex) {

            String mensagem = switch (ex.getStatusCode().value()) {
                case 401 -> "Sua sessão expirou ou o token foi recusado pela API.";
                case 403 -> "Você não tem permissão para excluir este livro.";
                case 404 -> "Livro não encontrado. Talvez já tenha sido excluído.";
                default -> "Erro ao excluir o livro. HTTP " + ex.getStatusCode().value() + ".";
            };

            flash.addFlashAttribute("erro", mensagem);

        } catch (ResourceAccessException ex) {
            flash.addFlashAttribute(
                    "erro",
                    "Não foi possível acessar a API da Biblioteca."
            );
        }

        return "redirect:/livros";
    }

    @PostMapping("/autores/{id}/excluir")
    public String excluirAutor(
            @PathVariable UUID id,
            @RegisteredOAuth2AuthorizedClient("biblioteca") OAuth2AuthorizedClient cliente,
            RedirectAttributes flash) {

        try {
            cadastroApi.deletarAutor(
                    cliente.getAccessToken().getTokenValue(),
                    id
            );

            flash.addFlashAttribute("sucesso", "Autor excluído com sucesso!");

        } catch (RestClientResponseException ex) {

            String mensagem = switch (ex.getStatusCode().value()) {
                case 400 -> "Não é possível excluir este autor porque existem livros cadastrados vinculados a ele.";
                case 401 -> "Sua sessão expirou ou o token foi recusado pela API.";
                case 403 -> "Somente usuários GERENTE podem excluir autores.";
                case 404 -> "Autor não encontrado. Talvez já tenha sido excluído.";
                default -> "Erro ao excluir o autor. HTTP " + ex.getStatusCode().value() + ".";
            };

            flash.addFlashAttribute("erro", mensagem);

        } catch (ResourceAccessException ex) {
            flash.addFlashAttribute(
                    "erro",
                    "Não foi possível acessar a API da Biblioteca."
            );
        }

        return "redirect:/autores";
    }

    @GetMapping("/autores/{id}/editar")
    public String formularioEdicaoAutor(
            @PathVariable UUID id,
            @RegisteredOAuth2AuthorizedClient("biblioteca") OAuth2AuthorizedClient cliente,
            Model model,
            RedirectAttributes flash) {

        try {
            var autor = consultaApi.consultarAutorPorId(
                    cliente.getAccessToken().getTokenValue(),
                    id
            );

            if (autor == null) {
                flash.addFlashAttribute("erro", "Autor não encontrado.");
                return "redirect:/autores";
            }

            NovoAutorForm form = new NovoAutorForm();

            form.setNome(autor.nome());
            form.setDataNascimento(
                    autor.dataNascimento() != null
                            ? autor.dataNascimento().toString()
                            : ""
            );
            form.setNacionalidade(autor.nacionalidade());

            model.addAttribute("form", form);
            model.addAttribute("idAutor", id);

            return "editar-autor";

        } catch (RestClientResponseException ex) {

            String mensagem = switch (ex.getStatusCode().value()) {
                case 401 -> "Sua sessão expirou ou o token foi recusado pela API.";
                case 403 -> "Você não tem permissão para consultar este autor.";
                case 404 -> "Autor não encontrado.";
                default -> "Erro ao consultar o autor. HTTP "
                        + ex.getStatusCode().value() + ".";
            };

            flash.addFlashAttribute("erro", mensagem);

        } catch (ResourceAccessException ex) {
            flash.addFlashAttribute(
                    "erro",
                    "Não foi possível acessar a API da Biblioteca."
            );
        }

        return "redirect:/autores";
    }

    @PostMapping("/autores/{id}/editar")
    public String atualizarAutor(
            @PathVariable UUID id,
            @ModelAttribute("form") NovoAutorForm form,
            @RegisteredOAuth2AuthorizedClient("biblioteca") OAuth2AuthorizedClient cliente,
            Model model,
            RedirectAttributes flash) {

        try {
            if (vazio(form.getNome())
                    || vazio(form.getNacionalidade())
                    || vazio(form.getDataNascimento())) {
                throw new IllegalArgumentException(
                        "Preencha nome, nascimento e nacionalidade."
                );
            }

            String nome = form.getNome().trim();
            String nacionalidade = form.getNacionalidade().trim();

            if (nome.length() < 2 || nome.length() > 100) {
                throw new IllegalArgumentException(
                        "O nome deve possuir entre 2 e 100 caracteres."
                );
            }

            if (nacionalidade.length() < 2 || nacionalidade.length() > 50) {
                throw new IllegalArgumentException(
                        "A nacionalidade deve possuir entre 2 e 50 caracteres."
                );
            }

            LocalDate nascimento = dataAnteriorHoje(
                    form.getDataNascimento(),
                    "nascimento"
            );

            cadastroApi.atualizarAutor(
                    cliente.getAccessToken().getTokenValue(),
                    id,
                    nome,
                    nascimento,
                    nacionalidade
            );

            flash.addFlashAttribute(
                    "sucesso",
                    "Autor atualizado com sucesso!"
            );

            return "redirect:/autores";

        } catch (IllegalArgumentException ex) {

            model.addAttribute("erro", ex.getMessage());

        } catch (RestClientResponseException ex) {

            String mensagem = switch (ex.getStatusCode().value()) {
                case 400, 422 -> "Dados do autor rejeitados pela API. Confira os campos.";
                case 401 -> "Sua sessão expirou ou o token foi recusado pela API.";
                case 403 -> "Somente usuários GERENTE podem editar autores.";
                case 404 -> "Autor não encontrado. Talvez tenha sido excluído.";
                case 409 -> "Já existe um autor cadastrado com esses dados.";
                default -> "Erro ao atualizar o autor. HTTP "
                        + ex.getStatusCode().value() + ".";
            };

            model.addAttribute("erro", mensagem);

        } catch (ResourceAccessException ex) {

            model.addAttribute(
                    "erro",
                    "Não foi possível acessar a API da Biblioteca."
            );
        }

        model.addAttribute("idAutor", id);

        return "editar-autor";
    }

    private void preencherAutores(OAuth2AuthorizedClient cliente, Model model) {
        model.addAttribute("generos", GENEROS);
        try {
            var autores = consultaApi.consultarAutores(cliente.getAccessToken().getTokenValue(), null, null);
            model.addAttribute("autores", autores == null ? List.of() : autores);
        } catch (RestClientResponseException ex) {
            model.addAttribute("autores", List.of());
            if (!model.containsAttribute("erro")) {
                model.addAttribute("erro", "Nao foi possivel consultar autores (HTTP " + ex.getStatusCode().value() + ").");
            }
        } catch (ResourceAccessException ex) {
            model.addAttribute("autores", List.of());
            if (!model.containsAttribute("erro")) {
                model.addAttribute("erro", "Nao foi possivel acessar a API para carregar os autores.");
            }
        }
    }

    private static LocalDate dataAnteriorHoje(String valor, String campo) {
        try {
            LocalDate data = LocalDate.parse(valor);
            if (!data.isBefore(LocalDate.now())) {
                throw new IllegalArgumentException("A data de " + campo + " precisa ser anterior a hoje.");
            }
            return data;
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Informe uma data valida para " + campo + ".");
        }
    }

    private static boolean vazio(String valor) {
        return valor == null || valor.isBlank();
    }

    private static String erroCadastro(RestClientResponseException ex, String recurso) {
        return switch (ex.getStatusCode().value()) {
            case 400, 422 -> "Dados do " + recurso + " rejeitados pela API. Confira os campos e as regras de validacao.";
            case 401 -> "A API recusou o token. Encerre a sessao e entre novamente.";
            case 403 -> "Acesso negado: autor exige GERENTE; livro exige GERENTE ou OPERADOR.";
            case 404 -> "Registro relacionado nao encontrado. Confira o autor selecionado.";
            case 409 -> "Cadastro duplicado. Confira ISBN ou dados do autor.";
            default -> "A API respondeu HTTP " + ex.getStatusCode().value() + ". Consulte os logs da API.";
        };
    }
}
