package com.yandex.praktikum.blog.model;

public record Comment(
        long id,
        String text,
        long postId
) {
}