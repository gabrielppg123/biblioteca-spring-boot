package br.com.gabrielppg.authorizationserver.service;

import br.com.gabrielppg.authorizationserver.model.Usuario;
import br.com.gabrielppg.authorizationserver.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository repository;
    private final PasswordEncoder encoder;

    // Exclusivo para cadastro. A edição nunca deve passar um hash já armazenado por este método.
    @Transactional
    public void salvar(Usuario usuario) {
        if (usuario.getId() != null) {
            throw new IllegalArgumentException("Utilize a operação de edição para usuários existentes.");
        }

        if (usuario.getLogin() == null || usuario.getLogin().isBlank()) {
            throw new IllegalArgumentException("Informe o login.");
        }
        if (usuario.getRoles() == null || usuario.getRoles().isEmpty()
                || usuario.getRoles().stream().anyMatch(role ->
                !"GERENTE".equals(role) && !"OPERADOR".equals(role))) {
            throw new IllegalArgumentException("Informe pelo menos um papel válido.");
        }
        if (usuario.getGoogleSub() == null
                && (usuario.getSenha() == null || usuario.getSenha().isBlank())) {
            throw new IllegalArgumentException("A senha é obrigatória para usuários locais.");
        }
        validarDuplicidades(usuario.getLogin(), usuario.getEmail(), null);

        if (usuario.getSenha() != null) {
            validarSenha(usuario.getSenha());
            usuario.setSenha(encoder.encode(usuario.getSenha()));
        }
        repository.save(usuario);
    }

    public Usuario obterPorLogin(String login) {
        return repository.findByLogin(login);
    }

    public Usuario obterPorEmail(String email) {
        return repository.findByEmail(email);
    }

    public Usuario obterPorGoogleSub(String googleSub) {
        return repository.findByGoogleSub(googleSub);
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> obterPorId(UUID id) {
        return repository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Usuario> pesquisar(String termo) {
        if (termo == null || termo.isBlank()) {
            return repository.findAll(Sort.by("login"));
        }
        String texto = termo.trim();
        try {
            UUID id = UUID.fromString(texto);
            return repository.findById(id).stream().toList();
        } catch (IllegalArgumentException ignored) {
            return repository.findByLoginContainingIgnoreCaseOrEmailContainingIgnoreCaseOrderByLoginAsc(
                    texto, texto
            );
        }
    }

    @Transactional
    public void atualizarDados(UUID id, String login, String email,
                               String role, UUID administradorId) {
        Usuario usuario = obterObrigatorio(id);
        String loginNormalizado = obrigatorio(login, "Informe o login.");
        String emailNormalizado = email == null || email.isBlank() ? null : email.trim();

        if (emailNormalizado == null && usuario.getGoogleSub() == null) {
            throw new IllegalArgumentException("E-mail obrigatório para usuários locais.");
        }
        validarRole(role);

        if (id.equals(administradorId) && !"GERENTE".equals(role)) {
            throw new IllegalArgumentException("Você não pode retirar seu próprio papel GERENTE.");
        }
        if (possuiPapelGerente(usuario) && !"GERENTE".equals(role)) {
            exigirOutroGerenteAtivo();
        }

        validarDuplicidades(loginNormalizado, emailNormalizado, id);
        usuario.setLogin(loginNormalizado);
        usuario.setEmail(emailNormalizado);
        usuario.setRoles(List.of(role));
        repository.save(usuario);
    }

    @Transactional
    public void redefinirSenha(UUID id, String novaSenha) {
        Usuario usuario = obterObrigatorio(id);
        if (usuario.getSenha() == null || usuario.getSenha().isBlank()) {
            throw new IllegalArgumentException(
                    "Conta exclusivamente Google: não existe senha local para redefinir."
            );
        }
        validarSenha(novaSenha);
        usuario.setSenha(encoder.encode(novaSenha));
        repository.save(usuario);
    }

    @Transactional
    public void alterarAtivacao(UUID id, boolean ativo, UUID administradorId) {
        Usuario usuario = obterObrigatorio(id);
        if (!ativo && id.equals(administradorId)) {
            throw new IllegalArgumentException("Você não pode desativar a própria conta.");
        }
        if (!ativo && usuario.isAtivo() && possuiPapelGerente(usuario)) {
            exigirOutroGerenteAtivo();
        }
        usuario.setAtivo(ativo);
        repository.save(usuario);
    }

    private Usuario obterObrigatorio(UUID id) {
        return repository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("Usuário não encontrado.")
        );
    }

    private void validarDuplicidades(String login, String email, UUID usuarioId) {
        if (login != null && !login.isBlank()) {
            Usuario mesmoLogin = repository.findByLoginIgnoreCase(login.trim());
            if (mesmoLogin != null && !mesmoLogin.getId().equals(usuarioId)) {
                throw new IllegalArgumentException("Esse login já está cadastrado.");
            }
        }
        if (email != null && !email.isBlank()) {
            Usuario mesmoEmail = repository.findByEmailIgnoreCase(email.trim());
            if (mesmoEmail != null && !mesmoEmail.getId().equals(usuarioId)) {
                throw new IllegalArgumentException("Esse e-mail já está cadastrado.");
            }
        }
    }

    private boolean possuiPapelGerente(Usuario usuario) {
        return usuario.getRoles() != null && usuario.getRoles().contains("GERENTE");
    }

    private void exigirOutroGerenteAtivo() {
        if (repository.contarGerentesAtivos() <= 1) {
            throw new IllegalArgumentException("É necessário manter ao menos um GERENTE ativo.");
        }
    }

    private void validarRole(String role) {
        if (!"GERENTE".equals(role) && !"OPERADOR".equals(role)) {
            throw new IllegalArgumentException("Papel de usuário inválido.");
        }
    }

    private String obrigatorio(String valor, String mensagem) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(mensagem);
        }
        return valor.trim();
    }

    private void validarSenha(String senha) {
        if (senha == null || senha.length() < 12 || senha.length() > 72
                || senha.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException(
                    "A senha precisa ter de 12 a 72 caracteres e no máximo 72 bytes em UTF-8."
            );
        }
    }
}
