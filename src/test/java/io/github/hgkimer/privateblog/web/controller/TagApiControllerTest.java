package io.github.hgkimer.privateblog.web.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import io.github.hgkimer.privateblog.domain.entity.Tag;
import io.github.hgkimer.privateblog.service.TagService;
import io.github.hgkimer.privateblog.web.dto.response.TagResponseDto;
import io.github.hgkimer.privateblog.web.exception.ErrorCode;
import io.github.hgkimer.privateblog.web.exception.ErrorResponse;
import io.github.hgkimer.privateblog.web.exception.FieldErrorResponse;
import io.github.hgkimer.privateblog.web.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@WebMvcTest(TagApiController.class)
class TagApiControllerTest {

    private final String uriRoot = "/tags";
    @Autowired
    private MockMvcTester mockMvcTester;
    @MockitoBean
    private TagService tagService;
    private Tag tag;

    @BeforeEach
    void setUp() {
        tag = Tag.builder().name("test").slug("test").build();
    }

    @Test
    void givenValidJSON_whenCreateTag_thenResponseCreated() {
        // given
        given(tagService.createTag(any())).willReturn(tag);
        String json = """
            {
             "name":"test",
             "slug":"test"
            }
            """;
        mockMvcTester.post().uri(uriRoot)
            .content(json)
            .contentType(MediaType.APPLICATION_JSON)
            .exchange()
            .assertThat()
            .hasStatus(HttpStatus.CREATED)
            .bodyJson().convertTo(TagResponseDto.class)
            .satisfies(response -> {
                assertThat(response.name()).isEqualTo(tag.getName());
                assertThat(response.slug()).isEqualTo(tag.getSlug());
            });
    }

    @Test
    void givenInvalidSlug_whenCreateTag_thenThrowBadRequest() {
        String json = """
            {
             "name":"test",
             "slug":"이건 잘 못된 slug"
            }
            """;
        mockMvcTester.post().uri(uriRoot)
            .content(json)
            .contentType(MediaType.APPLICATION_JSON)
            .exchange()
            .assertThat()
            .hasStatus(HttpStatus.BAD_REQUEST)
            .bodyJson().convertTo(ErrorResponse.class)
            .satisfies(response -> {
                assertThat(response.fieldErrors()).isNotEmpty().hasSize(1);
                FieldErrorResponse fieldError = response.fieldErrors().get(0);
                assertThat(fieldError.field()).isEqualTo("slug");
            })
        ;
    }

    @Test
    void givenId_whenDelete_thenResponseNoContent() {
        mockMvcTester.delete().uri(uriRoot + "/1")
            .exchange()
            .assertThat()
            .hasStatus(HttpStatus.NO_CONTENT);
    }

    @Test
    void givenId_whenUpdate_thenResponseOk() {
        Tag updated = Tag.builder().name("updated name").slug("updated-slug").build();
        given(tagService.updateTag(any(), any())).willReturn(updated);
        String json = """
            {
             "name":"updated name",
             "slug":"updated-slug"
            }
            """;
        mockMvcTester.patch().uri(uriRoot + "/1")
            .content(json)
            .contentType(MediaType.APPLICATION_JSON)
            .exchange()
            .assertThat()
            .hasStatus(HttpStatus.OK)
            .bodyJson()
            .convertTo(TagResponseDto.class)
            .satisfies(response -> {
                assertThat(response.name()).isEqualTo(updated.getName());
                assertThat(response.slug()).isEqualTo(updated.getSlug());
            });
    }

    @Test
    void givenId_whenGetTag_thenResponseOk() {
        given(tagService.getTagById(any())).willReturn(tag);
        mockMvcTester.get().uri(uriRoot + "/1")
            .exchange()
            .assertThat()
            .hasStatus(HttpStatus.OK)
            .bodyJson()
            .convertTo(TagResponseDto.class)
            .satisfies(response -> {
                assertThat(response.name()).isEqualTo(tag.getName());
                assertThat(response.slug()).isEqualTo(tag.getSlug());
            });
    }

    @Test
    void givenWrongId_whenGetTag_thenThrowNotFound() {
        given(tagService.getTagById(any())).willThrow(new ResourceNotFoundException(
            ErrorCode.TAG_NOT_FOUND));
        mockMvcTester.get().uri(uriRoot + "/100")
            .exchange()
            .assertThat()
            .hasStatus(HttpStatus.NOT_FOUND)
            .bodyJson().convertTo(ErrorResponse.class)
            .satisfies(response -> {
                assertThat(response.code()).isEqualTo(ErrorCode.TAG_NOT_FOUND.getCode());
                assertThat(response.message()).isEqualTo(ErrorCode.TAG_NOT_FOUND.getMessage());
            })
        ;
    }

}
