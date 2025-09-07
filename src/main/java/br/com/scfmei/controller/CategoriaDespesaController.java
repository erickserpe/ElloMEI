package br.com.scfmei.controller;

import br.com.scfmei.domain.CategoriaDespesa;
import br.com.scfmei.service.CategoriaDespesaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/categorias")
public class CategoriaDespesaController {

    @Autowired
    private CategoriaDespesaService categoriaService;

    // --- READ (Listar) ---
    @GetMapping
    public String listarCategorias(Model model) {
        List<CategoriaDespesa> categorias = categoriaService.buscarTodas();
        model.addAttribute("listaDeCategorias", categorias);
        return "categorias";
    }

    // --- CREATE (Mostrar formulário) ---
    @GetMapping("/nova")
    public String mostrarFormularioDeNovaCategoria(Model model) {
        model.addAttribute("categoria", new CategoriaDespesa());
        return "form-categoria";
    }

    // MÉTODO SALVAR ATUALIZADO COM VALIDAÇÃO
    @PostMapping
    public String salvarCategoria(@Valid @ModelAttribute("categoria") CategoriaDespesa categoria, BindingResult result, Model model) {
        if (result.hasErrors()) {
            // Se houver erros, retorna para a mesma página do formulário para mostrá-los
            return "form-categoria";
        }

        categoriaService.salvar(categoria);
        return "redirect:/categorias";
    }

    // --- UPDATE (Mostrar formulário de edição) ---
    @GetMapping("/editar/{id}")
    public String mostrarFormularioDeEdicao(@PathVariable Long id, Model model) {
        Optional<CategoriaDespesa> categoriaOpt = categoriaService.buscarPorId(id);
        if (categoriaOpt.isPresent()) {
            model.addAttribute("categoria", categoriaOpt.get());
            return "form-categoria";
        }
        return "redirect:/categorias";
    }

    // --- DELETE (Excluir) ---
    @GetMapping("/excluir/{id}")
    public String excluirCategoria(@PathVariable Long id) {
        categoriaService.excluirPorId(id);
        return "redirect:/categorias";
    }
}