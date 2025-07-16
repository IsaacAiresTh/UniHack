package com.unihack.unihack.controllers;

import com.unihack.unihack.models.Challenge;
import com.unihack.unihack.repository.ChallengeRepository;
import com.unihack.unihack.services.ChallengeActivationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/challenges")
public class ChallengeController {

    private final ChallengeRepository challengeRepository;
    
    @Autowired
    private ChallengeActivationService activationService;

    public ChallengeController(ChallengeRepository challengeRepository) {
        this.challengeRepository = challengeRepository;
    }

    @GetMapping("/all")
    @PreAuthorize("isAuthenticated()")
    public List<Challenge> getAllChallenges() {
        return challengeRepository.findAll();
    }

    @GetMapping("/details/{id}")
    public ResponseEntity<Challenge> getChallengeById(@PathVariable UUID id) {
        return challengeRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/create")
    public ResponseEntity<Challenge> createChallenge(@Valid @RequestBody Challenge challenge) {
        return ResponseEntity.ok(challengeRepository.save(challenge));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Challenge> updateChallenge(@PathVariable UUID id, @Valid @RequestBody Challenge challengeDetails) {
        return challengeRepository.findById(id)
                .map(challenge -> {
                    challenge.setTitle(challengeDetails.getTitle());
                    challenge.setDescription(challengeDetails.getDescription());
                    challenge.setDifficulty(challengeDetails.getDifficulty());
                    challenge.setScore(challengeDetails.getScore());
                    return ResponseEntity.ok(challengeRepository.save(challenge));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteChallenge(@PathVariable UUID id) {
        if (challengeRepository.existsById(id)) {
            challengeRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/start")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> startChallenge(@PathVariable UUID id) {
        // 1. Encontrar o desafio no banco para obter o nome da imagem Docker
        // Challenge challenge = challengeRepository.findById(id).orElse(...);
        // String dockerImage = challenge.getDockerImageName(); // Você precisaria adicionar esse campo no seu modelo

        // Exemplo hardcoded:
        String dockerImage = "unihack/desafio-sqli:latest"; // Você precisará criar e subir essas imagens para um registry

        // 2. Chamar o serviço para iniciar o contêiner
        String containerId = activationService.startChallengeContainer(dockerImage);

        // 3. Obter a porta e construir a URL de acesso
        // String accessUrl = ...; 

        // 4. Salvar o estado no banco e retornar a URL para o frontend
        // return ResponseEntity.ok(Map.of("containerId", containerId, "accessUrl", accessUrl));
        return ResponseEntity.ok(Map.of("message", "Desafio iniciado!", "containerId", containerId));
    }
}
