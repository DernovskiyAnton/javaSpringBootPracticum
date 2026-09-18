package com.yandex.praktikumSpringBoot.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.yandex.praktikumSpringBoot.dto.PostDto;
import com.yandex.praktikumSpringBoot.dto.PostPage;
import com.yandex.praktikumSpringBoot.dto.PostRequest;
import com.yandex.praktikumSpringBoot.service.PostService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    @GetMapping
    public PostPage findAll(
            @RequestParam(value = "search", defaultValue = "") String search,
            @RequestParam(value = "pageNumber", defaultValue = "1") int pageNumber,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        return postService.getAllPosts(search, pageNumber, pageSize);
    }

    @GetMapping("/{id}")
    public PostDto findById(@PathVariable("id") long id) {
        return postService.getPostById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PostDto create(@Valid @RequestBody PostRequest request) {
        return postService.createPost(request);
    }

    @PutMapping("/{id}")
    public PostDto update(@PathVariable long id, @Valid @RequestBody PostRequest request) {
        return postService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") long id) {
        postService.delete(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/likes")
    public int addLike(@PathVariable("id") long id) {
        return postService.addLike(id);
    }

    @PutMapping("/{id}/image")
    public ResponseEntity<Void> updateImage(
            @PathVariable("id") long id,
            @RequestParam("image") MultipartFile image) {
        postService.updateImage(id, image);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/image")
    public ResponseEntity<byte[]> getImage(@PathVariable("id") long id) {
        byte[] image = postService.getImage(id);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(image);
    }


}