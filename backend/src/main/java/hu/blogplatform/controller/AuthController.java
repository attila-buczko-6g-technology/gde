package hu.blogplatform.controller;

import hu.blogplatform.dto.AuthDtos;
import hu.blogplatform.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Autentikációs végpontok: regisztráció és bejelentkezés.
 *
 * <p>Útvonal: {@code /api/auth}</p>
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Új felhasználó regisztrációja.
     *
     * <p><b>Végpont:</b> {@code POST /api/auth/register}</p>
     *
     * <p>A kérés törzsében felhasználónevet, email címet és jelszót vár.
     * Sikeres regisztráció esetén JWT tokent és a felhasználó adatait adja vissza.
     * Hibás kérés (foglalt név, hibás email) esetén 400-as választ küld.</p>
     *
     * @param request a regisztrációs adatok (felhasználónév, email, jelszó)
     * @return a frissen kiállított JWT token és a felhasználó adatai
     */
    @PostMapping("/register")
    public ResponseEntity<AuthDtos.AuthResponse> register(@Valid @RequestBody AuthDtos.RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    /**
     * Meglévő felhasználó bejelentkezése.
     *
     * <p><b>Végpont:</b> {@code POST /api/auth/login}</p>
     *
     * <p>A kérés törzsében felhasználónevet és jelszót vár. Sikeres
     * azonosítás esetén JWT tokent ad vissza, amelyet a kliens minden
     * további hívásnál az {@code Authorization: Bearer ...} fejlécben küld.
     * Hibás belépés esetén 401-es választ küld.</p>
     *
     * @param request a belépési adatok (felhasználónév, jelszó)
     * @return a frissen kiállított JWT token és a felhasználó adatai
     */
    @PostMapping("/login")
    public ResponseEntity<AuthDtos.AuthResponse> login(@Valid @RequestBody AuthDtos.LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
