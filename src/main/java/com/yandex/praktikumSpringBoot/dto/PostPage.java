package com.yandex.praktikumSpringBoot.dto;

import java.util.List;

public record PostPage(
        List<PostDto> posts,
        boolean hasPrev,
        boolean hasNext,
        int lastPage
) {
}