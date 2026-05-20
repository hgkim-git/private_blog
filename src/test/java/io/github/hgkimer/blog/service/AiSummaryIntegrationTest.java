package io.github.hgkimer.blog.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 실제 Gemini API 연결을 검증하는 통합 테스트. GEMINI_API_KEY 환경변수가 없으면 자동으로 스킵된다.
 */
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "GEMINI_API_KEY", matches = ".+")
class AiSummaryIntegrationTest {

  @Autowired
  private AiSummaryService aiSummaryService;

  @Test
  @DisplayName("실제 Gemini API를 호출해 마크다운 본문을 요약해야 한다.")
  void givenMarkdownContent_whenSummarize_thenReturnNonEmptySummary() {
    String markdown = """
        # Spring AI 소개
        
        Spring AI는 Spring 생태계에서 AI 기능을 쉽게 통합할 수 있게 해주는 프레임워크입니다.
        ChatClient 추상화를 통해 OpenAI, Google Gemini, Ollama 등 다양한 LLM 프로바이더를
        코드 변경 없이 교체할 수 있습니다.
        
        ## 주요 기능
        - ChatClient 추상화
        - 프롬프트 템플릿
        - RAG(Retrieval-Augmented Generation) 지원
        - 벡터 스토어 연동
        
        Spring AI를 활용하면 LLM 프로바이더 교체 시 서비스 코드를 변경할 필요가 없습니다.
        """;

    String summary = aiSummaryService.summarize(markdown);

    assertThat(summary)
        .isNotBlank()
        .hasSizeLessThanOrEqualTo(300);
  }
}
