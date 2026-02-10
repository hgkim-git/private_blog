package io.github.hgkimer.privateblog.web.controller;


import io.github.hgkimer.privateblog.service.PostService;
import io.github.hgkimer.privateblog.web.dto.request.PostCreateDto;
import io.github.hgkimer.privateblog.web.dto.request.PostUpdateDto;
import io.github.hgkimer.privateblog.web.dto.response.PostDetailResponseDto;
import io.github.hgkimer.privateblog.web.dto.response.PostSummaryResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping()
    public PostDetailResponseDto createPost(@RequestBody PostCreateDto postCreateDto)
        throws RuntimeException {
        // TODO: Authorization check
        // TODO: validate
        // TODO: 새로운 태그는 클라이언트에서 먼저 처리하여 게시글 생성 요청에는 모두 존재하는 태그만 전달
        return PostDetailResponseDto.from(postService.createPost(postCreateDto));
    }

    @DeleteMapping("/{id}")
    public void deletePost(@PathVariable Long id) {
        postService.deletePost(id);
    }

    @PatchMapping("/{id}")
    public PostDetailResponseDto updatePost(@PathVariable Long id,
        @RequestBody PostUpdateDto postUpdateDto) {
        // TODO: Authorization check
        // TODO: validate
        return PostDetailResponseDto.from(postService.updatePost(id, postUpdateDto));
    }

    @GetMapping("/{slug}")
    public PostDetailResponseDto getPostBySlug(@PathVariable String slug) {
        return postService.getPostBySlug(slug);
    }

    @GetMapping("")
    public Page<PostSummaryResponseDto> getAllPosts(
        @RequestParam(required = false) String categorySlug,
        @RequestParam(required = false) String keyword,
        // size=10(default), page=0(default)
        @PageableDefault(direction = Sort.Direction.DESC, sort = "createdAt")
        Pageable pageable
    ) {
        return postService.getPostList(categorySlug, keyword, pageable);
    }

}
