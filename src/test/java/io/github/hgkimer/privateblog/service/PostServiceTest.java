package io.github.hgkimer.privateblog.service;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.hgkimer.privateblog.domain.entity.Category;
import io.github.hgkimer.privateblog.domain.entity.Post;
import io.github.hgkimer.privateblog.domain.entity.PostTag;
import io.github.hgkimer.privateblog.domain.entity.Tag;
import io.github.hgkimer.privateblog.domain.entity.User;
import io.github.hgkimer.privateblog.domain.enums.PostStatus;
import io.github.hgkimer.privateblog.domain.enums.UserRole;
import io.github.hgkimer.privateblog.persistence.jpa.CategoryRepository;
import io.github.hgkimer.privateblog.persistence.jpa.PostRepository;
import io.github.hgkimer.privateblog.persistence.jpa.TagRepository;
import io.github.hgkimer.privateblog.persistence.jpa.UserRepository;
import io.github.hgkimer.privateblog.web.dto.request.PostCreateDto;
import io.github.hgkimer.privateblog.web.dto.request.PostUpdateDto;
import io.github.hgkimer.privateblog.web.dto.response.PostDetailResponseDto;
import io.github.hgkimer.privateblog.web.dto.response.PostSummaryResponseDto;
import io.github.hgkimer.privateblog.web.dto.response.TagResponseDto;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@SpringBootTest
class PostServiceTest {

  private final String AUTHOR = "admin@example.com";
  @Autowired
  private PostRepository postRepository;
  @Autowired
  private CategoryRepository categoryRepository;
  @Autowired
  private TagRepository tagRepository;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private PostService postService;
  private PostCreateDto postCreateDto;

  @BeforeEach
  void setUp() {
    User author = User.builder()
        .email(AUTHOR)
        .password("password")
        .role(UserRole.ADMIN)
        .build();
    userRepository.save(author);
    Category category = Category.builder()
        .name("카테고리1")
        .slug("category1")
        .displayOrder(1)
        .build();
    categoryRepository.save(category);

    postCreateDto = new PostCreateDto(category.getId(),
        "제목",
        "내용", "요약", "test-slug", "DRAFT",
        Collections.emptyList());
  }

  @AfterEach
  void tearDown() {
    postRepository.deleteAll();
    tagRepository.deleteAll();
    userRepository.deleteAll();
    categoryRepository.deleteAll();
  }

  @Test
  void createNaivePost() {
    Post post = postService.createPost(postCreateDto, AUTHOR);
    assertThat(post).isNotNull();
  }

  @Test
  void givenNewTags_whenPostCreated_thenSavedWithTags() {
    //given
    Tag tag1 = Tag.builder().name("태그1").slug("tag-slug-1").build();
    tagRepository.save(tag1);
    Tag tag2 = Tag.builder().name("태그2").slug("tag-slug-2").build();
    tagRepository.save(tag2);
    List<Long> tagsIds = List.of(tag1.getId(), tag2.getId());
    postCreateDto = new PostCreateDto(postCreateDto.categoryId(),
        postCreateDto.title(), postCreateDto.content(), postCreateDto.summary(),
        postCreateDto.slug(), postCreateDto.status(), tagsIds);

    //when
    Post savedPost = postService.createPost(postCreateDto, AUTHOR);

    // then
    List<PostTag> postTags = savedPost.getPostTags();
    assertThat(postTags).hasSize(2);
    assertThat(postTags).allMatch(pt -> {
      Tag tag = pt.getTag();
      return tag.getName().equals(tag1.getName()) || tag.getName().equals(tag2.getName());
    });
  }

  @Test
  void deletePost() {
    Post saved = postService.createPost(postCreateDto, AUTHOR);
    Post found = postRepository.findByIdWithDetails(saved.getId());
    postService.deletePost(found.getId());
    assertThat(postRepository.findAll()).isEmpty();
  }

  @Test
  void givenNewParams_whenPostUpdated_thenChanged() {
    // given
    Post saved = postService.createPost(postCreateDto, AUTHOR);
    PostUpdateDto param = new PostUpdateDto(postCreateDto.categoryId(),
        "새로운 제목", "새로운 본문", "새로운 요약",
        "new-slug", PostStatus.PUBLISHED.name(), List.of()
    );

    // when
    saved = postService.updatePost(saved.getId(), param);

    // then
    assertThat(saved).usingRecursiveComparison()
        .ignoringFields("id",
            "category",
            "author",
            "postTags",
            "status",
            "contentHtml",
            "viewCount",
            "createdAt",
            "updatedAt")
        .isEqualTo(param);
    assertThat(saved.getStatus()).isEqualTo(PostStatus.PUBLISHED);
  }

  @Test
  void givenNewCategory_whenPostUpdated_thenSavedWithNewCategory() {
    // given
    Category newCategory = Category.builder().name("새로운 카테고리").slug("new-category").build();
    categoryRepository.save(newCategory);
    Post saved = postService.createPost(postCreateDto, AUTHOR);
    // when
    saved = postService.updatePost(saved.getId(),
        new PostUpdateDto(newCategory.getId(),
            saved.getTitle(), saved.getContent(), saved.getSummary(),
            saved.getSlug(), saved.getStatus().name(),
            saved.getPostTags().stream().map(pt -> pt.getTag().getId()).toList()));
    // then
    assertThat(saved.getCategory().getName()).isEqualTo(newCategory.getName());
  }

  @Test
  void givenTagChange_whenPostUpdated_thenSavedWithNewTags() {
    // given
    Tag tag1 = Tag.builder().name("태그1").slug("tag-slug-1").build();
    tagRepository.save(tag1);
    Tag tag2 = Tag.builder().name("태그2").slug("tag-slug-2").build();
    tagRepository.save(tag2);
    List<Long> tagIds = List.of(
        tag1.getId(),
        tag2.getId()
    );
    Post saved = postService.createPost(new PostCreateDto(postCreateDto.categoryId(),
        postCreateDto.title(), postCreateDto.content(),
        postCreateDto.summary(),
        postCreateDto.slug(), postCreateDto.status(), tagIds), AUTHOR);
    Tag newTag = Tag.builder().name("새로운 태그").slug("new-tag").build();
    tagRepository.save(newTag);

    // when
    saved = postService.updatePost(saved.getId(),
        new PostUpdateDto(saved.getCategory().getId(),
            saved.getTitle(),
            saved.getContent(),
            saved.getSummary(),
            saved.getSlug(),
            saved.getStatus().name(),
            List.of(newTag.getId())));

    // then
    assertThat(saved.getPostTags()).hasSize(1)
        .allSatisfy(pt -> assertThat(pt.getId()).isNotNull());
    assertThat(tagRepository.findAll()).hasSize(3);
    Long id = saved.getId();
    assertThat(saved.getPostTags()).allSatisfy(
        pt -> assertThat(pt.getPost().getId()).isEqualTo(id));
  }

  @Test
  void givenSlug_whenGetPostWithDetails_thenFound() {
    Post post = postService.createPost(postCreateDto, AUTHOR);
    PostDetailResponseDto found = postService.getPostBySlug(post.getSlug());
    assertThat(found).isNotNull();
    assertThat(found.id()).isEqualTo(post.getId());
    assertThat(found.slug()).isEqualTo(post.getSlug());
    assertThat(found.viewCount()).isEqualTo(1);
  }

  @Test
  void givenTagsExists_whenFindPost_thenReturnWithDetails() {
    Long categoryId = postCreateDto.categoryId();
    Tag tag = tagRepository.save(Tag.builder().name("test-tag").slug("test-slug").build());
    postCreateDto = new PostCreateDto(
        categoryId,
        postCreateDto.title(),
        postCreateDto.content(),
        postCreateDto.summary(),
        postCreateDto.slug(),
        postCreateDto.status(),
        List.of(tag.getId())
    );

    Post post = postService.createPost(postCreateDto, AUTHOR);
    PostDetailResponseDto found = postService.getPostBySlug(post.getSlug());
    assertThat(found).isNotNull();
    assertThat(found.id()).isEqualTo(post.getId());
    assertThat(found.slug()).isEqualTo(post.getSlug());
    assertThat(found.viewCount()).isEqualTo(1);

    List<TagResponseDto> tags = found.tags();
    assertThat(tags).hasSize(1);
    assertThat(tags.get(0).name()).isEqualTo(tag.getName());
    assertThat(tags.get(0).slug()).isEqualTo(tag.getSlug());
  }

  @Test
  void given5PostCreated_whenRequestedAllPost_thenReturnPageOrderedByCreatedDate() {
    for (int i = 0; i < 5; i++) {
      postService.createPost(
          new PostCreateDto(postCreateDto.categoryId(),
              postCreateDto.title(), postCreateDto.content(), postCreateDto.summary(),
              "test-slug" + i, PostStatus.PUBLISHED.name(), List.of()), AUTHOR);
    }
    Sort sort = Sort.by(Sort.Direction.fromString("DESC"), "createdAt");
    Pageable pageable = PageRequest.of(0, 10, sort);
    Page<PostSummaryResponseDto> page = postService.getAllPosts(null, PostStatus.PUBLISHED, null,
        pageable);
    assertThat(page).isNotNull();
    assertThat(page.getTotalElements()).isEqualTo(5);
    assertThat(page.getTotalPages()).isEqualTo(1);
    assertThat(page.getContent()).hasSize(5);
  }

  @Test
  void givenCategory_whenSearch_thenReturnPageSortedByCategory() {
    Category category = categoryRepository.findById(postCreateDto.categoryId()).orElseThrow();
    for (int i = 0; i < 5; i++) {
      postService.createPost(
          new PostCreateDto(postCreateDto.categoryId(),
              "카테고리 있는 게시글" + i, postCreateDto.content(), postCreateDto.summary(),
              "categorized-slug" + i, PostStatus.PUBLISHED.name(), List.of()), AUTHOR);
    }
    for (int i = 0; i < 5; i++) {
      postService.createPost(
          new PostCreateDto(null,
              postCreateDto.title(), postCreateDto.content(), postCreateDto.summary(),
              "just-slug" + i, PostStatus.PUBLISHED.name(), List.of()), AUTHOR);
    }
    Sort sort = Sort.by(Sort.Direction.fromString("DESC"), "createdAt");
    Pageable pageable = PageRequest.of(0, 10, sort);
    Page<PostSummaryResponseDto> page = postService.getAllPosts(
        category.getId(),
        PostStatus.PUBLISHED,
        null,
        pageable);
    assertThat(page).isNotNull();
    assertThat(page.getTotalElements()).isEqualTo(5);
    assertThat(page.getTotalPages()).isEqualTo(1);
    List<PostSummaryResponseDto> contents = page.getContent();
    assertThat(contents).hasSize(5);
    assertThat(contents).allMatch(post -> post.title().contains("카테고리"));
  }

  @Test
  void givenKeyword_whenSearch_thenReturnSearchResult() {
    for (int i = 0; i < 5; i++) {
      postService.createPost(
          new PostCreateDto(null,
              "키워드 있는 게시글" + i, postCreateDto.content(), postCreateDto.summary(),
              "keyword-slug" + i, PostStatus.PUBLISHED.name(), List.of()), AUTHOR);
    }
    for (int i = 0; i < 5; i++) {
      postService.createPost(
          new PostCreateDto(null,
              "일반 게시글", postCreateDto.content(), postCreateDto.summary(),
              "just-slug" + i, PostStatus.PUBLISHED.name(), List.of()), AUTHOR);
    }
    String keyword = "키워드";
    Sort sort = Sort.by(Sort.Direction.fromString("DESC"), "createdAt");
    Pageable pageable = PageRequest.of(0, 10, sort);
    Page<PostSummaryResponseDto> page = postService.getAllPosts(null, PostStatus.PUBLISHED, keyword,
        pageable);
    assertThat(page).isNotNull();
    assertThat(page.getTotalElements()).isEqualTo(5);
    assertThat(page.getTotalPages()).isEqualTo(1);
    List<PostSummaryResponseDto> contents = page.getContent();
    assertThat(contents).hasSize(5);
    assertThat(contents).allMatch(
        post -> post.title().contains(keyword));
  }

}