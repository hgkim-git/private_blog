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
import jakarta.annotation.Nullable;
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

  private final MarkdownService markdownService;

  public Post createPost(PostCreateDto postCreateDto) {
//    TODO: Security
//    validateUser(postCreateDto.author());
    if (postCreateDto.categoryId() != null) {
      validateCategory(postCreateDto.categoryId());
    }
    validateTags(postCreateDto.tagIds());
    if (postRepository.existsBySlug(postCreateDto.slug())) {
      throw new DuplicateResourceException(ErrorCode.DUPLICATE_POST_SLUG,
          postCreateDto.slug());
    }
    //TODO: Security
    User user = getRequiredUser("hgkimer@gmail.com");
    Category category = getOptionalCategory(postCreateDto.categoryId());
    String htmlContent = markdownService.convertToHtml(postCreateDto.content());
    Post post = Post.of(
        category,
        user,
        postCreateDto.title(),
        postCreateDto.content(),
        htmlContent,
        postCreateDto.summary(),
        postCreateDto.slug(),
        PostStatus.valueOf(postCreateDto.status().toUpperCase())
    );
    List<Tag> tags = tagRepository.findTagByIdIn(postCreateDto.tagIds());
    addTags(post, tags);
    if (category != null) {
      category.increasePostCount();
    }
    return postRepository.save(post);
  }

  public Post updatePost(Long id, PostUpdateDto postUpdateDto) {
    Long categoryId = postUpdateDto.categoryId();
    if (categoryId != null) {
      validateCategory(categoryId);
    }
    validateTags(postUpdateDto.tagIds());

    Post post = postRepository.findByIdWithDetails(id);
    if (post == null) {
      throw new ResourceNotFoundException(ErrorCode.POST_NOT_FOUND, id.toString());
    }

    String slug = postUpdateDto.slug();
    if (!slug.equals(post.getSlug()) && postRepository.existsBySlug(postUpdateDto.slug())) {
      throw new DuplicateResourceException(ErrorCode.DUPLICATE_POST_SLUG);
    }

    String contentHtml = markdownService.convertToHtml(postUpdateDto.content());
    Category oldCategory = post.getCategory();
    Category category = getOptionalCategory(categoryId);
    post.update(
        postUpdateDto.title(),
        postUpdateDto.content(),
        contentHtml,
        postUpdateDto.summary(),
        postUpdateDto.slug(),
        postUpdateDto.status().toUpperCase(),
        category
    );
    if (oldCategory != null) {
      oldCategory.decreasePostCount();
    }
    if (category != null) {
      category.increasePostCount();
    }
    List<Tag> tags = tagRepository.findTagByIdIn(postUpdateDto.tagIds());
    addTags(post, tags);
    return post;
  }

  public void deletePost(Long id) {
    postRepository.deleteById(id);
  }

  @Transactional(readOnly = true)
  public PostDetailResponseDto getPostById(Long id) {
    Post post = postRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.POST_NOT_FOUND, id.toString()));
    return PostDetailResponseDto.from(post);
  }

  public PostDetailResponseDto getPostBySlug(String slug) {
    Post post = postRepository.findBySlugWithDetails(slug)
        .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.POST_NOT_FOUND, slug));
    post.increaseViewCount();
    return PostDetailResponseDto.from(post);
  }

  @Transactional(readOnly = true)
  public Page<PostSummaryResponseDto> getAllPosts(@Nullable Long categoryId,
      @Nullable PostStatus status,
      @Nullable String keyword,
      Pageable pageable) {
    return postRepository.findAllPosts(categoryId, status, keyword, pageable).map(
        PostSummaryResponseDto::from);
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
    if (tagIds == null || tagIds.isEmpty()) {
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

  @Nullable
  private Category getOptionalCategory(Long categoryId) {
    return Optional.ofNullable(categoryId)
        .flatMap(categoryRepository::findById).orElse(null);
  }

}

