package com.unihack.unihack.controllers;

import com.unihack.unihack.dtos.UserProfileDTO;
import com.unihack.unihack.models.User;
import com.unihack.unihack.services.UsersService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users") // Padronizado para /api/users
public class UserController {

    private final UsersService usersService;

    public UserController(UsersService usersService) {
        this.usersService = usersService;
    }

    /**
     * Endpoint para buscar o perfil completo do usuário autenticado.
     * @return UserProfileDTO com dados do usuário e estatísticas.
     */
    @GetMapping("/me/profile")
    public ResponseEntity<UserProfileDTO> getCurrentUserProfile() {
        // Pega o nome de usuário (matrícula) a partir do token de segurança.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        // Chama o serviço para obter e calcular os dados do perfil.
        UserProfileDTO userProfile = usersService.getUserProfileByUsername(currentUsername);

        // Retorna o perfil completo.
        return ResponseEntity.ok(userProfile);
    }

    /**
     * Endpoint para obter o ranking de usuários, ordenado por pontos.
     * @return Lista de usuários ordenados.
     */
    @GetMapping("/ranking")
    public List<User> getUsersByRanking() {
        // Agora a chamada é feita através do UsersService, mantendo a arquitetura limpa.
        return usersService.getUsersForRanking();
    }
}