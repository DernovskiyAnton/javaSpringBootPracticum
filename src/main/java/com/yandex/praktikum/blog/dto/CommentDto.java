package com.yandex.praktikum.blog.dto;

public record CommentDto(
        long id,
        String text,
        long postId
) {
}
