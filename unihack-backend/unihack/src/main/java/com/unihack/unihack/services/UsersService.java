package com.unihack.unihack.services;

import com.unihack.unihack.dtos.UserProfileDTO;
import com.unihack.unihack.dtos.UserStatsDTO;
import com.unihack.unihack.exceptions.UserNotFoundException;
import com.unihack.unihack.models.SolvedChallenge;
import com.unihack.unihack.models.User;
import com.unihack.unihack.repository.ChallengeRepository;
import com.unihack.unihack.repository.SolvedChallengeRepository;
import com.unihack.unihack.repository.UserRepository;
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
    private final ChallengeRepository challengeRepository;
    private final SolvedChallengeRepository solvedChallengeRepository;

    public UsersService(
            UserRepository userRepository,
            ChallengeRepository challengeRepository,
            SolvedChallengeRepository solvedChallengeRepository) {
        this.userRepository = userRepository;
        this.challengeRepository = challengeRepository;
        this.solvedChallengeRepository = solvedChallengeRepository;
    }

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

    @Transactional(readOnly = true)
    public List<User> getUsersForRanking() {
        return userRepository.findAllByOrderByPointsDesc();
    }

    @Transactional(readOnly = true)
    public UserProfileDTO getUserProfileByUsername(String username) {
        User user = userRepository.findByMatricula(username)
                .orElseThrow(() -> new UserNotFoundException("User not found with matricula: " + username));

        UserProfileDTO profileDTO = new UserProfileDTO(user);
        profileDTO.setStats(calculateUserStats(user));
        return profileDTO;
    }

    private UserStatsDTO calculateUserStats(User user) {
        UserStatsDTO statsDTO = new UserStatsDTO();
        long totalChallenges = challengeRepository.count();
        List<SolvedChallenge> solved = solvedChallengeRepository.findByUser(user);
        long solvedCount = solved.size();

        statsDTO.setTotalChallenges(totalChallenges);
        statsDTO.setCompletedChallenges(solvedCount);

        if (totalChallenges > 0) {
            double progress = ((double) solvedCount / totalChallenges) * 100;
            statsDTO.setProgressPercentage(Math.round(progress * 100.0) / 100.0);
        } else {
            statsDTO.setProgressPercentage(0);
        }

        if (!solved.isEmpty()) {
            Map<String, Long> categoryCounts = solved.stream()
                    .map(s -> s.getChallenge().getCategory())
                    .filter(c -> c != null && !c.isBlank())
                    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

            statsDTO.setFavoriteCategory(
                    categoryCounts.entrySet().stream()
                            .max(Map.Entry.comparingByValue())
                            .map(Map.Entry::getKey)
                            .orElse("N/A"));
        } else {
            statsDTO.setFavoriteCategory("N/A");
        }
        return statsDTO;
    }
}
