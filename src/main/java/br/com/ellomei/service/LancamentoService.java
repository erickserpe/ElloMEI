package br.com.ellomei.service;

import br.com.ellomei.domain.*;
import br.com.ellomei.repository.ComprovanteRepository;
import br.com.ellomei.repository.LancamentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class LancamentoService {

    @Autowired private LancamentoRepository lancamentoRepository;
    @Autowired private ComprovanteRepository comprovanteRepository;
    @Autowired private ContaService contaService;
    @Autowired private FileStorageService fileStorageService;

    @Transactional(readOnly = true)
    @PreAuthorize("@customSecurityService.isLancamentoOwner(#id)")
    public Optional<Lancamento> buscarPorId(Long id) { return lancamentoRepository.findById(id); }

    @Transactional
    @PreAuthorize("@customSecurityService.isComprovanteOwner(#comprovanteId)")
    public void excluirComprovante(Long comprovanteId, Usuario usuario) {
        Comprovante comprovante = comprovanteRepository.findById(comprovanteId)
                .orElseThrow(() -> new RuntimeException("Comprovante não encontrado!"));

        // The @OneToMany(orphanRemoval=true) on Lancamento entity will handle the deletion
        comprovante.getLancamento().getComprovantes().remove(comprovante);
        comprovanteRepository.delete(comprovante);
    }

    /**
     * Orquestra a criação ou atualização de uma operação financeira (grupo de lançamentos).
     *
     * Este método coordena todo o fluxo:
     * 1. Gera ou reutiliza o grupoOperacao
     * 2. Se for edição, reverte e exclui lançamentos antigos
     * 3. Cria os novos lançamentos
     * 4. Processa os comprovantes anexados
     * 5. Aplica os efeitos financeiros (atualiza saldos)
     *
     * @param form Dados do formulário com informações da operação
     * @param comprovanteFiles Arquivos de comprovante enviados
     * @param usuario Usuário proprietário da operação
     */
    @Transactional
    public void salvarOuAtualizarOperacao(LancamentoFormDTO form, MultipartFile[] comprovanteFiles, Usuario usuario) {
        final String grupoOperacao = (form.getGrupoOperacao() != null && !form.getGrupoOperacao().isBlank())
                ? form.getGrupoOperacao()
                : UUID.randomUUID().toString();
        form.setGrupoOperacao(grupoOperacao);

        // Se for uma edição, lida com os lançamentos antigos primeiro
        if (isUpdateOperation(form)) {
            reverterEExcluirLancamentosAntigos(form.getGrupoOperacao(), usuario);
        }

        // Processa os novos lançamentos
        List<Lancamento> novosLancamentos = criarNovosLancamentos(form, usuario);

        // Processa os comprovantes
        processarComprovantes(novosLancamentos, comprovanteFiles);

        // Aplica os efeitos financeiros
        aplicarEfeitosFinanceiros(novosLancamentos);
    }

    /**
     * Verifica se a operação é uma atualização (edição) ou uma nova criação.
     *
     * Uma operação é considerada edição quando já possui um grupoOperacao definido,
     * indicando que é uma operação existente sendo modificada.
     *
     * @param form Dados do formulário
     * @return true se for uma edição, false se for uma nova operação
     */
    private boolean isUpdateOperation(LancamentoFormDTO form) {
        return form.getGrupoOperacao() != null && !form.getGrupoOperacao().isBlank();
    }

    /**
     * Reverte os efeitos financeiros e exclui os lançamentos antigos de um grupo.
     *
     * Este método é chamado durante a edição de uma operação para limpar
     * os lançamentos anteriores antes de criar os novos.
     *
     * @param grupoOperacao Identificador do grupo de lançamentos
     * @param usuario Usuário proprietário dos lançamentos
     */
    private void reverterEExcluirLancamentosAntigos(String grupoOperacao, Usuario usuario) {
        List<Lancamento> lancamentosAntigos = lancamentoRepository.findByGrupoOperacaoAndUsuario(grupoOperacao, usuario);
        lancamentosAntigos.forEach(this::reverterLancamentoNaContaSePago);
        lancamentoRepository.deleteAll(lancamentosAntigos);
    }

    /**
     * Reverte o lançamento na conta se ele estiver com status PAGO.
     *
     * @param lancamento Lançamento a ser revertido
     */
    private void reverterLancamentoNaContaSePago(Lancamento lancamento) {
        if (lancamento.getStatus() == StatusLancamento.PAGO) {
            reverterLancamentoNaConta(lancamento);
        }
    }

    /**
     * Cria e salva os novos lançamentos a partir dos dados do formulário.
     *
     * @param form Dados do formulário com informações da operação
     * @param usuario Usuário proprietário dos lançamentos
     * @return Lista de lançamentos salvos
     */
    private List<Lancamento> criarNovosLancamentos(LancamentoFormDTO form, Usuario usuario) {
        List<Lancamento> lancamentosSalvos = new ArrayList<>();
        for (PagamentoDTO pagamento : form.getPagamentos()) {
            if (pagamento.getValor() == null || pagamento.getConta() == null) continue;

            Lancamento lancamento = new Lancamento();
            mapearFormParaLancamento(form, lancamento, usuario, pagamento);

            // Associa comprovantes existentes (caso de edição)
            if (form.getGrupoOperacao() != null && form.getComprovantes() != null) {
                form.getComprovantes().forEach(comp -> {
                    comp.setLancamento(lancamento);
                    lancamento.getComprovantes().add(comp);
                });
            }

            lancamentosSalvos.add(lancamentoRepository.save(lancamento));
        }
        return lancamentosSalvos;
    }

    /**
     * Mapeia os dados do formulário para a entidade Lancamento.
     *
     * @param form Dados do formulário
     * @param lancamento Entidade a ser preenchida
     * @param usuario Usuário proprietário
     * @param pagamento Dados específicos do pagamento (conta e valor)
     */
    private void mapearFormParaLancamento(LancamentoFormDTO form, Lancamento lancamento, Usuario usuario, PagamentoDTO pagamento) {
        lancamento.setDescricao(form.getDescricao());
        lancamento.setData(form.getData());
        lancamento.setTipo(form.getTipo());
        lancamento.setCategoriaDespesa(form.getCategoriaDespesa());
        lancamento.setContato(form.getContato());
        lancamento.setComNotaFiscal(form.getComNotaFiscal());
        lancamento.setGrupoOperacao(form.getGrupoOperacao());
        lancamento.setStatus(form.getStatus());
        lancamento.setUsuario(usuario);

        Conta conta = contaService.buscarPorId(pagamento.getConta())
                .orElseThrow(() -> new RuntimeException("Conta não encontrada!"));
        lancamento.setConta(conta);
        lancamento.setValor(pagamento.getValor());
    }

    /**
     * Processa e salva os arquivos de comprovante para os lançamentos.
     *
     * Os arquivos são armazenados e associados a todos os lançamentos do grupo.
     * PDFs com múltiplas páginas são automaticamente divididos.
     *
     * @param lancamentos Lista de lançamentos que receberão os comprovantes
     * @param comprovanteFiles Arquivos enviados
     */
    private void processarComprovantes(List<Lancamento> lancamentos, MultipartFile[] comprovanteFiles) {
        if (comprovanteFiles == null || comprovanteFiles.length == 0 ||
            (comprovanteFiles.length == 1 && comprovanteFiles[0].isEmpty())) {
            return;
        }

        List<String> todosOsPaths = new ArrayList<>();
        for (MultipartFile file : comprovanteFiles) {
            if (file != null && !file.isEmpty()) {
                // O serviço de armazenamento já lida com divisão de páginas de PDF
                todosOsPaths.addAll(fileStorageService.storeFile(file));
            }
        }

        if (!todosOsPaths.isEmpty()) {
            for (Lancamento lancamento : lancamentos) {
                for (String path : todosOsPaths) {
                    Comprovante comprovante = new Comprovante();
                    comprovante.setPathArquivo(path);
                    comprovante.setLancamento(lancamento);
                    comprovanteRepository.save(comprovante);
                }
            }
        }
    }

    /**
     * Aplica os efeitos financeiros dos lançamentos nas contas.
     *
     * Apenas lançamentos com status PAGO afetam o saldo das contas.
     *
     * @param lancamentos Lista de lançamentos a serem aplicados
     */
    private void aplicarEfeitosFinanceiros(List<Lancamento> lancamentos) {
        lancamentos.stream()
                .filter(l -> l.getStatus() == StatusLancamento.PAGO)
                .forEach(this::aplicarLancamentoNaConta);
    }



    @Transactional(readOnly = true)
    public LancamentoFormDTO carregarOperacaoParaEdicao(Long lancamentoId) {
        Lancamento umLancamentoDoGrupo = buscarPorId(lancamentoId).orElseThrow(() -> new RuntimeException("Lançamento não encontrado!"));
        String grupoOperacao = umLancamentoDoGrupo.getGrupoOperacao();
        if (grupoOperacao == null) { grupoOperacao = umLancamentoDoGrupo.getId().toString(); }
        List<Lancamento> lancamentosDoGrupo = lancamentoRepository.findByGrupoOperacaoAndUsuario(grupoOperacao, umLancamentoDoGrupo.getUsuario());
        if(lancamentosDoGrupo.isEmpty()){ lancamentosDoGrupo.add(umLancamentoDoGrupo); }

        LancamentoFormDTO form = new LancamentoFormDTO();
        form.setGrupoOperacao(umLancamentoDoGrupo.getGrupoOperacao());
        form.setDescricao(umLancamentoDoGrupo.getDescricao());
        form.setData(umLancamentoDoGrupo.getData());
        form.setTipo(umLancamentoDoGrupo.getTipo());
        form.setCategoriaDespesa(umLancamentoDoGrupo.getCategoriaDespesa());
        form.setContato(umLancamentoDoGrupo.getContato());
        form.setComNotaFiscal(umLancamentoDoGrupo.getComNotaFiscal());
        form.setComprovantes(umLancamentoDoGrupo.getComprovantes());
        form.setStatus(umLancamentoDoGrupo.getStatus());

        List<PagamentoDTO> pagamentos = lancamentosDoGrupo.stream().map(l -> {
            PagamentoDTO dto = new PagamentoDTO();
            dto.setConta(l.getConta().getId());
            dto.setValor(l.getValor());
            return dto;
        }).collect(Collectors.toList());
        form.setPagamentos(pagamentos);
        return form;
    }

    @PreAuthorize("@customSecurityService.isLancamentoOwner(#lancamentoId)")
    public void excluirOperacao(Long lancamentoId, Usuario usuario) {
        Lancamento umLancamentoDoGrupo = buscarPorId(lancamentoId).orElseThrow(() -> new RuntimeException("Lançamento não encontrado!"));

        String grupoOperacao = umLancamentoDoGrupo.getGrupoOperacao();
        if (grupoOperacao == null || grupoOperacao.isBlank()) {
            if (umLancamentoDoGrupo.getStatus() == StatusLancamento.PAGO) {
                reverterLancamentoNaConta(umLancamentoDoGrupo);
            }
            lancamentoRepository.delete(umLancamentoDoGrupo);
        } else {
            excluirOperacaoPorGrupo(grupoOperacao, usuario);
        }
    }

    private void excluirOperacaoPorGrupo(String grupoOperacao, Usuario usuario) {
        if (grupoOperacao == null || grupoOperacao.isBlank()) return;
        List<Lancamento> lancamentosDoGrupo = lancamentoRepository.findByGrupoOperacaoAndUsuario(grupoOperacao, usuario);
        for (Lancamento lancamento : lancamentosDoGrupo) {
            if (lancamento.getStatus() == StatusLancamento.PAGO) {
                reverterLancamentoNaConta(lancamento);
            }
            lancamentoRepository.delete(lancamento);
        }
    }

    @Transactional(readOnly = true)
    public List<Lancamento> buscarComFiltros(LocalDate dataInicio, LocalDate dataFim, Long contaId, Long contatoId, TipoLancamento tipo, Long categoriaId, Boolean comNotaFiscal, String descricao, StatusLancamento status, Usuario usuario) {
        return lancamentoRepository.findComFiltros(dataInicio, dataFim, contaId, contatoId, tipo, categoriaId, comNotaFiscal, descricao, status, usuario);
    }

    /**
     * Busca despesas agrupadas por categoria com filtros aplicados.
     *
     * Retorna dados agregados para exibição em gráficos de pizza/barras,
     * mostrando o total gasto em cada categoria de despesa.
     *
     * @param dataInicio Data inicial do período
     * @param dataFim Data final do período
     * @param contaId ID da conta (opcional)
     * @param contatoId ID do contato (opcional)
     * @param categoriaId ID da categoria (opcional)
     * @param status Status do lançamento (opcional)
     * @param usuario Usuário proprietário dos dados
     * @return Lista de dados agregados por categoria
     */
    @Transactional(readOnly = true)
    public List<ChartData> buscarDespesasPorCategoria(LocalDate dataInicio, LocalDate dataFim, Long contaId, Long contatoId, Long categoriaId, StatusLancamento status, Usuario usuario) {
        return lancamentoRepository.findDespesasPorCategoriaComFiltros(dataInicio, dataFim, contaId, contatoId, categoriaId, status, usuario);
    }

    /**
     * Calcula a soma das entradas bancárias (excluindo caixa) em um período.
     *
     * Usado para calcular o faturamento bancário oficial, que exclui
     * movimentações em dinheiro (caixa).
     *
     * @param dataInicio Data inicial do período
     * @param dataFim Data final do período
     * @param usuario Usuário proprietário dos dados
     * @return Soma das entradas bancárias ou BigDecimal.ZERO se não houver
     */
    @Transactional(readOnly = true)
    public BigDecimal calcularEntradasBancarias(LocalDate dataInicio, LocalDate dataFim, Usuario usuario) {
        BigDecimal total = lancamentoRepository.sumEntradasBancariasNoPeriodo(dataInicio, dataFim, usuario);
        return total != null ? total : BigDecimal.ZERO;
    }

    // Método legado (sem paginação) - mantido para compatibilidade
    @Transactional(readOnly = true)
    public List<LancamentoGrupoDTO> buscarComFiltrosAgrupados(LocalDate dataInicio, LocalDate dataFim, Long contaId, Long contatoId, TipoLancamento tipo, Long categoriaId, Boolean comNotaFiscal, String descricao, StatusLancamento status, Usuario usuario) {
        List<Lancamento> lancamentos = lancamentoRepository.findComFiltros(dataInicio, dataFim, contaId, contatoId, tipo, categoriaId, comNotaFiscal, descricao, status, usuario);

        // Agrupa os lançamentos por grupoOperacao (ou por ID se não tiver grupo)
        Map<String, List<Lancamento>> grupos = lancamentos.stream()
                .collect(Collectors.groupingBy(l ->
                    l.getGrupoOperacao() != null ? l.getGrupoOperacao() : l.getId().toString()
                ));

        // Converte cada grupo em um LancamentoGrupoDTO
        return grupos.values().stream()
                .map(this::criarLancamentoGrupoDTO)
                .sorted((g1, g2) -> {
                    // Ordena por data decrescente, depois por ID decrescente
                    int dateCompare = g2.getData().compareTo(g1.getData());
                    return dateCompare != 0 ? dateCompare : g2.getId().compareTo(g1.getId());
                })
                .collect(Collectors.toList());
    }

    // Novo método com paginação
    @Transactional(readOnly = true)
    public Page<LancamentoGrupoDTO> buscarComFiltrosAgrupados(LocalDate dataInicio, LocalDate dataFim, Long contaId, Long contatoId, TipoLancamento tipo, Long categoriaId, Boolean comNotaFiscal, String descricao, StatusLancamento status, Usuario usuario, Pageable pageable) {
        // Busca os lançamentos paginados
        Page<Lancamento> lancamentosPage = lancamentoRepository.findComFiltros(dataInicio, dataFim, contaId, contatoId, tipo, categoriaId, comNotaFiscal, descricao, status, usuario, pageable);

        // Agrupa os lançamentos da PÁGINA ATUAL por grupoOperacao (ou por ID se não tiver grupo)
        Map<String, List<Lancamento>> grupos = lancamentosPage.getContent().stream()
                .collect(Collectors.groupingBy(l ->
                    l.getGrupoOperacao() != null ? l.getGrupoOperacao() : l.getId().toString()
                ));

        // Converte cada grupo em um LancamentoGrupoDTO
        List<LancamentoGrupoDTO> dtos = grupos.values().stream()
                .map(this::criarLancamentoGrupoDTO)
                .sorted((g1, g2) -> {
                    // Ordena por data decrescente, depois por ID decrescente
                    int dateCompare = g2.getData().compareTo(g1.getData());
                    return dateCompare != 0 ? dateCompare : g2.getId().compareTo(g1.getId());
                })
                .collect(Collectors.toList());

        // Retorna uma Page com os DTOs agrupados
        return new PageImpl<>(dtos, pageable, lancamentosPage.getTotalElements());
    }

    private LancamentoGrupoDTO criarLancamentoGrupoDTO(List<Lancamento> lancamentosDoGrupo) {
        if (lancamentosDoGrupo.isEmpty()) return null;

        Lancamento primeiro = lancamentosDoGrupo.get(0);

        // Calcula o valor total do grupo
        BigDecimal valorTotal = lancamentosDoGrupo.stream()
                .map(Lancamento::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Cria descrição das contas envolvidas
        String contasDescricao = lancamentosDoGrupo.stream()
                .map(l -> l.getConta().getNomeConta())
                .distinct()
                .collect(Collectors.joining(", "));

        // Se há apenas uma conta, mostra só o nome. Se há múltiplas, mostra "Múltiplas contas"
        if (lancamentosDoGrupo.size() > 1 && lancamentosDoGrupo.stream().map(l -> l.getConta().getId()).distinct().count() > 1) {
            contasDescricao = "Múltiplas contas (" + contasDescricao + ")";
        }

        return new LancamentoGrupoDTO(
                primeiro.getId(),
                primeiro.getGrupoOperacao(),
                primeiro.getDescricao(),
                primeiro.getData(),
                primeiro.getTipo(),
                primeiro.getStatus(),
                valorTotal,
                contasDescricao,
                primeiro.getCategoriaDespesa(),
                primeiro.getContato(),
                primeiro.getComNotaFiscal(),
                primeiro.getComprovantes()
        );
    }

    @Transactional(readOnly = true)
    public List<Lancamento> buscarContasAPagarPorUsuario(Usuario usuario) {
        return lancamentoRepository.findByStatusAndUsuarioOrderByDataAsc(StatusLancamento.A_PAGAR, usuario);
    }

    @PreAuthorize("@customSecurityService.isLancamentoOwner(#lancamentoId)")
    public void pagarConta(Long lancamentoId, Usuario usuario) {
        Lancamento lancamento = buscarPorId(lancamentoId).orElseThrow(() -> new RuntimeException("Lançamento não encontrado!"));

        if (lancamento.getStatus() == StatusLancamento.A_PAGAR) {
            lancamento.setStatus(StatusLancamento.PAGO);
            aplicarLancamentoNaConta(lancamento);
            lancamentoRepository.save(lancamento);
        }
    }

    private void aplicarLancamentoNaConta(Lancamento lancamento) {
        Conta conta = lancamento.getConta();
        if (lancamento.getTipo() == TipoLancamento.ENTRADA) {
            conta.setSaldoAtual(conta.getSaldoAtual().add(lancamento.getValor()));
        } else {
            conta.setSaldoAtual(conta.getSaldoAtual().subtract(lancamento.getValor()));
        }
        // CORREÇÃO AQUI
        contaService.salvar(conta, lancamento.getUsuario());
    }

    private void reverterLancamentoNaConta(Lancamento lancamento) {
        Conta conta = lancamento.getConta();
        if (lancamento.getTipo() == TipoLancamento.ENTRADA) {
            conta.setSaldoAtual(conta.getSaldoAtual().subtract(lancamento.getValor()));
        } else {
            conta.setSaldoAtual(conta.getSaldoAtual().add(lancamento.getValor()));
        }
        // CORREÇÃO AQUI
        contaService.salvar(conta, lancamento.getUsuario());
    }
}