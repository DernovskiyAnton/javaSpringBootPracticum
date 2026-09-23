package com.yandex.praktikum.blog.mapper;

import com.yandex.praktikum.blog.dto.PostDto;
import com.yandex.praktikum.blog.model.Post;
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