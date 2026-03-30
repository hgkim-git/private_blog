package io.github.hgkimer.privateblog.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import io.github.hgkimer.privateblog.domain.entity.Tag;
import io.github.hgkimer.privateblog.persistence.jpa.PostTagRepository;
import io.github.hgkimer.privateblog.persistence.jpa.TagRepository;
import io.github.hgkimer.privateblog.web.dto.response.TagResponseDto;
import io.github.hgkimer.privateblog.web.exception.ResourceNotFoundException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class TagServiceTest {

  @Mock
  private TagRepository tagRepository;

  @Mock
  private PostTagRepository postTagRepository;

  @InjectMocks
  private TagService tagService;

  private Tag tag;

  @BeforeEach
  void setUp() {
    tag = Tag.builder().name("Java").slug("java").build();
    ReflectionTestUtils.setField(tag, "id", 1L);
  }

  @Test
  @DisplayName("태그 생성 테스트: 새로운 태그가 정상적으로 저장되어야 한다")
  void testCreateTag() {
    // given
    given(tagRepository.save(any(Tag.class))).willReturn(tag);

    // when
    Tag result = tagService.createTag(tag);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getName()).isEqualTo("Java");
    then(tagRepository).should().save(any(Tag.class));
  }

  @Test
  @DisplayName("태그 수정 테스트: 존재하는 태그의 정보를 수정할 수 있어야 한다")
  void testUpdateTag() {
    // given
    given(tagRepository.findById(tag.getId())).willReturn(Optional.of(tag));
    Tag updateParam = Tag.builder().name("Spring").slug("spring").build();

    // when
    Tag result = tagService.updateTag(tag.getId(), updateParam);

    // then
    assertThat(result.getName()).isEqualTo("Spring");
    assertThat(result.getSlug()).isEqualTo("spring");
  }

  @Test
  @DisplayName("태그 수정 실패 테스트: 존재하지 않는 태그 수정 시 예외가 발생한다")
  void testUpdateNonExistingTag() {
    // given
    Long nonExistingId = 100L;
    Tag updateParam = Tag.builder().name("Spring").slug("spring").build();
    given(tagRepository.findById(nonExistingId)).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> tagService.updateTag(nonExistingId, updateParam))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  @DisplayName("태그 삭제 테스트: 존재하는 태그를 삭제하면 연관된 포스트 태그도 함께 삭제되어야 한다")
  void testDeleteTag() {
    // given
    given(tagRepository.existsById(tag.getId())).willReturn(true);

    // when
    tagService.deleteTag(tag.getId());

    // then
    then(postTagRepository).should().deleteByTagId(tag.getId());
    then(tagRepository).should().deleteById(tag.getId());
  }

  @Test
  @DisplayName("태그 삭제 실패 테스트: 존재하지 않는 태그 삭제 시 예외가 발생한다")
  void testDeleteNonExistingTag() {
    // given
    Long nonExistingId = 100L;
    given(tagRepository.existsById(nonExistingId)).willReturn(false);

    // when & then
    assertThatThrownBy(() -> tagService.deleteTag(nonExistingId))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  @DisplayName("태그 조회 테스트: ID로 특정 태그를 조회할 수 있어야 한다")
  void testGetTagById() {
    // given
    given(tagRepository.findById(tag.getId())).willReturn(Optional.of(tag));

    // when
    Tag result = tagService.getTagById(tag.getId());

    // then
    assertThat(result).isEqualTo(tag);
  }

  @Test
  @DisplayName("전체 태그 조회 테스트: 모든 태그 목록을 포스트 카운트와 함께 조회한다")
  void testGetAllTags() {
    // given
    List<TagResponseDto> tagResponses = List.of(
        new TagResponseDto(1L, "Java", "java", 5L),
        new TagResponseDto(2L, "Spring", "spring", 3L)
    );
    given(tagRepository.findAllWithPostCountOrderByName()).willReturn(tagResponses);

    // when
    List<TagResponseDto> result = tagService.getAllTags();

    // then
    assertThat(result).hasSize(2);
    assertThat(result.get(0).name()).isEqualTo("Java");
    then(tagRepository).should().findAllWithPostCountOrderByName();
  }
}
