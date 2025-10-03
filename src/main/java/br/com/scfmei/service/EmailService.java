package br.com.scfmei.service;

import br.com.scfmei.domain.Assinatura;
import br.com.scfmei.domain.Usuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

/**
 * Serviço responsável pelo envio de e-mails.
 * 
 * NOTA: Esta é uma implementação de MOCK para desenvolvimento.
 * Em produção, integre com um serviço real de e-mail como:
 * - AWS SES (Simple Email Service)
 * - SendGrid
 * - Mailgun
 * - SMTP tradicional
 * 
 * Responsabilidades:
 * - Enviar e-mail de boas-vindas
 * - Enviar e-mail de upgrade de plano
 * - Enviar e-mail de cancelamento
 * - Enviar e-mail de falha de pagamento
 * - Enviar e-mail de proximidade do limite
 * 
 * @author SCF-MEI Team
 * @since 1.0.0
 */
@Service
public class EmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    
    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    /**
     * Envia e-mail de boas-vindas para novo usuário.
     */
    public void enviarEmailBoasVindas(Usuario usuario) {
        logger.info("========================================");
        logger.info("📧 E-MAIL DE BOAS-VINDAS");
        logger.info("========================================");
        logger.info("Para: {}", usuario.getUsername() + "@scfmei.com.br");
        logger.info("Assunto: Bem-vindo ao SCF-MEI!");
        logger.info("");
        logger.info("Olá {}!", usuario.getUsername());
        logger.info("");
        logger.info("Seja bem-vindo ao SCF-MEI - Sistema de Controle Financeiro para MEI!");
        logger.info("");
        logger.info("Você está no plano FREE com:");
        logger.info("- Até 20 lançamentos por mês");
        logger.info("- Gestão completa de contas e contatos");
        logger.info("- Relatórios básicos");
        logger.info("");
        logger.info("Quer mais? Faça upgrade para o plano PRO:");
        logger.info("{}/assinatura/upgrade", baseUrl);
        logger.info("");
        logger.info("Comece agora: {}/dashboard", baseUrl);
        logger.info("");
        logger.info("Equipe SCF-MEI");
        logger.info("========================================");
        
        // TODO: Implementar envio real de e-mail
    }
    
    /**
     * Envia e-mail de confirmação de upgrade para PRO.
     */
    public void enviarEmailUpgrade(Usuario usuario, Assinatura assinatura) {
        logger.info("========================================");
        logger.info("📧 E-MAIL DE UPGRADE CONFIRMADO");
        logger.info("========================================");
        logger.info("Para: {}", usuario.getUsername() + "@scfmei.com.br");
        logger.info("Assunto: Upgrade para Plano PRO Confirmado! 🎉");
        logger.info("");
        logger.info("Parabéns {}!", usuario.getUsername());
        logger.info("");
        logger.info("Seu upgrade para o plano PRO foi confirmado!");
        logger.info("");
        logger.info("Agora você tem acesso a:");
        logger.info("✅ Lançamentos ILIMITADOS");
        logger.info("✅ Relatórios avançados em PDF");
        logger.info("✅ Dashboard completo");
        logger.info("✅ Suporte prioritário");
        logger.info("");
        logger.info("Detalhes da assinatura:");
        logger.info("- Valor: R$ {}", assinatura.getValorMensal());
        logger.info("- Próxima cobrança: {}", assinatura.getDataProximaCobranca().format(DATE_FORMATTER));
        logger.info("- Forma de pagamento: {}", assinatura.getFormaPagamento().getDescricao());
        logger.info("");
        logger.info("Acesse agora: {}/dashboard", baseUrl);
        logger.info("");
        logger.info("Obrigado por ser PRO!");
        logger.info("Equipe SCF-MEI");
        logger.info("========================================");
        
        // TODO: Implementar envio real de e-mail
    }
    
    /**
     * Envia e-mail de confirmação de cancelamento.
     */
    public void enviarEmailCancelamento(Usuario usuario, String motivo) {
        logger.info("========================================");
        logger.info("📧 E-MAIL DE CANCELAMENTO");
        logger.info("========================================");
        logger.info("Para: {}", usuario.getUsername() + "@scfmei.com.br");
        logger.info("Assunto: Assinatura Cancelada");
        logger.info("");
        logger.info("Olá {},", usuario.getUsername());
        logger.info("");
        logger.info("Sua assinatura PRO foi cancelada com sucesso.");
        logger.info("");
        logger.info("Você voltou para o plano FREE com:");
        logger.info("- Até 20 lançamentos por mês");
        logger.info("- Recursos básicos");
        logger.info("");
        if (motivo != null && !motivo.isBlank()) {
            logger.info("Motivo do cancelamento: {}", motivo);
            logger.info("");
        }
        logger.info("Sentiremos sua falta! 😢");
        logger.info("");
        logger.info("Se mudar de ideia, você pode fazer upgrade novamente:");
        logger.info("{}/assinatura/upgrade", baseUrl);
        logger.info("");
        logger.info("Equipe SCF-MEI");
        logger.info("========================================");
        
        // TODO: Implementar envio real de e-mail
    }
    
    /**
     * Envia e-mail de alerta de proximidade do limite.
     */
    public void enviarEmailProximoDoLimite(Usuario usuario, int lancamentosUsados, int lancamentosRestantes) {
        logger.info("========================================");
        logger.info("📧 E-MAIL DE ALERTA DE LIMITE");
        logger.info("========================================");
        logger.info("Para: {}", usuario.getUsername() + "@scfmei.com.br");
        logger.info("Assunto: ⚠️ Você está próximo do limite mensal");
        logger.info("");
        logger.info("Olá {},", usuario.getUsername());
        logger.info("");
        logger.info("Você já usou {} de 20 lançamentos neste mês.", lancamentosUsados);
        logger.info("Restam apenas {} lançamentos!", lancamentosRestantes);
        logger.info("");
        logger.info("Para não ficar sem lançamentos, considere fazer upgrade para o plano PRO:");
        logger.info("✅ Lançamentos ILIMITADOS");
        logger.info("✅ Apenas R$ 29,90/mês");
        logger.info("");
        logger.info("Fazer upgrade: {}/assinatura/upgrade", baseUrl);
        logger.info("");
        logger.info("Equipe SCF-MEI");
        logger.info("========================================");
        
        // TODO: Implementar envio real de e-mail
    }
    
    /**
     * Envia e-mail de limite excedido.
     */
    public void enviarEmailLimiteExcedido(Usuario usuario) {
        logger.info("========================================");
        logger.info("📧 E-MAIL DE LIMITE EXCEDIDO");
        logger.info("========================================");
        logger.info("Para: {}", usuario.getUsername() + "@scfmei.com.br");
        logger.info("Assunto: ⚠️ Limite mensal atingido");
        logger.info("");
        logger.info("Olá {},", usuario.getUsername());
        logger.info("");
        logger.info("Você atingiu o limite de 20 lançamentos do plano FREE.");
        logger.info("");
        logger.info("Você não poderá criar novos lançamentos até o próximo mês,");
        logger.info("a menos que faça upgrade para o plano PRO:");
        logger.info("");
        logger.info("✅ Lançamentos ILIMITADOS");
        logger.info("✅ Relatórios avançados");
        logger.info("✅ Apenas R$ 29,90/mês");
        logger.info("");
        logger.info("Fazer upgrade: {}/assinatura/upgrade", baseUrl);
        logger.info("");
        logger.info("Equipe SCF-MEI");
        logger.info("========================================");
        
        // TODO: Implementar envio real de e-mail
    }
    
    /**
     * Envia e-mail de falha de pagamento.
     */
    public void enviarEmailFalhaPagamento(Usuario usuario, Assinatura assinatura) {
        logger.info("========================================");
        logger.info("📧 E-MAIL DE FALHA DE PAGAMENTO");
        logger.info("========================================");
        logger.info("Para: {}", usuario.getUsername() + "@scfmei.com.br");
        logger.info("Assunto: ⚠️ Falha no pagamento da assinatura");
        logger.info("");
        logger.info("Olá {},", usuario.getUsername());
        logger.info("");
        logger.info("Não conseguimos processar o pagamento da sua assinatura PRO.");
        logger.info("");
        logger.info("Valor: R$ {}", assinatura.getValorMensal());
        logger.info("Forma de pagamento: {}", assinatura.getFormaPagamento().getDescricao());
        logger.info("");
        logger.info("Por favor, atualize seus dados de pagamento:");
        logger.info("{}/assinatura", baseUrl);
        logger.info("");
        logger.info("Se não recebermos o pagamento em 7 dias, sua assinatura será suspensa.");
        logger.info("");
        logger.info("Equipe SCF-MEI");
        logger.info("========================================");
        
        // TODO: Implementar envio real de e-mail
    }
}

