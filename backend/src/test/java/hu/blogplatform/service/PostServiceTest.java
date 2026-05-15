package hu.blogplatform.service;

import hu.blogplatform.dto.PostDtos;
import hu.blogplatform.entity.Post;
import hu.blogplatform.entity.User;
import hu.blogplatform.exception.ApiExceptions;
import hu.blogplatform.repository.PostRepository;
import hu.blogplatform.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tesztek a PostService osztályhoz, mockolt repository-kkal.
 */
@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PostService postService;

    private User author;
    private Post post;

    @BeforeEach
    void setUp() {
        author = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .passwordHash("hash")
                .createdAt(Instant.now())
                .build();

        post = Post.builder()
                .id(10L)
                .title("Teszt cím")
                .content("Teszt tartalom")
                .author(author)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Test
    void list_returnsPagedPosts() {
        Page<Post> page = new PageImpl<>(List.of(post));
        when(postRepository.findAllByOrderByCreatedAtDesc(any(PageRequest.class))).thenReturn(page);

        Page<PostDtos.PostResponse> result = postService.list(0, 10);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).title()).isEqualTo("Teszt cím");
        assertThat(result.getContent().get(0).authorUsername()).isEqualTo("testuser");
    }

    @Test
    void get_returnsPostWhenExists() {
        when(postRepository.findById(10L)).thenReturn(Optional.of(post));

        PostDtos.PostResponse result = postService.get(10L);

        assertThat(result.id()).isEqualTo(10L);
        assertThat(result.content()).isEqualTo("Teszt tartalom");
    }

    @Test
    void get_throwsNotFoundWhenMissing() {
        when(postRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.get(99L))
                .isInstanceOf(ApiExceptions.NotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void create_savesPostWithAuthenticatedAuthor() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(author));
        when(postRepository.save(any(Post.class))).thenAnswer(inv -> {
            Post p = inv.getArgument(0);
            p.setId(42L);
            p.setCreatedAt(Instant.now());
            p.setUpdatedAt(Instant.now());
            return p;
        });

        PostDtos.PostRequest req = new PostDtos.PostRequest("Új", "Tartalom");
        PostDtos.PostResponse res = postService.create(req, "testuser");

        assertThat(res.id()).isEqualTo(42L);
        assertThat(res.authorUsername()).isEqualTo("testuser");
        verify(postRepository).save(any(Post.class));
    }

    @Test
    void update_throwsForbiddenWhenNotOwner() {
        when(postRepository.findById(10L)).thenReturn(Optional.of(post));
        PostDtos.PostRequest req = new PostDtos.PostRequest("Új", "Tartalom");

        assertThatThrownBy(() -> postService.update(10L, req, "masikUser"))
                .isInstanceOf(ApiExceptions.ForbiddenException.class);

        verify(postRepository, never()).save(any());
    }
}
