package br.com.scfmei.controller;

import br.com.scfmei.config.security.CurrentUser;
import br.com.scfmei.domain.CategoriaDespesa;
import br.com.scfmei.domain.Usuario;
import br.com.scfmei.service.CategoriaDespesaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;

@Controller
@RequestMapping("/categorias")
public class CategoriaDespesaController {

    @Autowired private CategoriaDespesaService categoriaService;

    @GetMapping
    public String listarCategorias(Model model, @CurrentUser Usuario usuario, @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        Page<CategoriaDespesa> categoriasPage = categoriaService.buscarTodasPorUsuario(usuario, pageable);
        model.addAttribute("categoriasPage", categoriasPage);
        // Mantém compatibilidade com HTML antigo
        model.addAttribute("listaDeCategorias", categoriasPage.getContent());
        return "categorias";
    }

    @GetMapping("/nova")
    public String mostrarFormularioDeNovaCategoria(Model model) {
        model.addAttribute("categoria", new CategoriaDespesa());
        return "form-categoria";
    }

    @PostMapping
    public String salvarCategoria(@Valid @ModelAttribute("categoria") CategoriaDespesa categoria, BindingResult result, @CurrentUser Usuario usuario) {
        if (result.hasErrors()) {
            return "form-categoria";
        }
        categoriaService.salvar(categoria, usuario);
        return "redirect:/categorias";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioDeEdicao(@PathVariable Long id, Model model) {
        CategoriaDespesa categoria = categoriaService.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));
        model.addAttribute("categoria", categoria);
        return "form-categoria";
    }

    @GetMapping("/excluir/{id}")
    public String excluirCategoria(@PathVariable Long id) {
        categoriaService.excluirPorId(id);
        return "redirect:/categorias";
    }
}