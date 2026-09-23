package com.yandex.praktikum.blog.integration;

import com.yandex.praktikum.blog.dto.CommentDto;
import com.yandex.praktikum.blog.dto.CommentRequest;
import com.yandex.praktikum.blog.dto.PostDto;
import com.yandex.praktikum.blog.dto.PostRequest;
import com.yandex.praktikum.blog.exception.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class CommentIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private long testPostId;

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    @BeforeEach
    void setUp() {
        PostRequest postRequest = new PostRequest("Post for comments", "text", List.of("tag"));
        PostDto post = restTemplate.postForEntity(url("/api/posts"), postRequest, PostDto.class).getBody();
        testPostId = post.id();
    }

    @Test
    void createReadUpdateDelete_shouldWorkEndToEnd() {
        CommentRequest createRequest = new CommentRequest("Integration comment", testPostId);

        ResponseEntity<CommentDto> createResponse = restTemplate.postForEntity(
                url("/api/posts/" + testPostId + "/comments"), createRequest, CommentDto.class);

        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
        CommentDto created = createResponse.getBody();
        assertNotNull(created);
        assertEquals("Integration comment", created.text());
        assertEquals(testPostId, created.postId());

        ResponseEntity<CommentDto[]> listResponse = restTemplate.getForEntity(
                url("/api/posts/" + testPostId + "/comments"), CommentDto[].class);
        assertEquals(HttpStatus.OK, listResponse.getStatusCode());
        assertEquals(1, listResponse.getBody().length);

        CommentRequest updateRequest = new CommentRequest("Updated comment", testPostId);
        ResponseEntity<CommentDto> updateResponse = restTemplate.exchange(
                url("/api/posts/" + testPostId + "/comments/" + created.id()),
                org.springframework.http.HttpMethod.PUT,
                new org.springframework.http.HttpEntity<>(updateRequest),
                CommentDto.class);
        assertEquals(HttpStatus.OK, updateResponse.getStatusCode());
        assertEquals("Updated comment", updateResponse.getBody().text());

        restTemplate.delete(url("/api/posts/" + testPostId + "/comments/" + created.id()));

        ResponseEntity<ErrorResponse> afterDelete = restTemplate.getForEntity(
                url("/api/posts/" + testPostId + "/comments/" + created.id()), ErrorResponse.class);
        assertEquals(HttpStatus.NOT_FOUND, afterDelete.getStatusCode());
    }

    @Test
    void getComment_shouldReturn404_whenNotFound() {
        ResponseEntity<ErrorResponse> response = restTemplate.getForEntity(
                url("/api/posts/" + testPostId + "/comments/999999"), ErrorResponse.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(404, response.getBody().status());
    }

    @Test
    void createComment_shouldReturn400_whenValidationFails() {
        CommentRequest invalidRequest = new CommentRequest("", testPostId);

        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
                url("/api/posts/" + testPostId + "/comments"), invalidRequest, ErrorResponse.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Validation Failed", response.getBody().error());
    }
}
