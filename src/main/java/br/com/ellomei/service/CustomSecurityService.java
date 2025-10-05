package br.com.ellomei.service;

import br.com.ellomei.domain.Conta;
import br.com.ellomei.domain.Contato;
import br.com.ellomei.domain.CategoriaDespesa;
import br.com.ellomei.domain.Lancamento;
import br.com.ellomei.domain.Comprovante;
import br.com.ellomei.repository.ContaRepository;
import br.com.ellomei.repository.ContatoRepository;
import br.com.ellomei.repository.CategoriaDespesaRepository;
import br.com.ellomei.repository.LancamentoRepository;
import br.com.ellomei.repository.ComprovanteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Custom Security Service for centralized authorization logic.
 * This service provides methods to check if the currently authenticated user
 * is the owner of specific resources in the application.
 */
@Service("customSecurityService")
public class CustomSecurityService {

    @Autowired private ContaRepository contaRepository;
    @Autowired private ContatoRepository contatoRepository;
    @Autowired private CategoriaDespesaRepository categoriaDespesaRepository;
    @Autowired private LancamentoRepository lancamentoRepository;
    @Autowired private ComprovanteRepository comprovanteRepository;

    /**
     * Gets the username of the currently authenticated user.
     * @return the username of the authenticated user
     */
    private String getUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }

    /**
     * Checks if the current user is the owner of the specified Conta.
     * @param contaId the ID of the Conta to check
     * @return true if the current user owns the Conta, false otherwise
     */
    public boolean isContaOwner(Long contaId) {
        return contaRepository.findById(contaId)
                .map(conta -> conta.getUsuario().getUsername().equals(getUsername()))
                .orElse(false);
    }

    /**
     * Checks if the current user is the owner of the specified Contato.
     * @param contatoId the ID of the Contato to check
     * @return true if the current user owns the Contato, false otherwise
     */
    public boolean isContatoOwner(Long contatoId) {
        return contatoRepository.findById(contatoId)
                .map(contato -> contato.getUsuario().getUsername().equals(getUsername()))
                .orElse(false);
    }

    /**
     * Checks if the current user is the owner of the specified CategoriaDespesa.
     * @param categoriaId the ID of the CategoriaDespesa to check
     * @return true if the current user owns the CategoriaDespesa, false otherwise
     */
    public boolean isCategoriaOwner(Long categoriaId) {
        return categoriaDespesaRepository.findById(categoriaId)
                .map(categoria -> categoria.getUsuario().getUsername().equals(getUsername()))
                .orElse(false);
    }

    /**
     * Checks if the current user is the owner of the specified Lancamento.
     * @param lancamentoId the ID of the Lancamento to check
     * @return true if the current user owns the Lancamento, false otherwise
     */
    public boolean isLancamentoOwner(Long lancamentoId) {
        return lancamentoRepository.findById(lancamentoId)
                .map(lancamento -> lancamento.getUsuario().getUsername().equals(getUsername()))
                .orElse(false);
    }

    /**
     * Checks if the current user is the owner of the specified Comprovante.
     * This checks ownership through the associated Lancamento.
     * @param comprovanteId the ID of the Comprovante to check
     * @return true if the current user owns the Comprovante, false otherwise
     */
    public boolean isComprovanteOwner(Long comprovanteId) {
        return comprovanteRepository.findById(comprovanteId)
                .map(comprovante -> comprovante.getLancamento().getUsuario().getUsername().equals(getUsername()))
                .orElse(false);
    }
}
