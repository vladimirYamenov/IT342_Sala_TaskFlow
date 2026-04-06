package edu.cit.sala.TaskFlow.service;

import edu.cit.sala.TaskFlow.config.JwtUtil;
import edu.cit.sala.TaskFlow.entity.User;
import edu.cit.sala.TaskFlow.dto.AuthResponse;
import edu.cit.sala.TaskFlow.dto.LoginRequest;
import edu.cit.sala.TaskFlow.dto.RegisterRequest;
import edu.cit.sala.TaskFlow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;

    public AuthResponse register(RegisterRequest request) {

        if(userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }

        if(!request.getPassword().equals(request.getConfirmPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Passwords do not match");
        }

        User newUser = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .role("USER")
                .build();

        userRepository.save(newUser);

        emailService.sendWelcomeEmail(newUser.getEmail(), newUser.getFullName());

        String token = jwtUtil.generateToken(newUser.getId(), newUser.getEmail());

        return AuthResponse.builder()
                .token(token)
                .userId(newUser.getId())
                .email(newUser.getEmail())
                .fullName(newUser.getFullName())
                .role(newUser.getRole())
                .build();
    }

    public AuthResponse login(LoginRequest request) {

        User authenticatedUser = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if(!passwordEncoder.matches(request.getPassword(), authenticatedUser.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        String token = jwtUtil.generateToken(authenticatedUser.getId(), authenticatedUser.getEmail());

        return AuthResponse.builder()
                .token(token)
                .userId(authenticatedUser.getId())
                .email(authenticatedUser.getEmail())
                .fullName(authenticatedUser.getFullName())
                .role(authenticatedUser.getRole())
                .build();
    }

    public User getCurrentUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }
}
