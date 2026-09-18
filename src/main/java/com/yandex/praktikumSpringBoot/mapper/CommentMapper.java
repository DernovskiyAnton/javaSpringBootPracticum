package com.yandex.praktikumSpringBoot.mapper;

import com.yandex.praktikumSpringBoot.dto.CommentDto;
import com.yandex.praktikumSpringBoot.model.Comment;
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