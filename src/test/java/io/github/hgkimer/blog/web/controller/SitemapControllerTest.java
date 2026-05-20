package io.github.hgkimer.blog.web.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import io.github.hgkimer.blog.domain.entity.Post;
import io.github.hgkimer.blog.service.PostService;
import io.github.hgkimer.blog.support.domain.entity.PostFixtureFactory;
import io.github.hgkimer.blog.support.web.controller.ControllerSliceTest;
import io.github.hgkimer.blog.support.web.controller.ControllerTestBase;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@ControllerSliceTest(SitemapController.class)
@TestPropertySource(properties = "spring.cache.type=none")
class SitemapControllerTest extends ControllerTestBase {

  @Autowired
  private MockMvcTester mockMvcTester;

  @MockitoBean
  private PostService postService;

  @Test
  @DisplayName("발행된 게시글이 있을 때 sitemap.xml 요청 시 200 OK와 XML을 반환해야 한다.")
  void givenPublishedPosts_whenGetSitemap_thenReturnXml() {
    // given
    Post post = PostFixtureFactory.createFixture();
    ReflectionTestUtils.setField(post, "slug", "my-first-post");
    ReflectionTestUtils.setField(post, "updatedAt", LocalDateTime.of(2026, 1, 15, 0, 0));

    given(postService.getAllPublishedPosts()).willReturn(List.of(post));

    // when & then
    mockMvcTester.get().uri("/sitemap.xml")
        .exchange()
        .assertThat()
        .hasStatus(HttpStatus.OK)
        .hasContentTypeCompatibleWith(MediaType.APPLICATION_XML)
        .bodyText()
        .satisfies(body -> {
          assertThat(body).contains("<urlset");
          assertThat(body).contains("https://hgkimer.me/posts/my-first-post");
          assertThat(body).contains("<lastmod>2026-01-15</lastmod>");
          assertThat(body).contains("<changefreq>weekly</changefreq>");
          assertThat(body).contains("<loc>https://hgkimer.me</loc>");
        });
  }

  @Test
  @DisplayName("발행된 게시글이 없을 때 sitemap.xml 요청 시 홈 URL만 포함된 XML을 반환해야 한다.")
  void givenNoPosts_whenGetSitemap_thenReturnXmlWithHomeOnly() {
    // given
    given(postService.getAllPublishedPosts()).willReturn(List.of());

    // when & then
    mockMvcTester.get().uri("/sitemap.xml")
        .exchange()
        .assertThat()
        .hasStatus(HttpStatus.OK)
        .hasContentTypeCompatibleWith(MediaType.APPLICATION_XML)
        .bodyText()
        .satisfies(body -> {
          assertThat(body).contains("<loc>https://hgkimer.me</loc>");
          assertThat(body).doesNotContain("/posts/");
        });
  }

  @Test
  @DisplayName("sitemap.xml 요청 시 PostService.getAllPublishedPosts()가 호출되어야 한다.")
  void whenGetSitemap_thenCallsGetAllPublishedPosts() {
    // given
    given(postService.getAllPublishedPosts()).willReturn(List.of());

    // when
    mockMvcTester.get().uri("/sitemap.xml").exchange();

    // then
    then(postService).should().getAllPublishedPosts();
  }
}
