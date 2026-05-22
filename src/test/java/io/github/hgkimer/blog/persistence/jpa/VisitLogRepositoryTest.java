package io.github.hgkimer.blog.persistence.jpa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.hgkimer.blog.domain.entity.VisitLog;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

@DataJpaTest
class VisitLogRepositoryTest {

  @Autowired
  private VisitLogRepository visitLogRepository;

  @Test
  @DisplayName("오늘 방문한 고유 IP 수를 집계한다")
  void givenVisitLogs_whenCountDistinctIpByDate_thenReturnUniqueCount() {
    LocalDate today = LocalDate.now();
    LocalDate yesterday = today.minusDays(1);

    visitLogRepository.save(new VisitLog("1.1.1.1", today));
    visitLogRepository.save(new VisitLog("2.2.2.2", today));
    visitLogRepository.save(new VisitLog("1.1.1.1", yesterday));

    assertThat(visitLogRepository.countDistinctIpByDate(today)).isEqualTo(2);
  }

  @Test
  @DisplayName("전체 고유 IP 수를 집계한다")
  void givenVisitLogs_whenCountDistinctIpTotal_thenReturnUniqueCount() {
    LocalDate today = LocalDate.now();
    LocalDate yesterday = today.minusDays(1);

    visitLogRepository.save(new VisitLog("1.1.1.1", today));
    visitLogRepository.save(new VisitLog("2.2.2.2", today));
    visitLogRepository.save(new VisitLog("1.1.1.1", yesterday));

    // 1.1.1.1이 날짜가 다르므로 각각 1개 row, 총 IP 집계는 DISTINCT ip → 2
    assertThat(visitLogRepository.countDistinctIpTotal()).isEqualTo(2);
  }

  @Test
  @DisplayName("동일 IP + 동일 날짜 중복 저장 시 예외가 발생한다")
  void givenDuplicateIpAndDate_whenSave_thenThrowException() {
    LocalDate today = LocalDate.now();
    visitLogRepository.save(new VisitLog("1.1.1.1", today));
    visitLogRepository.flush();

    assertThatThrownBy(() -> {
      visitLogRepository.save(new VisitLog("1.1.1.1", today));
      visitLogRepository.flush();
    }).isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  @DisplayName("동일 IP도 날짜가 다르면 정상 저장된다")
  void givenSameIpDifferentDate_whenSave_thenSuccess() {
    LocalDate today = LocalDate.now();
    LocalDate yesterday = today.minusDays(1);

    visitLogRepository.save(new VisitLog("1.1.1.1", today));
    visitLogRepository.save(new VisitLog("1.1.1.1", yesterday));
    visitLogRepository.flush();

    assertThat(visitLogRepository.count()).isEqualTo(2);
  }
}
