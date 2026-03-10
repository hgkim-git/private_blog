package io.github.hgkimer.privateblog.web.controller;

import io.github.hgkimer.privateblog.service.CategoryService;
import io.github.hgkimer.privateblog.service.MarkdownService;
import io.github.hgkimer.privateblog.service.PostService;
import io.github.hgkimer.privateblog.web.dto.response.CategoryResponseDto;
import io.github.hgkimer.privateblog.web.dto.response.PostDetailResponseDto;
import io.github.hgkimer.privateblog.web.dto.response.PostSummaryResponseDto;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@Validated
public class BlogController {

  private final PostService postService;
  private final CategoryService categoryService;
  private final MarkdownService markdownService;

  @GetMapping("/posts")
  public String posts(
      @RequestParam(required = false) @Pattern(regexp = "^[a-z0-9-]+$") String categorySlug,
      @RequestParam(required = false) @Size(max = 50) String keyword,
      @PageableDefault(size = 10, direction = Sort.Direction.DESC, sort = "createdAt") Pageable pageable,
      Model model) {
    Page<PostSummaryResponseDto> page = categorySlug == null ? postService.getPostList(keyword,
        pageable) : postService.getCategorizedPostList(categorySlug, keyword, pageable);
    int pagSetSize = 5;
    int currentPage = page.getNumber();
    int startPage = (currentPage / pagSetSize) * pagSetSize;
    int endPage = Math.min(startPage + pagSetSize - 1, page.getTotalPages() - 1);
    model.addAttribute("posts", page.getContent());
    model.addAttribute("page", page);
    model.addAttribute("startPage", startPage);
    model.addAttribute("endPage", endPage);

    List<CategoryResponseDto> categories = categoryService.getAllCategories();
    model.addAttribute("categories", categories);

    StringBuilder queryParams = new StringBuilder();
    if (categorySlug != null) {
      queryParams.append("categorySlug=").append(categorySlug);
    }
    if (keyword != null) {
      if (!queryParams.isEmpty()) {
        queryParams.append("&");
      }
      queryParams.append("keyword=").append(keyword);
    }
    if (!queryParams.isEmpty()) {
      queryParams.append("&");
    }
    model.addAttribute("baseURL", "/posts?" + queryParams.toString());
    return "blog/main";
  }

  @GetMapping("/posts/{slug}")
  public String postDetail(@PathVariable @Pattern(regexp = "^[a-z0-9-]+$") String slug,
      Model model) {
    PostDetailResponseDto post = postService.getPostBySlug(slug);
    model.addAttribute("post", post);
    String contentHtml = post.content();
    model.addAttribute("tocHtml", markdownService.getTocHtml(contentHtml));
    return "blog/post-detail";
  }
}
