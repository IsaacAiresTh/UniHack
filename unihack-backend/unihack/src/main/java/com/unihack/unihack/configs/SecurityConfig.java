package com.unihack.unihack.configs;

import com.unihack.unihack.configs.security.JwtAuthenticationFilter;
// Se você criar um JwtAuthenticationEntryPoint, importe-o aqui também
// import com.unihack.unihack.security.jwt.JwtAuthenticationEntryPoint;

import com.unihack.unihack.enums.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy; //
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration; // Para configuração do CORS
import org.springframework.web.cors.CorsConfigurationSource; // Para configuração do CORS
import org.springframework.web.cors.UrlBasedCorsConfigurationSource; // Para configuração do CORS
import java.util.Arrays; // Para a configuração do CORS

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    // Se você criar um JwtAuthenticationEntryPoint, injete-o aqui também
    // @Autowired
    // private JwtAuthenticationEntryPoint unauthorizedHandler;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Desabilita CSRF, comum para APIs stateless
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Define a política de sessão como STATELESS
                // Se você tiver o unauthorizedHandler:
                // .exceptionHandling(exceptions -> exceptions
                //         .authenticationEntryPoint(unauthorizedHandler)
                // )
                .authorizeHttpRequests(requests -> requests
                        // Endpoints públicos
                        .requestMatchers(HttpMethod.POST, "/auth/register").permitAll() //
                        .requestMatchers(HttpMethod.POST, "/auth/login").permitAll() //
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll() //

                        // Suas regras existentes para Challenges e Users, adaptadas para usar .hasRole() ou .hasAuthority()
                        // Lembre-se que para .hasRole("NOME_ROLE") a authority no UserDetails deve ser "ROLE_NOME_ROLE"
                        .requestMatchers(HttpMethod.GET, "/challenges", "/challenges/all").authenticated()
                        .requestMatchers(HttpMethod.GET, "/challenges/details/**").hasRole(UserRole.USER.name())
                        .requestMatchers(HttpMethod.POST, "/challenges/create").hasRole(UserRole.ADMIN.name())
                        .requestMatchers(HttpMethod.PUT, "/challenges/update/**").hasRole(UserRole.ADMIN.name())
                        .requestMatchers(HttpMethod.DELETE, "/challenges/delete/**").hasRole(UserRole.ADMIN.name())

                        .requestMatchers("/users/me").authenticated()
                        .requestMatchers(HttpMethod.GET, "/users/ranking").authenticated() // Permitir ranking para todos autenticados
                        .requestMatchers(HttpMethod.GET, "/users/**").hasRole(UserRole.ADMIN.name())
                        .requestMatchers(HttpMethod.PUT, "/users/**").hasRole(UserRole.ADMIN.name())
                        .requestMatchers(HttpMethod.DELETE, "/users/**").hasRole(UserRole.ADMIN.name())

                        // Default: qualquer outra requisição precisa estar autenticada
                        .anyRequest().authenticated()
                )
                // Adiciona o filtro JWT ANTES do filtro padrão de UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); //
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200")); // Permita a origem do seu frontend
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")); // Métodos permitidos
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type")); // Cabeçalhos permitidos
        configuration.setAllowCredentials(true); // Se precisar enviar cookies ou usar autenticação baseada em sessão (não é o caso com JWT puro)
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // Aplica a configuração para todos os paths
        return source;
    }
}