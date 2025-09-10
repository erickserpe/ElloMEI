package br.com.scfmei.controller;

import br.com.scfmei.domain.Usuario;
import br.com.scfmei.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class RegistroController {

    @Autowired
    private UsuarioService usuarioService;

    // Método para MOSTRAR a página de registro
    @GetMapping("/registro")
    public String mostrarFormularioDeRegistro(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "registro";
    }

    // Método para PROCESSAR o formulário de registro
    @PostMapping("/registro")
    public String registrarUsuario(@Valid @ModelAttribute("usuario") Usuario usuario, BindingResult result) {
        // Validação (podemos adicionar no futuro, por enquanto está simples)
        if (result.hasErrors()) {
            return "registro";
        }

        usuarioService.salvar(usuario);
        return "redirect:/login?sucesso"; // Redireciona para o login com uma msg de sucesso
    }
}