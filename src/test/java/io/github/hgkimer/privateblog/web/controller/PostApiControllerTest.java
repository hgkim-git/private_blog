package io.github.hgkimer.privateblog.web.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

import io.github.hgkimer.privateblog.domain.entity.Category;
import io.github.hgkimer.privateblog.domain.entity.Post;
import io.github.hgkimer.privateblog.domain.entity.PostTag;
import io.github.hgkimer.privateblog.domain.entity.Tag;
import io.github.hgkimer.privateblog.domain.entity.User;
import io.github.hgkimer.privateblog.domain.enums.PostStatus;
import io.github.hgkimer.privateblog.domain.enums.UserRole;
import io.github.hgkimer.privateblog.security.JwtTokenProvider;
import io.github.hgkimer.privateblog.service.PostService;
import io.github.hgkimer.privateblog.web.dto.response.PostDetailResponseDto;
import io.github.hgkimer.privateblog.web.dto.response.PostSummaryResponseDto;
import io.github.hgkimer.privateblog.web.exception.ErrorCode;
import io.github.hgkimer.privateblog.web.exception.ErrorResponse;
import io.github.hgkimer.privateblog.web.exception.FieldErrorResponse;
import io.github.hgkimer.privateblog.web.exception.ResourceNotFoundException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@WebMvcTest(PostApiController.class)
class PostApiControllerTest {

  private final String uriRoot = "/api/posts";
  @Autowired
  private MockMvcTester mockMvcTester;
  @MockitoBean
  private PostService postService;
  @MockitoBean
  private JwtTokenProvider jwtTokenProvider;
  private Post post;

  @BeforeEach
  void setUp() {
    User author = User.builder()
        .email("test@example.com")
        .password("password")
        .role(UserRole.ADMIN)
        .build();

    Category category = Category.builder()
        .name("Test Category")
        .slug("test-category")
        .build();

    Tag tag = Tag.builder().name("java").slug("java").build();

    post = Post.builder()
        .author(author)
        .category(category)
        .title("Test Post")
        .content("Test Content")
        .summary("Test Summary")
        .slug("test-post")
        .status(PostStatus.PUBLISHED)
        .build();

    PostTag postTag = PostTag.builder().post(post).tag(tag).build();
    post.addTags(List.of(postTag));
  }

  @Test
  void givenValidJSON_whenCreatePost_thenResponseCreated() {
    given(postService.createPost(any(), any())).willReturn(post);
    String json = """
        {
            "author": "test@example.com",
            "categoryId": 1,
            "title": "Test Post",
            "content": "Test Content",
            "summary": "Test Summary",
            "slug": "test-post",
            "status": "PUBLISHED",
            "tagsIds": [1]
        }
        """;
    mockMvcTester.post().uri(uriRoot)
        .content(json)
        .contentType(MediaType.APPLICATION_JSON)
        .exchange()
        .assertThat()
        .hasStatus(HttpStatus.CREATED)
        .bodyJson().convertTo(PostDetailResponseDto.class)
        .satisfies(response -> {
          assertThat(response.title()).isEqualTo(post.getTitle());
          assertThat(response.slug()).isEqualTo(post.getSlug());
          assertThat(response.author()).isEqualTo("test@example.com");
          assertThat(response.status()).isEqualTo("PUBLISHED");
        });
  }

  @Test
  void givenInvalidSlug_whenCreatePost_thenThrowBadRequest() {
    String json = """
        {
            "author": "test@example.com",
            "title": "Test Post",
            "content": "Test Content",
            "slug": "잘못된 slug",
            "status": "PUBLISHED",
            "tagsIds": []
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
          assertThat(response.fieldErrors()).isNotEmpty();
          List<String> fields = response.fieldErrors().stream()
              .map(FieldErrorResponse::field).toList();
          assertThat(fields).contains("slug");
        });
  }

  @Test
  void givenId_whenDeletePost_thenResponseNoContent() {
    mockMvcTester.delete().uri(uriRoot + "/1")
        .exchange()
        .assertThat()
        .hasStatus(HttpStatus.NO_CONTENT);
  }

  @Test
  void givenValidParam_whenUpdatePost_thenResponseOk() {
    Post updated = Post.builder()
        .author(post.getAuthor())
        .category(post.getCategory())
        .title("Updated Title")
        .content("Updated Content")
        .summary("Updated Summary")
        .slug("updated-post")
        .status(PostStatus.PUBLISHED)
        .build();
    Tag tag = Tag.builder().name("java").slug("java").build();
    updated.addTags(List.of(PostTag.builder().post(updated).tag(tag).build()));

    given(postService.updatePost(any(), any())).willReturn(updated);
    String json = """
        {
            "title": "Updated Title",
            "content": "Updated Content",
            "summary": "Updated Summary",
            "slug": "updated-post",
            "status": "PUBLISHED",
            "tagsIds": [1]
        }
        """;
    mockMvcTester.patch().uri(uriRoot + "/1")
        .content(json)
        .contentType(MediaType.APPLICATION_JSON)
        .exchange()
        .assertThat()
        .hasStatus(HttpStatus.OK)
        .bodyJson().convertTo(PostDetailResponseDto.class)
        .satisfies(response -> {
          assertThat(response.title()).isEqualTo("Updated Title");
          assertThat(response.slug()).isEqualTo("updated-post");
        });
  }

  @Test
  void givenSlug_whenGetPost_thenResponseOk() {
    PostDetailResponseDto responseDto = PostDetailResponseDto.from(post);
    given(postService.getPostBySlug(anyString())).willReturn(responseDto);

    mockMvcTester.get().uri(uriRoot + "/test-post")
        .exchange()
        .assertThat()
        .hasStatus(HttpStatus.OK)
        .bodyJson().convertTo(PostDetailResponseDto.class)
        .satisfies(response -> {
          assertThat(response.title()).isEqualTo(post.getTitle());
          assertThat(response.slug()).isEqualTo(post.getSlug());
        });
  }

  @Test
  void givenWrongSlug_whenGetPost_thenThrowNotFound() {
    given(postService.getPostBySlug(anyString())).willThrow(
        new ResourceNotFoundException(ErrorCode.POST_NOT_FOUND, "wrong-slug"));

    mockMvcTester.get().uri(uriRoot + "/wrong-slug")
        .exchange()
        .assertThat()
        .hasStatus(HttpStatus.NOT_FOUND)
        .bodyJson().convertTo(ErrorResponse.class)
        .satisfies(response -> assertThat(response.code()).isEqualTo(
            ErrorCode.POST_NOT_FOUND.getCode()));
  }

  @Test
  void givenInvalidSlug_whenGetPost_thenThrowBadRequest() {
    mockMvcTester.get().uri(uriRoot + "/INVALID_SLUG!")
        .exchange()
        .assertThat()
        .hasStatus(HttpStatus.BAD_REQUEST);
  }

  @Test
  void givenNoParam_whenGetAllPosts_thenResponseOk() {
    Page<PostSummaryResponseDto> page = new PageImpl<>(List.of());
    given(postService.getAllPosts(any(), any(), any(), any())).willReturn(page);

    mockMvcTester.get().uri(uriRoot)
        .exchange()
        .assertThat()
        .hasStatus(HttpStatus.OK);
  }
}
