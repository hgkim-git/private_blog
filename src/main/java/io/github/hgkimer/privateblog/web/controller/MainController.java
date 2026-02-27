package io.github.hgkimer.privateblog.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class MainController {

  @RequestMapping("")
  public String landing() {
    return "redirect:/posts";
  }

  @RequestMapping("/admin")
  public String adminLanding() {
    return "redirect:/admin/dashboard";
  }
}
