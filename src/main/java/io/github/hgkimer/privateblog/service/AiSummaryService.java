package io.github.hgkimer.privateblog.service;

import io.github.hgkimer.privateblog.web.exception.BusinessException;
import io.github.hgkimer.privateblog.web.exception.ErrorCode;
import java.net.SocketTimeoutException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiSummaryService {

  private static final String SYSTEM_PROMPT = """
      당신은 개발자를 위한 기술 블로그의 에디터입니다.
      아래 글을 읽고 핵심 내용을 300자 이내의 한국어로 요약해 주세요.
      
      조건:
      - 경어체(~합니다, ~입니다)로 작성할 것
      - 기술 용어는 원문 그대로 유지할 것
      - 요약문만 출력하고 다른 설명은 붙이지 말 것
      """;

  private final ChatClient chatClient;

  public String summarize(String markdownContent) {
    log.debug("AI summary requested, input length: {}", markdownContent.length());
    long start = System.currentTimeMillis();
    try {
      String summary = chatClient.prompt()
          .system(SYSTEM_PROMPT)
          .user(markdownContent)
          .call()
          .content();
      log.debug("AI summary completed, output length: {}, elapsed: {}ms",
          Optional.ofNullable(summary).map(String::length).orElse(0),
          System.currentTimeMillis() - start);
      return summary;
    } catch (Exception e) {
      if (e.getCause() instanceof SocketTimeoutException) {
        log.warn("AI provider timeout");
        throw new BusinessException(ErrorCode.AI_PROVIDER_TIMEOUT);
      }
      if (isRateLimitError(e)) {
        log.warn("AI rate limit exceeded");
        throw new BusinessException(ErrorCode.AI_RATE_LIMITED);
      }
      if (isRequestTooLargeError(e)) {
        log.warn("AI request too large");
        throw new BusinessException(ErrorCode.AI_REQUEST_TOO_LARGE);
      }
      if (isAuthError(e)) {
        log.error("AI provider authentication failed", e);
        throw new BusinessException(ErrorCode.AI_PROVIDER_AUTH_FAILED);
      }
      log.error("AI summary request failed", e);
      throw new BusinessException(ErrorCode.AI_SUMMARY_FAILED);
    }
  }

  private boolean isRateLimitError(Throwable e) {
    if (e == null) {
      return false;
    }
    String msg = e.getMessage();
    if (msg != null) {
      String lower = msg.toLowerCase();
      if (msg.contains("429") || lower.contains("too many")
          || lower.contains("quota") || lower.contains("resource exhausted")) {
        return true;
      }
    }
    return isRateLimitError(e.getCause());
  }

  private boolean isRequestTooLargeError(Throwable e) {
    if (e == null) {
      return false;
    }
    String msg = e.getMessage();
    if (msg != null) {
      String lower = msg.toLowerCase();
      if (lower.contains("payload size") || lower.contains("exceeds")
          || lower.contains("token limit") || msg.contains("400")) {
        return true;
      }
    }
    return isRequestTooLargeError(e.getCause());
  }

  private boolean isAuthError(Throwable e) {
    if (e == null) {
      return false;
    }
    String msg = e.getMessage();
    if (msg != null && (msg.contains("401") || msg.contains("403")
        || msg.toLowerCase().contains("api_key_invalid")
        || msg.toLowerCase().contains("unauthorized"))) {
      return true;
    }
    return isAuthError(e.getCause());
  }
}
