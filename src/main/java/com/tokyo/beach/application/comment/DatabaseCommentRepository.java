package com.tokyo.beach.application.comment;

import com.tokyo.beach.application.restaurant.Restaurant;
import com.tokyo.beach.application.user.DatabaseUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class DatabaseCommentRepository implements CommentRepository {
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public DatabaseCommentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Comment create(NewComment newComment, long createdByUserId, String restaurantId) {
        String sql = "INSERT INTO comment (content, restaurant_id, created_by_user_id) VALUES (?, ?, ?) RETURNING *";
        return jdbcTemplate.queryForObject(
                sql,
                (rs, rowNum) -> {
                    return new Comment(
                            rs.getLong("id"),
                            rs.getString("content"),
                            rs.getString("created_at"),
                            rs.getLong("restaurant_id"),
                            rs.getLong("created_by_user_id")
                    );
                },
                newComment.getContent(),
                Long.parseLong(restaurantId),
                createdByUserId
        );
    }

    @Override
    public List<SerializedComment> findForRestaurant(Restaurant restaurant) {
        return jdbcTemplate.query(
                "SELECT comment.id as comment_id, users.id as user_id, * FROM comment " +
                        "inner join users on comment.created_by_user_id = users.id " +
                        "where restaurant_id = ?",
                (rs, rowNum) -> {

                    return new SerializedComment(
                            new Comment(
                                    rs.getLong("comment_id"),
                                    rs.getString("content"),
                                    rs.getString("created_at"),
                                    rs.getLong("restaurant_id"),
                                    rs.getLong("created_by_user_id")
                            ),
                            new DatabaseUser(
                                    rs.getLong("user_id"),
                                    rs.getString("email"),
                                    rs.getString("name")
                            )

                    );
                },
                restaurant.getId()
        );
    }
}
