package io.github.hgkimer.privateblog.service;

import io.github.hgkimer.privateblog.domain.entity.Category;
import io.github.hgkimer.privateblog.persistence.jpa.CategoryRepository;
import io.github.hgkimer.privateblog.web.dto.request.CategoryReorderDto;
import io.github.hgkimer.privateblog.web.dto.response.CategoryResponseDto;
import io.github.hgkimer.privateblog.web.exception.BusinessException;
import io.github.hgkimer.privateblog.web.exception.ErrorCode;
import io.github.hgkimer.privateblog.web.exception.ResourceNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
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
    if (!categoryRepository.existsById(id)) {
      throw new ResourceNotFoundException(ErrorCode.CATEGORY_NOT_FOUND, id.toString());
    }
    categoryRepository.deleteById(id);
  }

  public Category updateCategory(Long id, Category target) {
    Category category = getCategoryById(id);
    category.update(target.getName(), target.getSlug());
    return category;
  }

  @Transactional(readOnly = true)
  public List<CategoryResponseDto> getAllCategories() {
    return categoryRepository.findAllByOrderByDisplayOrderAscCreatedAtDesc().stream()
        .map(CategoryResponseDto::from).toList();
  }

  public void reorderCategories(List<CategoryReorderDto> orders) {
    // 중복 ID 검사
    Set<Long> requestIds = orders.stream().map(CategoryReorderDto::id).collect(Collectors.toSet());
    if (requestIds.size() != orders.size()) {
      throw new BusinessException(ErrorCode.INVALID_INPUT, "Duplicate category ids in request");
    }

    // 중복 displayOrder 검사
    Set<Integer> requestOrders = orders.stream().map(CategoryReorderDto::displayOrder)
        .collect(Collectors.toSet());
    if (requestOrders.size() != orders.size()) {
      throw new BusinessException(ErrorCode.INVALID_INPUT, "Duplicate display orders in request");
    }

    // 전체 카테고리 조회 및 완전성 검사 (일부만 보낸 경우 차단)
    List<Category> allCategories = categoryRepository.findAll();
    Set<Long> allIds = allCategories.stream().map(Category::getId).collect(Collectors.toSet());
    if (!requestIds.equals(allIds)) {
      throw new BusinessException(ErrorCode.INVALID_INPUT, "Request must include all categories");
    }

    Map<Long, Category> categoryMap = allCategories.stream()
        .collect(Collectors.toMap(Category::getId, c -> c));
    for (CategoryReorderDto order : orders) {
      categoryMap.get(order.id()).updateDisplayOrder(order.displayOrder());
    }
  }

  public Category getCategoryById(Long id) {
    return categoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
        ErrorCode.CATEGORY_NOT_FOUND, id.toString()));
  }

  public Category getCategoryBySlug(String slug) {
    return categoryRepository.findBySlug(slug).orElseThrow(() -> new ResourceNotFoundException(
        ErrorCode.CATEGORY_NOT_FOUND, slug));
  }

}