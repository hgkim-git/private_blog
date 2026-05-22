package io.github.hgkimer.blog.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

import io.github.hgkimer.blog.persistence.jpa.VisitLogRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(VisitService.class)
class VisitServiceIntegrationTest {

  @Autowired
  private VisitService visitService;

  @Autowired
  private VisitLogRepository visitLogRepository;

  @Test
  @DisplayName("같은 IP를 두 번 기록해도 중복 방문으로 처리되어야 한다")
  void givenSameIpTwice_whenRecordVisit_thenNoRollbackException() {
    visitService.recordVisit("1.1.1.1");

    assertThatNoException().isThrownBy(() -> visitService.recordVisit("1.1.1.1"));

    assertThat(visitLogRepository.count()).isEqualTo(1);
  }
}
