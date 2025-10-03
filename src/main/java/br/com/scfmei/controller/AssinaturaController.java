package br.com.scfmei.controller;

import br.com.scfmei.domain.*;
import br.com.scfmei.repository.UsuarioRepository;
import br.com.scfmei.service.AssinaturaService;
import br.com.scfmei.service.MercadoPagoService;
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

import java.security.Principal;
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
 * @author SCF-MEI Team
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
    private UsuarioRepository usuarioRepository;
    
    /**
     * Obtém o usuário logado.
     */
    private Usuario getUsuarioLogado(Principal principal) {
        return usuarioRepository.findByUsername(principal.getName())
            .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }
    
    /**
     * Página de gerenciamento de assinatura.
     */
    @GetMapping
    public String paginaAssinatura(Model model, Principal principal) {
        Usuario usuario = getUsuarioLogado(principal);
        
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
    public String paginaUpgrade(Model model, Principal principal) {
        Usuario usuario = getUsuarioLogado(principal);
        
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
    public String processarUpgradeCheckout(Principal principal, 
                                          RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = getUsuarioLogado(principal);
            
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
    public String processarUpgradePix(Principal principal) {
        try {
            Usuario usuario = getUsuarioLogado(principal);
            
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
                                  Principal principal,
                                  RedirectAttributes redirectAttributes) {
        
        logger.info("Pagamento aprovado - Payment ID: {} - External Ref: {}", 
                   payment_id, external_reference);
        
        try {
            if (payment_id != null) {
                // Buscar pagamento no Mercado Pago
                Payment payment = mercadoPagoService.buscarPagamento(Long.parseLong(payment_id));
                
                if ("approved".equals(payment.getStatus())) {
                    Usuario usuario = getUsuarioLogado(principal);
                    
                    // Processar upgrade
                    assinaturaService.processarUpgrade(
                        usuario, 
                        PlanoAssinatura.PRO, 
                        payment, 
                        FormaPagamento.CARTAO_CREDITO
                    );
                    
                    redirectAttributes.addFlashAttribute("sucesso", 
                        "Pagamento aprovado! Seu plano foi atualizado para PRO.");
                } else {
                    redirectAttributes.addFlashAttribute("aviso", 
                        "Pagamento pendente. Aguarde a confirmação.");
                }
            }
            
        } catch (Exception e) {
            logger.error("Erro ao processar callback de sucesso: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("erro", 
                "Erro ao processar pagamento. Entre em contato com o suporte.");
        }
        
        return "redirect:/assinatura";
    }
    
    /**
     * Callback de falha do pagamento.
     */
    @GetMapping("/pagamento/falha")
    public String pagamentoFalha(RedirectAttributes redirectAttributes) {
        logger.warn("Pagamento falhou ou foi cancelado pelo usuário");
        
        redirectAttributes.addFlashAttribute("erro", 
            "Pagamento não foi concluído. Tente novamente.");
        
        return "redirect:/assinatura/upgrade";
    }
    
    /**
     * Callback de pagamento pendente.
     */
    @GetMapping("/pagamento/pendente")
    public String pagamentoPendente(RedirectAttributes redirectAttributes) {
        logger.info("Pagamento pendente de confirmação");
        
        redirectAttributes.addFlashAttribute("aviso", 
            "Pagamento pendente. Você receberá um e-mail quando for confirmado.");
        
        return "redirect:/assinatura";
    }
    
    /**
     * Cancelar assinatura.
     */
    @PostMapping("/cancelar")
    public String cancelarAssinatura(@RequestParam String motivo,
                                    Principal principal,
                                    RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = getUsuarioLogado(principal);
            
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
}

