package com.yandex.praktikumSpringBoot.dto;

public record CommentDto(
        long id,
        String text,
        long postId
) {
}
