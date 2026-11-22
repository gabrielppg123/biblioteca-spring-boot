package br.com.gabrielppg.authorizationserver.controller.admin.dto;

import br.com.gabrielppg.authorizationserver.model.TipoCliente;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EdicaoClienteForm {

    @NotNull(message = "Selecione o tipo do cliente.")
    private TipoCliente tipoCliente;

    @NotBlank(message = "Informe pelo menos um scope.")
    @Size(max = 500, message = "Scopes devem ter até 500 caracteres.")
    private String scope;

    @Size(max = 500, message = "A Redirect URI deve ter até 500 caracteres.")
    private String redirectURI;
}
