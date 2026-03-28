package edu.cit.sala.TaskFlow.service;

import edu.cit.sala.TaskFlow.entity.User;
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

    public String register(RegisterRequest request) {

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
                .build();

        userRepository.save(newUser);

        return "User registered successfully";
    }

    public User login(LoginRequest request) {

        User authenticatedUser = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if(!passwordEncoder.matches(request.getPassword(), authenticatedUser.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        return authenticatedUser;
    }
}
