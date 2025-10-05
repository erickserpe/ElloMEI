package br.com.ellomei.controller;

import br.com.ellomei.config.security.CurrentUser;
import br.com.ellomei.domain.Lancamento;
import br.com.ellomei.domain.Usuario;
import br.com.ellomei.service.LancamentoService;
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
    public String listarContasAPagar(Model model, @CurrentUser Usuario usuario) {
        List<Lancamento> contasAPagar = lancamentoService.buscarContasAPagarPorUsuario(usuario);
        model.addAttribute("contasAPagar", contasAPagar);
        return "contas-a-pagar";
    }

    @PostMapping("/pagar/{id}")
    public String pagarConta(@PathVariable Long id, @CurrentUser Usuario usuario) {
        lancamentoService.pagarConta(id, usuario);
        return "redirect:/contas-a-pagar";
    }
}