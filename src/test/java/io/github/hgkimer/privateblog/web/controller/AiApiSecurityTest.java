package io.github.hgkimer.privateblog.web.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "spring.ai.google.genai.api-key=test-dummy-key")
class AiApiSecurityTest {

  private static final String URI = "/api/ai/summarize";

  @Autowired
  private MockMvcTester mockMvcTester;

  @Test
  @DisplayName("미인증 요청은 401을 반환해야 한다.")
  void givenUnauthenticatedUser_whenSummarize_thenReturnUnauthorized() {
    mockMvcTester.post().uri(URI)
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"content\":\"test\"}")
        .exchange()
        .assertThat()
        .hasStatus(HttpStatus.UNAUTHORIZED);
  }

  @Test
  @WithMockUser(authorities = "USER")
  @DisplayName("ADMIN이 아닌 사용자의 요청은 403을 반환해야 한다.")
  void givenNonAdminUser_whenSummarize_thenReturnForbidden() {
    mockMvcTester.post().uri(URI)
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"content\":\"test\"}")
        .exchange()
        .assertThat()
        .hasStatus(HttpStatus.FORBIDDEN);
  }
}