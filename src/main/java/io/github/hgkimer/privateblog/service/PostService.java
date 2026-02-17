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
import io.github.hgkimer.privateblog.web.exception.DuplicateResourceException;
import io.github.hgkimer.privateblog.web.exception.ErrorCode;
import io.github.hgkimer.privateblog.web.exception.ResourceNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;

    public Post createPost(PostCreateDto postCreateDto) {
        validateUser(postCreateDto.author());
        if (postCreateDto.categoryId() != null) {
            validateCategory(postCreateDto.categoryId());
        }
        validateTags(postCreateDto.tagsIds());
        if (postRepository.existsBySlug(postCreateDto.slug())) {
            throw new DuplicateResourceException(ErrorCode.DUPLICATE_POST_SLUG,
                postCreateDto.slug());
        }

        Post post = convertToPost(postCreateDto);
        List<Tag> tags = tagRepository.findTagByIdIn(postCreateDto.tagsIds());
        addTags(post, tags);
        return postRepository.save(post);
    }

    public Post updatePost(Long id, PostUpdateDto postUpdateDto) {
        Long categoryId = postUpdateDto.categoryId();
        if (categoryId != null) {
            validateCategory(categoryId);
        }
        validateTags(postUpdateDto.tagsIds());

        Post post = postRepository.findByIdWithDetails(id);
        if (post == null) {
            throw new ResourceNotFoundException(ErrorCode.POST_NOT_FOUND, id.toString());
        }

        String slug = postUpdateDto.slug();
        if (!slug.equals(post.getSlug()) && postRepository.existsBySlug(postUpdateDto.slug())) {
            throw new DuplicateResourceException(ErrorCode.DUPLICATE_POST_SLUG);
        }

        post.update(
            postUpdateDto.title(),
            postUpdateDto.content(),
            postUpdateDto.summary(),
            postUpdateDto.slug(),
            postUpdateDto.status(),
            categoryId == null ? null : categoryRepository.findById(categoryId).orElse(null)
        );
        List<Tag> tags = tagRepository.findTagByIdIn(postUpdateDto.tagsIds());
        addTags(post, tags);
        return post;
    }

    public void deletePost(Long id) {
        postRepository.deleteById(id);
    }

    public PostDetailResponseDto getPostBySlug(String slug) {
        Post post = postRepository.findBySlugWithDetails(slug)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.POST_NOT_FOUND, slug));
        post.increaseViewCount();
        return PostDetailResponseDto.from(post);
    }

    @Transactional(readOnly = true)
    public Page<PostSummaryResponseDto> getCategorizedPostList(String categorySlug, String keyword,
        Pageable pageable) {
        Long categoryId = categoryRepository.findBySlug(categorySlug)
            .orElseThrow(
                () -> new ResourceNotFoundException(ErrorCode.CATEGORY_NOT_FOUND, categorySlug))
            .getId();
        return postRepository.findAllPostsByCategoryId(PostStatus.PUBLISHED, categoryId, keyword,
            pageable).map(PostSummaryResponseDto::map);
    }

    @Transactional(readOnly = true)
    public Page<PostSummaryResponseDto> getPostList(String keyword,
        Pageable pageable) {
        return postRepository.findAllPosts(PostStatus.PUBLISHED, keyword, pageable).map(
            PostSummaryResponseDto::map);
    }

    private void validateUser(String email) {
        userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND, email));
    }

    private void validateCategory(Long categoryId) {
        categoryRepository.findById(categoryId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CATEGORY_NOT_FOUND,
                categoryId.toString()));
    }

    private void validateTags(List<Long> tagIds) {
        if (tagIds.isEmpty()) {
            return;
        }
        List<Tag> tags = tagRepository.findTagByIdIn(tagIds);
        if (tagIds.size() != tags.size()) {
            throw new IllegalArgumentException("Tags mismatched");
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
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND, email));
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
