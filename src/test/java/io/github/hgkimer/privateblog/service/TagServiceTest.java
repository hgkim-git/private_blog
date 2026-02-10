package io.github.hgkimer.privateblog.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import io.github.hgkimer.privateblog.domain.entity.Tag;
import io.github.hgkimer.privateblog.persistence.jpa.TagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TagServiceTest {

    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private TagService tagService;

    private Tag tag;

    @BeforeEach
    void setUp() {
        tag = Tag.builder().name("test").slug("test").build();
    }

    @Test
    void createTag() {
        given(tagRepository.save(any(Tag.class))).willReturn(tag);
        tag = tagService.createTag(tag);
        assertThat(tag).isNotNull();
        assertThat(tag.getName()).isEqualTo("test");
        assertThat(tag.getSlug()).isEqualTo("test");
    }

    @Test
    void deleteTag() {
        tagService.deleteTag(tag.getId());
        assertThat(tagRepository.findAll()).isEmpty();
    }
}