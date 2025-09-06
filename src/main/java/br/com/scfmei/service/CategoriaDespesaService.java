package br.com.scfmei.service;

import br.com.scfmei.domain.CategoriaDespesa;
import br.com.scfmei.repository.CategoriaDespesaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoriaDespesaService {

    @Autowired
    private CategoriaDespesaRepository categoriaDespesaRepository;

    @Transactional(readOnly = true)
    public List<CategoriaDespesa> buscarTodas() {
        return categoriaDespesaRepository.findAll();
    }

    @Transactional
    public CategoriaDespesa salvar(CategoriaDespesa categoriaDespesa) {
        return categoriaDespesaRepository.save(categoriaDespesa);
    }
}