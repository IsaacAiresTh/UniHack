package com.unihack.unihack.configs.security;

import com.unihack.unihack.services.UserDetailsServiceImpl; // Seu UserDetailsService
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private UserDetailsServiceImpl userDetailsServiceImpl;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);

            // Verifica se o token existe e é válido
            if (StringUtils.hasText(jwt) && tokenProvider.extractUsername(jwt) != null) { // Usei extractUsername para uma validação inicial antes de carregar UserDetails
                String username = tokenProvider.extractUsername(jwt); // Matrícula do usuário

                // Se o usuário ainda não está autenticado no contexto de segurança atual
                if (SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = userDetailsServiceImpl.loadUserByUsername(username);

                    // Se o token for válido para este usuário
                    if (tokenProvider.validateToken(jwt, userDetails)) {
                        // Cria um objeto de autenticação
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                        // Define o usuário como autenticado no contexto de segurança do Spring
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }
            }
        } catch (Exception ex) {
            // Logger para a exceção pode ser adicionado aqui
            // ex.printStackTrace(); // Para depuração, mas em produção use um logger
            // Não interrompa o filtro por exceções na validação do token,
            // apenas não configure o SecurityContextHolder.
            // Se o endpoint for protegido e não houver autenticação, o Spring Security cuidará disso.
            logger.error("Não foi possível definir a autenticação do usuário no contexto de segurança", ex);
        }

        // Continua a cadeia de filtros
        filterChain.doFilter(request, response);
    }

    // Extrai o token JWT do cabeçalho Authorization da requisição
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        // Verifica se o cabeçalho existe e começa com "Bearer "
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // Retorna apenas o token, sem o "Bearer "
        }
        return null;
    }
}