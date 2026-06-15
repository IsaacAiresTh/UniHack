package com.unihack.unihack.controllers;

import com.unihack.unihack.dtos.ChallengeResponseDto;
import com.unihack.unihack.dtos.CreateChallengeDto;
import com.unihack.unihack.models.Challenge;
import com.unihack.unihack.models.SolvedChallenge;
import com.unihack.unihack.models.User;
import com.unihack.unihack.repository.ChallengeRepository;
import com.unihack.unihack.repository.SolvedChallengeRepository;
import com.unihack.unihack.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/challenges")
public class ChallengeController {

    private final ChallengeRepository challengeRepository;
    private final SolvedChallengeRepository solvedChallengeRepository;
    private final UserRepository userRepository;

    public ChallengeController(
            ChallengeRepository challengeRepository,
            SolvedChallengeRepository solvedChallengeRepository,
            UserRepository userRepository) {
        this.challengeRepository = challengeRepository;
        this.solvedChallengeRepository = solvedChallengeRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/all")
    @PreAuthorize("isAuthenticated()")
    public List<ChallengeResponseDto> getAllChallenges() {
        return challengeRepository.findAll()
                .stream()
                .map(ChallengeResponseDto::new)
                .toList();
    }

    @GetMapping("/details/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ChallengeResponseDto> getChallengeById(@PathVariable UUID id) {
        return challengeRepository.findById(id)
                .map(c -> ResponseEntity.ok(new ChallengeResponseDto(c)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Boolean>> getChallengeStatus(@PathVariable UUID id) {
        Challenge challenge = challengeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Desafio não encontrado"));

        User user = getAuthenticatedUser();
        boolean solved = solvedChallengeRepository.existsByUserAndChallenge(user, challenge);
        return ResponseEntity.ok(Map.of("solved", solved));
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ChallengeResponseDto> createChallenge(@Valid @RequestBody CreateChallengeDto dto) {
        Challenge challenge = new Challenge();
        challenge.setTitle(dto.getTitle());
        challenge.setDescription(dto.getDescription());
        challenge.setDifficulty(dto.getDifficulty());
        challenge.setScore(dto.getScore());
        challenge.setSlug(dto.getSlug());
        challenge.setCategory(dto.getCategory());
        challenge.setFlagHash(DigestUtils.md5DigestAsHex(dto.getFlag().trim().getBytes()));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ChallengeResponseDto(challengeRepository.save(challenge)));
    }

    @PostMapping("/submit")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> submitFlag(@RequestBody Map<String, String> payload) {
        String submittedFlag = payload.get("flag");

        if (submittedFlag == null || submittedFlag.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "A flag não pode estar vazia."));
        }

        String hashedFlag = DigestUtils.md5DigestAsHex(submittedFlag.trim().getBytes());

        return challengeRepository.findByFlagHash(hashedFlag)
                .map(challenge -> {
                    User user = getAuthenticatedUser();

                    if (solvedChallengeRepository.existsByUserAndChallenge(user, challenge)) {
                        return ResponseEntity.ok(
                                Map.of("message", "Você já resolveu este desafio anteriormente."));
                    }

                    solvedChallengeRepository.save(new SolvedChallenge(user, challenge));
                    user.setPoints(user.getPoints() + challenge.getScore());
                    userRepository.save(user);

                    return ResponseEntity.ok(
                            Map.of("message", "Flag correta! +" + challenge.getScore() + " pontos adicionados."));
                })
                .orElse(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "Flag incorreta.")));
    }

    private User getAuthenticatedUser() {
        String matricula = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByMatricula(matricula)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
    }
}
