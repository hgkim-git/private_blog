package io.github.hgkimer.privateblog.persistence.jpa;

import io.github.hgkimer.privateblog.domain.entity.Tag;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    List<Tag> findTagByIdIn(List<Long> ids);


}
