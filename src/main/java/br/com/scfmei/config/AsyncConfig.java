package br.com.scfmei.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Configuração para processamento assíncrono e agendamento de tarefas.
 * 
 * @EnableAsync: Habilita processamento assíncrono de métodos anotados com @Async
 * @EnableScheduling: Habilita execução de tarefas agendadas com @Scheduled
 * 
 * Usado para:
 * - Envio assíncrono de e-mails
 * - Processamento assíncrono de relatórios
 * - Jobs agendados de renovação de assinaturas
 * - Jobs agendados de expiração de trials
 * 
 * @author SCF-MEI Team
 * @since 1.0.0
 */
@Configuration
@EnableAsync
@EnableScheduling
public class AsyncConfig {
    // Configuração padrão do Spring Boot é suficiente para nosso caso
    // Se precisar customizar o executor de threads, adicione um @Bean aqui
}

