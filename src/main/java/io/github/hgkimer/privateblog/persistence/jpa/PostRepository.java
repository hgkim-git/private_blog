package io.github.hgkimer.privateblog.persistence.jpa;

import io.github.hgkimer.privateblog.domain.entity.Post;
import io.github.hgkimer.privateblog.domain.enums.PostStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

  boolean existsBySlug(String slug);

  @Query("SELECT p FROM Post p "
      + "LEFT JOIN FETCH p.category "
      + "JOIN FETCH p.author "
      + "WHERE p.id = :id")
  Optional<Post> findByIdWithDetails(@Param("id") Long id);

  @Query("SELECT p FROM Post p "
      + "LEFT JOIN FETCH p.category "
      + "JOIN FETCH p.author "
      + "WHERE p.slug = :slug")
  Optional<Post> findBySlugWithDetails(@Param("slug") String slug);

  @Query(value = "SELECT p FROM Post p "
      + "LEFT JOIN FETCH p.category "
      + "JOIN FETCH p.author "
      + "WHERE (:categoryId IS NULL OR p.category.id = :categoryId) "
      + "AND (:status IS NULL OR p.status = :status) "
      + "AND (:keyword IS NULL OR "
      + "    (LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
      + "     LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%')))) ",
      countQuery = "SELECT COUNT(p) FROM Post p "
          + "WHERE (:categoryId IS NULL OR p.category.id = :categoryId) "
          + "AND (:status IS NULL OR p.status = :status) "
          + "AND (:keyword IS NULL OR "
          + "    (LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
          + "     LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%')))) ")
  Page<Post> findAllPosts(
      @Param("categoryId") Long categoryId,
      @Param("status") PostStatus status,
      @Param("keyword") String keyword,
      Pageable pageable);

  @Modifying
  @Query("UPDATE Post p SET p.viewCount = p.viewCount + 1 WHERE p.id = :id")
  void increaseViewCount(@Param("id") Long id);

  List<Post> findAllPostByStatus(PostStatus status);


}
