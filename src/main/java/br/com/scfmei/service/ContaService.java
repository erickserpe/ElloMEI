package br.com.scfmei.service;

import br.com.scfmei.domain.Conta;
import br.com.scfmei.repository.ContaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service // "Carimbo" que identifica esta classe como um Serviço gerenciado pelo Spring
public class ContaService {

    @Autowired // Mágica da Injeção de Dependência: Pede ao Spring para nos dar uma instância pronta do ContaRepository
    private ContaRepository contaRepository;

    /**
     * Busca todas as contas existentes no banco de dados.
     * @return Uma lista de todas as contas.
     */
    @Transactional(readOnly = true) // Otimização para operações que apenas leem dados
    public List<Conta> buscarTodas() {
        return contaRepository.findAll();
    }

    /**
     * Salva uma nova conta no banco de dados, aplicando regras de negócio.
     * @param conta O objeto Conta a ser salvo.
     * @return A conta salva com seu ID preenchido.
     */
    @Transactional
    public Conta salvar(Conta conta) {
        // Exemplo de Regra de Negócio: Ao criar uma nova conta, o saldo atual é igual ao inicial.
        if (conta.getId() == null) { // Apenas para contas novas
            conta.setSaldoAtual(conta.getSaldoInicial());
        }
        return contaRepository.save(conta);
    }
}