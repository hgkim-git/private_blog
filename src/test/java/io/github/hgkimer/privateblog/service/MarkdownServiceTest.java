package io.github.hgkimer.privateblog.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.github.hgkimer.privateblog.config.JsoupConfig;
import io.github.hgkimer.privateblog.config.MarkdownConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = {MarkdownService.class, MarkdownConfig.class, JsoupConfig.class})
class MarkdownServiceTest {

  @Autowired
  private MarkdownService markdownService;

  @Test
  void initTest() {
    assertNotNull(markdownService);
  }

  @Test
  void markdownToHtmlTest() {
    String markdown = """
        # H1 헤더
        ## H2 헤더
        ### H3 헤더
        테스트 본문
        """;
    String html = markdownService.convertToHtml(markdown);
    System.out.println(html);
  }

  @Test
  void testToc() {
    String markdown = """
        # 테스트 헤더
        ## 테스트 헤더
        ### 테스트 헤더
        테스트 본문
        """;
    String html = markdownService.convertToHtml(markdown);
    String toc = markdownService.getTocHtml(html);
    System.out.println(toc);
  }


}