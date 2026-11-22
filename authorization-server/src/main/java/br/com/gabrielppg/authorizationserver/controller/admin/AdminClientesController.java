package br.com.gabrielppg.authorizationserver.controller.admin;

import br.com.gabrielppg.authorizationserver.controller.admin.dto.CadastroClienteForm;
import br.com.gabrielppg.authorizationserver.controller.admin.dto.EdicaoClienteForm;
import br.com.gabrielppg.authorizationserver.model.Client;
import br.com.gabrielppg.authorizationserver.service.ClientService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

@Controller
@RequestMapping("/admin/clientes")
@PreAuthorize("hasAuthority('GERENTE')")
@RequiredArgsConstructor
public class AdminClientesController {

    private final ClientService clientService;

    @GetMapping
    public String listar(@RequestParam(name = "busca", defaultValue = "") String busca, Model model) {
        var resumo = clientService.pesquisar(busca).stream()
                .map(c -> new ClientResumo(c.getId(), c.getClientId(), c.getTipoCliente() == null ? "Não definido" : c.getTipoCliente().name(),
                        c.getScope(), c.getRedirectURI(), c.isAtivo()))
                .toList();
        model.addAttribute("clientes", resumo);
        model.addAttribute("busca", busca);
        return "admin/clientes";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("form", new CadastroClienteForm());
        return "admin/novo-cliente";
    }

    @PostMapping
    public String cadastrar(@Valid @ModelAttribute("form") CadastroClienteForm form,
                            BindingResult errors, RedirectAttributes redirectAttributes) {
        if (!errors.hasErrors()) {
            Client client = new Client();
            client.setClientId(form.getClientId());
            client.setClientSecret(form.getClientSecret());
            client.setTipoCliente(form.getTipoCliente());
            client.setScope(form.getScope());
            client.setRedirectURI(form.getRedirectURI());
            try {
                clientService.salvar(client);
                redirectAttributes.addFlashAttribute("sucesso", "Cliente OAuth2 cadastrado com sucesso.");
                return "redirect:/admin/clientes";
            } catch (IllegalArgumentException ex) {
                errors.reject("cliente.invalido", ex.getMessage());
            }
        }
        form.setClientSecret("");
        return "admin/novo-cliente";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable UUID id, Model model,
                         RedirectAttributes redirectAttributes) {
        Client client = clientService.obterPorId(id);
        if (client == null) {
            return naoEncontrado(redirectAttributes);
        }
        EdicaoClienteForm form = new EdicaoClienteForm();
        form.setTipoCliente(client.getTipoCliente());
        form.setScope(client.getScope());
        form.setRedirectURI(client.getRedirectURI());
        model.addAttribute("form", form);
        model.addAttribute("idCliente", id);
        model.addAttribute("clientId", client.getClientId());
        return "admin/editar-cliente";
    }

    @PostMapping("/{id}/editar")
    public String atualizar(@PathVariable UUID id,
                            @Valid @ModelAttribute("form") EdicaoClienteForm form,
                            BindingResult errors, Model model,
                            RedirectAttributes redirectAttributes) {
        Client client = clientService.obterPorId(id);
        if (client == null) {
            return naoEncontrado(redirectAttributes);
        }
        if (!errors.hasErrors()) {
            try {
                clientService.atualizar(id, form.getTipoCliente(), form.getScope(), form.getRedirectURI());
                redirectAttributes.addFlashAttribute("sucesso", "Configurações atualizadas com sucesso.");
                return "redirect:/admin/clientes";
            } catch (IllegalArgumentException ex) {
                errors.reject("cliente.invalido", ex.getMessage());
            }
        }
        model.addAttribute("idCliente", id);
        model.addAttribute("clientId", client.getClientId());
        return "admin/editar-cliente";
    }

    @GetMapping("/{id}/seguranca")
    public String seguranca(@PathVariable UUID id, Model model,
                           RedirectAttributes redirectAttributes) {
        Client client = clientService.obterPorId(id);
        if (client == null) {
            return naoEncontrado(redirectAttributes);
        }
        model.addAttribute("idCliente", id);
        model.addAttribute("clientId", client.getClientId());
        model.addAttribute("ativo", client.isAtivo());
        return "admin/seguranca-cliente";
    }

    @PostMapping("/{id}/renovar-segredo")
    public String renovarSegredo(@PathVariable UUID id,
                                 @RequestParam(name = "confirmacao", defaultValue = "") String confirmacao,
                                 RedirectAttributes redirectAttributes) {
        try {
            String segredo = clientService.renovarSegredo(id, confirmacao);
            // Flash attribute, não parâmetro da URL. O segredo só aparece no próximo request.
            redirectAttributes.addFlashAttribute("novoSegredo", segredo);
            return "redirect:/admin/clientes/" + id + "/segredo";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("erro", ex.getMessage());
            return "redirect:/admin/clientes/" + id + "/seguranca";
        }
    }

    @PostMapping("/{id}/definir-segredo")
    public String definirSegredo(@PathVariable UUID id,
                                 @RequestParam(name = "confirmacao", defaultValue = "") String confirmacao,
                                 @RequestParam(name = "novoSegredo", defaultValue = "") String novoSegredo,
                                 @RequestParam(name = "confirmacaoNovoSegredo", defaultValue = "") String confirmacaoNovoSegredo,
                                 RedirectAttributes redirectAttributes) {
        try {
            clientService.definirSegredo(id, confirmacao, novoSegredo, confirmacaoNovoSegredo);
            redirectAttributes.addFlashAttribute("sucessoSeguranca",
                    "Client Secret alterado com sucesso. O novo valor já está em vigor. "
                    + "Atualize os sistemas que utilizam este cliente.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("erro", ex.getMessage());
        }
        // Nunca redirecionar com o segredo em URL ou em FlashAttribute.
        return "redirect:/admin/clientes/" + id + "/seguranca";
    }

    @GetMapping("/{id}/segredo")
    public String exibirSegredo(@PathVariable UUID id, Model model,
                                HttpServletResponse response,
                                RedirectAttributes redirectAttributes) {
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
        if (!model.containsAttribute("novoSegredo")) {
            redirectAttributes.addFlashAttribute("erro", "O novo segredo só é mostrado uma vez. Gere outro se necessário.");
            return "redirect:/admin/clientes/" + id + "/seguranca";
        }
        model.addAttribute("idCliente", id);
        Client client = clientService.obterPorId(id);
        model.addAttribute("clientId", client == null ? "Cliente" : client.getClientId());
        return "admin/segredo-cliente";
    }

    @PostMapping("/{id}/desativar")
    public String desativar(@PathVariable UUID id,
                           @RequestParam(name = "confirmacao", defaultValue = "") String confirmacao,
                           RedirectAttributes redirectAttributes) {
        return alterarAtivacao(id, false, confirmacao, redirectAttributes);
    }

    @PostMapping("/{id}/reativar")
    public String reativar(@PathVariable UUID id,
                          @RequestParam(name = "confirmacao", defaultValue = "") String confirmacao,
                          RedirectAttributes redirectAttributes) {
        return alterarAtivacao(id, true, confirmacao, redirectAttributes);
    }

    private String alterarAtivacao(UUID id, boolean ativo, String confirmacao,
                                  RedirectAttributes redirectAttributes) {
        try {
            clientService.alterarAtivacao(id, ativo, confirmacao);
            redirectAttributes.addFlashAttribute("sucesso",
                    ativo ? "Cliente reativado com sucesso." : "Cliente desativado com sucesso.");
            return "redirect:/admin/clientes";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("erro", ex.getMessage());
            return "redirect:/admin/clientes/" + id + "/seguranca";
        }
    }

    private String naoEncontrado(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("erro", "Cliente OAuth2 não encontrado.");
        return "redirect:/admin/clientes";
    }

    public record ClientResumo(UUID id, String clientId, String tipoCliente,
                               String scopes, String redirectUri, boolean ativo) {}
}
