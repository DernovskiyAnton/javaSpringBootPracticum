package com.yandex.praktikum.blog.controller;

import tools.jackson.databind.ObjectMapper;
import com.yandex.praktikum.blog.dto.CommentDto;
import com.yandex.praktikum.blog.dto.CommentRequest;
import com.yandex.praktikum.blog.exception.ResourceNotFoundException;
import com.yandex.praktikum.blog.service.CommentService;
import com.yandex.praktikum.blog.service.PostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({PostController.class, CommentController.class})
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PostService postService;

    @MockitoBean
    private CommentService commentService;

    private CommentDto testCommentDto;
    private CommentRequest testCommentRequest;

    @BeforeEach
    void setUp() {
        testCommentDto = new CommentDto(1L, "Test comment text", 10L);
        testCommentRequest = new CommentRequest("Test comment text", 10L);
    }

    @Test
    void findAll_shouldReturnListOfComments() throws Exception {
        List<CommentDto> comments = List.of(testCommentDto);
        when(commentService.getAllByPostId(10L)).thenReturn(comments);

        mockMvc.perform(get("/api/posts/10/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].text").value("Test comment text"))
                .andExpect(jsonPath("$[0].postId").value(10));

        verify(commentService).getAllByPostId(10L);
    }

    @Test
    void findAll_shouldReturnEmptyList_whenNoComments() throws Exception {
        when(commentService.getAllByPostId(10L)).thenReturn(List.of());

        mockMvc.perform(get("/api/posts/10/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(commentService).getAllByPostId(10L);
    }

    @Test
    void findById_shouldReturnComment() throws Exception {
        when(commentService.getById(1L)).thenReturn(testCommentDto);

        mockMvc.perform(get("/api/posts/10/comments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.text").value("Test comment text"))
                .andExpect(jsonPath("$.postId").value(10));

        verify(commentService).getById(1L);
    }

    @Test
    void findById_shouldReturn404_whenCommentNotFound() throws Exception {
        when(commentService.getById(999L)).thenThrow(new ResourceNotFoundException("Comment", 999L));

        mockMvc.perform(get("/api/posts/10/comments/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));

        verify(commentService).getById(999L);
    }

    @Test
    void create_shouldReturnCreatedComment() throws Exception {
        when(commentService.create(any(CommentRequest.class))).thenReturn(testCommentDto);

        mockMvc.perform(post("/api/posts/10/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testCommentRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.text").value("Test comment text"))
                .andExpect(jsonPath("$.postId").value(10));

        verify(commentService).create(any(CommentRequest.class));
    }

    @Test
    void create_shouldReturn400_whenValidationFails() throws Exception {
        CommentRequest invalidRequest = new CommentRequest("", -1L);

        mockMvc.perform(post("/api/posts/10/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"));

        verify(commentService, never()).create(any());
    }

    @Test
    void update_shouldReturnUpdatedComment() throws Exception {
        CommentDto updatedDto = new CommentDto(1L, "Updated comment text", 10L);
        when(commentService.update(eq(1L), any(CommentRequest.class))).thenReturn(updatedDto);

        mockMvc.perform(put("/api/posts/10/comments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testCommentRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.text").value("Updated comment text"));

        verify(commentService).update(eq(1L), any(CommentRequest.class));
    }

    @Test
    void update_shouldReturn404_whenCommentNotFound() throws Exception {
        when(commentService.update(eq(999L), any())).thenThrow(new ResourceNotFoundException("Comment", 999L));

        mockMvc.perform(put("/api/posts/10/comments/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testCommentRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        verify(commentService).update(eq(999L), any());
    }

    @Test
    void update_shouldReturn400_whenValidationFails() throws Exception {
        CommentRequest invalidRequest = new CommentRequest("", 10L);

        mockMvc.perform(put("/api/posts/10/comments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(commentService, never()).update(anyLong(), any());
    }

    @Test
    void delete_shouldReturn200() throws Exception {
        doNothing().when(commentService).delete(1L);

        mockMvc.perform(delete("/api/posts/10/comments/1"))
                .andExpect(status().isOk());

        verify(commentService).delete(1L);
    }

    @Test
    void delete_shouldReturn404_whenCommentNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Comment", 999L)).when(commentService).delete(999L);

        mockMvc.perform(delete("/api/posts/10/comments/999"))
                .andExpect(status().isNotFound());

        verify(commentService).delete(999L);
    }
}
