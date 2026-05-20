package io.github.hgkimer.blog.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "tag")
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Tag extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "name", length = 30, nullable = false, unique = true)
  private String name;

  @Column(name = "slug", length = 50, nullable = false, unique = true)
  private String slug;

  @Builder
  public Tag(String name, String slug) {
    this.name = name;
    this.slug = slug;
  }

  public static Tag of(String name, String slug) {
    return new Tag(name, slug);
  }

  public void update(String name, String slug) {
    this.name = name;
    this.slug = slug;
  }

}
