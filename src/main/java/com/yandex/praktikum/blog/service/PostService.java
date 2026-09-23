package com.yandex.praktikum.blog.service;

import com.yandex.praktikum.blog.dto.PostDto;
import com.yandex.praktikum.blog.dto.PostPage;
import com.yandex.praktikum.blog.dto.PostRequest;
import com.yandex.praktikum.blog.exception.ImageProcessingException;
import com.yandex.praktikum.blog.exception.ResourceNotFoundException;
import com.yandex.praktikum.blog.mapper.PostMapper;
import com.yandex.praktikum.blog.model.Post;
import com.yandex.praktikum.blog.repository.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class PostService {

    private static final String RESOURCE_NAME = "Post";
    private static final String IMAGE_RESOURCE_NAME = "Image for post";

    private final PostRepository postRepository;
    private final PostMapper postMapper;

    public PostService(PostRepository postRepository, PostMapper postMapper) {
        this.postRepository = postRepository;
        this.postMapper = postMapper;
    }

    public PostPage getAllPosts(String search, int pageNumber, int pageSize) {
        List<PostDto> posts = postRepository.findAll(search, pageNumber, pageSize)
                .stream()
                .map(postMapper::toDto)
                .toList();

        int totalCount = postRepository.countAll(search);
        int lastPage = (int) Math.ceil((double) totalCount / pageSize);

        return new PostPage(posts, pageNumber > 1, pageNumber < lastPage, lastPage);
    }

    public PostDto getPostById(long id) {
        return postMapper.toDto(findPostById(id));
    }

    @Transactional
    public PostDto createPost(PostRequest request) {
        Post post = new Post(
                0L,
                request.title(),
                request.text(),
                request.tags(),
                0,
                0
        );
        return postMapper.toDto(postRepository.save(post));
    }

    @Transactional
    public PostDto update(long id, PostRequest request) {
        Post post = new Post(id, request.title(), request.text(), request.tags(), 0, 0);
        if (!postRepository.update(post)) {
            throw notFound(id);
        }
        return postMapper.toDto(findPostById(id));
    }

    @Transactional
    public void delete(long id) {
        if (!postRepository.deleteById(id)) {
            throw notFound(id);
        }
    }

    @Transactional
    public int addLike(long id) {
        if (!postRepository.addLike(id)) {
            throw notFound(id);
        }
        return postRepository.findLikesCountById(id)
                .orElseThrow(() -> notFound(id));
    }

    @Transactional
    public void updateImage(long id, MultipartFile image) {
        byte[] bytes;
        try {
            bytes = image.getBytes();
        } catch (IOException e) {
            throw new ImageProcessingException("Failed to read image file", e);
        }
        if (!postRepository.updateImage(id, bytes)) {
            throw notFound(id);
        }
    }

    public byte[] getImage(long id) {
        return postRepository.findImageById(id)
                .orElseThrow(() -> new ResourceNotFoundException(IMAGE_RESOURCE_NAME, id));
    }

    private Post findPostById(long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> notFound(id));
    }

    private ResourceNotFoundException notFound(long id) {
        return new ResourceNotFoundException(RESOURCE_NAME, id);
    }
}
