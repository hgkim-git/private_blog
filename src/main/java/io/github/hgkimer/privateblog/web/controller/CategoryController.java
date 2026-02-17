package io.github.hgkimer.privateblog.web.controller;

import io.github.hgkimer.privateblog.domain.entity.Category;
import io.github.hgkimer.privateblog.service.CategoryService;
import io.github.hgkimer.privateblog.web.dto.request.CategoryCreateDto;
import io.github.hgkimer.privateblog.web.dto.request.CategoryUpdateDto;
import io.github.hgkimer.privateblog.web.dto.response.CategoryResponseDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
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

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@Validated
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping()
    public ResponseEntity<CategoryResponseDto> createCategory(
        @RequestBody @Valid CategoryCreateDto createDto) {
        Category category = Category.of(createDto.name(), createDto.slug());
        CategoryResponseDto categoryResponseDto = CategoryResponseDto.from(
            categoryService.createCategory(category));
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryResponseDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponseDto> getCategoryById(@PathVariable @Positive Long id) {
        CategoryResponseDto categoryResponseDto = CategoryResponseDto.from(
            categoryService.getCategoryById(id));
        return ResponseEntity.ok(categoryResponseDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteCategory(@PathVariable @Positive Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CategoryResponseDto> updateCategory(@PathVariable @Positive Long id,
        @Valid @RequestBody CategoryUpdateDto updateDto) {
        Category category = Category.of(updateDto.name(), updateDto.slug(),
            updateDto.displayOrder());
        CategoryResponseDto categoryResponseDto = CategoryResponseDto.from(
            categoryService.updateCategory(id, category));
        return ResponseEntity.ok(categoryResponseDto);
    }


}
