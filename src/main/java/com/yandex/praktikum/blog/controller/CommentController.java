package com.yandex.praktikum.blog.controller;

import jakarta.validation.Valid;
import com.yandex.praktikum.blog.dto.CommentDto;
import com.yandex.praktikum.blog.dto.CommentRequest;
import com.yandex.praktikum.blog.service.CommentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@Validated
@RestController
@RequestMapping("/api/posts/{postId}/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping
    public List<CommentDto> findAll(@PathVariable("postId") long postId) {
        return commentService.getAllByPostId(postId);
    }

    @GetMapping("/{id}")
    public CommentDto findById(@PathVariable("id") long id) {
        return commentService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto create(@Valid @RequestBody CommentRequest request) {
        return commentService.create(request);
    }

    @PutMapping("/{id}")
    public CommentDto update(@PathVariable("id") long id,
                             @Valid @RequestBody CommentRequest request) {
        return commentService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") long id) {
        commentService.delete(id);
        return ResponseEntity.ok().build();
    }
}