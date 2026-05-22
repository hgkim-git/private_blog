package io.github.hgkimer.blog.persistence.jpa;

import io.github.hgkimer.blog.domain.entity.VisitLog;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VisitLogRepository extends JpaRepository<VisitLog, Long> {

  boolean existsByIpAndVisitedAt(String ip, LocalDate visitedAt);

  @Query("SELECT COUNT(DISTINCT v.ip) FROM VisitLog v WHERE v.visitedAt = :date")
  long countDistinctIpByDate(@Param("date") LocalDate date);

  @Query("SELECT COUNT(DISTINCT v.ip) FROM VisitLog v")
  long countDistinctIpTotal();
}
