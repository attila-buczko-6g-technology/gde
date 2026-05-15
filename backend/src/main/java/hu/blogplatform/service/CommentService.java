package hu.blogplatform.service;

import hu.blogplatform.dto.CommentDtos;
import hu.blogplatform.entity.Comment;
import hu.blogplatform.entity.Post;
import hu.blogplatform.entity.User;
import hu.blogplatform.exception.ApiExceptions;
import hu.blogplatform.repository.CommentRepository;
import hu.blogplatform.repository.PostRepository;
import hu.blogplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Kommentek üzleti logikáját kezelő szolgáltatás.
 */
@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<CommentDtos.CommentResponse> listForPost(Long postId) {
        if (!postRepository.existsById(postId)) {
            throw new ApiExceptions.NotFoundException("Bejegyzés nem található: " + postId);
        }
        return commentRepository.findAllByPost_IdOrderByCreatedAtAsc(postId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public CommentDtos.CommentResponse addComment(Long postId, CommentDtos.CommentRequest req, String username) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ApiExceptions.NotFoundException("Bejegyzés nem található: " + postId));
        User author = userRepository.findByUsername(username)
                .orElseThrow(() -> new ApiExceptions.UnauthorizedException("Ismeretlen felhasználó."));
        Comment c = Comment.builder()
                .post(post)
                .author(author)
                .content(req.content())
                .build();
        return toResponse(commentRepository.save(c));
    }

    private CommentDtos.CommentResponse toResponse(Comment c) {
        return new CommentDtos.CommentResponse(
                c.getId(),
                c.getContent(),
                c.getAuthor().getUsername(),
                c.getAuthor().getId(),
                c.getPost().getId(),
                c.getCreatedAt()
        );
    }
}
