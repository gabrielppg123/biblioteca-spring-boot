package br.com.gabrielppg.authorizationserver.service;

import br.com.gabrielppg.authorizationserver.model.Usuario;
import br.com.gabrielppg.authorizationserver.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository repository;
    private final PasswordEncoder encoder;

    public void salvar(Usuario usuario){
        if (usuario.getSenha() != null) {
            usuario.setSenha(encoder.encode(usuario.getSenha()));
        }
        repository.save(usuario);
    }

    public Usuario obterPorLogin(String login){
        return repository.findByLogin(login);
    }

    public Usuario obterPorEmail (String email){
        return repository.findByEmail(email);
    }


    public Usuario obterPorGoogleSub(String googleSub) {
        return repository.findByGoogleSub(googleSub);
    }
}
