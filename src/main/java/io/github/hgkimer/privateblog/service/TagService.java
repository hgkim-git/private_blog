package io.github.hgkimer.privateblog.service;

import io.github.hgkimer.privateblog.domain.entity.Tag;
import io.github.hgkimer.privateblog.persistence.jpa.PostTagRepository;
import io.github.hgkimer.privateblog.persistence.jpa.TagRepository;
import io.github.hgkimer.privateblog.web.dto.response.TagResponseDto;
import io.github.hgkimer.privateblog.web.exception.ErrorCode;
import io.github.hgkimer.privateblog.web.exception.ResourceNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class TagService {

  private final TagRepository tagRepository;
  private final PostTagRepository postTagRepository;

  public Tag createTag(Tag tag) {
    return tagRepository.save(tag);
  }

  public void deleteTag(Long id) {
    if (!tagRepository.existsById(id)) {
      throw new ResourceNotFoundException(ErrorCode.TAG_NOT_FOUND, id.toString());
    }
    postTagRepository.deleteByTagId(id);
    tagRepository.deleteById(id);
  }

  public Tag updateTag(Long id, Tag updatedTag) {
    Tag tag = getTagById(id);
    tag.update(updatedTag.getName(), updatedTag.getSlug());
    return tag;
  }

  // @Transactional(readOnly = true) 조회용 쿼리 이므로 JPA가 스냅샷을 남기지 않도록 함
  @Transactional(readOnly = true)
  public Tag getTagById(Long id) {
    return tagRepository.findById(id)
        .orElseThrow(
            () -> new ResourceNotFoundException(ErrorCode.TAG_NOT_FOUND, id.toString()));
  }

  @Transactional(readOnly = true)
  public List<TagResponseDto> getAllTags() {
    return tagRepository.findAllWithPostCountOrderByName();
  }

}