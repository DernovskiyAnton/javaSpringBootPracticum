package com.yandex.praktikumSpringBoot.service;

import lombok.RequiredArgsConstructor;
import com.yandex.praktikumSpringBoot.dto.PostDto;
import com.yandex.praktikumSpringBoot.dto.PostPage;
import com.yandex.praktikumSpringBoot.dto.PostRequest;
import com.yandex.praktikumSpringBoot.exception.ImageProcessingException;
import com.yandex.praktikumSpringBoot.mapper.PostMapper;
import com.yandex.praktikumSpringBoot.model.Post;
import com.yandex.praktikumSpringBoot.repository.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final PostMapper postMapper;

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
        return postMapper.toDto(postRepository.findById(id));
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
        postRepository.update(id, request);
        return postMapper.toDto(postRepository.findById(id));
    }

    @Transactional
    public void delete(long id) {
        postRepository.deleteById(id);
    }

    @Transactional
    public int addLike(long id) {
        return postRepository.addLike(id);
    }

    @Transactional
    public void updateImage(long id, MultipartFile image) {
        try {
            postRepository.updateImage(id, image.getBytes());
        } catch (IOException e) {
            throw new ImageProcessingException("Failed to read image file", e);
        }
    }

    public byte[] getImage(long id) {
        return postRepository.findImageById(id);
    }
}