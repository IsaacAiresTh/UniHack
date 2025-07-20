package com.unihack.unihack.configs; // Ou o seu pacote principal

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Permite CORS para todos os endpoints ("/**")
                .allowedOrigins("http://localhost:4200") // A URL do seu frontend Angular
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Métodos HTTP permitidos
                .allowedHeaders("*") // Todos os cabeçalhos permitidos
                .allowCredentials(true); // Permite o envio de credenciais (como cookies ou tokens de autenticação)
    }
}