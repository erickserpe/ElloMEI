package br.com.ellomei.controller;

import br.com.ellomei.config.security.CurrentUser;
import br.com.ellomei.domain.*;
import br.com.ellomei.repository.HistoricoPagamentoRepository;
import br.com.ellomei.service.AssinaturaService;
import br.com.ellomei.service.MercadoPagoService;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.payment.Payment;
import com.mercadopago.resources.preference.Preference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

/**
 * Controller responsável pela gestão de assinaturas e pagamentos.
 * 
 * Endpoints:
 * - GET /assinatura - Página de gerenciamento de assinatura
 * - GET /assinatura/upgrade - Página de upgrade de plano
 * - POST /assinatura/upgrade - Processar upgrade
 * - POST /assinatura/cancelar - Cancelar assinatura
 * - GET /assinatura/pagamento/sucesso - Callback de sucesso
 * - GET /assinatura/pagamento/falha - Callback de falha
 * 
 * @author ElloMEI Team
 * @since 1.0.0
 */
@Controller
@RequestMapping("/assinatura")
public class AssinaturaController {
    
    private static final Logger logger = LoggerFactory.getLogger(AssinaturaController.class);
    
    @Autowired
    private AssinaturaService assinaturaService;

    @Autowired
    private MercadoPagoService mercadoPagoService;

    @Autowired
    private HistoricoPagamentoRepository historicoPagamentoRepository;

    /**
     * Página de gerenciamento de assinatura.
     */
    @GetMapping
    public String paginaAssinatura(Model model, @CurrentUser Usuario usuario) {
        
        Optional<Assinatura> assinaturaAtiva = 
            assinaturaService.buscarAssinaturaAtiva(usuario);
        
        List<Assinatura> historico = 
            assinaturaService.buscarHistoricoAssinaturas(usuario);
        
        model.addAttribute("usuario", usuario);
        model.addAttribute("assinaturaAtiva", assinaturaAtiva.orElse(null));
        model.addAttribute("historico", historico);
        model.addAttribute("planoAtual", usuario.getPlano());
        
        return "assinatura/gerenciar";
    }
    
    /**
     * Página de upgrade de plano.
     */
    @GetMapping("/upgrade")
    public String paginaUpgrade(Model model, @CurrentUser Usuario usuario) {
        
        // Se já é PRO, redirecionar
        if (usuario.getPlano() == PlanoAssinatura.PRO) {
            return "redirect:/assinatura?erro=ja-pro";
        }
        
        model.addAttribute("usuario", usuario);
        model.addAttribute("planoAtual", usuario.getPlano());
        model.addAttribute("precoPro", "29.90");
        model.addAttribute("mercadoPagoPublicKey", mercadoPagoService.getPublicKey());
        
        return "assinatura/upgrade";
    }
    
    /**
     * Processar upgrade via checkout do Mercado Pago.
     */
    @PostMapping("/upgrade/checkout")
    public String processarUpgradeCheckout(@CurrentUser Usuario usuario,
                                          RedirectAttributes redirectAttributes) {
        try {
            
            // Criar preferência de pagamento
            Preference preference = mercadoPagoService.criarPreferenciaPagamento(
                usuario, PlanoAssinatura.PRO
            );
            
            logger.info("Preferência criada: {} - Redirecionando para checkout", 
                       preference.getId());
            
            // Redirecionar para checkout do Mercado Pago
            return "redirect:" + preference.getInitPoint();
            
        } catch (MPException | MPApiException e) {
            logger.error("Erro ao criar preferência de pagamento: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("erro", 
                "Erro ao processar pagamento. Tente novamente.");
            return "redirect:/assinatura/upgrade";
        }
    }
    
    /**
     * Processar upgrade via PIX.
     */
    @PostMapping("/upgrade/pix")
    @ResponseBody
    public String processarUpgradePix(@CurrentUser Usuario usuario) {
        try {
            
            // Criar pagamento PIX
            Payment payment = mercadoPagoService.criarPagamentoPix(
                usuario, PlanoAssinatura.PRO
            );
            
            logger.info("Pagamento PIX criado: {} - Status: {}", 
                       payment.getId(), payment.getStatus());
            
            // Retornar QR Code e dados do PIX
            return payment.getPointOfInteraction()
                .getTransactionData()
                .getQrCode();
            
        } catch (MPException | MPApiException e) {
            logger.error("Erro ao criar pagamento PIX: {}", e.getMessage());
            return "ERRO";
        }
    }
    
    /**
     * Callback de sucesso do pagamento.
     */
    @GetMapping("/pagamento/sucesso")
    public String pagamentoSucesso(@RequestParam(required = false) String payment_id,
                                  @RequestParam(required = false) String external_reference,
                                  @CurrentUser Usuario usuario,
                                  Model model) {

        logger.info("Pagamento aprovado - Payment ID: {} - External Ref: {}",
                   payment_id, external_reference);

        try {
            if (payment_id != null) {
                // Buscar pagamento no Mercado Pago
                Payment payment = mercadoPagoService.buscarPagamento(Long.parseLong(payment_id));

                if ("approved".equals(payment.getStatus())) {

                    // Processar upgrade
                    assinaturaService.processarUpgrade(
                        usuario,
                        PlanoAssinatura.PRO,
                        payment,
                        FormaPagamento.CARTAO_CREDITO
                    );

                    // Página de sucesso
                    return "assinatura/pagamento-sucesso";
                } else {
                    // Página de pendente
                    return "assinatura/pagamento-pendente";
                }
            }

        } catch (Exception e) {
            logger.error("Erro ao processar callback de sucesso: {}", e.getMessage());
            model.addAttribute("erro", "Erro ao processar pagamento. Entre em contato com o suporte.");
            return "assinatura/pagamento-falha";
        }

        return "assinatura/pagamento-pendente";
    }
    
    /**
     * Callback de falha do pagamento.
     */
    @GetMapping("/pagamento/falha")
    public String pagamentoFalha() {
        logger.warn("Pagamento falhou ou foi cancelado pelo usuário");
        return "assinatura/pagamento-falha";
    }

    /**
     * Callback de pagamento pendente.
     */
    @GetMapping("/pagamento/pendente")
    public String pagamentoPendente() {
        logger.info("Pagamento pendente de confirmação");
        return "assinatura/pagamento-pendente";
    }
    
    /**
     * Cancelar assinatura.
     */
    @PostMapping("/cancelar")
    public String cancelarAssinatura(@RequestParam String motivo,
                                    @CurrentUser Usuario usuario,
                                    RedirectAttributes redirectAttributes) {
        try {
            
            assinaturaService.cancelarAssinatura(usuario, motivo);
            
            redirectAttributes.addFlashAttribute("sucesso", 
                "Assinatura cancelada com sucesso. Você voltou para o plano FREE.");
            
        } catch (Exception e) {
            logger.error("Erro ao cancelar assinatura: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("erro", 
                "Erro ao cancelar assinatura. Tente novamente.");
        }
        
        return "redirect:/assinatura";
    }

    /**
     * Exibe o histórico de pagamentos do usuário.
     */
    @GetMapping("/historico")
    public String historicoPagamentos(@CurrentUser Usuario usuario, Model model) {

        List<HistoricoPagamento> historico =
            historicoPagamentoRepository.findByUsuarioOrderByDataPagamentoDesc(usuario);

        model.addAttribute("historico", historico);
        model.addAttribute("usuario", usuario);

        return "assinatura/historico";
    }
}

