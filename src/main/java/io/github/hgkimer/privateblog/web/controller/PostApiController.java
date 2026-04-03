package io.github.hgkimer.privateblog.web.controller;


import io.github.hgkimer.privateblog.domain.enums.PostStatus;
import io.github.hgkimer.privateblog.service.CategoryService;
import io.github.hgkimer.privateblog.service.PostService;
import io.github.hgkimer.privateblog.web.dto.request.PostCreateDto;
import io.github.hgkimer.privateblog.web.dto.request.PostUpdateDto;
import io.github.hgkimer.privateblog.web.dto.response.PostDetailResponseDto;
import io.github.hgkimer.privateblog.web.dto.response.PostSummaryResponseDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@Validated
public class PostApiController {

  private final PostService postService;
  private final CategoryService categoryService;

  @PostMapping()
  public ResponseEntity<PostDetailResponseDto> createPost(
      @RequestBody @Valid PostCreateDto postCreateDto,
      @AuthenticationPrincipal UserDetails userDetails) {
    PostDetailResponseDto responseDto = PostDetailResponseDto.from(
        postService.createPost(postCreateDto, userDetails.getUsername()));
    return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Object> deletePost(@PathVariable @Positive Long id) {
    postService.deletePost(id);
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{id}")
  public ResponseEntity<PostDetailResponseDto> updatePost(@PathVariable @Positive Long id,
      @Valid @RequestBody PostUpdateDto postUpdateDto) {
    PostDetailResponseDto responseDto = PostDetailResponseDto.from(
        postService.updatePost(id, postUpdateDto));
    return ResponseEntity.ok(responseDto);
  }

  @GetMapping("/{slug}")
  public ResponseEntity<PostDetailResponseDto> getPostBySlug(
      @PathVariable @Pattern(regexp = "^[a-z0-9-]+$") @NotBlank String slug) {
    PostDetailResponseDto post = postService.getPostBySlug(slug);
    return ResponseEntity.ok(post);
  }

  @GetMapping("")
  public ResponseEntity<Page<PostSummaryResponseDto>> getPosts(
      @RequestParam(required = false) @Pattern(regexp = "^[a-z0-9-]+$") String categorySlug,
      @RequestParam(required = false) @Pattern(regexp = "^(?i)(DRAFT|PUBLISHED)$") String statusText,
      @RequestParam(required = false) @Size(max = 50) String keyword,
      // size=10(default), page=0(default)
      @PageableDefault(direction = Sort.Direction.DESC, sort = "createdAt")
      Pageable pageable
  ) {
    Long categoryId =
        (categorySlug != null) ? categoryService.getCategoryBySlug(categorySlug).getId() : null;
    PostStatus status = (statusText != null) ? PostStatus.valueOf(statusText.toUpperCase()) : null;
    Page<PostSummaryResponseDto> list = postService.getAllPosts(categoryId, status, keyword,
        pageable);
    return ResponseEntity.ok(list);
  }


}