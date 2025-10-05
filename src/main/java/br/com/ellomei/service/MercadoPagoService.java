package br.com.ellomei.service;

import br.com.ellomei.domain.FormaPagamento;
import br.com.ellomei.domain.PlanoAssinatura;
import br.com.ellomei.domain.Usuario;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.common.IdentificationRequest;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.payment.PaymentCreateRequest;
import com.mercadopago.client.payment.PaymentPayerRequest;
import com.mercadopago.client.preference.*;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.payment.Payment;
import com.mercadopago.resources.preference.Preference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Serviço de integração com a API do Mercado Pago.
 * 
 * Responsabilidades:
 * - Criar preferências de pagamento (checkout)
 * - Processar pagamentos via cartão de crédito
 * - Gerar QR Code PIX
 * - Gerar boletos
 * - Criar assinaturas recorrentes
 * - Processar webhooks
 * 
 * Documentação oficial:
 * https://www.mercadopago.com.br/developers/pt/docs
 * 
 * @author ElloMEI Team
 * @since 1.0.0
 */
@Service
public class MercadoPagoService {
    
    private static final Logger logger = LoggerFactory.getLogger(MercadoPagoService.class);
    
    @Value("${mercadopago.access-token:TEST-YOUR-ACCESS-TOKEN}")
    private String accessToken;
    
    @Value("${mercadopago.public-key:TEST-YOUR-PUBLIC-KEY}")
    private String publicKey;
    
    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;
    
    // Preços dos planos (em reais)
    private static final BigDecimal PRECO_PLANO_PRO = new BigDecimal("29.90");
    
    /**
     * Inicializa a configuração do Mercado Pago.
     */
    private void initMercadoPago() {
        MercadoPagoConfig.setAccessToken(accessToken);
    }
    
    /**
     * Cria uma preferência de pagamento para checkout.
     * 
     * Esta preferência gera um link de pagamento que pode ser usado
     * para redirecionar o usuário para o checkout do Mercado Pago.
     * 
     * @param usuario Usuário que está fazendo o upgrade
     * @param plano Plano que está sendo contratado
     * @return Preferência criada com link de pagamento
     */
    public Preference criarPreferenciaPagamento(Usuario usuario, PlanoAssinatura plano) 
            throws MPException, MPApiException {
        
        initMercadoPago();
        
        // Criar item (plano)
        PreferenceItemRequest item = PreferenceItemRequest.builder()
            .id(plano.name())
            .title("Plano " + plano.name() + " - ElloMEI")
            .description("Assinatura mensal do plano " + plano.name())
            .categoryId("services")
            .quantity(1)
            .currencyId("BRL")
            .unitPrice(getPrecoPlano(plano))
            .build();
        
        List<PreferenceItemRequest> items = new ArrayList<>();
        items.add(item);
        
        // Configurar payer (comprador)
        PreferencePayerRequest payer = PreferencePayerRequest.builder()
            .name(usuario.getUsername())
            .email(usuario.getUsername() + "@ellomei.com") // Usar username como email
            .build();
        
        // Configurar URLs de retorno
        PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
            .success(baseUrl + "/assinatura/pagamento/sucesso")
            .failure(baseUrl + "/assinatura/pagamento/falha")
            .pending(baseUrl + "/assinatura/pagamento/pendente")
            .build();
        
        // Criar preferência
        PreferenceRequest preferenceRequest = PreferenceRequest.builder()
            .items(items)
            .payer(payer)
            .backUrls(backUrls)
            .autoReturn("approved")
            .externalReference("USER_" + usuario.getId() + "_PLAN_" + plano.name())
            .statementDescriptor("ElloMEI " + plano.name())
            .notificationUrl(baseUrl + "/api/webhooks/mercadopago")
            .build();
        
        PreferenceClient client = new PreferenceClient();
        Preference preference = client.create(preferenceRequest);
        
        logger.info("Preferência de pagamento criada: {} para usuário: {}", 
                   preference.getId(), usuario.getUsername());
        
        return preference;
    }
    
    /**
     * Processa um pagamento via cartão de crédito.
     * 
     * @param usuario Usuário que está pagando
     * @param plano Plano sendo contratado
     * @param token Token do cartão gerado pelo Mercado Pago.js
     * @param installments Número de parcelas
     * @return Pagamento processado
     */
    public Payment processarPagamentoCartao(Usuario usuario, PlanoAssinatura plano, 
                                           String token, int installments) 
            throws MPException, MPApiException {
        
        initMercadoPago();
        
        BigDecimal valor = getPrecoPlano(plano);
        
        PaymentCreateRequest paymentCreateRequest = PaymentCreateRequest.builder()
            .transactionAmount(valor)
            .token(token)
            .description("Plano " + plano.name() + " - ElloMEI")
            .installments(installments)
            .paymentMethodId("credit_card")
            .payer(
                PaymentPayerRequest.builder()
                    .email(usuario.getUsername() + "@ellomei.com")
                    .firstName(usuario.getUsername())
                    .identification(
                        IdentificationRequest.builder()
                            .type("CPF")
                            .number(usuario.getCpf())
                            .build()
                    )
                    .build()
            )
            .externalReference("USER_" + usuario.getId() + "_PLAN_" + plano.name())
            .statementDescriptor("ElloMEI")
            .build();
        
        PaymentClient client = new PaymentClient();
        Payment payment = client.create(paymentCreateRequest);
        
        logger.info("Pagamento processado: {} - Status: {} para usuário: {}", 
                   payment.getId(), payment.getStatus(), usuario.getUsername());
        
        return payment;
    }
    
    /**
     * Cria um pagamento PIX.
     * 
     * @param usuario Usuário que está pagando
     * @param plano Plano sendo contratado
     * @return Pagamento com QR Code PIX
     */
    public Payment criarPagamentoPix(Usuario usuario, PlanoAssinatura plano) 
            throws MPException, MPApiException {
        
        initMercadoPago();
        
        BigDecimal valor = getPrecoPlano(plano);
        
        PaymentCreateRequest paymentCreateRequest = PaymentCreateRequest.builder()
            .transactionAmount(valor)
            .description("Plano " + plano.name() + " - ElloMEI")
            .paymentMethodId("pix")
            .payer(
                PaymentPayerRequest.builder()
                    .email(usuario.getUsername() + "@ellomei.com")
                    .firstName(usuario.getUsername())
                    .identification(
                        IdentificationRequest.builder()
                            .type("CPF")
                            .number(usuario.getCpf())
                            .build()
                    )
                    .build()
            )
            .externalReference("USER_" + usuario.getId() + "_PLAN_" + plano.name())
            .build();
        
        PaymentClient client = new PaymentClient();
        Payment payment = client.create(paymentCreateRequest);
        
        logger.info("Pagamento PIX criado: {} para usuário: {}", 
                   payment.getId(), usuario.getUsername());
        
        return payment;
    }
    
    /**
     * Busca um pagamento pelo ID.
     */
    public Payment buscarPagamento(Long paymentId) throws MPException, MPApiException {
        initMercadoPago();
        PaymentClient client = new PaymentClient();
        return client.get(paymentId);
    }
    
    /**
     * Retorna o preço do plano.
     */
    private BigDecimal getPrecoPlano(PlanoAssinatura plano) {
        return switch (plano) {
            case PRO -> PRECO_PLANO_PRO;
            case FREE -> BigDecimal.ZERO;
        };
    }
    
    /**
     * Retorna a chave pública do Mercado Pago (para uso no frontend).
     */
    public String getPublicKey() {
        return publicKey;
    }
}

