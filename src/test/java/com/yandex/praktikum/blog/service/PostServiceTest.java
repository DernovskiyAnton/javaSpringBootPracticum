package com.yandex.praktikum.blog.service;

import com.yandex.praktikum.blog.dto.PostDto;
import com.yandex.praktikum.blog.dto.PostPage;
import com.yandex.praktikum.blog.dto.PostRequest;
import com.yandex.praktikum.blog.exception.ImageProcessingException;
import com.yandex.praktikum.blog.exception.ResourceNotFoundException;
import com.yandex.praktikum.blog.mapper.PostMapper;
import com.yandex.praktikum.blog.model.Post;
import com.yandex.praktikum.blog.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostMapper postMapper;

    @InjectMocks
    private PostService postService;

    private Post testPost;
    private PostDto testPostDto;
    private PostRequest testPostRequest;

    @BeforeEach
    void setUp() {
        testPost = new Post(1L, "Test Title", "Test Text", List.of("tag1", "tag2"), 10, 5);
        testPostDto = new PostDto(1L, "Test Title", "Test Text", List.of("tag1", "tag2"), 10, 5);
        testPostRequest = new PostRequest("Test Title", "Test Text", List.of("tag1", "tag2"));
    }

    @Test
    void getAllPosts_shouldReturnPostPage() {
        String search = "test";
        int pageNumber = 1;
        int pageSize = 10;
        List<Post> posts = List.of(testPost);
        when(postRepository.findAll(search, pageNumber, pageSize)).thenReturn(posts);
        when(postRepository.countAll(search)).thenReturn(1);
        when(postMapper.toDto(testPost)).thenReturn(testPostDto);

        PostPage result = postService.getAllPosts(search, pageNumber, pageSize);

        assertNotNull(result);
        assertEquals(1, result.posts().size());
        assertEquals(testPostDto, result.posts().get(0));
        assertFalse(result.hasPrev());
        assertFalse(result.hasNext());
        assertEquals(1, result.lastPage());
        verify(postRepository).findAll(search, pageNumber, pageSize);
        verify(postRepository).countAll(search);
        verify(postMapper).toDto(testPost);
    }

    @Test
    void getAllPosts_shouldCalculatePaginationCorrectly() {
        String search = "";
        int pageNumber = 2;
        int pageSize = 10;
        when(postRepository.findAll(search, pageNumber, pageSize)).thenReturn(List.of(testPost));
        when(postRepository.countAll(search)).thenReturn(25);
        when(postMapper.toDto(any())).thenReturn(testPostDto);

        PostPage result = postService.getAllPosts(search, pageNumber, pageSize);

        assertTrue(result.hasPrev());
        assertTrue(result.hasNext());
        assertEquals(3, result.lastPage());
    }

    @Test
    void getPostById_shouldReturnPostDto() {
        long postId = 1L;
        when(postRepository.findById(postId)).thenReturn(Optional.of(testPost));
        when(postMapper.toDto(testPost)).thenReturn(testPostDto);

        PostDto result = postService.getPostById(postId);

        assertNotNull(result);
        assertEquals(testPostDto, result);
        verify(postRepository).findById(postId);
        verify(postMapper).toDto(testPost);
    }

    @Test
    void getPostById_shouldThrowException_whenPostNotFound() {
        long postId = 999L;
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> postService.getPostById(postId));
        verify(postRepository).findById(postId);
        verify(postMapper, never()).toDto(any());
    }

    @Test
    void createPost_shouldReturnCreatedPostDto() {
        Post savedPost = new Post(1L, "Test Title", "Test Text", List.of("tag1", "tag2"), 0, 0);
        when(postRepository.save(any(Post.class))).thenReturn(savedPost);
        when(postMapper.toDto(savedPost)).thenReturn(testPostDto);

        PostDto result = postService.createPost(testPostRequest);

        assertNotNull(result);
        assertEquals(testPostDto, result);
        verify(postRepository).save(argThat(post ->
                post.id() == 0L &&
                post.title().equals("Test Title") &&
                post.text().equals("Test Text") &&
                post.tags().equals(List.of("tag1", "tag2")) &&
                post.likesCount() == 0 &&
                post.commentsCount() == 0
        ));
        verify(postMapper).toDto(savedPost);
    }

    @Test
    void update_shouldReturnUpdatedPostDto() {
        long postId = 1L;
        Post updatedPost = new Post(1L, "Updated Title", "Updated Text", List.of("tag3"), 10, 5);
        PostDto updatedDto = new PostDto(1L, "Updated Title", "Updated Text", List.of("tag3"), 10, 5);
        PostRequest updateRequest = new PostRequest("Updated Title", "Updated Text", List.of("tag3"));
        when(postRepository.update(any(Post.class))).thenReturn(true);
        when(postRepository.findById(postId)).thenReturn(Optional.of(updatedPost));
        when(postMapper.toDto(updatedPost)).thenReturn(updatedDto);

        PostDto result = postService.update(postId, updateRequest);

        assertNotNull(result);
        assertEquals(updatedDto, result);
        verify(postRepository).update(argThat(post ->
                post.id() == postId &&
                post.title().equals("Updated Title") &&
                post.text().equals("Updated Text") &&
                post.tags().equals(List.of("tag3"))
        ));
        verify(postRepository).findById(postId);
        verify(postMapper).toDto(updatedPost);
    }

    @Test
    void update_shouldThrowException_whenPostNotFound() {
        long postId = 999L;
        when(postRepository.update(any(Post.class))).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> postService.update(postId, testPostRequest));
        verify(postRepository).update(any(Post.class));
        verify(postRepository, never()).findById(anyLong());
    }

    @Test
    void delete_shouldCallRepositoryDelete() {
        long postId = 1L;
        when(postRepository.deleteById(postId)).thenReturn(true);

        postService.delete(postId);

        verify(postRepository).deleteById(postId);
    }

    @Test
    void delete_shouldThrowException_whenPostNotFound() {
        long postId = 999L;
        when(postRepository.deleteById(postId)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> postService.delete(postId));
        verify(postRepository).deleteById(postId);
    }

    @Test
    void addLike_shouldReturnUpdatedLikesCount() {
        long postId = 1L;
        int expectedLikesCount = 11;
        when(postRepository.addLike(postId)).thenReturn(true);
        when(postRepository.findLikesCountById(postId)).thenReturn(Optional.of(expectedLikesCount));

        int result = postService.addLike(postId);

        assertEquals(expectedLikesCount, result);
        verify(postRepository).addLike(postId);
        verify(postRepository).findLikesCountById(postId);
    }

    @Test
    void addLike_shouldThrowException_whenPostNotFound() {
        long postId = 999L;
        when(postRepository.addLike(postId)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> postService.addLike(postId));
        verify(postRepository).addLike(postId);
        verify(postRepository, never()).findLikesCountById(anyLong());
    }

    @Test
    void updateImage_shouldCallRepositoryWithImageBytes() throws IOException {
        long postId = 1L;
        byte[] imageBytes = new byte[]{1, 2, 3, 4, 5};
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getBytes()).thenReturn(imageBytes);
        when(postRepository.updateImage(postId, imageBytes)).thenReturn(true);

        postService.updateImage(postId, mockFile);

        verify(mockFile).getBytes();
        verify(postRepository).updateImage(postId, imageBytes);
    }

    @Test
    void updateImage_shouldThrowException_whenPostNotFound() throws IOException {
        long postId = 999L;
        byte[] imageBytes = new byte[]{1, 2, 3, 4, 5};
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getBytes()).thenReturn(imageBytes);
        when(postRepository.updateImage(postId, imageBytes)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> postService.updateImage(postId, mockFile));
        verify(postRepository).updateImage(postId, imageBytes);
    }

    @Test
    void updateImage_shouldThrowImageProcessingException_whenIOExceptionOccurs() throws IOException {
        long postId = 1L;
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getBytes()).thenThrow(new IOException("File read error"));

        ImageProcessingException exception = assertThrows(
                ImageProcessingException.class,
                () -> postService.updateImage(postId, mockFile)
        );
        assertTrue(exception.getMessage().contains("Failed to read image file"));
        verify(mockFile).getBytes();
        verify(postRepository, never()).updateImage(anyLong(), any());
    }

    @Test
    void getImage_shouldReturnImageBytes() {
        long postId = 1L;
        byte[] expectedImage = new byte[]{1, 2, 3, 4, 5};
        when(postRepository.findImageById(postId)).thenReturn(Optional.of(expectedImage));

        byte[] result = postService.getImage(postId);

        assertNotNull(result);
        assertArrayEquals(expectedImage, result);
        verify(postRepository).findImageById(postId);
    }

    @Test
    void getImage_shouldThrowException_whenPostNotFound() {
        long postId = 999L;
        when(postRepository.findImageById(postId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> postService.getImage(postId));
        verify(postRepository).findImageById(postId);
    }
}
