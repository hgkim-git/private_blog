package io.github.hgkimer.blog.web.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import io.github.hgkimer.blog.service.CategoryService;
import io.github.hgkimer.blog.service.PostService;
import io.github.hgkimer.blog.service.TagService;
import io.github.hgkimer.blog.service.VisitService;
import io.github.hgkimer.blog.support.web.controller.ControllerSliceTest;
import io.github.hgkimer.blog.support.web.controller.ControllerTestBase;
import io.github.hgkimer.blog.web.dto.response.DashboardStatsDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@ControllerSliceTest(AdminController.class)
class AdminControllerTest extends ControllerTestBase {

  @Autowired
  private MockMvcTester mockMvcTester;

  @MockitoBean
  private PostService postService;

  @MockitoBean
  private CategoryService categoryService;

  @MockitoBean
  private TagService tagService;

  @MockitoBean
  private VisitService visitService;

  @Test
  @DisplayName("대시보드 요청 시 게시글 수와 방문자 수가 모델에 담겨 200 OK를 반환한다")
  @WithMockUser(authorities = "ADMIN")
  void givenStats_whenGetDashboard_thenReturnDashboardView() {
    // given
    given(postService.getPublishedPostCount()).willReturn(10L);
    given(postService.getDraftPostCount()).willReturn(3L);
    given(visitService.getTodayVisitorCount()).willReturn(42L);
    given(visitService.getTotalVisitorCount()).willReturn(1234L);

    // when & then
    mockMvcTester.get().uri("/admin/dashboard")
        .exchange()
        .assertThat()
        .hasStatus(HttpStatus.OK)
        .hasViewName("admin/dashboard")
        .model()
        .containsKey("stats")
        .extractingByKey("stats")
        .satisfies(raw -> {
          DashboardStatsDto stats = (DashboardStatsDto) raw;
          assertThat(stats.totalPostCount()).isEqualTo(13L);
          assertThat(stats.publishedPostCount()).isEqualTo(10L);
          assertThat(stats.draftPostCount()).isEqualTo(3L);
          assertThat(stats.todayVisitorCount()).isEqualTo(42L);
          assertThat(stats.totalVisitorCount()).isEqualTo(1234L);
        });
  }

  @Test
  @DisplayName("대시보드 요청 시 PostService와 VisitService가 호출된다")
  @WithMockUser(authorities = "ADMIN")
  void whenGetDashboard_thenCallsServicesForStats() {
    // given
    given(postService.getPublishedPostCount()).willReturn(0L);
    given(postService.getDraftPostCount()).willReturn(0L);
    given(visitService.getTodayVisitorCount()).willReturn(0L);
    given(visitService.getTotalVisitorCount()).willReturn(0L);

    // when
    mockMvcTester.get().uri("/admin/dashboard").exchange();

    // then
    then(postService).should().getPublishedPostCount();
    then(postService).should().getDraftPostCount();
    then(visitService).should().getTodayVisitorCount();
    then(visitService).should().getTotalVisitorCount();
  }
}
