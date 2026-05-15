package hu.blogplatform.controller;

import hu.blogplatform.dto.PostDtos;
import hu.blogplatform.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * Blogbejegyzéseket kezelő REST végpontok.
 *
 * <p>Útvonal: {@code /api/posts}</p>
 */
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    /**
     * Bejegyzések listázása lapozással, legújabbtól a legrégebbi felé.
     *
     * <p><b>Végpont:</b> {@code GET /api/posts?page=0&size=10}</p>
     *
     * <p>Bárki számára elérhető, nem igényel autentikációt.</p>
     *
     * @param page  a kért lap sorszáma (0-tól)
     * @param size  a lap mérete (alapértelmezett 10)
     * @return a bejegyzések adott lapja
     */
    @GetMapping
    public ResponseEntity<Page<PostDtos.PostResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(postService.list(page, size));
    }

    /**
     * Egy bejegyzés részleteinek lekérése azonosító alapján.
     *
     * <p><b>Végpont:</b> {@code GET /api/posts/{id}}</p>
     *
     * <p>Bárki számára elérhető. Ha a megadott azonosítóhoz nem
     * tartozik bejegyzés, 404-es választ ad.</p>
     *
     * @param id a bejegyzés azonosítója
     * @return a bejegyzés adatai
     */
    @GetMapping("/{id}")
    public ResponseEntity<PostDtos.PostResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(postService.get(id));
    }

    /**
     * Új blogbejegyzés létrehozása.
     *
     * <p><b>Végpont:</b> {@code POST /api/posts}</p>
     *
     * <p>Csak bejelentkezett felhasználó hívhatja. A bejegyzés szerzője
     * automatikusan a bejelentkezett felhasználó lesz.</p>
     *
     * @param request a bejegyzés címe és tartalma
     * @param user    a bejelentkezett felhasználó (Spring Security tölti ki)
     * @return a létrehozott bejegyzés adatai
     */
    @PostMapping
    public ResponseEntity<PostDtos.PostResponse> create(@Valid @RequestBody PostDtos.PostRequest request,
                                                         @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(postService.create(request, user.getUsername()));
    }

    /**
     * Saját bejegyzés szerkesztése.
     *
     * <p><b>Végpont:</b> {@code PUT /api/posts/{id}}</p>
     *
     * <p>Csak a bejegyzés szerzője módosíthatja. Más felhasználó próbálkozása
     * 403-as választ eredményez.</p>
     *
     * @param id      a bejegyzés azonosítója
     * @param request az új cím és tartalom
     * @param user    a bejelentkezett felhasználó
     * @return a frissített bejegyzés
     */
    @PutMapping("/{id}")
    public ResponseEntity<PostDtos.PostResponse> update(@PathVariable Long id,
                                                        @Valid @RequestBody PostDtos.PostRequest request,
                                                        @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(postService.update(id, request, user.getUsername()));
    }

    /**
     * Saját bejegyzés törlése.
     *
     * <p><b>Végpont:</b> {@code DELETE /api/posts/{id}}</p>
     *
     * <p>Csak a bejegyzés szerzője törölheti.</p>
     *
     * @param id   a bejegyzés azonosítója
     * @param user a bejelentkezett felhasználó
     * @return üres 204-es válasz sikeres törlés esetén
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                       @AuthenticationPrincipal UserDetails user) {
        postService.delete(id, user.getUsername());
        return ResponseEntity.noContent().build();
    }
}
