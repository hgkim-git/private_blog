package io.github.hgkimer.privateblog.service;

import io.github.hgkimer.privateblog.domain.entity.Category;
import io.github.hgkimer.privateblog.persistence.jpa.CategoryRepository;
import io.github.hgkimer.privateblog.web.exception.ErrorCode;
import io.github.hgkimer.privateblog.web.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public Category createCategory(Category category) {
        return categoryRepository.save(category);
    }

    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }

    public Category updateCategory(Long id, Category updatedCategory) {
        Category category = getCategoryById(id);
        category.update(updatedCategory.getName(), updatedCategory.getSlug(),
            updatedCategory.getDisplayOrder());
        return category;
    }


    // @Transactional(readOnly = true) 조회용 쿼리 이므로 JPA가 스냅샷을 남기지 않도록 함
    @Transactional(readOnly = true)
    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
            ErrorCode.CATEGORY_NOT_FOUND, id.toString()));
    }

}