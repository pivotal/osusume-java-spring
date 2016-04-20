package com.tokyo.beach.restaurant;

import com.tokyo.beach.restaurants.cuisine.NewCuisine;
import com.tokyo.beach.restaurants.restaurant.DatabaseRestaurantRepository;
import com.tokyo.beach.restaurants.restaurant.NewRestaurant;
import com.tokyo.beach.restaurants.restaurant.Restaurant;
import com.tokyo.beach.restaurants.user.UserRegistration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.tokyo.beach.TestDatabaseUtils.*;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class DatabaseRestaurantRepositoryTest {
    private DatabaseRestaurantRepository restaurantRepository;
    private JdbcTemplate jdbcTemplate;

    @Before
    public void setUp() {
        jdbcTemplate = new JdbcTemplate(buildDataSource());
        restaurantRepository = new DatabaseRestaurantRepository(jdbcTemplate);
        createDefaultCuisine(jdbcTemplate);
    }

    @After
    public void tearDown() {
        truncateAllTables(jdbcTemplate);
    }

    @Test
    public void test_getAll() {
        Number userId = insertUserIntoDatabase(
                jdbcTemplate,
                new UserRegistration("joe@pivotal.io", "password", "Joe")
        );

        Integer restaurantId = jdbcTemplate.queryForObject(
                "INSERT INTO restaurant " +
                        "(name, address, offers_english_menu, walk_ins_ok, accepts_credit_cards, notes, created_by_user_id)" +
                        "VALUES ('Afuri', 'Roppongi', FALSE, TRUE, FALSE, '', ?)" +
                        "RETURNING *",
                (rs, rowNum) -> {
                    return rs.getInt("id");
                },
                userId
        );

        List<Restaurant> restaurants = restaurantRepository.getAll();


        Restaurant expectedRestaurant = new Restaurant(
                restaurantId,
                "Afuri",
                "Roppongi",
                false,
                true,
                false,
                "",
                "created-date",
                userId.longValue());

        assertThat(restaurants, is(singletonList(expectedRestaurant)));
    }

    @Test
    public void testCreateRestaurant() throws Exception {
        Number userId = insertUserIntoDatabase(
                jdbcTemplate,
                new UserRegistration("joe@pivotal.io", "password", "Joe")
        );

        NewRestaurant kfcNewRestaurant = new NewRestaurant(
                "KFC",
                "Shibuya",
                Boolean.TRUE,
                Boolean.TRUE,
                Boolean.TRUE,
                "Notes",
                0L,
                emptyList()
        );


        Restaurant createdRestaurant = restaurantRepository.createRestaurant(kfcNewRestaurant, userId.longValue());


        Map<String, Object> map = jdbcTemplate.queryForMap(
                "SELECT * FROM restaurant WHERE id = ?",
                createdRestaurant.getId()
        );


        assertEquals(map.get("id"), createdRestaurant.getId());
        assertEquals(map.get("name"), createdRestaurant.getName());
        assertEquals(map.get("address"), createdRestaurant.getAddress());
        assertEquals(map.get("offers_english_menu"), createdRestaurant.getOffersEnglishMenu());
        assertEquals(map.get("walk_ins_ok"), createdRestaurant.getWalkInsOk());
        assertEquals(map.get("accepts_credit_cards"), createdRestaurant.getAcceptsCreditCards());
        assertEquals(map.get("notes"), createdRestaurant.getNotes());
        assertEquals(map.get("created_by_user_id"), userId.longValue());
        assertEquals(map.get("cuisine_id"), 0L);
    }

    @Test
    public void testCreateRestaurant_withoutCuisineId() throws Exception {
        Number userId = insertUserIntoDatabase(
                jdbcTemplate,
                new UserRegistration("joe@pivotal.io", "password", "Joe")
        );

        NewRestaurant kfcNewRestaurant = new NewRestaurant(
                "KFC",
                "Shibuya",
                Boolean.TRUE,
                Boolean.TRUE,
                Boolean.TRUE,
                "Notes",
                null,
                emptyList()
        );


        Restaurant createdRestaurant = restaurantRepository.createRestaurant(kfcNewRestaurant, userId.longValue());


        NewRestaurant actualRestaurant = jdbcTemplate.queryForObject(
                "SELECT * FROM restaurant WHERE id = ?",
                (rs, rowNum) -> new NewRestaurant(
                        rs.getString("name"),
                        rs.getString("address"),
                        rs.getBoolean("offers_english_menu"),
                        rs.getBoolean("walk_ins_ok"),
                        rs.getBoolean("accepts_credit_cards"),
                        rs.getString("notes"),
                        rs.getLong("cuisine_id"),
                        emptyList()
                ),
                createdRestaurant.getId()
        );

        assertThat(actualRestaurant.getName(), is("KFC"));
        assertThat(actualRestaurant.getAddress(), is("Shibuya"));
        assertThat(actualRestaurant.getCuisineId(), is(0L));
    }

    @Test
    public void test_get_returnsRestaurant() throws Exception {
        Number userId = insertUserIntoDatabase(
                jdbcTemplate,
                new UserRegistration("joe@pivotal.io", "password", "Joe")
        );

        long id = jdbcTemplate.queryForObject(
                "INSERT INTO restaurant (name, created_by_user_id) " +
                        "VALUES ('Amazing Restaurant', ?) " +
                        "RETURNING id",
                (rs, rowNum) -> {
                    return rs.getLong("id");
                },
                userId
        );


        Optional<Restaurant> maybeRestaurant = restaurantRepository.get(id);


        assertThat(maybeRestaurant.get().getName(), is("Amazing Restaurant"));
    }

    @Test
    public void test_get_returnsEmptyOptionalForInvalidRestaurantId() throws Exception {
        Optional<Restaurant> maybeRestaurant = restaurantRepository.get(999);


        assertFalse(maybeRestaurant.isPresent());
    }

    @Test
    public void test_getRestaurantsPostedByUser() throws Exception {
        Number userId = insertUserIntoDatabase(
                jdbcTemplate,
                new UserRegistration("user_email", "password", "username")
        );
        Long cuisineId = insertCuisineIntoDatabase(
                jdbcTemplate,
                new NewCuisine("cuisine_name")
        );
        Long restaurantId = insertRestaurantIntoDatabase(
                jdbcTemplate,
                new NewRestaurant(
                        "restaurant_name",
                        "address",
                        true,
                        true,
                        true,
                        "",
                        cuisineId,
                        emptyList()),
                userId.longValue()
        );

        List<Restaurant> restaurantList = restaurantRepository.getRestaurantsPostedByUser(userId.longValue());

        assertEquals(restaurantList.size(), 1);
        assertThat(restaurantList.get(0).getName(), is("restaurant_name"));
        assertThat(restaurantList.get(0).getCreatedByUserId(), is(userId.longValue()));
    }

    @Test
    public void test_getRestaurantByIds_returnsListRestaurants() throws Exception {
        Number userId = insertUserIntoDatabase(
                jdbcTemplate,
                new UserRegistration("user_email", "password", "username")
        );
        Long cuisineId = insertCuisineIntoDatabase(
                jdbcTemplate,
                new NewCuisine("cuisine_name")
        );
        Long restaurantId = insertRestaurantIntoDatabase(
                jdbcTemplate,
                new NewRestaurant(
                        "restaurant_name",
                        "address",
                        true,
                        true,
                        true,
                        "",
                        cuisineId,
                        emptyList()),
                userId.longValue()
        );

        List<Restaurant> restaurantList = restaurantRepository.getRestaurantsByIds(singletonList(restaurantId));

        assertEquals(restaurantList.size(), 1);
        assertThat(restaurantList.get(0).getId(), is(restaurantId));
        assertThat(restaurantList.get(0).getName(), is("restaurant_name"));

    }

    @Test
    public void test_updateRestaurant_updatesRestaurant() throws Exception {
        Number userId = insertUserIntoDatabase(
                jdbcTemplate,
                new UserRegistration("rob@pivotal.io", "password", "Rob")
        );

        NewRestaurant newRestaurant = new NewRestaurant(
                "KFC",
                "Shibuya",
                Boolean.TRUE,
                Boolean.TRUE,
                Boolean.TRUE,
                "Healthy!",
                0L,
                emptyList()
        );

        Long restaurantId = insertRestaurantIntoDatabase(
                jdbcTemplate,
                newRestaurant,
                userId.longValue()
        );

        NewRestaurant updatedNewRestaurant = new NewRestaurant(
                "Kentucky",
                "East Shibuya",
                Boolean.FALSE,
                Boolean.FALSE,
                Boolean.FALSE,
                "Actually, not really healthy...",
                0L,
                emptyList()
        );


        Restaurant updatedRestaurant = restaurantRepository.updateRestaurant(
                restaurantId,
                updatedNewRestaurant
        );


        Map<String, Object> map = jdbcTemplate.queryForMap(
                "SELECT * FROM restaurant WHERE id = ?",
                restaurantId
        );

        assertEquals(map.get("id"), updatedRestaurant.getId());
        assertEquals(map.get("name"), updatedRestaurant.getName());
        assertEquals(map.get("address"), updatedRestaurant.getAddress());
        assertEquals(map.get("offers_english_menu"), updatedRestaurant.getOffersEnglishMenu());
        assertEquals(map.get("walk_ins_ok"), updatedRestaurant.getWalkInsOk());
        assertEquals(map.get("accepts_credit_cards"), updatedRestaurant.getAcceptsCreditCards());
        assertEquals(map.get("notes"), updatedRestaurant.getNotes());
        assertEquals(map.get("created_by_user_id"), userId.longValue());
        assertEquals(map.get("cuisine_id"), 0L);
    }
}
