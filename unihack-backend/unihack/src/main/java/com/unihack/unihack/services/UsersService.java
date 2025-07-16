package com.unihack.unihack.services;

import com.unihack.unihack.exceptions.UserNotFoundException; // Exemplo de exceção customizada
import com.unihack.unihack.models.User;
import com.unihack.unihack.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UsersService {

    private final UserRepository userRepository;
    // private final PasswordEncoder passwordEncoder; // Descomente e injete se estiver usando Spring Security

    @Autowired // Opcional em construtores de uma única dependência a partir do Spring 4.3
    public UsersService(UserRepository userRepository /*, PasswordEncoder passwordEncoder */) {
        this.userRepository = userRepository;
        // this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User createUser(User user) {
        // Adicionar validações de existência se necessário:
        // if (userRepository.existsByUsername(user.getUsername())) {
        //     throw new UsernameAlreadyExistsException("Username " + user.getUsername() + " already exists.");
        // }
        // if (userRepository.existsByMatricula(user.getMatricula())) { // Se matricula for String e única
        //     throw new MatriculaAlreadyExistsException("Matricula " + user.getMatricula() + " already exists.");
        // }

        // Codificar a senha antes de salvar
        // user.setPassword(passwordEncoder.encode(user.getPassword()));
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
        // Aqui, você pode querer buscar o usuário existente primeiro para garantir que ele existe
        // e para aplicar atualizações parciais de forma controlada, possivelmente usando um DTO.
        // Se for atualizar a senha, não se esqueça de codificá-la.
        // Ex: if (updateRequestDto.getPassword() != null) {
        //          existingUser.setPassword(passwordEncoder.encode(updateRequestDto.getPassword()));
        //      }
        return userRepository.save(user); // O save também funciona como update se o ID existir
    }

    @Transactional(readOnly = true) // Bom para operações de leitura
    public User getUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public User getUserByMatricula(String matricula) { // Alterado para String se matricula for String
        return userRepository.findByMatricula(matricula) // Assegure que este método existe no UserRepository
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