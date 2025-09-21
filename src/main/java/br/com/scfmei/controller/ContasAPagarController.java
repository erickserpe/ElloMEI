
package br.com.scfmei.controller;

import br.com.scfmei.domain.Lancamento;
import br.com.scfmei.service.LancamentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/contas-a-pagar")
public class ContasAPagarController {

    @Autowired
    private LancamentoService lancamentoService;

    @GetMapping
    public String listarContasAPagar(Model model) {
        List<Lancamento> contasAPagar = lancamentoService.buscarContasAPagar();
        model.addAttribute("contasAPagar", contasAPagar);
        return "contas-a-pagar";
    }

    @PostMapping("/pagar/{id}")
    public String pagarConta(@PathVariable Long id) {
        lancamentoService.pagarConta(id);
        return "redirect:/contas-a-pagar";
    }
}