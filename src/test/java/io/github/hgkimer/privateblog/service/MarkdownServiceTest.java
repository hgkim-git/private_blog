package io.github.hgkimer.privateblog.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.github.hgkimer.privateblog.config.JsoupConfig;
import io.github.hgkimer.privateblog.config.MarkdownConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = {MarkdownService.class, MarkdownConfig.class, JsoupConfig.class})
class MarkdownServiceTest {

  @Autowired
  private MarkdownService markdownService;

  @Test
  @DisplayName("서비스 초기화 테스트")
  void initTest() {
    assertNotNull(markdownService);
  }

  @Test
  @DisplayName("마크다운을 HTML로 변환 테스트")
  void markdownToHtmlTest() {
    String markdown = """
        # H1 헤더
        ## H2 헤더
        ### H3 헤더
        테스트 본문
        """;
    assertThat(markdownService.convertToHtml(markdown))
        .isNotBlank()
        // (ex) <h1 id="generated toc uuid>
        .contains("<h1").contains("<h2").contains("<h3")
        .contains("<p>테스트 본문</p>");
  }

  @Test
  @DisplayName("목차 HTML(TOC) 생성 테스트")
  void testToc() {
    String markdown = """
        # 테스트 헤더
        ## 테스트 헤더
        ### 테스트 헤더
        테스트 본문
        """;
    String html = markdownService.convertToHtml(markdown);
    assertThat(markdownService.getTocHtml(html)).isNotBlank()
        .contains("class=\"" + "toc-item" + "\"")
        .contains("class=\"" + "toc-link" + "\"");
  }

}