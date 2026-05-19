package io.github.hgkimer.privateblog.web.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

import io.github.hgkimer.privateblog.service.AiSummaryService;
import io.github.hgkimer.privateblog.support.web.controller.ControllerSliceTest;
import io.github.hgkimer.privateblog.support.web.controller.ControllerTestBase;
import io.github.hgkimer.privateblog.web.dto.response.AiSummaryResponseDto;
import io.github.hgkimer.privateblog.web.exception.ErrorResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@ControllerSliceTest(AiApiController.class)
class AiApiControllerTest extends ControllerTestBase {

  private static final String URI = "/api/ai/summarize";

  @Autowired
  private MockMvcTester mockMvcTester;

  @MockitoBean
  private AiSummaryService aiSummaryService;

  @Test
  @DisplayName("ADMIN 권한으로 유효한 요청 시 200 OK와 요약 결과를 반환해야 한다.")
  @WithMockUser(authorities = "ADMIN")
  void givenAdminUser_whenSummarize_thenReturnSummary() {
    given(aiSummaryService.summarize(anyString())).willReturn("요약 결과입니다.");
    String json = """
        {"content": "# 제목\\n본문 내용입니다."}
        """;

    mockMvcTester.post().uri(URI)
        .contentType(MediaType.APPLICATION_JSON)
        .content(json)
        .exchange()
        .assertThat()
        .hasStatus(HttpStatus.OK)
        .bodyJson()
        .convertTo(AiSummaryResponseDto.class)
        .satisfies(response -> assertThat(response.summary()).isEqualTo("요약 결과입니다."));
  }

  @Test
  @DisplayName("content가 빈 문자열이면 400 Bad Request를 반환해야 한다.")
  @WithMockUser(authorities = "ADMIN")
  void givenBlankContent_whenSummarize_thenReturnBadRequest() {
    String json = """
        {"content": ""}
        """;

    mockMvcTester.post().uri(URI)
        .contentType(MediaType.APPLICATION_JSON)
        .content(json)
        .exchange()
        .assertThat()
        .hasStatus(HttpStatus.BAD_REQUEST)
        .bodyJson()
        .convertTo(ErrorResponse.class)
        .satisfies(response -> {
          assertThat(response.fieldErrors()).isNotEmpty();
          assertThat(response.fieldErrors().get(0).field()).isEqualTo("content");
        });
  }

  @Test
  @DisplayName("content가 50,000자를 초과하면 400 Bad Request를 반환해야 한다.")
  @WithMockUser(authorities = "ADMIN")
  void givenTooLargeContent_whenSummarize_thenReturnBadRequest() {
    String oversized = "a".repeat(50001);
    String json = "{\"content\": \"" + oversized + "\"}";

    mockMvcTester.post().uri(URI)
        .contentType(MediaType.APPLICATION_JSON)
        .content(json)
        .exchange()
        .assertThat()
        .hasStatus(HttpStatus.BAD_REQUEST)
        .bodyJson()
        .convertTo(ErrorResponse.class)
        .satisfies(response -> {
          assertThat(response.fieldErrors()).isNotEmpty();
          assertThat(response.fieldErrors().get(0).field()).isEqualTo("content");
        });
  }

}
