package hu.blogplatform.controller;

import hu.blogplatform.dto.CommentDtos;
import hu.blogplatform.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Kommenteket kezelő REST végpontok egy adott bejegyzéshez.
 *
 * <p>Útvonal: {@code /api/posts/{postId}/comments}</p>
 */
@RestController
@RequestMapping("/api/posts/{postId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /**
     * Egy adott bejegyzéshez tartozó kommentek listázása.
     *
     * <p><b>Végpont:</b> {@code GET /api/posts/{postId}/comments}</p>
     *
     * <p>Bárki számára elérhető, autentikáció nem szükséges.</p>
     *
     * @param postId a bejegyzés azonosítója
     * @return a bejegyzéshez tartozó kommentek időrendben (legrégebbitől)
     */
    @GetMapping
    public ResponseEntity<List<CommentDtos.CommentResponse>> list(@PathVariable Long postId) {
        return ResponseEntity.ok(commentService.listForPost(postId));
    }

    /**
     * Új komment hozzáadása egy bejegyzéshez.
     *
     * <p><b>Végpont:</b> {@code POST /api/posts/{postId}/comments}</p>
     *
     * <p>Csak bejelentkezett felhasználó hívhatja. A komment szerzője
     * a bejelentkezett felhasználó lesz.</p>
     *
     * @param postId  a bejegyzés azonosítója, amelyhez a komment tartozik
     * @param request a komment tartalma
     * @param user    a bejelentkezett felhasználó
     * @return a létrehozott komment adatai
     */
    @PostMapping
    public ResponseEntity<CommentDtos.CommentResponse> add(@PathVariable Long postId,
                                                            @Valid @RequestBody CommentDtos.CommentRequest request,
                                                            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(commentService.addComment(postId, request, user.getUsername()));
    }
}
