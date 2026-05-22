package io.github.hgkimer.blog.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "visit_log",
    uniqueConstraints = @UniqueConstraint(name = "idx_ip_date", columnNames = {"ip", "visited_at"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VisitLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "ip", nullable = false, length = 45)
  private String ip;

  @Column(name = "visited_at", nullable = false)
  private LocalDate visitedAt;

  public VisitLog(String ip, LocalDate visitedAt) {
    this.ip = ip;
    this.visitedAt = visitedAt;
  }
}
