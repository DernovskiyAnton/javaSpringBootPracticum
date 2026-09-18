package com.yandex.praktikumSpringBoot.dto;

import java.util.List;

public record PostDto(
        long id,
        String title,
        String text,
        List<String> tags,
        int likesCount,
        int commentsCount
) {
}