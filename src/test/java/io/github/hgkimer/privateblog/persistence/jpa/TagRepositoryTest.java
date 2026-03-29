package io.github.hgkimer.privateblog.persistence.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.hgkimer.privateblog.domain.entity.Tag;
import io.github.hgkimer.privateblog.support.domain.entity.TagFixtureFactory;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class TagRepositoryTest {

  @Autowired
  EntityManager entityManager;
  @Autowired
  private TagRepository tagRepository;
  private Tag tag;

  @BeforeEach
  void setup() {
    tag = TagFixtureFactory.createFixture();
  }


  @Test
  @DisplayName("태그 생성 테스트")
  void createTag() {
    assertThat(tag.getId()).isNull();
    tagRepository.save(tag);
    assertThat(tag.getId()).isNotNull();
  }

  @Test
  @DisplayName("태그 삭제 테스트")
  void deleteTag() {
    tagRepository.save(tag);
    tagRepository.delete(tag);
    entityManager.flush();

    assertThat(tagRepository.findAll()).isEmpty();
  }

  @Test
  @DisplayName("태그 수정 테스트")
  void updateTag() {
    tagRepository.save(tag);
    tag.update("test2", "test2");
    entityManager.flush();

    assertThat(tag)
        .hasFieldOrPropertyWithValue("name", "test2")
        .hasFieldOrPropertyWithValue("slug", "test2");
  }

}