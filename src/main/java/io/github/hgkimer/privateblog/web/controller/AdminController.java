package io.github.hgkimer.privateblog.web.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Size;
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
  public String newPostForm(Model model) {
    return "admin/new-post-form";
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
