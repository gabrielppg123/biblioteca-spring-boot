package br.com.gabrielppg.authorizationserver.controller.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record UsuarioDTO(
        @NotBlank(message = "campo obrigatório")
        String login,
        @NotBlank(message = "campo obrigatório")
        @Size(min = 12, max = 72, message = "A senha deve ter de 12 a 72 caracteres.")
        String senha,
        @Email (message = "email inválido")
        @NotBlank(message = "campo obrigatório")
        String email,
        @NotEmpty(message = "Informe pelo menos um papel de usuário.")
        List<@Pattern(regexp = "GERENTE|OPERADOR", message = "Papel inválido.") String> roles) {
}
