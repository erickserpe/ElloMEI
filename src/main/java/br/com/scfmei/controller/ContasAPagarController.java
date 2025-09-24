package br.com.scfmei.controller;

import br.com.scfmei.domain.Lancamento;
import br.com.scfmei.domain.Usuario;
import br.com.scfmei.repository.UsuarioRepository;
import br.com.scfmei.service.LancamentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/contas-a-pagar")
public class ContasAPagarController {

    @Autowired
    private LancamentoService lancamentoService;


    @Autowired
    private UsuarioRepository usuarioRepository;

    private Usuario getUsuarioLogado(Principal principal) {
        return usuarioRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalStateException("Usuário logado não encontrado."));
    }


    @GetMapping
    public String listarContasAPagar(Model model, Principal principal) {
        Usuario usuario = getUsuarioLogado(principal);

        List<Lancamento> contasAPagar = lancamentoService.buscarContasAPagarPorUsuario(usuario);
        model.addAttribute("contasAPagar", contasAPagar);
        return "contas-a-pagar";
    }

    @PostMapping("/pagar/{id}")
    public String pagarConta(@PathVariable Long id, Principal principal) {
        Usuario usuario = getUsuarioLogado(principal);

        lancamentoService.pagarConta(id, usuario);
        return "redirect:/contas-a-pagar";
    }
}