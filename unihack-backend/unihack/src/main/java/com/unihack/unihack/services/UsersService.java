package com.unihack.unihack.services;

import com.unihack.unihack.dtos.UserProfileDTO;
import com.unihack.unihack.dtos.UserStatsDTO;
import com.unihack.unihack.exceptions.UserNotFoundException;
import com.unihack.unihack.models.CompletedChallenge;
import com.unihack.unihack.models.User;
import com.unihack.unihack.repository.ChallengeRepository;
import com.unihack.unihack.repository.CompletedChallengeRepository;
import com.unihack.unihack.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class UsersService {

    private final UserRepository userRepository;
    // Novas dependências injetadas
    private final ChallengeRepository challengeRepository;
    private final CompletedChallengeRepository completedChallengeRepository;

    @Autowired
    public UsersService(UserRepository userRepository, ChallengeRepository challengeRepository, CompletedChallengeRepository completedChallengeRepository) {
        this.userRepository = userRepository;
        this.challengeRepository = challengeRepository;
        this.completedChallengeRepository = completedChallengeRepository;
    }

    // ===============================================================
    // SEUS MÉTODOS EXISTENTES (INTACTOS)
    // ===============================================================

    @Transactional
    public User createUser(User user) {
        return userRepository.save(user);
    }

    @Transactional
    public User updateUserPoints(UUID userId, int points) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        user.setPoints(points);
        return userRepository.save(user);
    }

    @Transactional
    public User updateUser(User user) {
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User getUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public User getUserByMatricula(String matricula) {
        return userRepository.findByMatricula(matricula)
                .orElseThrow(() -> new UserNotFoundException("User not found with matricula: " + matricula));
    }

    @Transactional
    public void deleteUserById(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User not found with id: " + id + ". Cannot delete.");
        }
        userRepository.deleteById(id);
    }

    // ===============================================================
    // --- NOVOS MÉTODOS PARA PERFIL E RANKING (CORRIGIDOS) ---
    // ===============================================================

    @Transactional(readOnly = true)
    public List<User> getUsersForRanking() {
        return userRepository.findAllByOrderByPointsDesc();
    }

    @Transactional(readOnly = true)
    public UserProfileDTO getUserProfileByUsername(String username) {
        // CORREÇÃO APLICADA: Usando findByMatricula, que existe no seu repositório.
        // O "username" que vem do token de segurança é, na verdade, a matrícula.
        User user = userRepository.findByMatricula(username)
                .orElseThrow(() -> new UserNotFoundException("User not found with matricula: " + username));

        UserProfileDTO profileDTO = new UserProfileDTO(user);
        profileDTO.setStats(calculateUserStats(user));
        return profileDTO;
    }

    private UserStatsDTO calculateUserStats(User user) {
        UserStatsDTO statsDTO = new UserStatsDTO();
        long totalChallenges = challengeRepository.count();
        List<CompletedChallenge> completedChallenges = completedChallengeRepository.findByUser(user);
        long completedCount = completedChallenges.size();

        statsDTO.setTotalChallenges(totalChallenges);
        statsDTO.setCompletedChallenges(completedCount);

        if (totalChallenges > 0) {
            double progress = ((double) completedCount / totalChallenges) * 100;
            statsDTO.setProgressPercentage(Math.round(progress * 100.0) / 100.0);
        } else {
            statsDTO.setProgressPercentage(0);
        }

        if (!completedChallenges.isEmpty()) {
            // CORREÇÃO APLICADA: Agora o .getCategory() vai funcionar porque adicionamos o campo no model Challenge.
            Map<String, Long> categoryCounts = completedChallenges.stream()
                    .map(completed -> completed.getChallenge().getCategory())
                    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

            String favoriteCategory = categoryCounts.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("N/A");

            statsDTO.setFavoriteCategory(favoriteCategory);
        } else {
            statsDTO.setFavoriteCategory("N/A");
        }
        return statsDTO;
    }
}