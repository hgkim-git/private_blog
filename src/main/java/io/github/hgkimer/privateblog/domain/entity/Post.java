package io.github.hgkimer.privateblog.domain.entity;

import io.github.hgkimer.privateblog.domain.enums.PostStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;

@Entity
@Table(name = "post")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseTimeEntity {


  // getTags가 호출될때, IN절 안에 post_id를 10개 단위로 post_tag 테이블에서 row를 조회
  @BatchSize(size = 100)
  @OneToMany(mappedBy = "post", orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @OrderBy("id ASC")
  private final List<PostTag> postTags = new ArrayList<>();

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "author_id", nullable = false, foreignKey = @ForeignKey(name = "fk_post_to_user"))
  private User author;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "category_id", foreignKey = @ForeignKey(name = "fk_post_to_category"))
  private Category category;

  @Column(name = "title", length = 200, nullable = false)
  private String title;

  // TEXT와 같은 대용량 데이터 타입을 다루는 JPA 전통적인 방식으로 @Lob 어노테이션을 붙일 수 있음
  // 다만 @Column 어노테이션과 columnDefinition = "TEXT"를 통해 더 명시적으로 의미 전달
  @Column(name = "content", nullable = false, columnDefinition = "TEXT")
  private String content;

  @Column(name = "content_html", nullable = false, columnDefinition = "TEXT")
  private String contentHtml;

  @Column(name = "summary", length = 500)
  private String summary;

  @Column(name = "slug", length = 250, nullable = false, unique = true)
  private String slug;

  @Column(name = "status", nullable = false)
  @Enumerated(EnumType.STRING)
  private PostStatus status = PostStatus.DRAFT;

  @Column(name = "view_count", nullable = false)
  private Integer viewCount = 0;

  @Builder
  public Post(Category category, User author, String title, String content, String contentHtml,
      String summary,
      String slug, PostStatus status) {
    this.category = category;
    this.author = author;
    this.title = title;
    this.content = content;
    this.contentHtml = contentHtml;
    this.summary = summary;
    this.slug = slug;
    this.status = status;
  }

  public static Post of(Category category, User author, String title, String content,
      String contentHtml,
      String summary, String slug, PostStatus status) {
    if (author == null) {
      throw new IllegalArgumentException("author must not be null");
    }
    return new Post(category, author, title, content, contentHtml, summary, slug, status);
  }

  public void publish() {
    if (this.status == PostStatus.PUBLISHED) {
      return;
    }
    this.status = PostStatus.PUBLISHED;
  }

  public void draft() {
    if (this.status == PostStatus.DRAFT) {
      return;
    }
    this.status = PostStatus.DRAFT;
  }

  public void addTags(List<PostTag> postTags) {
    this.postTags.clear();
    this.postTags.addAll(postTags);
  }

  public void increaseViewCount() {
    this.viewCount++;
  }

  public void update(String title, String content, String contentHtml, String summary, String slug,
      String status,
      Category category) {
    this.title = title;
    this.content = content;
    this.contentHtml = contentHtml;
    this.summary = summary;
    this.slug = slug;
    this.category = category;
    this.status = PostStatus.valueOf(status);
  }

}
