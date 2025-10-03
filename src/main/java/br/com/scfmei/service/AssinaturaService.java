package br.com.scfmei.service;

import br.com.scfmei.domain.*;
import br.com.scfmei.event.PlanUpgradedEvent;
import br.com.scfmei.event.SubscriptionCancelledEvent;
import br.com.scfmei.repository.AssinaturaRepository;
import br.com.scfmei.repository.UsuarioRepository;
import com.mercadopago.resources.payment.Payment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Serviço responsável pela gestão de assinaturas no sistema SaaS.
 * 
 * Responsabilidades:
 * - Criar assinaturas (trial e pagas)
 * - Processar upgrades e downgrades
 * - Cancelar assinaturas
 * - Renovar assinaturas automaticamente
 * - Gerenciar status de assinaturas
 * 
 * @author SCF-MEI Team
 * @since 1.0.0
 */
@Service
public class AssinaturaService {
    
    private static final Logger logger = LoggerFactory.getLogger(AssinaturaService.class);
    
    @Autowired
    private AssinaturaRepository assinaturaRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    
    /**
     * Cria uma assinatura trial para um novo usuário.
     * 
     * @param usuario Usuário que receberá o trial
     * @param diasTrial Número de dias do período de trial
     * @return Assinatura trial criada
     */
    @Transactional
    public Assinatura criarAssinaturaTrial(Usuario usuario, int diasTrial) {
        logger.info("Criando assinatura trial para usuário: {}", usuario.getUsername());
        
        Assinatura assinatura = new Assinatura();
        assinatura.setUsuario(usuario);
        assinatura.setPlano(PlanoAssinatura.PRO); // Trial do plano PRO
        assinatura.setStatus(StatusAssinatura.TRIAL);
        assinatura.setDataInicio(LocalDate.now());
        assinatura.setDataExpiracao(LocalDate.now().plusDays(diasTrial));
        assinatura.setValorMensal(BigDecimal.ZERO);
        assinatura.setTrial(true);
        
        assinatura = assinaturaRepository.save(assinatura);
        
        // Atualizar plano do usuário
        usuario.setPlano(PlanoAssinatura.PRO);
        usuarioRepository.save(usuario);
        
        logger.info("Assinatura trial criada com sucesso. ID: {}", assinatura.getId());
        
        return assinatura;
    }
    
    /**
     * Processa um upgrade de plano após pagamento aprovado.
     * 
     * @param usuario Usuário que está fazendo upgrade
     * @param planoNovo Novo plano contratado
     * @param payment Pagamento aprovado do Mercado Pago
     * @param formaPagamento Forma de pagamento utilizada
     * @return Assinatura criada
     */
    @Transactional
    public Assinatura processarUpgrade(Usuario usuario, PlanoAssinatura planoNovo, 
                                      Payment payment, FormaPagamento formaPagamento) {
        
        logger.info("Processando upgrade para usuário: {} - Plano: {}", 
                   usuario.getUsername(), planoNovo);
        
        PlanoAssinatura planoAnterior = usuario.getPlano();
        
        // Cancelar assinatura anterior (se existir)
        Optional<Assinatura> assinaturaAnterior = 
            assinaturaRepository.findByUsuarioAndStatus(usuario, StatusAssinatura.ATIVA);
        
        assinaturaAnterior.ifPresent(a -> {
            a.cancelar("Upgrade para plano " + planoNovo);
            assinaturaRepository.save(a);
        });
        
        // Criar nova assinatura
        Assinatura novaAssinatura = new Assinatura();
        novaAssinatura.setUsuario(usuario);
        novaAssinatura.setPlano(planoNovo);
        novaAssinatura.setStatus(StatusAssinatura.ATIVA);
        novaAssinatura.setDataInicio(LocalDate.now());
        novaAssinatura.setDataProximaCobranca(LocalDate.now().plusDays(30));

        // Definir valor mensal baseado no plano
        BigDecimal valorMensal = planoNovo == PlanoAssinatura.PRO ?
            new BigDecimal("29.90") : BigDecimal.ZERO;
        novaAssinatura.setValorMensal(valorMensal);

        novaAssinatura.setFormaPagamento(formaPagamento);

        // Salvar ID do pagamento se disponível
        if (payment != null && payment.getId() != null) {
            novaAssinatura.setIdPagamentoExterno(payment.getId().toString());
        }

        novaAssinatura.setTrial(false);
        
        novaAssinatura = assinaturaRepository.save(novaAssinatura);
        
        // Atualizar plano do usuário
        usuario.setPlano(planoNovo);
        usuarioRepository.save(usuario);
        
        // Publicar evento de upgrade
        eventPublisher.publishEvent(
            new PlanUpgradedEvent(this, usuario, planoAnterior, planoNovo, novaAssinatura)
        );
        
        logger.info("Upgrade processado com sucesso. Assinatura ID: {}", novaAssinatura.getId());
        
        return novaAssinatura;
    }
    
    /**
     * Cancela uma assinatura.
     * 
     * @param usuario Usuário que está cancelando
     * @param motivo Motivo do cancelamento
     */
    @Transactional
    public void cancelarAssinatura(Usuario usuario, String motivo) {
        logger.info("Cancelando assinatura do usuário: {}", usuario.getUsername());
        
        Optional<Assinatura> assinaturaOpt = 
            assinaturaRepository.findByUsuarioAndStatus(usuario, StatusAssinatura.ATIVA);
        
        if (assinaturaOpt.isEmpty()) {
            logger.warn("Nenhuma assinatura ativa encontrada para usuário: {}", 
                       usuario.getUsername());
            return;
        }
        
        Assinatura assinatura = assinaturaOpt.get();
        assinatura.cancelar(motivo);
        assinaturaRepository.save(assinatura);
        
        // Voltar para plano FREE
        usuario.setPlano(PlanoAssinatura.FREE);
        usuarioRepository.save(usuario);
        
        // Publicar evento de cancelamento
        eventPublisher.publishEvent(
            new SubscriptionCancelledEvent(this, usuario, assinatura, motivo)
        );
        
        logger.info("Assinatura cancelada com sucesso. ID: {}", assinatura.getId());
    }
    
    /**
     * Busca a assinatura ativa de um usuário.
     */
    public Optional<Assinatura> buscarAssinaturaAtiva(Usuario usuario) {
        return assinaturaRepository.findByUsuarioAndStatus(usuario, StatusAssinatura.ATIVA);
    }
    
    /**
     * Busca o histórico de assinaturas de um usuário.
     */
    public List<Assinatura> buscarHistoricoAssinaturas(Usuario usuario) {
        return assinaturaRepository.findByUsuarioOrderByDataInicioDesc(usuario);
    }
    
    /**
     * Renova assinaturas que vencem hoje (job agendado).
     *
     * Executa todos os dias às 2h da manhã.
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void renovarAssinaturasVencidas() {
        logger.info("Iniciando renovação de assinaturas vencidas...");
        
        List<Assinatura> assinaturas = 
            assinaturaRepository.findAssinaturasParaRenovar(LocalDate.now());
        
        logger.info("Encontradas {} assinaturas para renovar", assinaturas.size());
        
        for (Assinatura assinatura : assinaturas) {
            try {
                // TODO: Processar cobrança via Mercado Pago
                // Se aprovado:
                assinatura.renovar();
                assinaturaRepository.save(assinatura);
                logger.info("Assinatura renovada: {}", assinatura.getId());
                
                // Se recusado:
                // assinatura.suspender();
                // assinaturaRepository.save(assinatura);
                
            } catch (Exception e) {
                logger.error("Erro ao renovar assinatura {}: {}", 
                           assinatura.getId(), e.getMessage());
            }
        }
    }
    
    /**
     * Expira assinaturas suspensas que passaram do prazo (job agendado).
     *
     * Executa todos os dias às 3h da manhã.
     */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void expirarAssinaturasSuspensas() {
        logger.info("Iniciando expiração de assinaturas suspensas...");
        
        List<Assinatura> assinaturas = 
            assinaturaRepository.findAssinaturasSuspensasExpiradas(LocalDate.now());
        
        logger.info("Encontradas {} assinaturas suspensas expiradas", assinaturas.size());
        
        for (Assinatura assinatura : assinaturas) {
            assinatura.setStatus(StatusAssinatura.EXPIRADA);
            assinaturaRepository.save(assinatura);
            
            // Voltar usuário para plano FREE
            Usuario usuario = assinatura.getUsuario();
            usuario.setPlano(PlanoAssinatura.FREE);
            usuarioRepository.save(usuario);
            
            logger.info("Assinatura expirada: {} - Usuário: {}", 
                       assinatura.getId(), usuario.getUsername());
        }
    }
    
    /**
     * Expira trials que venceram (job agendado).
     *
     * Executa todos os dias às 4h da manhã.
     */
    @Scheduled(cron = "0 0 4 * * *")
    @Transactional
    public void expirarTrials() {
        logger.info("Iniciando expiração de trials...");
        
        List<Assinatura> trials = 
            assinaturaRepository.findTrialsExpirados(LocalDate.now());
        
        logger.info("Encontrados {} trials expirados", trials.size());
        
        for (Assinatura trial : trials) {
            trial.setStatus(StatusAssinatura.EXPIRADA);
            assinaturaRepository.save(trial);
            
            // Voltar usuário para plano FREE
            Usuario usuario = trial.getUsuario();
            usuario.setPlano(PlanoAssinatura.FREE);
            usuarioRepository.save(usuario);
            
            logger.info("Trial expirado: {} - Usuário: {}", 
                       trial.getId(), usuario.getUsername());
        }
    }
}

