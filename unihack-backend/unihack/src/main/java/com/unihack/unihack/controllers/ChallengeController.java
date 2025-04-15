package com.unihack.unihack.controllers;

import com.unihack.unihack.models.Challenge;
import com.unihack.unihack.repository.ChallengeRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/challenges")
public class ChallengeController {

    private final ChallengeRepository challengeRepository;

    public ChallengeController(ChallengeRepository challengeRepository) {
        this.challengeRepository = challengeRepository;
    }

    @GetMapping
    public List<Challenge> getAllChallenges() {
        return challengeRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Challenge> getChallengeById(@PathVariable Long id) {
        return challengeRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Challenge> createChallenge(@Valid @RequestBody Challenge challenge) {
        return ResponseEntity.ok(challengeRepository.save(challenge));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Challenge> updateChallenge(@PathVariable Long id, @Valid @RequestBody Challenge challengeDetails) {
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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteChallenge(@PathVariable Long id) {
        if (challengeRepository.existsById(id)) {
            challengeRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}