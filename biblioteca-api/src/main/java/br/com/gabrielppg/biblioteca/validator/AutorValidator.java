package br.com.gabrielppg.biblioteca.validator;

import br.com.gabrielppg.biblioteca.exception.RegistroDuplicadoException;
import br.com.gabrielppg.biblioteca.model.Autor;
import br.com.gabrielppg.biblioteca.repository.AutorRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AutorValidator {

    private AutorRepository repository;

    public AutorValidator(AutorRepository repository) {
        this.repository = repository;
    }

    public void validar (Autor autor){
        if (existeAutorCadastrado(autor)){
            throw new RegistroDuplicadoException("Autor já cadastrado!");
        }
    }

    private boolean existeAutorCadastrado(Autor autor) {

        Optional<Autor> autorEncontrado =
                repository.findByNomeAndNacionalidadeAndDataNascimento(
                        autor.getNome(),
                        autor.getNacionalidade(),
                        autor.getDataNascimento()
                );

        if (autor.getId() == null) {
            return autorEncontrado.isPresent();
        }

        return autorEncontrado
                .map(encontrado -> !autor.getId().equals(encontrado.getId()))
                .orElse(false);
    }

}
