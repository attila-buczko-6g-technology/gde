package hu.blogplatform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;

/**
 * Bejegyzésekhez tartozó kérés-/válasz-objektumok.
 */
public final class PostDtos {

    private PostDtos() {}

    public record PostRequest(
            @NotBlank @Size(max = 200) String title,
            @NotBlank String content
    ) {}

    public record PostResponse(
            Long id,
            String title,
            String content,
            String authorUsername,
            Long authorId,
            Instant createdAt,
            Instant updatedAt
    ) {}
}
