package com.yandex.praktikumSpringBoot.model;

import java.util.List;

public record Post(
        long id,
        String title,
        String text,
        List<String> tags,
        int likesCount,
        int commentsCount
) {
}