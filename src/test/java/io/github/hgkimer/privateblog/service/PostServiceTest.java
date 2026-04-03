package io.github.hgkimer.privateblog.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import io.github.hgkimer.privateblog.domain.entity.Category;
import io.github.hgkimer.privateblog.domain.entity.Post;
import io.github.hgkimer.privateblog.domain.entity.Tag;
import io.github.hgkimer.privateblog.domain.entity.User;
import io.github.hgkimer.privateblog.domain.enums.PostStatus;
import io.github.hgkimer.privateblog.persistence.jpa.CategoryRepository;
import io.github.hgkimer.privateblog.persistence.jpa.PostRepository;
import io.github.hgkimer.privateblog.persistence.jpa.TagRepository;
import io.github.hgkimer.privateblog.persistence.jpa.UserRepository;
import io.github.hgkimer.privateblog.support.domain.entity.CategoryFixtureFactory;
import io.github.hgkimer.privateblog.support.domain.entity.PostFixtureFactory;
import io.github.hgkimer.privateblog.support.domain.entity.TagFixtureFactory;
import io.github.hgkimer.privateblog.support.domain.entity.UserFixtureFactory;
import io.github.hgkimer.privateblog.support.web.dto.PostCreateDtoFixtureFactory;
import io.github.hgkimer.privateblog.web.dto.request.PostCreateDto;
import io.github.hgkimer.privateblog.web.dto.request.PostUpdateDto;
import io.github.hgkimer.privateblog.web.dto.response.PostDetailResponseDto;
import io.github.hgkimer.privateblog.web.dto.response.PostSummaryResponseDto;
import io.github.hgkimer.privateblog.web.exception.DuplicateResourceException;
import io.github.hgkimer.privateblog.web.exception.ResourceNotFoundException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

  private final String AUTHOR_EMAIL = "admin@example.com";

  @Mock
  private PostRepository postRepository;

  @Mock
  private CategoryRepository categoryRepository;

  @Mock
  private TagRepository tagRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private MarkdownService markdownService;

  @InjectMocks
  private PostService postService;

  private PostCreateDto postCreateDto;

  @BeforeEach
  void setUp() {
    postCreateDto = PostCreateDtoFixtureFactory.createPostCreateDto();
  }

  @Test
  @DisplayName("기본 게시글 생성 테스트")
  void testCreateNaivePost() {
    // given
    User author = UserFixtureFactory.createAdminFixture(AUTHOR_EMAIL);
    given(markdownService.convertToHtml(anyString())).willReturn("<p>테스트 내용</p>");
    given(userRepository.findByEmail(AUTHOR_EMAIL)).willReturn(Optional.of(author));
    given(postRepository.existsBySlug(postCreateDto.slug())).willReturn(false);

    // 명시적으로 첫 번째 인자를 반환하도록 설정
    given(postRepository.save(any(Post.class))).will(returnsFirstArg());

    // when
    Post result = postService.createPost(postCreateDto, AUTHOR_EMAIL);

    // then
    ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
    then(postRepository).should().save(postCaptor.capture());

    Post savedPost = postCaptor.getValue();
    assertThat(savedPost.getTitle()).isEqualTo(postCreateDto.title());
    assertThat(result).isEqualTo(savedPost);
  }

  @Test
  @DisplayName("카테고리와 태그가 포함된 게시글이 정상 저장되어야 한다")
  void testCreatePostWithCategoryAndTags() {
    // given
    User author = UserFixtureFactory.createAdminFixture(AUTHOR_EMAIL);
    Category category = CategoryFixtureFactory.createFixture();
    Long categoryId = 10L;
    ReflectionTestUtils.setField(category, "id", categoryId);

    Tag tag1 = TagFixtureFactory.createFixture("태그1", "tag1");
    Tag tag2 = TagFixtureFactory.createFixture("태그2", "tag2");
    ReflectionTestUtils.setField(tag1, "id", 1L);
    ReflectionTestUtils.setField(tag2, "id", 2L);
    List<Long> tagIds = List.of(1L, 2L);

    PostCreateDto dto = new PostCreateDto(categoryId, "제목", "내용", "요약", "slug", "PUBLISHED",
        tagIds);

    given(markdownService.convertToHtml(anyString())).willReturn("<p>테스트 내용</p>");
    given(categoryRepository.findById(categoryId)).willReturn(Optional.of(category));
    given(tagRepository.findTagByIdIn(tagIds)).willReturn(List.of(tag1, tag2));
    given(userRepository.findByEmail(AUTHOR_EMAIL)).willReturn(Optional.of(author));
    given(postRepository.existsBySlug("slug")).willReturn(false);

    // 명시적으로 첫 번째 인자를 반환하도록 설정
    given(postRepository.save(any(Post.class))).will(returnsFirstArg());

    // when
    Post result = postService.createPost(dto, AUTHOR_EMAIL);

    // then
    ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
    then(postRepository).should().save(postCaptor.capture());

    Post savedPost = postCaptor.getValue();
    assertThat(savedPost.getTitle()).isEqualTo("제목");
    assertThat(savedPost.getCategory()).isEqualTo(category);
    assertThat(savedPost.getPostTags()).hasSize(2);
    assertThat(category.getPostCount()).isEqualTo(1);

    assertThat(result).isEqualTo(savedPost);
  }

  @Test
  @DisplayName("게시글 생성 실패: 중복된 슬러그가 존재하면 예외가 발생한다")
  void testCreatePostWithDuplicateSlug() {
    // given
    given(postRepository.existsBySlug(anyString())).willReturn(true);

    // when & then
    assertThatThrownBy(() -> postService.createPost(postCreateDto, AUTHOR_EMAIL))
        .isInstanceOf(DuplicateResourceException.class);
  }

  @Test
  @DisplayName("게시글 생성 실패: 존재하지 않는 카테고리 ID일 경우 예외가 발생한다")
  void testCreatePostWithCategoryNotFound() {
    // given
    Long categoryId = 1L;
    PostCreateDto dto = new PostCreateDto(categoryId, "제목", "내용", "요약", "slug", "PUBLISHED",
        List.of());
    given(categoryRepository.findById(categoryId)).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> postService.createPost(dto, AUTHOR_EMAIL))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  @DisplayName("게시글 정보 업데이트 시 필드 값이 변경되어야 한다.")
  void testUpdatePost() {
    // given
    Long postId = 1L;
    Category oldCategory = CategoryFixtureFactory.createFixture("Old", "old");
    Category newCategory = CategoryFixtureFactory.createFixture("New", "new");
    ReflectionTestUtils.setField(oldCategory, "id", 1L);
    ReflectionTestUtils.setField(newCategory, "id", 2L);
    oldCategory.increasePostCount(); // 초기 카운트 1

    Post post = PostFixtureFactory.createFixture(oldCategory,
        UserFixtureFactory.createAdminFixture());
    ReflectionTestUtils.setField(post, "id", postId);

    PostUpdateDto updateDto = new PostUpdateDto(2L, "새 제목", "새 내용", "새 요약", "new-slug", "PUBLISHED",
        List.of());

    given(markdownService.convertToHtml(anyString())).willReturn("<p>테스트 내용</p>");
    given(categoryRepository.findById(2L)).willReturn(Optional.of(newCategory));
    given(postRepository.findByIdWithDetails(postId)).willReturn(post);
    given(postRepository.existsBySlug("new-slug")).willReturn(false);

    // when
    postService.updatePost(postId, updateDto);

    // then
    assertThat(post.getTitle()).isEqualTo("새 제목");
    assertThat(post.getSlug()).isEqualTo("new-slug");
    assertThat(post.getCategory()).isEqualTo(newCategory);
    assertThat(oldCategory.getPostCount()).isZero();
    assertThat(newCategory.getPostCount()).isEqualTo(1);
  }

  @Test
  @DisplayName("게시글 삭제 테스트")
  void testDeletePost() {
    // given
    Long postId = 1L;
    given(postRepository.existsById(postId)).willReturn(true);

    // when
    postService.deletePost(postId);

    // then
    then(postRepository).should().deleteById(postId);
  }

  @Test
  @DisplayName("게시글 삭제 실패: 존재하지 않는 게시글일 경우 예외가 발생한다")
  void testDeletePostNotFound() {
    // given
    Long postId = 1L;
    given(postRepository.existsById(postId)).willReturn(false);

    // when & then
    assertThatThrownBy(() -> postService.deletePost(postId))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  @DisplayName("슬러그로 게시글 상세 조회 시 게시글을 반환하고 조회수가 증가해야 한다.")
  void testGetPostBySlug() {
    // given
    String slug = "test-slug";
    Post post = PostFixtureFactory.createFixture();
    ReflectionTestUtils.setField(post, "slug", slug);
    ReflectionTestUtils.setField(post, "id", 1L);
    given(postRepository.findBySlugWithDetails(slug)).willReturn(Optional.of(post));
    given(postRepository.findByIdWithDetails(post.getId())).willReturn(post);

    // when
    PostDetailResponseDto result = postService.getPostBySlug(slug);

    // then
    assertThat(result).isNotNull();
    assertThat(result.id()).isEqualTo(1L);
    then(postRepository).should().increaseViewCount(post.getId());
    assertThat(post.getViewCount()).isEqualTo(1);
  }

  @Test
  @DisplayName("전체 게시글 조회 시 페이징된 결과를 반환해야 한다.")
  void testGetAllPosts() {
    // given
    Pageable pageable = PageRequest.of(0, 10);
    List<Post> posts = List.of(PostFixtureFactory.createFixture());
    Page<Post> page = new PageImpl<>(posts, pageable, 1);

    given(postRepository.findAllPosts(null, PostStatus.PUBLISHED, null, pageable)).willReturn(page);

    // when
    Page<PostSummaryResponseDto> result = postService.getAllPosts(null, PostStatus.PUBLISHED, null,
        pageable);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(1);
    then(postRepository).should().findAllPosts(null, PostStatus.PUBLISHED, null, pageable);
  }

}
