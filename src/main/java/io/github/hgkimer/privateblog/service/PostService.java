package io.github.hgkimer.privateblog.service;

import io.github.hgkimer.privateblog.domain.entity.Category;
import io.github.hgkimer.privateblog.domain.entity.Post;
import io.github.hgkimer.privateblog.domain.entity.PostTag;
import io.github.hgkimer.privateblog.domain.entity.Tag;
import io.github.hgkimer.privateblog.domain.entity.User;
import io.github.hgkimer.privateblog.domain.enums.PostStatus;
import io.github.hgkimer.privateblog.persistence.jpa.CategoryRepository;
import io.github.hgkimer.privateblog.persistence.jpa.PostRepository;
import io.github.hgkimer.privateblog.persistence.jpa.TagRepository;
import io.github.hgkimer.privateblog.persistence.jpa.UserRepository;
import io.github.hgkimer.privateblog.web.dto.request.PostCreateDto;
import io.github.hgkimer.privateblog.web.dto.request.PostUpdateDto;
import io.github.hgkimer.privateblog.web.dto.response.PostDetailResponseDto;
import io.github.hgkimer.privateblog.web.dto.response.PostSummaryResponseDto;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

@Service
@RequiredArgsConstructor
@Transactional
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;

    public Post createPost(PostCreateDto postCreateDto) {
        validate(postCreateDto);
        if (postRepository.existsBySlug(postCreateDto.slug())) {
            throw new IllegalArgumentException("Slug already exists");
        }

        Post post = convertToPost(postCreateDto);
        List<Tag> tags = tagRepository.findTagByIdIn(postCreateDto.tagsIds());
        addTags(post, tags);
        return postRepository.save(post);
    }

    public Post updatePost(Long id, PostUpdateDto postUpdateDto) {
        Post post = postRepository.findByIdWithDetails(id);
        if (post == null) {
            throw new IllegalArgumentException("Post not found");
        }
        validate(postUpdateDto);

        String slug = postUpdateDto.slug();
        if (!slug.equals(post.getSlug()) && postRepository.existsBySlug(postUpdateDto.slug())) {
            throw new IllegalArgumentException("Slug already exists");
        }

        Long categoryId = postUpdateDto.categoryId();
        Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        post.update(postUpdateDto.title(),
            postUpdateDto.content(),
            postUpdateDto.summary(),
            postUpdateDto.slug(),
            postUpdateDto.status(),
            category
        );
        List<Tag> tags = tagRepository.findTagByIdIn(postUpdateDto.tagsIds());
        addTags(post, tags);
        return post;
    }

    public void deletePost(Long id) {
        postRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public PostDetailResponseDto getPostBySlug(String slug) {
        Post post = postRepository.findBySlugWithDetails(slug)
            .orElseThrow(() -> new IllegalArgumentException("Post not found"));
        post.increaseViewCount();
        return PostDetailResponseDto.from(post);
    }

    @Transactional(readOnly = true)
    public Page<PostSummaryResponseDto> getPostList(String categorySlug, String keyword,
        Pageable pageable) {
        if (categorySlug != null) {
            Long categoryId = categoryRepository.findBySlug(categorySlug)
                .orElseThrow(() -> new IllegalArgumentException("Category not found")).getId();
            return postRepository.findAllPostsByCategoryId(PostStatus.PUBLISHED, categoryId,
                keyword, pageable).map(PostSummaryResponseDto::map);
        } else {
            return postRepository.findAllPosts(PostStatus.PUBLISHED, keyword, pageable).map(
                PostSummaryResponseDto::map);
        }
    }

    private void validate(PostCreateDto dto) {
        userRepository.findByEmail(dto.author())
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (dto.categoryId() != null) {
            categoryRepository.findById(dto.categoryId()).orElseThrow(
                () -> new IllegalArgumentException("Category not found")
            );
        }
        if (!dto.tagsIds().isEmpty()) {
            Assert.notEmpty(tagRepository.findTagByIdIn(dto.tagsIds()), "Tags not found");
        }
    }

    private void validate(PostUpdateDto dto) {
        if (dto.categoryId() != null) {
            categoryRepository.findById(dto.categoryId()).orElseThrow(
                () -> new IllegalArgumentException("Category not found")
            );
        }
        if (!dto.tagsIds().isEmpty()) {
            Assert.notEmpty(tagRepository.findTagByIdIn(dto.tagsIds()), "Tags not found");
        }
    }

    private void addTags(Post post, List<Tag> tags) {
        List<PostTag> postTags = tags.stream()
            .map(tag -> PostTag.builder().post(post).tag(tag).build())
            .collect(Collectors.toList());
        post.addTags(postTags);
    }

    private User getRequiredUser(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    private Category getOptionalCategory(Long categoryId) {
        return Optional.ofNullable(categoryId)
            .flatMap(categoryRepository::findById).orElse(null);
    }

    private Post convertToPost(PostCreateDto postCreateDto) {
        User user = getRequiredUser(postCreateDto.author());
        Category category = getOptionalCategory(postCreateDto.categoryId());
        return Post.of(category, user, postCreateDto.title(),
            postCreateDto.content(),
            postCreateDto.summary(),
            postCreateDto.slug(),
            PostStatus.valueOf(postCreateDto.status().toUpperCase())
        );
    }

}
