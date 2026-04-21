package io.github.hgkimer.privateblog.web.controller;

import io.github.hgkimer.privateblog.domain.entity.Post;
import io.github.hgkimer.privateblog.service.PostService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequiredArgsConstructor
public class SitemapController {

  private final PostService postService;
  private final String domain = "https://hgkimer.me";

  @GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
  @ResponseBody
  @Cacheable("sitemap")
  public String getSitemap() {
    List<Post> posts = postService.getAllPublishedPosts();

    StringBuilder sitemap = new StringBuilder();
    sitemap.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    sitemap.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");
    sitemap.append("<url><loc>").append(domain).append("</loc></url>\n");
    posts.forEach(post -> {
      sitemap.append("<url>\n");
      sitemap.append("<loc>").append(domain).append("/posts/").append(post.getSlug())
          .append("</loc>\n");
      sitemap.append("<lastmod>").append(post.getUpdatedAt().toLocalDate()).append("</lastmod>\n");
      sitemap.append("<changefreq>weekly</changefreq>\n");
      sitemap.append("</url>\n");
    });

    sitemap.append("</urlset>\n");
    return sitemap.toString();
  }
}
