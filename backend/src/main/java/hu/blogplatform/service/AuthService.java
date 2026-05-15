package hu.blogplatform.service;

import hu.blogplatform.dto.AuthDtos;
import hu.blogplatform.entity.User;
import hu.blogplatform.exception.ApiExceptions;
import hu.blogplatform.repository.UserRepository;
import hu.blogplatform.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Felhasználói regisztrációért és belépésért felelős szolgáltatás.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public AuthDtos.AuthResponse register(AuthDtos.RegisterRequest req) {
        if (userRepository.existsByUsername(req.username())) {
            throw new ApiExceptions.BadRequestException("A felhasználónév már foglalt.");
        }
        if (userRepository.existsByEmail(req.email())) {
            throw new ApiExceptions.BadRequestException("Az email cím már regisztrálva van.");
        }
        User user = User.builder()
                .username(req.username())
                .email(req.email())
                .passwordHash(passwordEncoder.encode(req.password()))
                .build();
        user = userRepository.save(user);
        String token = jwtService.generateToken(user.getUsername(), user.getId());
        return new AuthDtos.AuthResponse(token, user.getUsername(), user.getId());
    }

    @Transactional(readOnly = true)
    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest req) {
        User user = userRepository.findByUsername(req.username())
                .orElseThrow(() -> new BadCredentialsException("Hibás felhasználónév vagy jelszó."));
        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Hibás felhasználónév vagy jelszó.");
        }
        String token = jwtService.generateToken(user.getUsername(), user.getId());
        return new AuthDtos.AuthResponse(token, user.getUsername(), user.getId());
    }
}
