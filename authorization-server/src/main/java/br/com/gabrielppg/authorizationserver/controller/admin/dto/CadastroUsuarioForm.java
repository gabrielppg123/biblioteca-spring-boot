package br.com.gabrielppg.authorizationserver.controller.admin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Formulário exclusivo da área administrativa. Não recebe entidades JPA diretamente. */
public class CadastroUsuarioForm {

    @NotBlank(message = "Informe o login.")
    @Pattern(regexp = "[A-Za-z0-9._-]{3,50}", message = "Use de 3 a 50 caracteres: letras, números, ponto, hífen ou sublinhado.")
    private String login;

    @NotBlank(message = "Informe o email.")
    @Email(message = "Informe um email válido.")
    private String email;

    @NotBlank(message = "Informe uma senha.")
    @Size(min = 12, max = 72, message = "A senha deve ter entre 12 e 72 caracteres.")
    private String senha;

    @NotBlank(message = "Selecione o papel do usuário.")
    @Pattern(regexp = "GERENTE|OPERADOR", message = "Papel inválido.")
    private String role = "OPERADOR";

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
