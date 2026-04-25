package io.github.hgkimer.privateblog.web.controller;

import io.github.hgkimer.privateblog.domain.entity.Tag;
import io.github.hgkimer.privateblog.service.TagService;
import io.github.hgkimer.privateblog.web.dto.request.TagCreateDto;
import io.github.hgkimer.privateblog.web.dto.request.TagUpdateDto;
import io.github.hgkimer.privateblog.web.dto.response.TagResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@io.swagger.v3.oas.annotations.tags.Tag(name = "Tag", description = "태그 API")
@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
@Validated
public class TagApiController {

  private final TagService tagService;

  @Operation(summary = "태그 생성")
  @PostMapping()
  public ResponseEntity<TagResponseDto> createTag(@RequestBody @Valid TagCreateDto dto) {
    Tag tag = Tag.of(dto.name(), dto.slug());
    TagResponseDto responseDto = TagResponseDto.from(tagService.createTag(tag));
    return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
  }

  @Operation(summary = "태그 삭제")
  @DeleteMapping("/{id}")
  public ResponseEntity<Object> deleteTag(@PathVariable @Positive Long id) {
    tagService.deleteTag(id);
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "태그 수정")
  @PatchMapping("/{id}")
  public ResponseEntity<TagResponseDto> updateTag(@PathVariable @Positive Long id,
      @Valid @RequestBody TagUpdateDto dto) {
    Tag tag = Tag.of(dto.name(), dto.slug());
    TagResponseDto responseDto = TagResponseDto.from(tagService.updateTag(id, tag));
    return ResponseEntity.status(HttpStatus.OK).body(responseDto);
  }

  @Operation(summary = "태그 단건 조회")
  @GetMapping("/{id}")
  public ResponseEntity<TagResponseDto> getTagById(@PathVariable @Positive Long id) {
    TagResponseDto responseDto = TagResponseDto.from(tagService.getTagById(id));
    return ResponseEntity.ok(responseDto);
  }

  @Operation(summary = "태그 전체 조회")
  @GetMapping()
  public ResponseEntity<List<TagResponseDto>> getAllTags() {
    return ResponseEntity.ok(tagService.getAllTags());
  }


}
