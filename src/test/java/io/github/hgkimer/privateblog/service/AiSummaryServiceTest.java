package io.github.hgkimer.privateblog.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

import io.github.hgkimer.privateblog.web.exception.BusinessException;
import io.github.hgkimer.privateblog.web.exception.ErrorCode;
import java.net.SocketTimeoutException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

@ExtendWith(MockitoExtension.class)
class AiSummaryServiceTest {

  @Mock
  private ChatClient chatClient;
  @Mock
  private ChatClient.ChatClientRequestSpec requestSpec;
  @Mock
  private ChatClient.CallResponseSpec callSpec;

  @InjectMocks
  private AiSummaryService aiSummaryService;

  @Test
  @DisplayName("마크다운 본문을 전달하면 AI 요약 결과를 반환해야 한다.")
  void givenMarkdownContent_whenSummarize_thenReturnSummary() {
    given(chatClient.prompt()).willReturn(requestSpec);
    given(requestSpec.system(anyString())).willReturn(requestSpec);
    given(requestSpec.user(anyString())).willReturn(requestSpec);
    given(requestSpec.call()).willReturn(callSpec);
    given(callSpec.content()).willReturn("테스트 요약 결과");

    String result = aiSummaryService.summarize("# 테스트\n본문 내용");

    assertThat(result).isEqualTo("테스트 요약 결과");
  }

  @Test
  @DisplayName("AI 호출이 실패하면 AI_SUMMARY_FAILED BusinessException을 던져야 한다.")
  void givenAiFailure_whenSummarize_thenThrowAiSummaryFailed() {
    given(chatClient.prompt()).willReturn(requestSpec);
    given(requestSpec.system(anyString())).willReturn(requestSpec);
    given(requestSpec.user(anyString())).willReturn(requestSpec);
    given(requestSpec.call()).willReturn(callSpec);
    given(callSpec.content()).willThrow(new RuntimeException("Internal Server Error"));

    assertThatThrownBy(() -> aiSummaryService.summarize("# 테스트\n본문 내용"))
        .isInstanceOf(BusinessException.class)
        .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
            .isEqualTo(ErrorCode.AI_SUMMARY_FAILED));
  }

  @Test
  @DisplayName("AI가 429를 반환하면 AI_RATE_LIMITED BusinessException을 던져야 한다.")
  void givenRateLimitError_whenSummarize_thenThrowAiRateLimited() {
    given(chatClient.prompt()).willReturn(requestSpec);
    given(requestSpec.system(anyString())).willReturn(requestSpec);
    given(requestSpec.user(anyString())).willReturn(requestSpec);
    given(requestSpec.call()).willReturn(callSpec);
    given(callSpec.content()).willThrow(new RuntimeException("429 Too Many Requests"));

    assertThatThrownBy(() -> aiSummaryService.summarize("# 테스트\n본문 내용"))
        .isInstanceOf(BusinessException.class)
        .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
            .isEqualTo(ErrorCode.AI_RATE_LIMITED));
  }

  @Test
  @DisplayName("rate limit 오류가 cause 체인 안에 있어도 AI_RATE_LIMITED를 던져야 한다.")
  void givenRateLimitInCause_whenSummarize_thenThrowAiRateLimited() {
    given(chatClient.prompt()).willReturn(requestSpec);
    given(requestSpec.system(anyString())).willReturn(requestSpec);
    given(requestSpec.user(anyString())).willReturn(requestSpec);
    given(requestSpec.call()).willReturn(callSpec);
    RuntimeException cause = new RuntimeException("429 Too Many Requests");
    given(callSpec.content()).willThrow(new RuntimeException("AI call failed", cause));

    assertThatThrownBy(() -> aiSummaryService.summarize("# 테스트\n본문 내용"))
        .isInstanceOf(BusinessException.class)
        .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
            .isEqualTo(ErrorCode.AI_RATE_LIMITED));
  }

  @Test
  @DisplayName("SocketTimeoutException이 cause에 있으면 AI_PROVIDER_TIMEOUT을 던져야 한다.")
  void givenSocketTimeout_whenSummarize_thenThrowAiProviderTimeout() {
    given(chatClient.prompt()).willReturn(requestSpec);
    given(requestSpec.system(anyString())).willReturn(requestSpec);
    given(requestSpec.user(anyString())).willReturn(requestSpec);
    given(requestSpec.call()).willReturn(callSpec);
    given(callSpec.content()).willThrow(
        new RuntimeException("connection timed out", new SocketTimeoutException("Read timed out")));

    assertThatThrownBy(() -> aiSummaryService.summarize("# 테스트\n본문 내용"))
        .isInstanceOf(BusinessException.class)
        .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
            .isEqualTo(ErrorCode.AI_PROVIDER_TIMEOUT));
  }

  @Test
  @DisplayName("요청 크기 초과 오류가 발생하면 AI_REQUEST_TOO_LARGE를 던져야 한다.")
  void givenRequestTooLarge_whenSummarize_thenThrowAiRequestTooLarge() {
    given(chatClient.prompt()).willReturn(requestSpec);
    given(requestSpec.system(anyString())).willReturn(requestSpec);
    given(requestSpec.user(anyString())).willReturn(requestSpec);
    given(requestSpec.call()).willReturn(callSpec);
    given(callSpec.content()).willThrow(
        new RuntimeException("Request payload size exceeds the limit"));

    assertThatThrownBy(() -> aiSummaryService.summarize("# 테스트\n본문 내용"))
        .isInstanceOf(BusinessException.class)
        .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
            .isEqualTo(ErrorCode.AI_REQUEST_TOO_LARGE));
  }

  @Test
  @DisplayName("인증 오류가 발생하면 AI_PROVIDER_AUTH_FAILED를 던져야 한다.")
  void givenAuthError_whenSummarize_thenThrowAiProviderAuthFailed() {
    given(chatClient.prompt()).willReturn(requestSpec);
    given(requestSpec.system(anyString())).willReturn(requestSpec);
    given(requestSpec.user(anyString())).willReturn(requestSpec);
    given(requestSpec.call()).willReturn(callSpec);
    given(callSpec.content()).willThrow(new RuntimeException("401 Unauthorized"));

    assertThatThrownBy(() -> aiSummaryService.summarize("# 테스트\n본문 내용"))
        .isInstanceOf(BusinessException.class)
        .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
            .isEqualTo(ErrorCode.AI_PROVIDER_AUTH_FAILED));
  }
}
