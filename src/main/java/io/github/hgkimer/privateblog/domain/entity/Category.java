package io.github.hgkimer.privateblog.domain.entity;

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

@Entity
@Table(name = "category")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "name", length = 50, nullable = false, unique = true)
  private String name;

  @Column(name = "slug", length = 100, nullable = false, unique = true)
  private String slug;

  @Column(name = "display_order", nullable = false)
  private Integer displayOrder = 0;

  @Column(name = "post_count", nullable = false)
  private Integer postCount = 0;

  @Builder
  public Category(String name, String slug, Integer displayOrder) {
    this.name = name;
    this.slug = slug;
    this.displayOrder = displayOrder != null ? displayOrder : 0;
  }

  public static Category of(String name, String slug) {
    return Category.of(name, slug, 0);
  }

  public static Category of(String name, String slug, Integer displayOrder) {
    return new Category(name, slug, displayOrder);
  }

  public void update(String name, String slug) {
    this.name = name;
    this.slug = slug;
  }

  public void updateDisplayOrder(Integer displayOrder) {
    this.displayOrder = displayOrder;
  }

}
