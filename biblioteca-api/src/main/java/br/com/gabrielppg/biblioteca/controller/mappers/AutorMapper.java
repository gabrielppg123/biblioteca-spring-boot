package br.com.gabrielppg.biblioteca.controller.mappers;

import br.com.gabrielppg.biblioteca.controller.dto.AutorDTO;
import br.com.gabrielppg.biblioteca.model.Autor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AutorMapper {
    @Mapping(target = "idUsuario", ignore = true)
    Autor toEntity(AutorDTO dto);
    AutorDTO toDTO(Autor autor);
}
