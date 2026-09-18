package com.yandex.praktikumSpringBoot.repository;

import lombok.RequiredArgsConstructor;
import com.yandex.praktikumSpringBoot.dto.PostDto;
import com.yandex.praktikumSpringBoot.dto.PostRequest;
import com.yandex.praktikumSpringBoot.exception.ResourceNotFoundException;
import com.yandex.praktikumSpringBoot.model.Post;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class PostRepository {

    private final JdbcTemplate jdbcTemplate;

    private static final String COL_ID =             "id";
    private static final String COL_TITLE =          "title";
    private static final String COL_TEXT =           "text";
    private static final String COL_TAGS =           "tags";
    private static final String COL_LIKES_COUNT =    "likes_count";
    private static final String COL_COMMENTS_COUNT = "comments_count";

    private static final RowMapper<Post> POST_ROW_MAPPER = (rs, rowNum) -> new Post(
            rs.getLong(COL_ID),
            rs.getString(COL_TITLE),
            rs.getString(COL_TEXT),
            List.of(rs.getString(COL_TAGS).split(",")),
            rs.getInt(COL_LIKES_COUNT),
            rs.getInt(COL_COMMENTS_COUNT)
    );

    public List<Post> findAll(String search, int pageNumber, int pageSize) {
        int offset = (pageNumber - 1) * pageSize;
        return jdbcTemplate.query(
                "SELECT * FROM posts WHERE title LIKE ? OR text LIKE ? LIMIT ? OFFSET ?",
                POST_ROW_MAPPER,
                "%" + search + "%",
                "%" + search + "%",
                pageSize,
                offset
        );
    }

    public Post findById(long id) {
        return jdbcTemplate.queryForObject(
                "SELECT * FROM posts WHERE id = ?",
                POST_ROW_MAPPER,
                id
        );
    }

    public int countAll(String search) {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM posts WHERE title LIKE ? OR text LIKE ?",
                Integer.class,
                "%" + search + "%",
                "%" + search + "%"
        );
    }

    public Post save(Post post) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO posts (title, text, tags, likes_count, comments_count) VALUES (?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, post.title());
            ps.setString(2, post.text());
            ps.setString(3, String.join(",", post.tags()));
            ps.setInt(4, 0);
            ps.setInt(5, 0);
            return ps;
        }, keyHolder);

        long id = keyHolder.getKey().longValue();
        return findById(id);
    }

    public void update(long id, PostRequest request) {
        int updated = jdbcTemplate.update(
                "UPDATE posts SET title = ?, text = ?, tags = ? WHERE id = ?",
                request.title(),
                request.text(),
                String.join(",", request.tags()),
                id
        );
        if (updated == 0) {
            throw new ResourceNotFoundException("Post", id);
        }
    }

    public void deleteById(long id) {
        int deleted = jdbcTemplate.update("DELETE FROM posts WHERE id = ?", id);
        if (deleted == 0) {
            throw new ResourceNotFoundException("Post", id);
        }
    }

    public int addLike(long id) {
        int updated = jdbcTemplate.update(
                "UPDATE posts SET likes_count = likes_count + 1 WHERE id = ?", id
        );
        if (updated == 0) {
            throw new ResourceNotFoundException("Post", id);
        }
        return jdbcTemplate.queryForObject(
                "SELECT likes_count FROM posts WHERE id = ?",
                Integer.class,
                id
        );
    }

    public void updateImage(long id, byte[] image) {
        int updated = jdbcTemplate.update(
                "UPDATE posts SET image = ? WHERE id = ?", image, id
        );
        if (updated == 0) {
            throw new ResourceNotFoundException("Post", id);
        }
    }

    public byte[] findImageById(long id) {
        return jdbcTemplate.queryForObject(
                "SELECT image FROM posts WHERE id = ?",
                byte[].class,
                id
        );
    }
}