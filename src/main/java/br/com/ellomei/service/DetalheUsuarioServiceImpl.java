package br.com.ellomei.service;

import br.com.ellomei.domain.Usuario;
import br.com.ellomei.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DetalheUsuarioServiceImpl implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);
        if (usuarioOpt.isEmpty()) {
            throw new UsernameNotFoundException("Usuário não encontrado.");
        }

        Usuario usuario = usuarioOpt.get();

        // Converte o Set<Role> para List<SimpleGrantedAuthority>
        // O Spring Security espera objetos GrantedAuthority
        // Filtra roles nulas ou com nome nulo para evitar NullPointerException
        return new User(
                usuario.getUsername(),
                usuario.getPassword(),
                usuario.getRoles() != null
                    ? usuario.getRoles().stream()
                            .filter(role -> role != null && role.getNome() != null)
                            .map(role -> new SimpleGrantedAuthority(role.getNome()))
                            .collect(Collectors.toList())
                    : java.util.Collections.emptyList()
        );
    }
}