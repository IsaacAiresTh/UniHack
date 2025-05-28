package com.unihack.unihack.configs;

import com.unihack.unihack.enums.UserRole;
// Import para SessionCreationPolicy
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.http.HttpMethod;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
// Se você for adicionar um filtro JWT, precisará de algo como:
// import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
// import com.unihack.unihack.security.JwtAuthenticationFilter; // Seu filtro JWT

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    // Se você criar um filtro JWT, você o injetaria aqui
    // private final JwtAuthenticationFilter jwtAuthenticationFilter;
    // private final AuthenticationEntryPoint unauthorizedHandler;
    // private final AccessDeniedHandler accessDeniedHandler;

    // Construtor para injetar dependências (ex: filtro JWT, handlers de erro)
    // public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
    //                       AuthenticationEntryPoint unauthorizedHandler,
    //                       AccessDeniedHandler accessDeniedHandler) {
    //     this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    //     this.unauthorizedHandler = unauthorizedHandler;
    //     this.accessDeniedHandler = accessDeniedHandler;
    // }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Nova sintaxe lambda
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Para JWT
                // .exceptionHandling(exceptions -> exceptions // Para handlers de erro customizados
                //         .authenticationEntryPoint(unauthorizedHandler)
                //         .accessDeniedHandler(accessDeniedHandler)
                // )
                .authorizeHttpRequests(requests -> requests
                        // Endpoints públicos
                        .requestMatchers(HttpMethod.POST, "/users").permitAll() // Cadastro de usuário
                        .requestMatchers(HttpMethod.POST, "/auth/register").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                        // Endpoints de documentação (exemplo)
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                        // Regras para Challenges
                        .requestMatchers(HttpMethod.GET, "/challenges", "/challenges/all").authenticated()
                        .requestMatchers(HttpMethod.GET, "/challenges/details/**").hasRole(UserRole.USER.name())
                        // Se /challenges/** para GET for mais genérico para USER, pode vir aqui.
                        // Cuidado para não conflitar com regras de ADMIN mais específicas.
                        // .requestMatchers(HttpMethod.GET, "/challenges/**").hasRole(UserRole.USER.name())

                        .requestMatchers(HttpMethod.POST, "/challenges/create").hasRole(UserRole.ADMIN.name())
                        .requestMatchers(HttpMethod.PUT, "/challenges/update/**").hasRole(UserRole.ADMIN.name())
                        .requestMatchers(HttpMethod.DELETE, "/challenges/delete/**").hasRole(UserRole.ADMIN.name())
                        // Se houver outros métodos em /challenges/** que são apenas para ADMIN
                        // .requestMatchers(HttpMethod.PATCH, "/challenges/**").hasRole(UserRole.ADMIN.name())


                        // Regras para Users
                        .requestMatchers("/users/me").authenticated() // Exemplo: endpoint para pegar dados do usuário logado
                        .requestMatchers(HttpMethod.GET, "/users/**").hasRole(UserRole.ADMIN.name()) // Listar todos os usuários (ADMIN)
                        .requestMatchers(HttpMethod.PUT, "/users/**").hasRole(UserRole.ADMIN.name()) // Atualizar usuário (ADMIN)
                        .requestMatchers(HttpMethod.DELETE, "/users/**").hasRole(UserRole.ADMIN.name()) // Deletar usuário (ADMIN)


                        // Default: qualquer outra requisição precisa estar autenticada
                        .anyRequest().authenticated()
                );
        // Quando implementar JWT, você removerá o httpBasic e adicionará seu filtro:
        // .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        // Por enquanto, se ainda estiver usando httpBasic temporariamente:
        // .httpBasic(Customizer.withDefaults()); // Ou a sintaxe antiga: httpBasic(withDefaults());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Você também precisará de um AuthenticationManager bean se estiver fazendo autenticação customizada
    // e um UserDetailsService para carregar os dados do usuário.
}