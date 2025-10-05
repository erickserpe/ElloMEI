package br.com.ellomei.service;

import br.com.ellomei.domain.Usuario;
import br.com.ellomei.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Serviço responsável pelo gerenciamento do perfil do usuário.
 * 
 * Centraliza a lógica de atualização de dados pessoais, senha e configurações
 * da conta, garantindo segurança e validação adequadas.
 */
@Service
@Transactional
public class ProfileService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Atualiza os dados pessoais do usuário de forma segura.
     * 
     * Este método copia apenas os campos permitidos do formulário para o usuário logado,
     * evitando que campos sensíveis (id, password, roles, plano) sejam alterados
     * por requisições maliciosas.
     * 
     * @param usuarioLogado Usuário autenticado (do SecurityContext)
     * @param dadosFormulario Dados enviados pelo formulário
     * @return Usuário atualizado
     */
    public Usuario atualizarDadosPessoais(Usuario usuarioLogado, Usuario dadosFormulario) {
        // Copia apenas os campos permitidos para atualização
        usuarioLogado.setNomeCompleto(dadosFormulario.getNomeCompleto());
        usuarioLogado.setEmail(dadosFormulario.getEmail());
        usuarioLogado.setCpf(dadosFormulario.getCpf());
        usuarioLogado.setRazaoSocial(dadosFormulario.getRazaoSocial());
        usuarioLogado.setNomeFantasia(dadosFormulario.getNomeFantasia());
        usuarioLogado.setCnpj(dadosFormulario.getCnpj());
        usuarioLogado.setDataAberturaMei(dadosFormulario.getDataAberturaMei());

        // Salva as alterações
        return usuarioRepository.save(usuarioLogado);
    }

    /**
     * Atualiza a senha do usuário.
     * 
     * @param usuarioLogado Usuário autenticado
     * @param senhaAtual Senha atual para validação
     * @param novaSenha Nova senha a ser definida
     * @return true se a senha foi atualizada com sucesso, false se a senha atual estiver incorreta
     */
    public boolean atualizarSenha(Usuario usuarioLogado, String senhaAtual, String novaSenha) {
        // Valida a senha atual
        if (!passwordEncoder.matches(senhaAtual, usuarioLogado.getPassword())) {
            return false;
        }

        // Criptografa e salva a nova senha
        usuarioLogado.setPassword(passwordEncoder.encode(novaSenha));
        usuarioRepository.save(usuarioLogado);
        return true;
    }

    /**
     * Busca o usuário atualizado do banco de dados.
     * 
     * Útil para recarregar os dados do usuário após atualizações.
     * 
     * @param id ID do usuário
     * @return Usuário atualizado
     */
    @Transactional(readOnly = true)
    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));
    }
}

