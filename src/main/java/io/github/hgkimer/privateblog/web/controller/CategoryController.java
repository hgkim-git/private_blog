package io.github.hgkimer.privateblog.web.controller;

import io.github.hgkimer.privateblog.domain.entity.Category;
import io.github.hgkimer.privateblog.service.CategoryService;
import io.github.hgkimer.privateblog.web.dto.request.CategoryCreateDto;
import io.github.hgkimer.privateblog.web.dto.request.CategoryUpdateDto;
import lombok.RequiredArgsConstructor;
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
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping()
    public Category createCategory(@RequestBody CategoryCreateDto createDto) {
        Category category = Category.of(createDto.name(), createDto.slug());
        return categoryService.createCategory(category);
    }

    @GetMapping("/{id}")
    public Category getCategoryById(@PathVariable Long id) {
        return categoryService.getCategoryById(id);
    }

    @DeleteMapping("/{id}")
    public void deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
    }

    @PatchMapping("/{id}")
    public Category updateCategory(@PathVariable Long id,
        @RequestBody CategoryUpdateDto updateDto) {
        Category category = Category.of(updateDto.name(), updateDto.slug(),
            updateDto.displayOrder());
        return categoryService.updateCategory(id, category);
    }


}
