package com.unihack.unihack.services;

import com.unihack.unihack.models.User;
import com.unihack.unihack.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;


@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String matricula) throws UsernameNotFoundException {
        // Busca o usuário pelo repositório usando a matrícula
        User user = userRepository.findByMatricula(matricula)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado com a matrícula: " + matricula));

        // Cria uma lista de permissões (GrantedAuthority) a partir do UserRole do usuário
        // O Spring Security espera que as roles comecem com "ROLE_" se você usar .hasRole() na configuração.
        // Ex: se user.getRole() for UserRole.ADMIN, a authority será "ROLE_ADMIN"
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().name()); //
        Collection<GrantedAuthority> authorities = Collections.singletonList(authority);

        // Retorna um objeto UserDetails do Spring Security.
        // O primeiro argumento é o "username" que o Spring Security vai usar (matrícula).
        // O segundo é a senha (já codificada do banco de dados).
        // O terceiro são as permissões/roles.
        return new org.springframework.security.core.userdetails.User(user.getMatricula(), user.getPassword(), authorities); //
    }
}