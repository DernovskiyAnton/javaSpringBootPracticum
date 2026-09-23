package com.yandex.praktikum.blog.service;

import com.yandex.praktikum.blog.dto.CommentDto;
import com.yandex.praktikum.blog.dto.CommentRequest;
import com.yandex.praktikum.blog.exception.ResourceNotFoundException;
import com.yandex.praktikum.blog.mapper.CommentMapper;
import com.yandex.praktikum.blog.model.Comment;
import com.yandex.praktikum.blog.repository.CommentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CommentService {

    private static final String RESOURCE_NAME = "Comment";

    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;

    public CommentService(CommentRepository commentRepository, CommentMapper commentMapper) {
        this.commentRepository = commentRepository;
        this.commentMapper = commentMapper;
    }

    public List<CommentDto> getAllByPostId(long postId) {
        return commentRepository.findAllByPostId(postId)
                .stream()
                .map(commentMapper::toDto)
                .toList();
    }

    public CommentDto getById(long id) {
        return commentMapper.toDto(findCommentById(id));
    }

    @Transactional
    public CommentDto create(CommentRequest request) {
        Comment comment = new Comment(0L, request.text(), request.postId());
        return commentMapper.toDto(commentRepository.save(comment));
    }

    @Transactional
    public CommentDto update(long id, CommentRequest request) {
        Comment comment = new Comment(id, request.text(), request.postId());
        if (!commentRepository.update(comment)) {
            throw new ResourceNotFoundException(RESOURCE_NAME, id);
        }
        return commentMapper.toDto(findCommentById(id));
    }

    @Transactional
    public void delete(long id) {
        if (!commentRepository.deleteById(id)) {
            throw new ResourceNotFoundException(RESOURCE_NAME, id);
        }
    }

    private Comment findCommentById(long id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(RESOURCE_NAME, id));
    }

}
