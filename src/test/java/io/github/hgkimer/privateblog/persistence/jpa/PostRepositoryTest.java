package io.github.hgkimer.privateblog.persistence.jpa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.hgkimer.privateblog.domain.entity.Category;
import io.github.hgkimer.privateblog.domain.entity.Post;
import io.github.hgkimer.privateblog.domain.entity.PostTag;
import io.github.hgkimer.privateblog.domain.entity.Tag;
import io.github.hgkimer.privateblog.domain.entity.User;
import io.github.hgkimer.privateblog.domain.enums.PostStatus;
import io.github.hgkimer.privateblog.domain.enums.UserRole;
import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@DataJpaTest
class PostRepositoryTest {

  @Autowired
  EntityManager entityManager;
  @Autowired
  private PostRepository postRepository;
  private Category category;
  private User author;

  @BeforeEach
  void setUp() {
    category = Category.builder()
        .name("Test Category").slug("test-category")
        .build();
    entityManager.persist(category);
    author = User.builder()
        .email("test@example.com")
        .role(UserRole.ADMIN)
        .password("password")
        .build();
    entityManager.persist(author);

    entityManager.flush();
    entityManager.clear();
  }

  private Post createPost(String title, String slug) {
    return Post.builder()
        .author(author)
        .category(category)
        .title(title)
        .slug(slug)
        .content("테스트 내용")
        .status(PostStatus.DRAFT)
        .build();
  }

  @Test
  void givenSlug_whenExistsBySlug_thenReturnBoolean() {
    Post post = createPost("테스트 게시글", "test-slug");
    postRepository.save(post);
    entityManager.flush();
    entityManager.clear();

    assertTrue(postRepository.existsBySlug(post.getSlug()));
  }

  @Test
  void findBySlugWithDetails() {
    Post post = createPost("테스트 게시글", "test-slug");
    postRepository.save(post);
    entityManager.flush();
    entityManager.clear();

    Post result = postRepository.findBySlugWithDetails(post.getSlug()).orElseThrow();
    assertEquals(post.getId(), result.getId());
  }


  @Test
  void givenTags_whenPostCreated_thenSavedWithTags() {
    Post post = createPost("테스트 게시글", "test-slug");

    List<PostTag> postTags = new ArrayList<>();

    Tag tag1 = Tag.builder().name("tag1").slug("tag1").build();
    entityManager.persist(tag1);
    PostTag postTag = PostTag.builder().post(post).tag(tag1).build();
    postTags.add(postTag);

    Tag tag2 = Tag.builder().name("tag2").slug("tag2").build();
    PostTag postTag2 = PostTag.builder().post(post).tag(tag2).build();
    postTags.add(postTag2);
    entityManager.persist(tag2);
    post.publish();
    post.addTags(postTags);
    postRepository.save(post);
    entityManager.flush();
    entityManager.clear();

    Post found = postRepository.findById(post.getId())
        .orElseThrow(() -> new IllegalArgumentException("Post not found"));
    assertThat(found).isNotNull();
    assertThat(found.getPostTags()).hasSize(2);
    assertThat(found.getPostTags()).extracting(PostTag::getTag).extracting(Tag::getName)
        .containsExactlyInAnyOrder(tag1.getName(), tag2.getName());
  }

  @Test
  void given10PostsCreated_whenFindAllPosts_thenReturnPage() {
    for (int i = 0; i < 10; i++) {
      Post post = createPost("테스트 게시글" + i, "test-slug" + i);
      post.publish();
      postRepository.save(post);
    }
    entityManager.flush();
    Page<Post> page = postRepository.findAllPosts(null, PostStatus.PUBLISHED, null,
        Pageable.ofSize(10));
    assertThat(page).hasSize(10);
    assertThat(page).allMatch(p -> p.getStatus() == PostStatus.PUBLISHED);
  }

  @Test
  void givenSpecificPostStatus_whenFindAllPosts_thenReturnFilteredPage() {
    for (int i = 0; i < 5; i++) {
      Post post = createPost("테스트 게시글" + i, "test-slug" + i);
      post.publish();
      postRepository.save(post);
    }
    for (int i = 0; i < 5; i++) {
      Post post = createPost("임시 저장 게시글" + i, "temp-slug" + i);
      postRepository.save(post);
    }
    entityManager.flush();
    Page<Post> draftPosts = postRepository.findAllPosts(null, PostStatus.DRAFT, null,
        Pageable.ofSize(10));
    assertThat(draftPosts).hasSize(5);
    assertThat(draftPosts).allMatch(p -> p.getStatus() == PostStatus.DRAFT);

    Page<Post> publishedPosts = postRepository.findAllPosts(null, PostStatus.PUBLISHED, null,
        Pageable.ofSize(10));
    assertThat(publishedPosts).hasSize(5);
    assertThat(publishedPosts).allMatch(p -> p.getStatus() == PostStatus.PUBLISHED);
  }

  @Test
  void givenKeyword_WhenFindAllPosts_thenReturnFilteredPage() {
    for (int i = 0; i < 5; i++) {
      Post post = Post.of(category, author, "테스트 게시글" + i, "테스트 본문", "", "요약", "test-slug" + i,
          PostStatus.PUBLISHED);
      postRepository.save(post);
    }
    entityManager.flush();
    String keyword = "테스트";
    Page<Post> filteredPosts = postRepository.findAllPosts(null, PostStatus.PUBLISHED, keyword,
        Pageable.ofSize(10));
    assertThat(filteredPosts).hasSize(5);
    assertThat(filteredPosts).allMatch(p -> p.getTitle().contains(keyword));
  }

  @Test
  void givenCategoryId_whenFindAllPostsByCategoryId_thenReturnPage() {
    for (int i = 0; i < 5; i++) {
      Post post = Post.of(category, author, "카테고리 속한 게시글" + i, "테스트 본문", "", "요약",
          "test-slug" + i,
          PostStatus.PUBLISHED);
      postRepository.save(post);
    }
    for (int i = 5; i < 10; i++) {
      Post post = Post.of(null, author, "일반 테스트 게시글" + i, "테스트 본문", "", "요약",
          "test-slug" + i,
          PostStatus.PUBLISHED);
      postRepository.save(post);
    }
    entityManager.flush();

    Page<Post> postsByCategoryId = postRepository.findAllPosts(category.getId(),
        PostStatus.PUBLISHED, null, Pageable.ofSize(10));
    assertThat(postsByCategoryId).hasSize(5);
    assertThat(postsByCategoryId).allMatch(
        p -> p.getCategory().getId().equals(category.getId()));
    assertThat(postsByCategoryId).allMatch(p -> p.getTitle().contains("카테고리 속한 게시글"));
  }


}