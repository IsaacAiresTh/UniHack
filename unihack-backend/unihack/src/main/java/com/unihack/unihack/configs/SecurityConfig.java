package com.unihack.unihack.configs;

import static org.springframework.security.config.Customizer.withDefaults;

import com.unihack.unihack.enums.UserRole;
import org.springframework.http.HttpMethod;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf().disable() // Desabilitar CSRF para APIs REST
            .authorizeHttpRequests(
                    (requests) -> requests
                            .requestMatchers(HttpMethod.POST,"/users").permitAll()
                            .requestMatchers(HttpMethod.POST, "/auth/register").permitAll()
                            .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                            .requestMatchers(HttpMethod.GET,"/challenges/**").hasRole(UserRole.USER.name())
                            .requestMatchers(HttpMethod.GET, "/challenges").authenticated()
                            .requestMatchers(HttpMethod.GET, "/challenges/all").authenticated()
                            .requestMatchers(HttpMethod.GET, "/challenges/details/**").hasRole(UserRole.USER.name())
                            .requestMatchers("/challenges/create").hasRole(UserRole.ADMIN.name())
                            .requestMatchers("/challenges/update/**").hasRole(UserRole.ADMIN.name())
                            .requestMatchers("/challenges/delete/**").hasRole(UserRole.ADMIN.name())
                            .requestMatchers("/challenges/**").hasRole(UserRole.ADMIN.name())
                            .requestMatchers("/users/**").authenticated()
                            .anyRequest().permitAll()
            )
            .httpBasic(withDefaults());
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {return new BCryptPasswordEncoder();}
}
