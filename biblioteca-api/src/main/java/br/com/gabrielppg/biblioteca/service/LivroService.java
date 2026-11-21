package br.com.gabrielppg.biblioteca.service;

import br.com.gabrielppg.biblioteca.model.GeneroLivro;
import br.com.gabrielppg.biblioteca.model.Livro;
import br.com.gabrielppg.biblioteca.repository.LivroRepository;
import static br.com.gabrielppg.biblioteca.repository.specs.LivroSpecs.*;

import br.com.gabrielppg.biblioteca.security.SecurityService;
import br.com.gabrielppg.biblioteca.validator.LivroValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import br.com.gabrielppg.biblioteca.model.Autor;
import br.com.gabrielppg.biblioteca.repository.AutorRepository;
import br.com.gabrielppg.biblioteca.exception.RegistroNaoEncontradoException;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LivroService {
    private final LivroRepository repository;
    private final LivroValidator validator;
    private final SecurityService securityService;
    private final AutorRepository autorRepository;

    @Transactional
    public Livro salvar(Livro livro, UUID idAutor) {
        Autor autor = autorRepository.findById(idAutor)
                .orElseThrow(() ->
                        new RegistroNaoEncontradoException(
                                "Autor não encontrado: " + idAutor
                        )
                );
        livro.setAutor(autor);
        validator.validar(livro);
        livro.setIdUsuario(securityService.obterIdUsuarioLogado());
        return repository.save(livro);
    }

    public Optional<Livro> obterPorId(UUID id){
        return repository.findById(id);
    }

    public void deletar (Livro livro){
        repository.delete(livro);
    }

    public Page<Livro> pesquisa(
            String isbn,
            String titulo,
            String nomeAutor,
            GeneroLivro genero,
            Integer anoPublicacao,
            Integer pagina,
            Integer tamanhoPagina
            ){

        Specification<Livro> specs = Specification.where((root, query, cb) -> cb.conjunction());

        if (isbn != null){
            specs = specs.and(isbnEqual(isbn));
        }

        if (titulo != null){
            specs = specs.and(tituloLike(titulo));
        }

        if (genero != null){
            specs = specs.and(generoEqual(genero));
        }

        if (anoPublicacao != null){
            specs = specs.and(anoPublicacaoEqual(anoPublicacao));
        }

        if (nomeAutor != null){
            specs = specs.and(nomeAutorLike(nomeAutor));
        }

        Pageable pageRequest = PageRequest.of(pagina,tamanhoPagina);



        return repository.findAll(specs,pageRequest);

    }


    @Transactional
    public void atualizar(
            UUID id,
            Livro dados,
            UUID idAutor) {

        if (id == null) {
            throw new IllegalArgumentException(
                    "O ID do livro é obrigatório para atualização"
            );
        }

        Livro livro = repository.findById(id)
                .orElseThrow(() ->
                        new RegistroNaoEncontradoException(
                                "Livro não encontrado: " + id
                        )
                );

        Autor autor = autorRepository.findById(idAutor)
                .orElseThrow(() ->
                        new RegistroNaoEncontradoException(
                                "Autor não encontrado: " + idAutor
                        )
                );

        livro.setIsbn(dados.getIsbn());
        livro.setTitulo(dados.getTitulo());
        livro.setDataPublicacao(dados.getDataPublicacao());
        livro.setGenero(dados.getGenero());
        livro.setPreco(dados.getPreco());
        livro.setAutor(autor);

        validator.validar(livro);

        repository.save(livro);
    }

}
