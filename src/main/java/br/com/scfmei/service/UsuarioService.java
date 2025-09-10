package br.com.scfmei.service;

import br.com.scfmei.domain.Usuario;
import br.com.scfmei.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder; // Injetamos o codificador de senhas

    public Usuario salvar(Usuario usuario) {
        // Criptografa a senha antes de salvar no banco
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        // Define um papel padrão para novos usuários
        usuario.setRoles("USER");
        return usuarioRepository.save(usuario);
    }
}