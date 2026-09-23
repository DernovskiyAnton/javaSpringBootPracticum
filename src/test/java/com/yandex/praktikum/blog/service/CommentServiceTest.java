package com.yandex.praktikum.blog.service;

import com.yandex.praktikum.blog.dto.CommentDto;
import com.yandex.praktikum.blog.dto.CommentRequest;
import com.yandex.praktikum.blog.exception.ResourceNotFoundException;
import com.yandex.praktikum.blog.mapper.CommentMapper;
import com.yandex.praktikum.blog.model.Comment;
import com.yandex.praktikum.blog.repository.CommentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private CommentMapper commentMapper;

    @InjectMocks
    private CommentService commentService;

    private Comment testComment;
    private CommentDto testCommentDto;
    private CommentRequest testCommentRequest;

    @BeforeEach
    void setUp() {
        testComment = new Comment(1L, "Test comment text", 10L);
        testCommentDto = new CommentDto(1L, "Test comment text", 10L);
        testCommentRequest = new CommentRequest("Test comment text", 10L);
    }

    @Test
    void getAllByPostId_shouldReturnListOfCommentDtos() {
        long postId = 10L;
        List<Comment> comments = List.of(testComment);
        when(commentRepository.findAllByPostId(postId)).thenReturn(comments);
        when(commentMapper.toDto(testComment)).thenReturn(testCommentDto);

        List<CommentDto> result = commentService.getAllByPostId(postId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testCommentDto, result.get(0));
        verify(commentRepository).findAllByPostId(postId);
        verify(commentMapper).toDto(testComment);
    }

    @Test
    void getAllByPostId_shouldReturnEmptyList_whenNoComments() {
        long postId = 10L;
        when(commentRepository.findAllByPostId(postId)).thenReturn(List.of());

        List<CommentDto> result = commentService.getAllByPostId(postId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(commentRepository).findAllByPostId(postId);
        verify(commentMapper, never()).toDto(any());
    }

    @Test
    void getById_shouldReturnCommentDto() {
        long commentId = 1L;
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(testComment));
        when(commentMapper.toDto(testComment)).thenReturn(testCommentDto);

        CommentDto result = commentService.getById(commentId);

        assertNotNull(result);
        assertEquals(testCommentDto, result);
        verify(commentRepository).findById(commentId);
        verify(commentMapper).toDto(testComment);
    }

    @Test
    void getById_shouldThrowException_whenCommentNotFound() {
        long commentId = 999L;
        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> commentService.getById(commentId));
        verify(commentRepository).findById(commentId);
        verify(commentMapper, never()).toDto(any());
    }

    @Test
    void create_shouldReturnCreatedCommentDto() {
        Comment savedComment = new Comment(1L, "Test comment text", 10L);
        when(commentRepository.save(any(Comment.class))).thenReturn(savedComment);
        when(commentMapper.toDto(savedComment)).thenReturn(testCommentDto);

        CommentDto result = commentService.create(testCommentRequest);

        assertNotNull(result);
        assertEquals(testCommentDto, result);
        verify(commentRepository).save(argThat(comment ->
                comment.id() == 0L &&
                comment.text().equals("Test comment text") &&
                comment.postId() == 10L
        ));
        verify(commentMapper).toDto(savedComment);
    }

    @Test
    void update_shouldReturnUpdatedCommentDto() {
        long commentId = 1L;
        Comment updatedComment = new Comment(1L, "Updated comment text", 10L);
        CommentDto updatedDto = new CommentDto(1L, "Updated comment text", 10L);
        CommentRequest updateRequest = new CommentRequest("Updated comment text", 10L);
        when(commentRepository.update(updatedComment)).thenReturn(true);
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(updatedComment));
        when(commentMapper.toDto(updatedComment)).thenReturn(updatedDto);

        CommentDto result = commentService.update(commentId, updateRequest);

        assertNotNull(result);
        assertEquals(updatedDto, result);
        verify(commentRepository).update(updatedComment);
        verify(commentMapper).toDto(updatedComment);
    }

    @Test
    void update_shouldThrowException_whenCommentNotFound() {
        long commentId = 999L;
        when(commentRepository.update(any(Comment.class))).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> commentService.update(commentId, testCommentRequest));
        verify(commentRepository).update(any(Comment.class));
        verify(commentRepository, never()).findById(anyLong());
        verify(commentMapper, never()).toDto(any());
    }

    @Test
    void delete_shouldCallRepositoryDelete() {
        long commentId = 1L;
        when(commentRepository.deleteById(commentId)).thenReturn(true);

        commentService.delete(commentId);

        verify(commentRepository).deleteById(commentId);
    }

    @Test
    void delete_shouldThrowException_whenCommentNotFound() {
        long commentId = 999L;
        when(commentRepository.deleteById(commentId)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> commentService.delete(commentId));
        verify(commentRepository).deleteById(commentId);
    }
}
