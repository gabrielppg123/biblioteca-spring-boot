
package br.com.gabrielppg.biblioteca.controller.mappers;

import br.com.gabrielppg.biblioteca.controller.dto.CadastroLivroDTO;
import br.com.gabrielppg.biblioteca.controller.dto.PesquisaLivroDTO;
import br.com.gabrielppg.biblioteca.model.Livro;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = AutorMapper.class)
public interface LivroMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "autor", ignore = true)
    @Mapping(target = "idUsuario", ignore = true)
    @Mapping(target = "dataCadastro", ignore = true)
    @Mapping(target = "dataAtualizacao", ignore = true)
    Livro toEntity(CadastroLivroDTO dto);

    PesquisaLivroDTO toDTO(Livro livro);
}
