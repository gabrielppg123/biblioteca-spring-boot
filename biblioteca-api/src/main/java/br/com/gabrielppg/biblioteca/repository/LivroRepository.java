
package br.com.gabrielppg.biblioteca.repository;

import br.com.gabrielppg.biblioteca.model.Autor;
import br.com.gabrielppg.biblioteca.model.Livro;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LivroRepository extends
        JpaRepository<Livro, UUID>,
        JpaSpecificationExecutor<Livro> {

    @Override
    @EntityGraph(attributePaths = {"autor"})
    Page<Livro> findAll(
            Specification<Livro> spec,
            Pageable pageable
    );

    @Override
    @EntityGraph(attributePaths = {"autor"})
    Optional<Livro> findById(UUID id);

    List<Livro> findByAutor(Autor autor);

    boolean existsByAutor(Autor autor);

    Optional<Livro> findByIsbn(String isbn);
}
