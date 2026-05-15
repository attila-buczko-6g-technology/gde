package hu.blogplatform.service;

import hu.blogplatform.dto.PostDtos;
import hu.blogplatform.entity.Post;
import hu.blogplatform.entity.User;
import hu.blogplatform.exception.ApiExceptions;
import hu.blogplatform.repository.PostRepository;
import hu.blogplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Bejegyzések üzleti logikáját tartalmazó szolgáltatás.
 */
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<PostDtos.PostResponse> list(int page, int size) {
        return postRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size))
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public PostDtos.PostResponse get(Long id) {
        Post p = postRepository.findById(id)
                .orElseThrow(() -> new ApiExceptions.NotFoundException("Bejegyzés nem található: " + id));
        return toResponse(p);
    }

    @Transactional
    public PostDtos.PostResponse create(PostDtos.PostRequest req, String username) {
        User author = userRepository.findByUsername(username)
                .orElseThrow(() -> new ApiExceptions.UnauthorizedException("Ismeretlen felhasználó."));
        Post post = Post.builder()
                .title(req.title())
                .content(req.content())
                .author(author)
                .build();
        return toResponse(postRepository.save(post));
    }

    @Transactional
    public PostDtos.PostResponse update(Long id, PostDtos.PostRequest req, String username) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ApiExceptions.NotFoundException("Bejegyzés nem található: " + id));
        if (!post.getAuthor().getUsername().equals(username)) {
            throw new ApiExceptions.ForbiddenException("Csak a saját bejegyzésedet szerkesztheted.");
        }
        post.setTitle(req.title());
        post.setContent(req.content());
        return toResponse(postRepository.save(post));
    }

    @Transactional
    public void delete(Long id, String username) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ApiExceptions.NotFoundException("Bejegyzés nem található: " + id));
        if (!post.getAuthor().getUsername().equals(username)) {
            throw new ApiExceptions.ForbiddenException("Csak a saját bejegyzésedet törölheted.");
        }
        postRepository.delete(post);
    }

    private PostDtos.PostResponse toResponse(Post p) {
        return new PostDtos.PostResponse(
                p.getId(),
                p.getTitle(),
                p.getContent(),
                p.getAuthor().getUsername(),
                p.getAuthor().getId(),
                p.getCreatedAt(),
                p.getUpdatedAt()
        );
    }
}
