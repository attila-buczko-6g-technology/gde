package hu.blogplatform.service;

import hu.blogplatform.dto.AuthDtos;
import hu.blogplatform.entity.User;
import hu.blogplatform.exception.ApiExceptions;
import hu.blogplatform.repository.UserRepository;
import hu.blogplatform.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Unit tesztek az AuthService regisztráció / belépés logikájához.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;

    @InjectMocks private AuthService authService;

    @Test
    void register_succeedsForNewUser() {
        when(userRepository.existsByUsername("uj")).thenReturn(false);
        when(userRepository.existsByEmail("uj@x.hu")).thenReturn(false);
        when(passwordEncoder.encode("jelszo123")).thenReturn("HASH");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(7L);
            return u;
        });
        when(jwtService.generateToken("uj", 7L)).thenReturn("token-xyz");

        AuthDtos.AuthResponse res = authService.register(
                new AuthDtos.RegisterRequest("uj", "uj@x.hu", "jelszo123"));

        assertThat(res.token()).isEqualTo("token-xyz");
        assertThat(res.username()).isEqualTo("uj");
        assertThat(res.userId()).isEqualTo(7L);
    }

    @Test
    void register_failsWhenUsernameTaken() {
        when(userRepository.existsByUsername("foglalt")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(
                new AuthDtos.RegisterRequest("foglalt", "a@b.hu", "jelszo123")))
                .isInstanceOf(ApiExceptions.BadRequestException.class)
                .hasMessageContaining("foglalt");
    }

    @Test
    void login_failsWithWrongPassword() {
        User u = User.builder().id(1L).username("a").passwordHash("HASH").build();
        when(userRepository.findByUsername("a")).thenReturn(Optional.of(u));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(
                new AuthDtos.LoginRequest("a", "rosszJelszo")))
                .isInstanceOf(BadCredentialsException.class);
    }
}
