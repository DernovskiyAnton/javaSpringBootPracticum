package com.yandex.praktikumSpringBoot.model;

public record Comment(
        long id,
        String text,
        long postId
) {
}