package io.github.hgkimer.blog.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import io.github.hgkimer.blog.domain.entity.VisitLog;
import io.github.hgkimer.blog.persistence.jpa.VisitLogRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class VisitServiceTest {

  @Mock
  private VisitLogRepository visitLogRepository;

  @InjectMocks
  private VisitService visitService;

  @Test
  @DisplayName("신규 IP 방문 시 visit_log가 저장된다")
  void givenNewIp_whenRecordVisit_thenSaved() {
    given(visitLogRepository.save(any(VisitLog.class))).willAnswer(inv -> inv.getArgument(0));

    visitService.recordVisit("1.1.1.1");

    then(visitLogRepository).should().save(any(VisitLog.class));
  }

  @Test
  @DisplayName("동일 IP 재방문 시 예외를 무시하고 정상 처리된다")
  void givenDuplicateIp_whenRecordVisit_thenIgnoreException() {
    given(visitLogRepository.save(any(VisitLog.class)))
        .willThrow(new DataIntegrityViolationException("duplicate"));

    assertThatNoException().isThrownBy(() -> visitService.recordVisit("1.1.1.1"));
  }

  @Test
  @DisplayName("이미 기록된 IP와 날짜의 방문은 저장하지 않아야 한다")
  void givenExistingVisit_whenRecordVisit_thenDoNotSave() {
    LocalDate today = LocalDate.now();
    given(visitLogRepository.existsByIpAndVisitedAt("1.1.1.1", today)).willReturn(true);

    visitService.recordVisit("1.1.1.1");

    then(visitLogRepository).should().existsByIpAndVisitedAt("1.1.1.1", today);
    then(visitLogRepository).shouldHaveNoMoreInteractions();
  }

  @Test
  @DisplayName("오늘 방문자 수를 반환한다")
  void whenGetTodayVisitorCount_thenReturnCount() {
    given(visitLogRepository.countDistinctIpByDate(LocalDate.now())).willReturn(5L);

    assertThat(visitService.getTodayVisitorCount()).isEqualTo(5L);
  }

  @Test
  @DisplayName("전체 방문자 수를 반환한다")
  void whenGetTotalVisitorCount_thenReturnCount() {
    given(visitLogRepository.countDistinctIpTotal()).willReturn(100L);

    assertThat(visitService.getTotalVisitorCount()).isEqualTo(100L);
  }
}
