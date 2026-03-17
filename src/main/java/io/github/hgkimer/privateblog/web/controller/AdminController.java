package io.github.hgkimer.privateblog.web.controller;

import io.github.hgkimer.privateblog.domain.entity.Tag;
import io.github.hgkimer.privateblog.domain.enums.PostStatus;
import io.github.hgkimer.privateblog.service.CategoryService;
import io.github.hgkimer.privateblog.service.PostService;
import io.github.hgkimer.privateblog.service.TagService;
import io.github.hgkimer.privateblog.web.dto.response.CategoryResponseDto;
import io.github.hgkimer.privateblog.web.dto.response.PostDetailResponseDto;
import io.github.hgkimer.privateblog.web.dto.response.PostSummaryResponseDto;
import jakarta.servlet.http.HttpServletRequest;
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
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
@Validated
public class AdminController {

  private final CategoryService categoryService;
  private final TagService tagService;
  private final PostService postService;

  @ModelAttribute("currentUri")
  public String currentUri(HttpServletRequest request) {
    return request.getRequestURI();
  }

  @GetMapping("/dashboard")
  public String dashboard(Model model) {
    return "admin/dashboard";
  }

  @GetMapping("/posts")
  public String postManagement(
      @RequestParam(required = false, name = "category") @Pattern(regexp = "^[a-z0-9-]+$") String categorySlug,
      @RequestParam(required = false, name = "status") @Pattern(regexp = "^(?i)(DRAFT|PUBLISHED)$") String statusText,
      @RequestParam(required = false) @Size(max = 50) String keyword,
      @PageableDefault(size = 5, direction = Sort.Direction.DESC, sort = "createdAt") Pageable pageable,
      Model model) {
    Long categoryId =
        (categorySlug != null) ? categoryService.getCategoryBySlug(categorySlug).getId() : null;
    PostStatus status = (statusText != null) ? PostStatus.valueOf(statusText.toUpperCase()) : null;

    Page<PostSummaryResponseDto> page = postService.getAllPosts(categoryId, status, keyword,
        pageable);
    int pageSetSize = 5;
    int lastPageNum = page.getTotalPages() > 0 ? page.getTotalPages() - 1 : 0;
    int currentPage = Math.min(page.getNumber(), lastPageNum);
    int startPage = (currentPage / pageSetSize) * pageSetSize;
    int endPage = Math.min(startPage + pageSetSize - 1, lastPageNum);
    model.addAttribute("posts", page.getContent());
    model.addAttribute("page", page);
    model.addAttribute("startPage", startPage);
    model.addAttribute("endPage", endPage);
    List<CategoryResponseDto> categories = categoryService.getAllCategories();
    model.addAttribute("categories", categories);

    StringBuilder queryParams = new StringBuilder();
    if (categorySlug != null) {
      queryParams.append("category=").append(categorySlug);
    }
    if (keyword != null) {
      if (!queryParams.isEmpty()) {
        queryParams.append("&");
      }
      queryParams.append("keyword=").append(keyword);
    }
    if (statusText != null) {
      if (!queryParams.isEmpty()) {
        queryParams.append("&");
      }
      queryParams.append("status=").append(statusText);
    }
    if (!queryParams.isEmpty()) {
      queryParams.append("&");
    }
    model.addAttribute("baseURL", "/admin/posts?" + queryParams.toString());
    return "admin/posts";
  }

  @GetMapping("/posts/form")
  public String postForm(@RequestParam(required = false) Long id, Model model) {
    List<CategoryResponseDto> categories = categoryService.getAllCategories();
    model.addAttribute("categories", categories);
    List<Tag> tags = tagService.getAllTags();
    model.addAttribute("tags", tags);
    if (id != null) {
      PostDetailResponseDto post = postService.getPostById(id);
      model.addAttribute("post", post);
    }
    return "admin/post-form";
  }

  @GetMapping("/categories")
  public String categoryManagement(Model model) {
    List<CategoryResponseDto> categories = categoryService.getAllCategories();
    model.addAttribute("categories", categories);
    return "admin/categories";
  }

  @GetMapping("/tags")
  public String tagManagement(Model model) {
    return "admin/tags";
  }

//  @GetMapping("/comments")
//  public String commentManagement(Model model) {
//    return "admin/comments";
//  }
}
