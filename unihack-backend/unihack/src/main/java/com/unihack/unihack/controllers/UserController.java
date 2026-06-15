package com.unihack.unihack.controllers;

import com.unihack.unihack.dtos.RankingUserDto;
import com.unihack.unihack.dtos.UserProfileDTO;
import com.unihack.unihack.services.UsersService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UsersService usersService;

    public UserController(UsersService usersService) {
        this.usersService = usersService;
    }

    @GetMapping("/ranking")
    @PreAuthorize("isAuthenticated()")
    public List<RankingUserDto> getRanking() {
        return usersService.getUsersForRanking()
                .stream()
                .map(RankingUserDto::new)
                .toList();
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserProfileDTO> getMe() {
        String matricula = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(usersService.getUserProfileByUsername(matricula));
    }
}
