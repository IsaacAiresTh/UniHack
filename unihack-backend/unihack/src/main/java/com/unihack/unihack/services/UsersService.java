package com.unihack.unihack.services;

import com.unihack.unihack.exceptions.UserNotFoundException;
import com.unihack.unihack.models.User;
import com.unihack.unihack.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UsersService {

    private final UserRepository userRepository;

    @Autowired
    public UsersService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public User createUser(User user) {
        // Validações e lógica de codificação de senha podem ser adicionadas aqui
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
        // O método save() do JpaRepository funciona como update se o ID do objeto já existir no banco.
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
}