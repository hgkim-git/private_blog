package io.github.hgkimer.privateblog.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class MainController {

  @RequestMapping("/")
  public String landing() {
    return "redirect:/posts";
  }

  @RequestMapping("/admin")
  public String adminLanding() {
    return "redirect:/admin/posts";
  }

  @GetMapping("/admin/login")
  public String login(
      @RequestParam(name = "logout", required = false) Boolean logout,
      Model model
  ) {
    if (logout != null && logout) {
      model.addAttribute("logoutMessage", "로그아웃되었습니다.");
    }
    return "login";
  }
}
