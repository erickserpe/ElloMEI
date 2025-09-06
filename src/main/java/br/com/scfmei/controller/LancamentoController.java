package br.com.scfmei.controller;

import br.com.scfmei.domain.Lancamento;
import br.com.scfmei.service.LancamentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/lancamentos")
public class LancamentoController {

    @Autowired
    private LancamentoService lancamentoService;

    @GetMapping
    public String listarLancamentos(Model model) {
        List<Lancamento> lancamentos = lancamentoService.buscarTodos();
        model.addAttribute("listaDeLancamentos", lancamentos);
        return "lancamentos"; // Procurará por lancamentos.html
    }
}