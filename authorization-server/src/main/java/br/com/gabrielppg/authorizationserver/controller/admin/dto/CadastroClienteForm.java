package br.com.gabrielppg.authorizationserver.controller.admin.dto;

import br.com.gabrielppg.authorizationserver.model.TipoCliente;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CadastroClienteForm {

    @NotBlank(message = "Client ID é obrigatório.")
    @Pattern(
            regexp = "^[A-Za-z0-9][A-Za-z0-9._-]{2,99}$",
            message = "Utilize de 3 a 100 caracteres: letras, números, ponto, hífen ou underline."
    )
    private String clientId;

    @NotBlank(message = "Client Secret é obrigatório.")
    @Size(max = 72, message = "O segredo deve ter no máximo 72 caracteres.")
    private String clientSecret;

    @NotNull(message = "Selecione o tipo do cliente.")
    private TipoCliente tipoCliente = TipoCliente.INTERATIVO;

    @NotBlank(message = "Informe pelo menos um scope.")
    @Size(max = 500, message = "O campo scope deve ter no máximo 500 caracteres.")
    private String scope;

    @Size(max = 500, message = "A Redirect URI deve ter no máximo 500 caracteres.")
    private String redirectURI;
}
