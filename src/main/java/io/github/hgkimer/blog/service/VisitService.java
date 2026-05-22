package io.github.hgkimer.blog.service;

import io.github.hgkimer.blog.domain.entity.VisitLog;
import io.github.hgkimer.blog.persistence.jpa.VisitLogRepository;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VisitService {

  private final VisitLogRepository visitLogRepository;

  public void recordVisit(String ip) {
    LocalDate today = LocalDate.now();
    if (visitLogRepository.existsByIpAndVisitedAt(ip, today)) {
      return;
    }

    try {
      visitLogRepository.save(new VisitLog(ip, today));
    } catch (DataIntegrityViolationException ignored) {
      // 동시 요청으로 같은 IP/날짜 방문 기록이 이미 저장된 경우이므로 중복 방문으로 보고 무시한다.
    }
  }

  @Transactional(readOnly = true)
  public long getTodayVisitorCount() {
    return visitLogRepository.countDistinctIpByDate(LocalDate.now());
  }

  @Transactional(readOnly = true)
  public long getTotalVisitorCount() {
    return visitLogRepository.countDistinctIpTotal();
  }
}
