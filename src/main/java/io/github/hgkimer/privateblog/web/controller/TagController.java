package io.github.hgkimer.privateblog.web.controller;

import io.github.hgkimer.privateblog.domain.entity.Tag;
import io.github.hgkimer.privateblog.service.TagService;
import io.github.hgkimer.privateblog.web.dto.request.TagCreateDto;
import io.github.hgkimer.privateblog.web.dto.request.TagUpdateDto;
import io.github.hgkimer.privateblog.web.dto.response.TagResponseDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
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
@Validated
public class TagController {

    private final TagService tagService;

    @PostMapping()
    public ResponseEntity<TagResponseDto> createTag(@RequestBody @Valid TagCreateDto dto) {
        Tag tag = Tag.of(dto.name(), dto.slug());
        TagResponseDto responseDto = TagResponseDto.from(tagService.createTag(tag));
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteTag(@PathVariable @Positive Long id) {
        tagService.deleteTag(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TagResponseDto> updateTag(@PathVariable @Positive Long id,
        @Valid @RequestBody TagUpdateDto dto) {
        Tag tag = Tag.of(dto.name(), dto.slug());
        TagResponseDto responseDto = TagResponseDto.from(tagService.updateTag(id, tag));
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TagResponseDto> getTagById(@PathVariable @Positive Long id) {
        TagResponseDto responseDto = TagResponseDto.from(tagService.getTagById(id));
        return ResponseEntity.ok(responseDto);
    }


}
