package com.yandex.praktikum.blog.repository;

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
class PostRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Post testPost;
    private Post updatedPost;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM comments");
        jdbcTemplate.execute("DELETE FROM posts");

        testPost = new Post(
                0L,
                "Spring Boot",
                "Post text",
                List.of("java", "spring"),
                0,
                0
        );
        updatedPost = new Post(
                0L,
                "New title",
                "New text",
                List.of("newTag1", "newTag2"),
                0,
                0
        );
    }

    @Test
    void save_shouldSavePost() {
        Post saved = postRepository.save(testPost);

        assertNotNull(saved);
        assertNotNull(saved.id());
        assertEquals("Spring Boot", saved.title());
        assertEquals("Post text", saved.text());
        assertEquals(List.of("java", "spring"), saved.tags());
        assertEquals(0, saved.likesCount());
        assertEquals(0, saved.commentsCount());
    }

    @Test
    void findById_shouldReturnPost() {
        Post saved = postRepository.save(testPost);

        Post found = postRepository.findById(saved.id()).orElseThrow();

        assertEquals(saved.id(), found.id());
        assertEquals(saved.title(), found.title());
        assertEquals(saved.text(), found.text());
        assertEquals(saved.tags(), found.tags());
    }

    @Test
    void findById_shouldReturnEmpty_whenPostNotFound() {
        assertTrue(postRepository.findById(999L).isEmpty());
    }

    @Test
    void findAll_shouldReturnAllPosts() {
        postRepository.save(testPost);
        Post secondPost = new Post(0L, "Another Post", "Another text", List.of("tag"), 0, 0);
        postRepository.save(secondPost);

        List<Post> posts = postRepository.findAll("", 1, 10);

        assertEquals(2, posts.size());
    }

    @Test
    void findAll_shouldFilterBySearch() {
        postRepository.save(testPost);
        Post secondPost = new Post(0L, "Different Title", "Different text", List.of("tag"), 0, 0);
        postRepository.save(secondPost);

        List<Post> posts = postRepository.findAll("Spring", 1, 10);

        assertEquals(1, posts.size());
        assertTrue(posts.get(0).title().contains("Spring"));
    }

    @Test
    void findAll_shouldSupportPagination() {
        for (int i = 0; i < 15; i++) {
            Post post = new Post(0L, "Post " + i, "Text " + i, List.of("tag"), 0, 0);
            postRepository.save(post);
        }

        List<Post> page1 = postRepository.findAll("", 1, 10);
        List<Post> page2 = postRepository.findAll("", 2, 10);

        assertEquals(10, page1.size());
        assertEquals(5, page2.size());
    }

    @Test
    void countAll_shouldReturnTotalCount() {
        postRepository.save(testPost);
        Post secondPost = new Post(0L, "Another Post", "Another text", List.of("tag"), 0, 0);
        postRepository.save(secondPost);

        int count = postRepository.countAll("");

        assertEquals(2, count);
    }

    @Test
    void countAll_shouldFilterBySearch() {
        postRepository.save(testPost);
        Post secondPost = new Post(0L, "Different Title", "Different text", List.of("tag"), 0, 0);
        postRepository.save(secondPost);

        int count = postRepository.countAll("Spring");

        assertEquals(1, count);
    }

    @Test
    void update_shouldUpdatePost() {
        Post saved = postRepository.save(testPost);

        boolean result = postRepository.update(withId(updatedPost, saved.id()));
        Post updated = postRepository.findById(saved.id()).orElseThrow();

        assertTrue(result);
        assertEquals(saved.id(), updated.id());
        assertEquals("New title", updated.title());
        assertEquals("New text", updated.text());
        assertEquals(List.of("newTag1", "newTag2"), updated.tags());
        assertEquals(0, updated.likesCount());
        assertEquals(0, updated.commentsCount());
    }

    @Test
    void update_shouldReturnFalse_whenPostNotFound() {
        assertFalse(postRepository.update(withId(updatedPost, 999L)));
    }

    @Test
    void deleteById_shouldDeletePost() {
        Post saved = postRepository.save(testPost);

        boolean result = postRepository.deleteById(saved.id());

        assertTrue(result);
        assertTrue(postRepository.findById(saved.id()).isEmpty());
    }

    @Test
    void deleteById_shouldReturnFalse_whenPostNotFound() {
        assertFalse(postRepository.deleteById(999L));
    }

    @Test
    void addLike_shouldIncrementLikesCount() {
        Post saved = postRepository.save(testPost);

        boolean result = postRepository.addLike(saved.id());

        assertTrue(result);
        assertEquals(Optional.of(1), postRepository.findLikesCountById(saved.id()));
    }

    @Test
    void addLike_shouldReturnFalse_whenPostNotFound() {
        assertFalse(postRepository.addLike(999L));
    }

    @Test
    void findLikesCountById_shouldReturnEmpty_whenPostNotFound() {
        assertTrue(postRepository.findLikesCountById(999L).isEmpty());
    }

    @Test
    void updateImage_shouldSaveImage() {
        Post saved = postRepository.save(testPost);
        byte[] imageBytes = new byte[]{1, 2, 3, 4, 5};

        boolean result = postRepository.updateImage(saved.id(), imageBytes);

        assertTrue(result);
        byte[] retrievedImage = postRepository.findImageById(saved.id()).orElseThrow();
        assertArrayEquals(imageBytes, retrievedImage);
    }

    @Test
    void updateImage_shouldReturnFalse_whenPostNotFound() {
        byte[] imageBytes = new byte[]{1, 2, 3, 4, 5};
        assertFalse(postRepository.updateImage(999L, imageBytes));
    }

    @Test
    void findImageById_shouldReturnImage() {
        Post saved = postRepository.save(testPost);
        byte[] imageBytes = new byte[]{1, 2, 3, 4, 5};
        postRepository.updateImage(saved.id(), imageBytes);

        byte[] retrievedImage = postRepository.findImageById(saved.id()).orElseThrow();

        assertArrayEquals(imageBytes, retrievedImage);
    }

    @Test
    void findImageById_shouldReturnEmpty_whenPostHasNoImage() {
        Post saved = postRepository.save(testPost);

        assertTrue(postRepository.findImageById(saved.id()).isEmpty());
    }

    @Test
    void findImageById_shouldReturnEmpty_whenPostNotFound() {
        assertTrue(postRepository.findImageById(999L).isEmpty());
    }

    private static Post withId(Post post, long id) {
        return new Post(id, post.title(), post.text(), post.tags(), post.likesCount(), post.commentsCount());
    }
}
