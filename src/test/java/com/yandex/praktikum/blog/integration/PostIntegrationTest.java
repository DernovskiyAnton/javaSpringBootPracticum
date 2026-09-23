package com.yandex.praktikum.blog.integration;

import com.yandex.praktikum.blog.dto.PostDto;
import com.yandex.praktikum.blog.dto.PostRequest;
import com.yandex.praktikum.blog.exception.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class PostIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    @Test
    void createReadUpdateDelete_shouldWorkEndToEnd() {
        PostRequest createRequest = new PostRequest("Integration Post", "Integration text", List.of("java", "boot"));

        ResponseEntity<PostDto> createResponse = restTemplate.postForEntity(url("/api/posts"), createRequest, PostDto.class);

        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
        PostDto created = createResponse.getBody();
        assertNotNull(created);
        assertEquals("Integration Post", created.title());
        assertEquals(0, created.likesCount());

        ResponseEntity<PostDto> getResponse = restTemplate.getForEntity(url("/api/posts/" + created.id()), PostDto.class);
        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertEquals(created.id(), getResponse.getBody().id());

        PostRequest updateRequest = new PostRequest("Updated Post", "Updated text", List.of("updated"));
        restTemplate.put(url("/api/posts/" + created.id()), updateRequest);

        ResponseEntity<PostDto> afterUpdate = restTemplate.getForEntity(url("/api/posts/" + created.id()), PostDto.class);
        assertEquals("Updated Post", afterUpdate.getBody().title());

        ResponseEntity<Integer> likeResponse = restTemplate.postForEntity(url("/api/posts/" + created.id() + "/likes"), null, Integer.class);
        assertEquals(HttpStatus.OK, likeResponse.getStatusCode());
        assertEquals(1, likeResponse.getBody());

        restTemplate.delete(url("/api/posts/" + created.id()));

        ResponseEntity<ErrorResponse> afterDelete = restTemplate.getForEntity(url("/api/posts/" + created.id()), ErrorResponse.class);
        assertEquals(HttpStatus.NOT_FOUND, afterDelete.getStatusCode());
    }

    @Test
    void getPost_shouldReturn404_whenNotFound() {
        ResponseEntity<ErrorResponse> response = restTemplate.getForEntity(url("/api/posts/999999"), ErrorResponse.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(404, response.getBody().status());
    }

    @Test
    void createPost_shouldReturn400_whenValidationFails() {
        PostRequest invalidRequest = new PostRequest("", "", List.of());

        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(url("/api/posts"), invalidRequest, ErrorResponse.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Validation Failed", response.getBody().error());
    }
}
