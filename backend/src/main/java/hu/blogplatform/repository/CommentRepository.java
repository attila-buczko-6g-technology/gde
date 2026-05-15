package hu.blogplatform.repository;

import hu.blogplatform.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Kommenteket kezelő Spring Data JPA repository.
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findAllByPost_IdOrderByCreatedAtAsc(Long postId);
}
