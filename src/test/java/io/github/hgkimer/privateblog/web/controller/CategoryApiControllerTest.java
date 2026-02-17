package io.github.hgkimer.privateblog.web.controller;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import io.github.hgkimer.privateblog.domain.entity.Category;
import io.github.hgkimer.privateblog.service.CategoryService;
import io.github.hgkimer.privateblog.web.dto.response.CategoryResponseDto;
import io.github.hgkimer.privateblog.web.exception.ErrorCode;
import io.github.hgkimer.privateblog.web.exception.ErrorResponse;
import io.github.hgkimer.privateblog.web.exception.FieldErrorResponse;
import io.github.hgkimer.privateblog.web.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@WebMvcTest(CategoryApiController.class)
class CategoryApiControllerTest {

    private final String uriRoot = "/categories";
    @Autowired
    private MockMvcTester mockMvcTester;
    @MockitoBean
    private CategoryService categoryService;
    private Category category;

    @BeforeEach
    void setUp() {
        category = Category.builder()
            .name("Test Category")
            .slug("test-category")
            .build();
    }

    @Test
    void givenValidCategory_whenCreateCategory_thenReturnCreatedCategory() {
        given(categoryService.createCategory(any())).willReturn(category);
        String json = """
            {
             "name":"Test Category",
             "slug":"test-category"
            }
            """;
        mockMvcTester.post().uri(uriRoot)
            .content(json)
            .contentType(MediaType.APPLICATION_JSON)
            .exchange()
            .assertThat()
            .hasStatus(HttpStatus.CREATED)
            .bodyJson()
            .convertTo(CategoryResponseDto.class)
            .satisfies(response -> {
                assertThat(response.name()).isEqualTo(category.getName());
                assertThat(response.slug()).isEqualTo(category.getSlug());
            });
    }

    @Test
    void givenInvalidCategory_whenCreateCategory_thenThrowBadRequest() {
        String json = """
            {
                "name":"Invalid Category",
                "slug":"부적절한 카테고리 slug"
            }
            """;
        mockMvcTester.post().uri(uriRoot)
            .content(json)
            .contentType(MediaType.APPLICATION_JSON)
            .exchange()
            .assertThat()
            .hasStatus(HttpStatus.BAD_REQUEST)
            .bodyJson().convertTo(ErrorResponse.class)
            .satisfies(response -> {
                assertThat(response.fieldErrors()).isNotEmpty().hasSize(1);
                FieldErrorResponse fieldError = response.fieldErrors().get(0);
                assertThat(fieldError.field()).isEqualTo("slug");
            });
    }

    @Test
    void givenId_whenGetCategory_thenOk() {
        given(categoryService.getCategoryById(any())).willReturn(category);
        mockMvcTester.get().uri(uriRoot + "/1")
            .exchange()
            .assertThat()
            .hasStatus(HttpStatus.OK)
            .bodyJson()
            .convertTo(CategoryResponseDto.class)
            .satisfies(response -> {
                assertThat(response.name()).isEqualTo(category.getName());
                assertThat(response.slug()).isEqualTo(category.getSlug());
            });
    }

    @Test
    void givenWrongId_whenGetCategory_thenThrowNotFound() {
        given(categoryService.getCategoryById(any())).willThrow(new ResourceNotFoundException(
            ErrorCode.CATEGORY_NOT_FOUND));
        mockMvcTester.get().uri(uriRoot + "/100")
            .exchange()
            .assertThat()
            .hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void givenId_whenDeleteCategory_thenResponseNoContent() {
        mockMvcTester.delete().uri(uriRoot + "/1")
            .exchange()
            .assertThat()
            .hasStatus(HttpStatus.NO_CONTENT);
    }

    @Test
    void givenValidParam_whenUpdateCategory_thenOk() {
        Category updated = Category.builder().name("updated").slug("updated-category").build();
        given(categoryService.updateCategory(any(), any())).willReturn(updated);
        String json = """
            {
             "name":"updated",
             "slug":"updated-category",
             "displayOrder":1
            }
            """;
        mockMvcTester.patch().uri(uriRoot + "/1")
            .content(json)
            .contentType(MediaType.APPLICATION_JSON)
            .exchange()
            .assertThat()
            .hasStatus(HttpStatus.OK)
            .bodyJson().convertTo(CategoryResponseDto.class)
            .satisfies(response -> {
                assertThat(response.name()).isEqualTo("updated");
                assertThat(response.slug()).isEqualTo("updated-category");
            });

    }

}