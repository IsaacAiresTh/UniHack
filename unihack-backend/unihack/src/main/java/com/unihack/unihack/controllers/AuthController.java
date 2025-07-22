package com.unihack.unihack.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.unihack.unihack.dtos.RegisterDto;
import com.unihack.unihack.dtos.LoginDto;
import com.unihack.unihack.models.User;
import com.unihack.unihack.repository.UserRepository;
import com.unihack.unihack.configs.security.JwtTokenProvider; 

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository; //

    @Autowired
    private PasswordEncoder passwordEncoder; //

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegisterDto registerDto) {
        if (userRepository.existsByMatricula(registerDto.getMatricula())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Usuário com esta matrícula já existe."));
        }
        // Adicione uma verificação para username também, se ele deve ser único
        // if (userRepository.existsByUsername(registerDto.getName())) {
        //     return ResponseEntity.status(HttpStatus.CONFLICT)
        //             .body(Map.of("message", "Nome de usuário já existe."));
        // }
        if (!registerDto.getPassword().equals(registerDto.getConfirmPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "As senhas não conferem."));
        }

        String encodedPassword = passwordEncoder.encode(registerDto.getPassword());
        User newUser = new User(registerDto.getName(), registerDto.getMatricula(), encodedPassword);
        // Por padrão, o UserRole já é USER e points é 0, conforme seu modelo User.java
        userRepository.save(newUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Usuário registrado com sucesso"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginDto loginDto) {
        try {

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginDto.getMatricula(),
                            loginDto.getPassword()
                    )
            );

            // Se a autenticação for bem-sucedida, define a autenticação no contexto de segurança
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Gera o token JWT
            // O principal do objeto Authentication é o UserDetails que foi retornado pelo UserDetailsServiceImpl
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String jwt = tokenProvider.generateToken(userDetails);

            // Retorna o token JWT para o cliente
            // Você pode retornar mais informações se desejar, como o papel do usuário, etc.
            return ResponseEntity.ok(Map.of(
                    "token", jwt,
                    "message", "Login bem-sucedido!",
                    "matricula", userDetails.getUsername() // userDetails.getUsername() aqui é a matrícula
                    // "roles", userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()) // Exemplo para retornar roles
            ));

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Credenciais inválidas. Verifique sua matrícula e senha."));
        } catch (Exception e) {
            // Logar a exceção em um ambiente de produção
            // logger.error("Erro durante o login: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Ocorreu um erro interno durante o login."));
        }
    }
}