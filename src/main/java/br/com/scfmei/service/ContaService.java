package br.com.scfmei.service;

import br.com.scfmei.domain.Conta;
import br.com.scfmei.domain.Usuario;
import br.com.scfmei.repository.ContaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ContaService {

    @Autowired
    private ContaRepository contaRepository;

    @Transactional(readOnly = true)
    public List<Conta> buscarTodasPorUsuario(Usuario usuario) {
        return contaRepository.findByUsuario(usuario);
    }

    @Transactional
    public Conta salvar(Conta conta, Usuario usuario) {
        conta.setUsuario(usuario);

        if (conta.getId() == null) {
            conta.setSaldoAtual(conta.getSaldoInicial());
        }
        return contaRepository.save(conta);
    }

    @Transactional(readOnly = true)
    public Optional<Conta> buscarPorId(Long id) {
        return contaRepository.findById(id);
    }

    @Transactional
    public void excluirPorId(Long id) {
        contaRepository.deleteById(id);
    }
}