package io.github.hgkimer.privateblog.persistence.jpa;

import io.github.hgkimer.privateblog.domain.entity.Category;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

  Optional<Category> findBySlug(String slug);

  List<Category> findAllByOrderByDisplayOrderAscCreatedAtDesc();

  @Modifying(clearAutomatically = true)
  @Query("UPDATE Category c "
      + "SET c.postCount = ("
      + "SELECT COUNT(p.id) "
      + "FROM Post p "
      + "WHERE p.category = c "
      + "AND p.status = io.github.hgkimer.privateblog.domain.enums.PostStatus.PUBLISHED)")
  void updateCategoriesPostCounts();

}
