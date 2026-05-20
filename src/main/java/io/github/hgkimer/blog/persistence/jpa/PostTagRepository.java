package io.github.hgkimer.blog.persistence.jpa;

import io.github.hgkimer.blog.domain.entity.PostTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PostTagRepository extends JpaRepository<PostTag, Long> {

  @Modifying
  @Query("DELETE FROM PostTag pt WHERE pt.tag.id = :tagId")
  void deleteByTagId(Long tagId);
}
