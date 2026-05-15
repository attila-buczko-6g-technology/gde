package hu.blogplatform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;

/**
 * Kommentekhez tartozó kérés-/válasz-objektumok.
 */
public final class CommentDtos {

    private CommentDtos() {}

    public record CommentRequest(
            @NotBlank @Size(max = 2000) String content
    ) {}

    public record CommentResponse(
            Long id,
            String content,
            String authorUsername,
            Long authorId,
            Long postId,
            Instant createdAt
    ) {}
}
