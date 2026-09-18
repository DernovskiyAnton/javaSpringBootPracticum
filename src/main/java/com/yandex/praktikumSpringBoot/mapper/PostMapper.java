package com.yandex.praktikumSpringBoot.mapper;

import com.yandex.praktikumSpringBoot.dto.PostDto;
import com.yandex.praktikumSpringBoot.model.Post;
import org.springframework.stereotype.Component;

@Component
public class PostMapper {

    public PostDto toDto(Post post) {
        return new PostDto(
                post.id(),
                post.title(),
                post.text(),
                post.tags(),
                post.likesCount(),
                post.commentsCount()
        );
    }
}