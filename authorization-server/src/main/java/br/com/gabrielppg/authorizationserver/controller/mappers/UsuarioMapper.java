package br.com.gabrielppg.authorizationserver.controller.mappers;

import br.com.gabrielppg.authorizationserver.controller.dto.UsuarioDTO;
import br.com.gabrielppg.authorizationserver.model.Usuario;
import org.mapstruct.Mapper;

@Mapper(componentModel="spring")
public interface UsuarioMapper {
    Usuario toEntity (UsuarioDTO dto);
}
