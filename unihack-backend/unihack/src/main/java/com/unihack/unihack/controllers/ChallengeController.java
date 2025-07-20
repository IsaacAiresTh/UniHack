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

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/challenges")
public class ChallengeController {

    private final ChallengeRepository challengeRepository;
    private final ChallengeActivationService activationService;

    @Autowired
    public ChallengeController(ChallengeRepository challengeRepository, ChallengeActivationService activationService) {
        this.challengeRepository = challengeRepository;
        this.activationService = activationService;
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
    @PreAuthorize("hasRole('ADMIN')") // Apenas Admins podem criar desafios
    public ResponseEntity<Challenge> createChallenge(@Valid @RequestBody Challenge challenge) {
        return ResponseEntity.ok(challengeRepository.save(challenge));
    }

    // ... outros endpoints PUT e DELETE ...

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
}