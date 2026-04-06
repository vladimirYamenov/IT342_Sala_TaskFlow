package edu.cit.sala.TaskFlow.controller;

import edu.cit.sala.TaskFlow.dto.AuthResponse;
import edu.cit.sala.TaskFlow.dto.GoogleAuthRequest;
import edu.cit.sala.TaskFlow.dto.LoginRequest;
import edu.cit.sala.TaskFlow.dto.RegisterRequest;
import edu.cit.sala.TaskFlow.entity.User;
import edu.cit.sala.TaskFlow.service.AuthService;
import edu.cit.sala.TaskFlow.service.GoogleAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final GoogleAuthService googleAuthService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/oauth/google")
    public ResponseEntity<AuthResponse> googleAuth(@RequestBody GoogleAuthRequest request) {
        return ResponseEntity.ok(googleAuthService.authenticateWithGoogle(request.getIdToken()));
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponse> getCurrentUser(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(AuthResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .build());
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }
}