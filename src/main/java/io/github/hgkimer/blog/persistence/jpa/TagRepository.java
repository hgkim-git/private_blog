package io.github.hgkimer.blog.persistence.jpa;

import io.github.hgkimer.blog.domain.entity.Tag;
import io.github.hgkimer.blog.web.dto.response.TagResponseDto;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

  List<Tag> findTagByIdIn(List<Long> ids);

  @Query("SELECT new io.github.hgkimer.blog.web.dto.response.TagResponseDto"
      + "(t.id, t.name, t.slug, COUNT(pt.id)) "
      + "FROM Tag t LEFT JOIN PostTag pt ON pt.tag = t "
      + "GROUP BY t.id, t.name, t.slug "
      + "ORDER BY t.name ASC")
  List<TagResponseDto> findAllWithPostCountOrderByName();

}
