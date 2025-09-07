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

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // BEAN 1: O Codificador de Senhas
    @Bean
    public PasswordEncoder passwordEncoder() {
        // Usa o BCrypt, o padrão moderno e seguro para criptografar senhas
        return new BCryptPasswordEncoder();
    }

    // BEAN 2: O Serviço de Detalhes do Usuário (Nossos usuários)
    @Bean
    public UserDetailsService userDetailsService() {
        // Cria um usuário em memória. Ótimo para começar.
        UserDetails user = User.builder()
                .username("erick")
                .password(passwordEncoder().encode("5522")) // A senha é criptografada antes de ser guardada
                .roles("USER")
                .build();

        return new InMemoryUserDetailsManager(user);
    }

    // BEAN 3: O Filtro de Segurança (As regras do jogo)
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .anyRequest().authenticated() // Exige que TODAS as requisições sejam autenticadas
                )
                .formLogin(withDefaults()); // Usa a página de login padrão do Spring por enquanto

        return http.build();
    }
}