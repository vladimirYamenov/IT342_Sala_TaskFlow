package edu.cit.sala.TaskFlow.controller;

import edu.cit.sala.TaskFlow.dto.LoginRequest;
import edu.cit.sala.TaskFlow.dto.RegisterRequest;
import edu.cit.sala.TaskFlow.entity.User;
import edu.cit.sala.TaskFlow.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public String register(@RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public User login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }
}