package edu.cit.sala.TaskFlow.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import edu.cit.sala.TaskFlow.config.JwtUtil;
import edu.cit.sala.TaskFlow.dto.AuthResponse;
import edu.cit.sala.TaskFlow.entity.User;
import edu.cit.sala.TaskFlow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class GoogleAuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Value("${google.client.id}")
    private String googleClientId;

    public AuthResponse authenticateWithGoogle(String idTokenString) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);

            if (idToken == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Google token");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            String fullName = (String) payload.get("name");

            User user = userRepository.findByEmail(email).orElseGet(() -> {
                User newUser = User.builder()
                        .email(email)
                        .password("")
                        .fullName(fullName != null ? fullName : email)
                        .role("USER")
                        .build();
                return userRepository.save(newUser);
            });

            String token = jwtUtil.generateToken(user.getId(), user.getEmail());

            return AuthResponse.builder()
                    .token(token)
                    .userId(user.getId())
                    .email(user.getEmail())
                    .fullName(user.getFullName())
                    .role(user.getRole())
                    .build();

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Google authentication failed");
        }
    }
}
