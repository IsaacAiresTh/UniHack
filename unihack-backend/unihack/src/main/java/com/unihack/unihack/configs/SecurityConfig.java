package com.unihack.unihack.configs;

import com.unihack.unihack.configs.security.JwtAuthenticationFilter;
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
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(requests -> requests
                        // Endpoints públicos
                        .requestMatchers(HttpMethod.POST, "/auth/register").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                        // --- ALTERAÇÃO APLICADA AQUI ---
                        // Rota do ranking corrigida e tornada pública.
                        .requestMatchers(HttpMethod.GET, "/api/users/ranking").permitAll()

                        // Endpoints que precisam de autenticação
                        .requestMatchers(HttpMethod.GET, "/challenges", "/challenges/all").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/users/me/profile").authenticated()
                        .requestMatchers("/users/me").authenticated() // Mantido caso esteja em uso

                        // Regras por Role (Mantidas como estavam)
                        .requestMatchers(HttpMethod.GET, "/challenges/details/**").hasRole(UserRole.USER.name())
                        .requestMatchers(HttpMethod.POST, "/challenges/create").hasRole(UserRole.ADMIN.name())
                        .requestMatchers(HttpMethod.PUT, "/challenges/update/**").hasRole(UserRole.ADMIN.name())
                        .requestMatchers(HttpMethod.DELETE, "/challenges/delete/**").hasRole(UserRole.ADMIN.name())
                        .requestMatchers(HttpMethod.GET, "/users/**").hasRole(UserRole.ADMIN.name())
                        .requestMatchers(HttpMethod.PUT, "/users/**").hasRole(UserRole.ADMIN.name())
                        .requestMatchers(HttpMethod.DELETE, "/users/**").hasRole(UserRole.ADMIN.name())

                        // Default: qualquer outra requisição precisa estar autenticada
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}