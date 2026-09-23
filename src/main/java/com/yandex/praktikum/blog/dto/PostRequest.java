package com.yandex.praktikum.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record PostRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 255, message = "Title must not exceed 255 characters")
        String title,

        @NotBlank(message = "Text is required")
        String text,

        @NotNull(message = "Tags list cannot be null")
        List<@NotBlank(message = "Tag cannot be blank") String> tags
) {
}
