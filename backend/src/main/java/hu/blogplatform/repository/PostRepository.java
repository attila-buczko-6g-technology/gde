package hu.blogplatform.repository;

import hu.blogplatform.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Bejegyzéseket kezelő Spring Data JPA repository.
 */
@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    Page<Post> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<Post> findAllByAuthor_IdOrderByCreatedAtDesc(Long authorId, Pageable pageable);
}
