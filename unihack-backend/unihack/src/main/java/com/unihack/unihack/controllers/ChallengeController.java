package com.unihack.unihack.controllers;

import com.unihack.unihack.models.Challenge;
import com.unihack.unihack.repository.ChallengeRepository;
import com.unihack.unihack.services.ChallengeActivationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import com.unihack.unihack.models.User; // Importar o modelo User
import com.unihack.unihack.repository.UserRepository; // Importar o repositório User
import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/challenges")
public class ChallengeController {

    private final ChallengeRepository challengeRepository;
    private final ChallengeActivationService activationService;
    private final UserRepository userRepository;

    @Autowired
    public ChallengeController(ChallengeRepository challengeRepository, ChallengeActivationService activationService,  UserRepository userRepository) {
        this.challengeRepository = challengeRepository;
        this.activationService = activationService;
        this.userRepository = userRepository;
    }

    @GetMapping("/all")
    @PreAuthorize("isAuthenticated()")
    public List<Challenge> getAllChallenges() {
        return challengeRepository.findAll();
    }

    @GetMapping("/details/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Challenge> getChallengeById(@PathVariable UUID id) {
        return challengeRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Challenge> createChallenge(@Valid @RequestBody Challenge challenge) {
        return ResponseEntity.ok(challengeRepository.save(challenge));
    }


    @PostMapping("/{id}/start")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> startChallenge(@PathVariable UUID id) {
        // 1. Busca o desafio no banco de dados
        Challenge challenge = challengeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Desafio não encontrado"));

        // 2. Usa o nome da imagem do desafio para iniciar o contêiner
        Map<String, String> challengeInfo = activationService.startChallengeContainer(challenge.getDockerImage());

        // 3. Salva a sessão ativa no banco de dados (PRÓXIMO PASSO)
        // ...

        // 4. Retorna a URL completa e o ID do contêiner para o frontend
        return ResponseEntity.ok(challengeInfo);
    }

    // --- NOVO ENDPOINT PARA SUBMETER A FLAG ---
    @PostMapping("/submit")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> submitFlag(@RequestBody Map<String, String> payload) {
        String submittedFlag = payload.get("flag");
        String containerId = payload.get("containerId"); // Recebemos o ID do contentor do frontend

        if (submittedFlag == null || submittedFlag.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "A flag não pode estar vazia."));
        }

        // 1. Encontrar o desafio que corresponde à flag
        return challengeRepository.findByFlag(submittedFlag)
                .map(challenge -> {
                    // 2. Encontrar o utilizador atualmente autenticado
                    String username = SecurityContextHolder.getContext().getAuthentication().getName();
                    User user = userRepository.findByMatricula(username) // Assumindo que o username no token é a matrícula
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilizador não encontrado"));

                    // 3. Adicionar os pontos ao utilizador
                    user.setPoints(user.getPoints() + challenge.getScore());
                    userRepository.save(user);

                    // 4. Parar o contentor do desafio específico
                    activationService.stopContainer(containerId);

                    return ResponseEntity.ok(Map.of("message", "Flag correta! Pontos adicionados e ambiente finalizado."));
                })
                .orElse(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Flag incorreta.")));
    }
}