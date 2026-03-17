package io.github.hgkimer.privateblog.persistence.jpa;

import io.github.hgkimer.privateblog.domain.entity.Category;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

  Optional<Category> findBySlug(String slug);

  List<Category> findAllByOrderByDisplayOrderAscCreatedAtDesc();

}
