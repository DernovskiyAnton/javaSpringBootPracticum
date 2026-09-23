package com.yandex.praktikum.blog.mapper;

import com.yandex.praktikum.blog.dto.CommentDto;
import com.yandex.praktikum.blog.model.Comment;
import org.springframework.stereotype.Component;

@Component
public class CommentMapper {

    public CommentDto toDto(Comment comment) {
        return new CommentDto(
                comment.id(),
                comment.text(),
                comment.postId()
        );
    }
}