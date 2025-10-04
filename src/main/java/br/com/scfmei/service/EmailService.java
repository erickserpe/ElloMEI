package br.com.scfmei.service;

import br.com.scfmei.domain.Assinatura;
import br.com.scfmei.domain.Usuario;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
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

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@scfmei.com.br}")
    private String remetente;

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

    /**
     * Envia e-mail de falha de pagamento com informações de retry.
     */
    public void enviarEmailFalhaPagamento(Usuario usuario, Assinatura assinatura,
                                         int tentativaAtual, int maxTentativas) {
        logger.info("========================================");
        logger.info("📧 E-MAIL DE FALHA DE PAGAMENTO");
        logger.info("========================================");
        logger.info("Para: {}", usuario.getUsername() + "@scfmei.com.br");
        logger.info("Assunto: Falha no Pagamento - Tentativa {}/{}", tentativaAtual, maxTentativas);
        logger.info("");
        logger.info("Olá {},", usuario.getUsername());
        logger.info("");
        logger.info("Infelizmente, não conseguimos processar seu pagamento.");
        logger.info("");
        logger.info("Motivo: {}", assinatura.getMotivoFalhaPagamento() != null ?
                   assinatura.getMotivoFalhaPagamento() : "Não especificado");
        logger.info("Tentativa: {}/{}", tentativaAtual, maxTentativas);
        logger.info("");
        logger.info("O que fazer:");
        logger.info("1. Verifique os dados do seu cartão");
        logger.info("2. Certifique-se de ter saldo disponível");
        logger.info("3. Entre em contato com seu banco se necessário");
        logger.info("");
        logger.info("Tentaremos processar o pagamento novamente em 24 horas.");
        logger.info("");
        logger.info("Atualize seus dados de pagamento:");
        logger.info("{}/assinatura", baseUrl);
        logger.info("");
        logger.info("Equipe SCF-MEI");
        logger.info("========================================");

        // TODO: Implementar envio real de e-mail
    }

    /**
     * Envia e-mail de pagamento recuperado após retry bem-sucedido.
     */
    public void enviarEmailPagamentoRecuperado(Usuario usuario, Assinatura assinatura) {
        logger.info("========================================");
        logger.info("📧 E-MAIL DE PAGAMENTO RECUPERADO");
        logger.info("========================================");
        logger.info("Para: {}", usuario.getUsername() + "@scfmei.com.br");
        logger.info("Assunto: Pagamento Processado com Sucesso!");
        logger.info("");
        logger.info("Olá {},", usuario.getUsername());
        logger.info("");
        logger.info("Ótimas notícias! Seu pagamento foi processado com sucesso.");
        logger.info("");
        logger.info("Sua assinatura PRO foi reativada e você já pode usar todos os recursos.");
        logger.info("");
        logger.info("Valor: R$ {}", assinatura.getValorMensal());
        logger.info("Próxima cobrança: {}", assinatura.getDataProximaCobranca().format(DATE_FORMATTER));
        logger.info("");
        logger.info("Obrigado por continuar conosco!");
        logger.info("");
        logger.info("Equipe SCF-MEI");
        logger.info("========================================");

        // TODO: Implementar envio real de e-mail
    }

    /**
     * Envia e-mail de falha definitiva após esgotar tentativas.
     */
    public void enviarEmailPagamentoFalhaDefinitiva(Usuario usuario, Assinatura assinatura) {
        logger.info("========================================");
        logger.info("📧 E-MAIL DE CANCELAMENTO POR FALHA DE PAGAMENTO");
        logger.info("========================================");
        logger.info("Para: {}", usuario.getUsername() + "@scfmei.com.br");
        logger.info("Assunto: Assinatura Cancelada - Falha de Pagamento");
        logger.info("");
        logger.info("Olá {},", usuario.getUsername());
        logger.info("");
        logger.info("Lamentamos informar que sua assinatura PRO foi cancelada.");
        logger.info("");
        logger.info("Após múltiplas tentativas, não conseguimos processar seu pagamento.");
        logger.info("");
        logger.info("Você foi movido para o plano FREE com as seguintes limitações:");
        logger.info("- Máximo de 20 lançamentos por mês");
        logger.info("- Recursos básicos");
        logger.info("");
        logger.info("Para reativar o plano PRO:");
        logger.info("{}/assinatura/upgrade", baseUrl);
        logger.info("");
        logger.info("Se precisar de ajuda, entre em contato conosco.");
        logger.info("");
        logger.info("Equipe SCF-MEI");
        logger.info("========================================");

        // TODO: Implementar envio real de e-mail
    }

    /**
     * Envia email de recuperação de senha com link contendo token.
     *
     * @param destinatario Email do destinatário
     * @param nomeUsuario Nome do usuário
     * @param token Token de recuperação
     */
    public void enviarEmailRecuperacaoSenha(String destinatario, String nomeUsuario, String token) {
        if (mailSender == null) {
            logger.warn("JavaMailSender não configurado. Email de recuperação não será enviado.");
            logger.info("========================================");
            logger.info("📧 E-MAIL DE RECUPERAÇÃO DE SENHA (MOCK)");
            logger.info("========================================");
            logger.info("Para: {}", destinatario);
            logger.info("Nome: {}", nomeUsuario);
            logger.info("Token: {}", token);
            logger.info("Link: {}/recuperar-senha/redefinir?token={}", baseUrl, token);
            logger.info("========================================");
            return;
        }

        try {
            String assunto = "Recuperação de Senha - Ello MEI";
            String linkRecuperacao = baseUrl + "/recuperar-senha/redefinir?token=" + token;

            String corpo = construirEmailRecuperacaoSenha(nomeUsuario, linkRecuperacao);

            enviarEmailHtml(destinatario, assunto, corpo);

            logger.info("✅ Email de recuperação de senha enviado para: {}", destinatario);

        } catch (Exception e) {
            logger.error("❌ Erro ao enviar email de recuperação de senha para {}: {}", destinatario, e.getMessage());
            throw new RuntimeException("Erro ao enviar email de recuperação de senha", e);
        }
    }

    /**
     * Envia email HTML usando JavaMailSender.
     */
    private void enviarEmailHtml(String destinatario, String assunto, String corpoHtml) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(remetente);
        helper.setTo(destinatario);
        helper.setSubject(assunto);
        helper.setText(corpoHtml, true); // true = HTML

        mailSender.send(message);
    }

    /**
     * Constrói o HTML do email de recuperação de senha.
     */
    private String construirEmailRecuperacaoSenha(String nomeUsuario, String linkRecuperacao) {
        return """
            <!DOCTYPE html>
            <html lang="pt-BR">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body {
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                        background-color: #f4f4f4;
                        margin: 0;
                        padding: 0;
                    }
                    .container {
                        max-width: 600px;
                        margin: 40px auto;
                        background-color: #ffffff;
                        border-radius: 10px;
                        overflow: hidden;
                        box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
                    }
                    .header {
                        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                        color: white;
                        padding: 30px;
                        text-align: center;
                    }
                    .header h1 {
                        margin: 0;
                        font-size: 28px;
                    }
                    .content {
                        padding: 40px 30px;
                    }
                    .content h2 {
                        color: #333;
                        margin-top: 0;
                    }
                    .content p {
                        color: #666;
                        line-height: 1.6;
                        font-size: 16px;
                    }
                    .button {
                        display: inline-block;
                        padding: 15px 40px;
                        margin: 20px 0;
                        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                        color: white;
                        text-decoration: none;
                        border-radius: 5px;
                        font-weight: bold;
                        font-size: 16px;
                    }
                    .button:hover {
                        opacity: 0.9;
                    }
                    .footer {
                        background-color: #f8f8f8;
                        padding: 20px;
                        text-align: center;
                        color: #999;
                        font-size: 14px;
                    }
                    .warning {
                        background-color: #fff3cd;
                        border-left: 4px solid #ffc107;
                        padding: 15px;
                        margin: 20px 0;
                        border-radius: 4px;
                    }
                    .warning p {
                        margin: 0;
                        color: #856404;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🔐 Ello MEI</h1>
                    </div>
                    <div class="content">
                        <h2>Olá, %s!</h2>
                        <p>Recebemos uma solicitação para redefinir a senha da sua conta no <strong>Ello MEI</strong>.</p>
                        <p>Para criar uma nova senha, clique no botão abaixo:</p>
                        <div style="text-align: center;">
                            <a href="%s" class="button">Redefinir Senha</a>
                        </div>
                        <p>Ou copie e cole o link abaixo no seu navegador:</p>
                        <p style="word-break: break-all; background-color: #f8f8f8; padding: 10px; border-radius: 4px; font-size: 14px;">
                            %s
                        </p>
                        <div class="warning">
                            <p><strong>⚠️ Importante:</strong> Este link expira em <strong>1 hora</strong>.</p>
                        </div>
                        <p>Se você não solicitou a redefinição de senha, ignore este email. Sua senha permanecerá inalterada.</p>
                    </div>
                    <div class="footer">
                        <p>© 2024 Ello MEI - Sistema de Controle Financeiro para MEI</p>
                        <p>Este é um email automático, por favor não responda.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(nomeUsuario, linkRecuperacao, linkRecuperacao);
    }
}

