package io.github.hgkimer.blog.web.dto.response;

public record DashboardStatsDto(
    long totalPostCount,
    long publishedPostCount,
    long draftPostCount,
    long todayVisitorCount,
    long totalVisitorCount
) {

}
