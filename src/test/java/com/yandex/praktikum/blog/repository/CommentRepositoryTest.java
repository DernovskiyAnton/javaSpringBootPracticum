package com.yandex.praktikum.blog.repository;

import com.yandex.praktikum.blog.model.Comment;
import com.yandex.praktikum.blog.model.Post;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@Import({PostRepository.class, CommentRepository.class})
class CommentRepositoryTest {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long testPostId;
    private Comment testComment;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM comments");
        jdbcTemplate.execute("DELETE FROM posts");

        Post testPost = new Post(0L, "Test Post", "Test text", List.of("tag"), 0, 0);
        Post savedPost = postRepository.save(testPost);
        testPostId = savedPost.id();

        testComment = new Comment(0L, "Test comment text", testPostId);
    }

    @Test
    void save_shouldSaveComment() {
        Comment saved = commentRepository.save(testComment);

        assertNotNull(saved);
        assertNotNull(saved.id());
        assertEquals("Test comment text", saved.text());
        assertEquals(testPostId, saved.postId());
    }

    @Test
    void findById_shouldReturnComment() {
        Comment saved = commentRepository.save(testComment);

        Comment found = commentRepository.findById(saved.id()).orElseThrow();

        assertEquals(saved.id(), found.id());
        assertEquals(saved.text(), found.text());
        assertEquals(saved.postId(), found.postId());
    }

    @Test
    void findById_shouldReturnEmpty_whenCommentNotFound() {
        assertTrue(commentRepository.findById(999L).isEmpty());
    }

    @Test
    void findAllByPostId_shouldReturnAllComments() {
        commentRepository.save(testComment);
        Comment secondComment = new Comment(0L, "Another comment", testPostId);
        commentRepository.save(secondComment);

        List<Comment> comments = commentRepository.findAllByPostId(testPostId);

        assertEquals(2, comments.size());
        assertTrue(comments.stream().allMatch(c -> c.postId() == testPostId));
    }

    @Test
    void findAllByPostId_shouldReturnEmptyList_whenNoComments() {
        List<Comment> comments = commentRepository.findAllByPostId(testPostId);

        assertNotNull(comments);
        assertTrue(comments.isEmpty());
    }

    @Test
    void findAllByPostId_shouldFilterByPostId() {
        Post anotherPost = new Post(0L, "Another Post", "Text", List.of("tag"), 0, 0);
        Long anotherPostId = postRepository.save(anotherPost).id();

        commentRepository.save(testComment);
        Comment commentForAnotherPost = new Comment(0L, "Comment for another post", anotherPostId);
        commentRepository.save(commentForAnotherPost);

        List<Comment> commentsForTestPost = commentRepository.findAllByPostId(testPostId);
        List<Comment> commentsForAnotherPost = commentRepository.findAllByPostId(anotherPostId);

        assertEquals(1, commentsForTestPost.size());
        assertEquals(testPostId, commentsForTestPost.get(0).postId());
        assertEquals(1, commentsForAnotherPost.size());
        assertEquals(anotherPostId, commentsForAnotherPost.get(0).postId());
    }

    @Test
    void update_shouldUpdateComment() {
        Comment saved = commentRepository.save(testComment);

        boolean result = commentRepository.update(new Comment(saved.id(), "Updated comment text", testPostId));
        Comment updated = commentRepository.findById(saved.id()).orElseThrow();

        assertTrue(result);
        assertEquals(saved.id(), updated.id());
        assertEquals("Updated comment text", updated.text());
        assertEquals(testPostId, updated.postId());
    }

    @Test
    void update_shouldReturnFalse_whenCommentNotFound() {
        assertFalse(commentRepository.update(new Comment(999L, "Updated comment text", testPostId)));
    }

    @Test
    void deleteById_shouldDeleteComment() {
        Comment saved = commentRepository.save(testComment);

        boolean result = commentRepository.deleteById(saved.id());

        assertTrue(result);
        assertEquals(Optional.empty(), commentRepository.findById(saved.id()));
    }

    @Test
    void deleteById_shouldReturnFalse_whenCommentNotFound() {
        assertFalse(commentRepository.deleteById(999L));
    }
}
