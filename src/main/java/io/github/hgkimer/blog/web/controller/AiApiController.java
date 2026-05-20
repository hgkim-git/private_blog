package io.github.hgkimer.blog.web.controller;

import io.github.hgkimer.blog.service.AiSummaryService;
import io.github.hgkimer.blog.web.dto.request.AiSummarizeRequestDto;
import io.github.hgkimer.blog.web.dto.response.AiSummaryResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "AI", description = "AI 기능 API")
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiApiController {

  private final AiSummaryService aiSummaryService;

  @Operation(summary = "게시글 AI 요약")
  @PostMapping("/summarize")
  public ResponseEntity<AiSummaryResponseDto> summarize(
      @RequestBody @Valid AiSummarizeRequestDto dto) {
    String summary = aiSummaryService.summarize(dto.content());
    return ResponseEntity.ok(new AiSummaryResponseDto(summary));
  }
}
