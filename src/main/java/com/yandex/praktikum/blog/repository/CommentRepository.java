package com.yandex.praktikum.blog.repository;

import com.yandex.praktikum.blog.model.Comment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
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

    public CommentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Comment> findAllByPostId(long postId) {
        return jdbcTemplate.query(
                "SELECT * FROM comments WHERE post_id = ?",
                COMMENT_ROW_MAPPER,
                postId
        );
    }

    public Optional<Comment> findById(long id) {
        return jdbcTemplate.query(
                "SELECT * FROM comments WHERE id = ?",
                COMMENT_ROW_MAPPER,
                id
        ).stream().findFirst();
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
        return new Comment(keyHolder.getKey().longValue(), comment.text(), comment.postId());
    }

    public boolean update(Comment comment) {
        int updated = jdbcTemplate.update(
                "UPDATE comments SET text = ? WHERE id = ?",
                comment.text(),
                comment.id()
        );
        return updated > 0;
    }

    public boolean deleteById(long id) {
        int deleted = jdbcTemplate.update(
                "DELETE FROM comments WHERE id = ?", id
        );
        return deleted > 0;
    }
}
