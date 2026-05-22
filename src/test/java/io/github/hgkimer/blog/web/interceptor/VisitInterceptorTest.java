package io.github.hgkimer.blog.web.interceptor;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.hgkimer.blog.service.VisitService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VisitInterceptorTest {

  @Mock
  private VisitService visitService;

  @InjectMocks
  private VisitInterceptor visitInterceptor;

  @Test
  @DisplayName("X-Forwarded-For 헤더가 있으면 첫 번째 IP로 기록한다")
  void givenXForwardedForHeader_whenPreHandle_thenRecordFirstIp() throws Exception {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    when(request.getHeader("X-Forwarded-For")).thenReturn("1.1.1.1, 2.2.2.2");

    visitInterceptor.preHandle(request, response, new Object());

    verify(visitService).recordVisit("1.1.1.1");
  }

  @Test
  @DisplayName("X-Forwarded-For 헤더가 없으면 remoteAddr로 기록한다")
  void givenNoXForwardedFor_whenPreHandle_thenRecordRemoteAddr() throws Exception {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    when(request.getHeader("X-Forwarded-For")).thenReturn(null);
    when(request.getRemoteAddr()).thenReturn("3.3.3.3");

    visitInterceptor.preHandle(request, response, new Object());

    verify(visitService).recordVisit("3.3.3.3");
  }
}
