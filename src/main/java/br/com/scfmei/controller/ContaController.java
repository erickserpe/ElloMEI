package br.com.scfmei.controller;

import br.com.scfmei.domain.Conta;
import br.com.scfmei.service.ContaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.Optional;

import java.util.List;

@Controller // Anotação para classes que recebem requisições web e retornam uma página HTML
@RequestMapping("/contas") // Mapeia todos os métodos desta classe para URLs que começam com /contas
public class ContaController {

    @Autowired
    private ContaService contaService; // Injeta o serviço que contém a lógica de negócio

    @GetMapping // Mapeia para requisições GET para a URL base (/contas)
    public String listarContas(Model model) {
        // 1. Chama o serviço para buscar os dados no banco
        List<Conta> contas = contaService.buscarTodas();

        // 2. Adiciona a lista de contas ao "Model" para levar ao HTML
        // "listaDeContas" será o nome da nossa lista no Thymeleaf.
        model.addAttribute("listaDeContas", contas);

        // 3. Retorna o nome do arquivo HTML que deve ser renderizado
        return "contas"; // Procura por um arquivo chamado contas.html em src/main/resources/templates
    }

    // MÉTODO PARA MOSTRAR O FORMULÁRIO DE CADASTRO
    @GetMapping("/novo")
    public String mostrarFormularioDeCadastro(Model model) {
        // Criamos um objeto "conta" vazio para conectar com o formulário
        model.addAttribute("conta", new Conta());
        return "form-conta"; // Retorna o nome do arquivo HTML do formulário
    }

    // MÉTODO PARA RECEBER OS DADOS DO FORMULÁRIO E SALVAR
    @PostMapping
    public String salvarConta(@ModelAttribute Conta conta) {
        // O Spring preenche o objeto "conta" com os dados do formulário
        contaService.salvar(conta); // Usa o serviço para salvar a conta no banco
        return "redirect:/contas"; // Redireciona o usuário de volta para a página de listagem
    }

    // MÉTODO PARA MOSTRAR O FORMULÁRIO PREENCHIDO PARA EDIÇÃO
    @GetMapping("/editar/{id}")
    public String mostrarFormularioDeEdicao(@PathVariable Long id, Model model) {
        // 1. Precisamos de um método no serviço para buscar a conta pelo ID
        Optional<Conta> contaOptional = contaService.buscarPorId(id);

        // 2. Verifica se a conta foi encontrada no banco
        if (contaOptional.isPresent()) {
            // 3. Se encontrou, adiciona a conta ao Model para preencher o formulário
            model.addAttribute("conta", contaOptional.get());
            return "form-conta"; // Reutiliza o mesmo template do formulário de cadastro!
        } else {
            // 4. Se não encontrou, redireciona para a lista
            return "redirect:/contas";
        }
    }

    // MÉTODO PARA EXCLUIR UMA CONTA
    @GetMapping("/excluir/{id}")
    public String excluirConta(@PathVariable Long id) {
        contaService.excluirPorId(id); // Chama o serviço para apagar a conta
        return "redirect:/contas";     // Redireciona de volta para a lista atualizada
    }

}