package io.github.hgkimer.privateblog.web.controller;

import io.github.hgkimer.privateblog.domain.entity.Tag;
import io.github.hgkimer.privateblog.service.TagService;
import io.github.hgkimer.privateblog.web.dto.request.TagCreateDto;
import io.github.hgkimer.privateblog.web.dto.request.TagUpdateDto;
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
@RequestMapping("/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @PostMapping()
    public Tag createTag(@RequestBody TagCreateDto dto) {
        Tag tag = Tag.of(dto.name(), dto.slug());
        return tagService.createTag(tag);
    }

    @DeleteMapping("/{id}")
    public void deleteTag(@PathVariable Long id) {
        tagService.deleteTag(id);
    }

    @PatchMapping("/{id}")
    public Tag updateTag(@PathVariable Long id, @RequestBody TagUpdateDto dto) {
        Tag tag = Tag.of(dto.name(), dto.slug());
        return tagService.updateTag(id, tag);
    }

    @GetMapping("/{id}")
    public Tag getTagById(@PathVariable Long id) {
        return tagService.getTagById(id);
    }


}
