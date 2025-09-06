package br.com.scfmei.controller;

import br.com.scfmei.domain.Conta;
import br.com.scfmei.service.ContaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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
}