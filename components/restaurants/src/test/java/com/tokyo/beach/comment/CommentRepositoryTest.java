package com.tokyo.beach.comment;

import com.tokyo.beach.restaurant.RestaurantFixture;
import com.tokyo.beach.restaurants.comment.Comment;
import com.tokyo.beach.restaurants.comment.CommentRepository;
import com.tokyo.beach.restaurants.comment.NewComment;
import com.tokyo.beach.restaurants.comment.SerializedComment;
import com.tokyo.beach.restaurants.restaurant.Restaurant;
import com.tokyo.beach.restaurants.user.NewUser;
import com.tokyo.beach.restaurants.user.User;
import com.tokyo.beach.user.UserFixture;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Optional;

import static com.tokyo.beach.TestDatabaseUtils.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class CommentRepositoryTest {
    JdbcTemplate jdbcTemplate;
    CommentRepository commentRepository;

    @Before
    public void setUp() throws Exception {
        jdbcTemplate = new JdbcTemplate(buildDataSource());
        commentRepository = new CommentRepository(jdbcTemplate);
        createDefaultCuisine(jdbcTemplate);
        createDefaultPriceRange(jdbcTemplate);
    }

    @After
    public void tearDown() throws Exception {
        truncateAllTables(jdbcTemplate);
    }

    @Test
    public void test_create_createsAComment() throws Exception {
        Number userId = insertUserIntoDatabase(
                jdbcTemplate,
                new NewUser("joe@pivotal.io", "password", "Joe")
        ).getId();

        long restaurantId = jdbcTemplate.queryForObject(
                "INSERT INTO restaurant (name, created_by_user_id) VALUES " +
                        "('TEST RESTAURANT', ?) RETURNING id",
                (rs, rowNum) -> {
                    return rs.getLong("id");
                },
                userId
        );

        Comment createdComment = commentRepository.create(
                new NewComment("New Comment Content"),
                userId.longValue(),
                String.valueOf(restaurantId)
        );


        Comment actualComment = jdbcTemplate.queryForObject(
                "SELECT * FROM comment WHERE id=?",
                (rs, rowNum) -> {
                    return new Comment(
                            rs.getLong("id"),
                            rs.getString("content"),
                            rs.getString("created_at"),
                            rs.getLong("restaurant_id"),
                            rs.getLong("created_by_user_id")
                    );
                },
                createdComment.getId()
        );

        assertThat(actualComment.getContent(), is("New Comment Content"));
        assertThat(actualComment.getCreatedByUserId(), is(userId.longValue()));
        assertThat(actualComment.getRestaurantId(), is(restaurantId));
    }

    @Test
    public void test_findForRestaurant_returnsCommentsOnRestaurant() throws Exception {
        Number userId = insertUserIntoDatabase(
                jdbcTemplate,
                new NewUser("joe@pivotal.io", "password", "Joe")
        ).getId();

        Restaurant restaurant = jdbcTemplate.queryForObject(
                "INSERT INTO restaurant (name, created_by_user_id) VALUES " +
                        "('TEST RESTAURANT', ?) RETURNING *",
                (rs, rowNum) -> {
                    return new Restaurant(
                            rs.getLong("id"),
                            rs.getString("name"),
                            rs.getString("address"),
                            rs.getBoolean("offers_english_menu"),
                            rs.getBoolean("walk_ins_ok"),
                            rs.getBoolean("accepts_credit_cards"),
                            rs.getString("notes"),
                            rs.getString("created_at"),
                            rs.getString("updated_at"),
                            rs.getLong("created_by_user_id"),
                            0L,
                            rs.getLong("cuisine_id")
                    );
                },
                userId
        );

        Comment createdComment = commentRepository.create(
                new NewComment("New Comment Content"),
                userId.longValue(),
                String.valueOf(restaurant.getId())
        );


        List<SerializedComment> actualComments = commentRepository.findForRestaurant(restaurant.getId());
        assertEquals(actualComments.size(), 1);
        assertThat(actualComments.get(0).getContent(), is("New Comment Content"));
        assertThat(actualComments.get(0).getUser().getId(), is(userId.longValue()));
        assertThat(actualComments.get(0).getRestaurantId(), is(restaurant.getId()));
    }

    @Test
    public void test_delete_deletesComment() throws Exception {
        User user = new UserFixture().withEmail("email1").persist(jdbcTemplate);
        Restaurant restaurant = new RestaurantFixture()
                .withUser(user)
                .persist(jdbcTemplate);
        Comment comment = new CommentFixture()
                .withContent("content")
                .withCreatedByUserId(user.getId())
                .withRestaurantId(restaurant.getId())
                .persist(jdbcTemplate);

        commentRepository.delete(comment.getId());

        int count = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM comment WHERE id = ?",
                new Object[]{comment.getId()},
                Integer.class
        );

        assertEquals(0, count);
    }

    @Test
    public void test_get_returnsCommentMatchingId() throws Exception {
        User user = new UserFixture().withEmail("email1").persist(jdbcTemplate);
        Restaurant restaurant = new RestaurantFixture()
                .withUser(user)
                .persist(jdbcTemplate);
        Comment persistedComment = new CommentFixture()
                .withContent("content")
                .withCreatedByUserId(user.getId())
                .withRestaurantId(restaurant.getId())
                .persist(jdbcTemplate);


        Comment retrievedComment = commentRepository.get(persistedComment.getId()).get();


        assertEquals(persistedComment, retrievedComment);
    }

    @Test
    public void test_get_returnsEmptyOptionalWhenCommentDoesntExistWithId() throws Exception {
        Optional<Comment> retrievedComment = commentRepository.get(991);


        assertEquals(retrievedComment, Optional.empty());
    }
}