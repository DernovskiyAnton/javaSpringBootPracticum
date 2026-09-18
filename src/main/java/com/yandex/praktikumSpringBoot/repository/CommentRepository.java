package com.yandex.praktikumSpringBoot.repository;

import lombok.RequiredArgsConstructor;
import com.yandex.praktikumSpringBoot.dto.CommentRequest;
import com.yandex.praktikumSpringBoot.exception.ResourceNotFoundException;
import com.yandex.praktikumSpringBoot.model.Comment;
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
public class CommentRepository {
    private static final String COL_ID =      "id";
    private static final String COL_TEXT =    "text";
    private static final String COL_POST_ID = "post_id";

    private static final RowMapper<Comment> COMMENT_ROW_MAPPER = (rs, rowNum) -> new Comment(
            rs.getLong(COL_ID),
            rs.getString(COL_TEXT),
            rs.getLong(COL_POST_ID)
    );

    private final JdbcTemplate jdbcTemplate;

    public List<Comment> findAllByPostId(long postId) {
        return jdbcTemplate.query(
                "SELECT * FROM comments WHERE post_id = ?",
                COMMENT_ROW_MAPPER,
                postId
        );
    }

    public Comment findById(long id) {
        return jdbcTemplate.queryForObject(
                "SELECT * FROM comments WHERE id = ?",
                COMMENT_ROW_MAPPER,
                id
        );
    }

    public Comment save(Comment comment) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO comments (text, post_id) VALUES (?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, comment.text());
            ps.setLong(2, comment.postId());
            return ps;
        }, keyHolder);
        return findById(keyHolder.getKey().longValue());
    }

    public Comment update(long id, CommentRequest request) {
        int updated = jdbcTemplate.update(
                "UPDATE comments SET text = ? WHERE id = ?",
                request.text(),
                id
        );
        if (updated == 0) {
            throw new ResourceNotFoundException("Comment", id);
        }
        return findById(id);
    }

    public void deleteById(long id) {
        int deleted = jdbcTemplate.update(
                "DELETE FROM comments WHERE id = ?", id
        );
        if (deleted == 0) {
            throw new ResourceNotFoundException("Comment", id);
        }
    }
}
