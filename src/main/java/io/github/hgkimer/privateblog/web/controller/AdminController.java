package io.github.hgkimer.privateblog.web.controller;

import io.github.hgkimer.privateblog.domain.entity.Tag;
import io.github.hgkimer.privateblog.service.CategoryService;
import io.github.hgkimer.privateblog.service.TagService;
import io.github.hgkimer.privateblog.web.dto.response.CategoryResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.RequiredArgsConstructor;
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

  @ModelAttribute("currentUri")
  public String currentUri(HttpServletRequest request) {
    return request.getRequestURI();
  }

  @GetMapping("/dashboard")
  public String dashboard(Model model) {
    return "admin/dashboard";
  }

  @GetMapping("/posts")
  public String postManagement(@RequestParam(required = false) @Size(max = 50) String keyword,
      @PageableDefault(size = 5, direction = Sort.Direction.DESC, sort = "createdAt") Pageable pageable,
      Model model) {

    return "admin/posts";
  }

  @GetMapping("/posts/form")
  public String postForm(Model model) {
    List<CategoryResponseDto> categories = categoryService.getAllCategories();
    model.addAttribute("categories", categories);
    List<Tag> tags = tagService.getAllTags();
    model.addAttribute("tags", tags);
    return "admin/post-form";
  }

  @GetMapping("/categories")
  public String categoryManagement(Model model) {
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
