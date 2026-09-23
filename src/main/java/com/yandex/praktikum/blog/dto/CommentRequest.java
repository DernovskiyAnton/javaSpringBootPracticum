package com.yandex.praktikum.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record CommentRequest(
        @NotBlank(message = "Comment text is required")
        String text,

        @Positive(message = "Post ID must be positive")
        long postId
) {
}