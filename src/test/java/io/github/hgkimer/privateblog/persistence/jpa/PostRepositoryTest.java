package io.github.hgkimer.privateblog.persistence.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.hgkimer.privateblog.domain.entity.Category;
import io.github.hgkimer.privateblog.domain.entity.Post;
import io.github.hgkimer.privateblog.domain.entity.PostTag;
import io.github.hgkimer.privateblog.domain.entity.Tag;
import io.github.hgkimer.privateblog.domain.entity.User;
import io.github.hgkimer.privateblog.domain.enums.PostStatus;
import io.github.hgkimer.privateblog.support.domain.entity.CategoryFixtureFactory;
import io.github.hgkimer.privateblog.support.domain.entity.PostFixtureFactory;
import io.github.hgkimer.privateblog.support.domain.entity.TagFixtureFactory;
import io.github.hgkimer.privateblog.support.domain.entity.UserFixtureFactory;
import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
    category = CategoryFixtureFactory.createFixture();
    entityManager.persist(category);
    author = UserFixtureFactory.createAdminFixture();
    entityManager.persist(author);

    entityManager.flush();
    entityManager.clear();
  }

  @Test
  @DisplayName("슬러그 존재 여부 확인 테스트")
  void givenSlug_whenExistsBySlug_thenReturnBoolean() {
    Post post = PostFixtureFactory.createFixture(category, author, "테스트 게시글", "test-slug",
        PostStatus.DRAFT);
    postRepository.save(post);
    entityManager.flush();
    entityManager.clear();

    assertThat(postRepository.existsBySlug(post.getSlug())).isTrue();
  }

  @Test
  @DisplayName("슬러그로 상세 정보와 함께 게시글 조회 테스트")
  void findBySlugWithDetails() {
    Post post = PostFixtureFactory.createFixture(category, author, "테스트 게시글", "test-slug",
        PostStatus.DRAFT);
    postRepository.save(post);
    entityManager.flush();
    entityManager.clear();

    Post result = postRepository.findBySlugWithDetails(post.getSlug()).orElseThrow();
    assertThat(result.getId()).isEqualTo(post.getId());
  }


  @Test
  @DisplayName("태그가 포함된 게시글 저장 시 태그 관계가 올바르게 저장되어야 한다.")
  void givenTags_whenPostCreated_thenSavedWithTags() {
    Post post = PostFixtureFactory.createFixture(category, author, "테스트 게시글", "test-slug",
        PostStatus.DRAFT);

    List<PostTag> postTags = new ArrayList<>();

    Tag tag1 = TagFixtureFactory.createFixture("tag1", "tag1");
    entityManager.persist(tag1);
    PostTag postTag = PostTag.builder().post(post).tag(tag1).build();
    postTags.add(postTag);

    Tag tag2 = TagFixtureFactory.createFixture("tag2", "tag2");
    entityManager.persist(tag2);
    PostTag postTag2 = PostTag.builder().post(post).tag(tag2).build();
    postTags.add(postTag2);

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
  @DisplayName("게시글 페이징 조회 테스트")
  void given10PostsCreated_whenFindAllPosts_thenReturnPage() {
    for (int i = 0; i < 10; i++) {
      Post post = PostFixtureFactory.createFixture(category, author, "테스트 게시글" + i,
          "test-slug" + i, PostStatus.PUBLISHED);
      postRepository.save(post);
    }
    entityManager.flush();
    Page<Post> page = postRepository.findAllPosts(null, PostStatus.PUBLISHED, null,
        Pageable.ofSize(10));
    assertThat(page).hasSize(10);
    assertThat(page).allMatch(p -> p.getStatus() == PostStatus.PUBLISHED);
  }

  @Test
  @DisplayName("게시글 상태별 필터링 조회 테스트")
  void givenSpecificPostStatus_whenFindAllPosts_thenReturnFilteredPage() {
    for (int i = 0; i < 5; i++) {
      Post post = PostFixtureFactory.createFixture(category, author, "테스트 게시글" + i,
          "test-slug" + i, PostStatus.PUBLISHED);
      postRepository.save(post);
    }
    for (int i = 0; i < 5; i++) {
      Post post = PostFixtureFactory.createFixture(category, author, "임시 저장 게시글" + i,
          "temp-slug" + i, PostStatus.DRAFT);
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
  @DisplayName("키워드 기반 게시글 검색 테스트")
  void givenKeyword_WhenFindAllPosts_thenReturnFilteredPage() {
    for (int i = 0; i < 5; i++) {
      Post post = PostFixtureFactory.createFixture(category, author, "테스트 게시글" + i,
          "test-slug" + i, PostStatus.PUBLISHED);
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
  @DisplayName("카테고리별 게시글 조회 테스트")
  void givenCategoryId_whenFindAllPostsByCategoryId_thenReturnPage() {
    for (int i = 0; i < 5; i++) {
      Post post = PostFixtureFactory.createFixture(category, author, "카테고리 속한 게시글" + i,
          "test-slug" + i, PostStatus.PUBLISHED);
      postRepository.save(post);
    }
    for (int i = 5; i < 10; i++) {
      Post post = PostFixtureFactory.createFixture(null, author, "일반 테스트 게시글" + i,
          "test-slug" + i, PostStatus.PUBLISHED);
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