package br.com.scfmei.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails user = User.builder()
                .username("erick")
                .password(passwordEncoder().encode("5522"))
                .roles("USER")
                .build();
        return new InMemoryUserDetailsManager(user);
    }

    // --- A ÚNICA E CORRETA VERSÃO DESTE MÉTODO ---
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        // Permite acesso irrestrito ao login, CSS e arquivos web (Bootstrap)
                        .requestMatchers("/login", "/webjars/**", "/css/**").permitAll()
                        // Todas as outras requisições precisam de autenticação
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        // Diz ao Spring Security qual é a nossa página de login customizada
                        .loginPage("/login")
                        // Para onde ir após o login com sucesso
                        .defaultSuccessUrl("/", true)
                )
                .logout(logout -> logout
                        // Para onde ir após fazer logout
                        .logoutSuccessUrl("/login?logout")
                );

        return http.build();
    }
}