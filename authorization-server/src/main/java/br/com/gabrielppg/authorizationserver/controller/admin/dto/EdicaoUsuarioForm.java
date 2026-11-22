package br.com.gabrielppg.authorizationserver.controller.admin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class EdicaoUsuarioForm {

    @NotBlank(message = "Informe o login.")
    @Pattern(regexp = "[A-Za-z0-9._-]{3,50}",
            message = "Use de 3 a 50 caracteres: letras, números, ponto, hífen ou sublinhado.")
    private String login;

    @Email(message = "Informe um e-mail válido.")
    private String email;

    @NotBlank(message = "Selecione o papel do usuário.")
    @Pattern(regexp = "GERENTE|OPERADOR", message = "Papel inválido.")
    private String role;

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
