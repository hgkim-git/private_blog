package io.github.hgkimer.privateblog.web.controller;

import io.github.hgkimer.privateblog.domain.entity.Category;
import io.github.hgkimer.privateblog.service.CategoryService;
import io.github.hgkimer.privateblog.web.dto.request.CategoryCreateDto;
import io.github.hgkimer.privateblog.web.dto.request.CategoryReorderDto;
import io.github.hgkimer.privateblog.web.dto.request.CategoryUpdateDto;
import io.github.hgkimer.privateblog.web.dto.response.CategoryResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Category", description = "카테고리 API")
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Validated
public class CategoryApiController {

  private final CategoryService categoryService;

  @Operation(summary = "카테고리 생성")
  @PostMapping()
  public ResponseEntity<CategoryResponseDto> createCategory(
      @RequestBody @Valid CategoryCreateDto createDto) {
    Category category = Category.of(createDto.name(), createDto.slug());
    CategoryResponseDto categoryResponseDto = CategoryResponseDto.from(
        categoryService.createCategory(category));
    return ResponseEntity.status(HttpStatus.CREATED).body(categoryResponseDto);
  }

  @Operation(summary = "카테고리 단건 조회")
  @GetMapping("/{id}")
  public ResponseEntity<CategoryResponseDto> getCategoryById(@PathVariable @Positive Long id) {
    CategoryResponseDto categoryResponseDto = CategoryResponseDto.from(
        categoryService.getCategoryById(id));
    return ResponseEntity.ok(categoryResponseDto);
  }

  @Operation(summary = "카테고리 삭제")
  @DeleteMapping("/{id}")
  public ResponseEntity<Object> deleteCategory(@PathVariable @Positive Long id) {
    categoryService.deleteCategory(id);
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "카테고리 수정")
  @PatchMapping("/{id}")
  public ResponseEntity<CategoryResponseDto> updateCategory(@PathVariable @Positive Long id,
      @Valid @RequestBody CategoryUpdateDto updateDto) {
    Category category = Category.of(updateDto.name(), updateDto.slug());
    CategoryResponseDto categoryResponseDto = CategoryResponseDto.from(
        categoryService.updateCategory(id, category));
    return ResponseEntity.ok(categoryResponseDto);
  }

  @Operation(summary = "카테고리 순서 변경")
  @PutMapping("/reorder")
  public ResponseEntity<Void> reorderCategories(
      @RequestBody List<@Valid CategoryReorderDto> orders) {
    categoryService.reorderCategories(orders);
    return ResponseEntity.noContent().build();
  }

}
