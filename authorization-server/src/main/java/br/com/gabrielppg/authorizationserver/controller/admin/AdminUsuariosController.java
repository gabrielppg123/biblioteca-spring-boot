package br.com.gabrielppg.authorizationserver.controller.admin;

import br.com.gabrielppg.authorizationserver.controller.admin.dto.CadastroUsuarioForm;
import br.com.gabrielppg.authorizationserver.controller.admin.dto.EdicaoUsuarioForm;
import br.com.gabrielppg.authorizationserver.controller.admin.dto.RedefinicaoSenhaForm;
import br.com.gabrielppg.authorizationserver.model.Usuario;
import br.com.gabrielppg.authorizationserver.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/admin/usuarios")
@PreAuthorize("hasAuthority('GERENTE')")
@RequiredArgsConstructor
public class AdminUsuariosController {

    private final UsuarioService usuarioService;

    @GetMapping
    public String listar(@RequestParam(name = "busca", defaultValue = "") String busca,
                         @AuthenticationPrincipal Usuario administrador,
                         Model model) {
        var resumo = usuarioService.pesquisar(busca).stream()
                .map(u -> new UsuarioResumo(
                        u.getId(),
                        u.getLogin(),
                        u.getEmail() == null ? "—" : u.getEmail(),
                        u.getGoogleSub() == null ? "Local" : "Google",
                        u.getRoles() == null ? List.<String>of() : List.copyOf(u.getRoles()),
                        u.isAtivo(),
                        u.getSenha() != null && !u.getSenha().isBlank()
                ))
                .toList();
        model.addAttribute("usuarios", resumo);
        model.addAttribute("busca", busca);
        model.addAttribute("administradorId", administradorId(administrador));
        return "admin/usuarios";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("form", new CadastroUsuarioForm());
        return "admin/novo-usuario";
    }

    @PostMapping
    public String cadastrar(@Valid @ModelAttribute("form") CadastroUsuarioForm form,
                            BindingResult errors,
                            RedirectAttributes redirectAttributes) {
        if (errors.hasErrors()) {
            form.setSenha("");
            return "admin/novo-usuario";
        }
        Usuario usuario = new Usuario();
        usuario.setLogin(form.getLogin().trim());
        usuario.setEmail(form.getEmail().trim());
        usuario.setSenha(form.getSenha());
        usuario.setRoles(List.of(form.getRole()));
        usuario.setGoogleSub(null);

        try {
            usuarioService.salvar(usuario);
        } catch (IllegalArgumentException exception) {
            errors.reject("usuario.invalido", exception.getMessage());
            form.setSenha("");
            return "admin/novo-usuario";
        }
        redirectAttributes.addFlashAttribute("sucesso", "Usuário cadastrado com sucesso.");
        return "redirect:/admin/usuarios";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable UUID id, Model model,
                         RedirectAttributes redirectAttributes) {
        var usuario = usuarioService.obterPorId(id).orElse(null);
        if (usuario == null) {
            redirectAttributes.addFlashAttribute("erro", "Usuário não encontrado.");
            return "redirect:/admin/usuarios";
        }
        var form = new EdicaoUsuarioForm();
        form.setLogin(usuario.getLogin());
        form.setEmail(usuario.getEmail());
        form.setRole(usuario.getRoles() != null && usuario.getRoles().contains("GERENTE")
                ? "GERENTE" : "OPERADOR");
        model.addAttribute("form", form);
        model.addAttribute("idUsuario", id);
        model.addAttribute("origem", usuario.getGoogleSub() == null ? "Local" : "Google");
        return "admin/editar-usuario";
    }

    @PostMapping("/{id}/editar")
    public String atualizar(@PathVariable UUID id,
                            @Valid @ModelAttribute("form") EdicaoUsuarioForm form,
                            BindingResult errors, Model model,
                            @AuthenticationPrincipal Usuario administrador,
                            RedirectAttributes redirectAttributes) {
        if (!errors.hasErrors()) {
            try {
                usuarioService.atualizarDados(id, form.getLogin(), form.getEmail(),
                        form.getRole(), administradorId(administrador));
                redirectAttributes.addFlashAttribute("sucesso", "Usuário atualizado com sucesso.");
                return "redirect:/admin/usuarios";
            } catch (IllegalArgumentException exception) {
                errors.reject("usuario.invalido", exception.getMessage());
            }
        }
        model.addAttribute("idUsuario", id);
        model.addAttribute("origem", usuarioService.obterPorId(id)
                .map(u -> u.getGoogleSub() == null ? "Local" : "Google").orElse("Não encontrada"));
        return "admin/editar-usuario";
    }

    @GetMapping("/{id}/senha")
    public String formularioSenha(@PathVariable UUID id, Model model,
                                  RedirectAttributes redirectAttributes) {
        var usuario = usuarioService.obterPorId(id).orElse(null);
        if (usuario == null) {
            redirectAttributes.addFlashAttribute("erro", "Usuário não encontrado.");
            return "redirect:/admin/usuarios";
        }
        if (usuario.getSenha() == null || usuario.getSenha().isBlank()) {
            redirectAttributes.addFlashAttribute("erro", "Conta Google sem senha local.");
            return "redirect:/admin/usuarios";
        }
        model.addAttribute("form", new RedefinicaoSenhaForm());
        model.addAttribute("idUsuario", id);
        model.addAttribute("login", usuario.getLogin());
        return "admin/senha-usuario";
    }

    @PostMapping("/{id}/senha")
    public String redefinirSenha(@PathVariable UUID id,
                                 @Valid @ModelAttribute("form") RedefinicaoSenhaForm form,
                                 BindingResult errors, Model model,
                                 RedirectAttributes redirectAttributes) {
        if (!errors.hasErrors()) {
            try {
                usuarioService.redefinirSenha(id, form.getNovaSenha());
                redirectAttributes.addFlashAttribute("sucesso", "Senha redefinida com sucesso.");
                return "redirect:/admin/usuarios";
            } catch (IllegalArgumentException exception) {
                errors.reject("senha.invalida", exception.getMessage());
            }
        }
        form.setNovaSenha("");
        model.addAttribute("idUsuario", id);
        model.addAttribute("login", usuarioService.obterPorId(id)
                .map(Usuario::getLogin).orElse("Usuário não encontrado"));
        return "admin/senha-usuario";
    }

    @PostMapping("/{id}/desativar")
    public String desativar(@PathVariable UUID id,
                           @AuthenticationPrincipal Usuario administrador,
                           RedirectAttributes redirectAttributes) {
        return alterarAtivacao(id, false, administradorId(administrador), redirectAttributes);
    }

    @PostMapping("/{id}/reativar")
    public String reativar(@PathVariable UUID id,
                          @AuthenticationPrincipal Usuario administrador,
                          RedirectAttributes redirectAttributes) {
        return alterarAtivacao(id, true, administradorId(administrador), redirectAttributes);
    }

    private String alterarAtivacao(UUID id, boolean ativo, UUID administradorId,
                                  RedirectAttributes redirectAttributes) {
        try {
            usuarioService.alterarAtivacao(id, ativo, administradorId);
            redirectAttributes.addFlashAttribute("sucesso",
                    ativo ? "Usuário reativado com sucesso." : "Usuário desativado com sucesso.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("erro", exception.getMessage());
        }
        return "redirect:/admin/usuarios";
    }

    private UUID administradorId(Usuario administrador) {
        if (administrador == null || administrador.getId() == null) {
            throw new AccessDeniedException("Identidade do administrador indisponível.");
        }
        return administrador.getId();
    }

    public record UsuarioResumo(UUID id, String login, String email,
                                String origem, List<String> roles,
                                boolean ativo, boolean possuiSenhaLocal) {}
}
