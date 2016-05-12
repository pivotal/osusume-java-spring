package com.tokyo.beach.restaurant;

import com.tokyo.beach.restaurants.pricerange.PriceRange;
import com.tokyo.beach.restaurants.pricerange.PriceRangeRepository;
import com.tokyo.beach.restaurants.comment.Comment;
import com.tokyo.beach.restaurants.comment.CommentRepository;
import com.tokyo.beach.restaurants.comment.SerializedComment;
import com.tokyo.beach.restaurants.cuisine.Cuisine;
import com.tokyo.beach.restaurants.cuisine.CuisineRepository;
import com.tokyo.beach.restaurants.like.Like;
import com.tokyo.beach.restaurants.like.LikeRepository;
import com.tokyo.beach.restaurants.photos.NewPhotoUrl;
import com.tokyo.beach.restaurants.photos.PhotoRepository;
import com.tokyo.beach.restaurants.photos.PhotoUrl;
import com.tokyo.beach.restaurants.restaurant.NewRestaurant;
import com.tokyo.beach.restaurants.restaurant.Restaurant;
import com.tokyo.beach.restaurants.restaurant.RestaurantRepository;
import com.tokyo.beach.restaurants.restaurant.RestaurantsController;
import com.tokyo.beach.restaurants.user.User;
import com.tokyo.beach.restaurants.user.UserRepository;
import com.tokyo.beach.restutils.RestControllerExceptionHandler;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.tokyo.beach.TestDatabaseUtils.buildDataSource;
import static com.tokyo.beach.restutils.ControllerTestingUtils.createControllerAdvice;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

public class RestaurantsControllerTest {
    private RestaurantRepository restaurantRepository;
    private MockMvc mockMvc;
    private PhotoRepository photoRepository;
    private CuisineRepository cuisineRepository;
    private UserRepository userRepository;
    private CommentRepository commentRepository;
    private LikeRepository mockLikeRepository;
    private PriceRangeRepository mockPriceRangeRepository;

    @Before
    public void setUp() {
        restaurantRepository = mock(RestaurantRepository.class);
        photoRepository = mock(PhotoRepository.class);
        cuisineRepository = mock(CuisineRepository.class);
        userRepository = mock(UserRepository.class);
        commentRepository = mock(CommentRepository.class);
        mockLikeRepository = mock(LikeRepository.class);
        mockPriceRangeRepository = mock(PriceRangeRepository.class);

        RestaurantsController restaurantsController = new RestaurantsController(
                restaurantRepository,
                photoRepository,
                cuisineRepository,
                userRepository,
                commentRepository,
                mockLikeRepository,
                mockPriceRangeRepository
        );

        mockMvc = standaloneSetup(restaurantsController)
                .setControllerAdvice(createControllerAdvice(new RestControllerExceptionHandler()))
                .build();
    }

    @Test
    public void test_getAll_returnsAListOfRestaurants() throws Exception {
        List<Restaurant> restaurants = singletonList(
                new Restaurant(
                        1,
                        "Afuri",
                        "Roppongi",
                        false,
                        true,
                        false,
                        "とても美味しい",
                        "created-date",
                        1,
                        1L
                )
        );
        when(restaurantRepository.getAll()).thenReturn(restaurants);
        when(photoRepository.findForRestaurants(anyObject()))
                .thenReturn(singletonList(new PhotoUrl(999, "http://www.cats.com/my-cat.jpg", 1)));
        when(userRepository.findForUserIds(anyList()))
                .thenReturn(Arrays.asList(new User(1L, "taro@email.com", "taro")));
        when(mockPriceRangeRepository.getAll()).thenReturn(
                asList(new PriceRange(1L, "100yen"))
        );
        when(mockLikeRepository.findForRestaurants(restaurants)).thenReturn(
                asList(new Like(1L, 1L), new Like(2L, 1L))
        );


        mockMvc.perform(get("/restaurants"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", equalTo(1)))
                .andExpect(jsonPath("$[0].name", equalTo("Afuri")))
                .andExpect(jsonPath("$[0].address", equalTo("Roppongi")))
                .andExpect(jsonPath("$[0].offers_english_menu", equalTo(false)))
                .andExpect(jsonPath("$[0].walk_ins_ok", equalTo(true)))
                .andExpect(jsonPath("$[0].accepts_credit_cards", equalTo(false)))
                .andExpect(jsonPath("$[0].notes", equalTo("とても美味しい")))
                .andExpect(jsonPath("$[0].photo_urls[0].url", equalTo("http://www.cats.com/my-cat.jpg")))
                .andExpect(jsonPath("$[0].price_range", equalTo("100yen")))
                .andExpect(jsonPath("$[0].num_likes", equalTo(2)))
                .andExpect(jsonPath("$[0].created_by_user_name", equalTo("taro")));
    }

    @Test
    public void test_getAll_returnsRestaurantsWithoutLikes() throws Exception {
        List<Restaurant> restaurants = singletonList(
                new Restaurant(
                        1,
                        "Afuri",
                        "Roppongi",
                        false,
                        true,
                        false,
                        "とても美味しい",
                        "created-date",
                        1,
                        1L
                )
        );
        when(restaurantRepository.getAll()).thenReturn(restaurants);
        when(photoRepository.findForRestaurants(anyObject()))
                .thenReturn(singletonList(new PhotoUrl(999, "http://www.cats.com/my-cat.jpg", 1)));
        when(userRepository.findForUserIds(anyList()))
                .thenReturn(Arrays.asList(new User(1L, "taro@email.com", "taro")));
        when(mockPriceRangeRepository.getAll()).thenReturn(
                asList(new PriceRange(1L, "100yen"))
        );
        when(mockLikeRepository.findForRestaurants(restaurants)).thenReturn(
                emptyList()
        );


        mockMvc.perform(get("/restaurants"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", equalTo(1)))
                .andExpect(jsonPath("$[0].name", equalTo("Afuri")))
                .andExpect(jsonPath("$[0].address", equalTo("Roppongi")))
                .andExpect(jsonPath("$[0].offers_english_menu", equalTo(false)))
                .andExpect(jsonPath("$[0].walk_ins_ok", equalTo(true)))
                .andExpect(jsonPath("$[0].accepts_credit_cards", equalTo(false)))
                .andExpect(jsonPath("$[0].notes", equalTo("とても美味しい")))
                .andExpect(jsonPath("$[0].photo_urls[0].url", equalTo("http://www.cats.com/my-cat.jpg")))
                .andExpect(jsonPath("$[0].price_range", equalTo("100yen")))
                .andExpect(jsonPath("$[0].num_likes", equalTo(0)))
                .andExpect(jsonPath("$[0].created_by_user_name", equalTo("taro")));
    }

    @Test
    public void test_getAll_returnsEmptyListWhenNoRestaurants() throws Exception {
        RestaurantsController controller = new RestaurantsController(
                restaurantRepository,
                new PhotoRepository(new JdbcTemplate(buildDataSource())),
                cuisineRepository,
                userRepository,
                commentRepository,
                mockLikeRepository,
                mockPriceRangeRepository
        );


        mockMvc = standaloneSetup(controller).build();
        when(restaurantRepository.getAll()).thenReturn(emptyList());

        mockMvc.perform(get("/restaurants"))
                .andExpect(status().isOk())
                .andExpect(content().string("[]"));
    }

    @Test
    public void test_create_persistsARestaurant() throws Exception {
        ArgumentCaptor<NewRestaurant> attributeNewRestaurant = ArgumentCaptor.forClass(NewRestaurant.class);
        ArgumentCaptor<Long> attributeCreatedByUserId = ArgumentCaptor.forClass(Long.class);

        when(restaurantRepository.createRestaurant(
                attributeNewRestaurant.capture(),
                attributeCreatedByUserId.capture()
        )).thenReturn(
                new Restaurant(
                        1,
                        "Afuri",
                        "Roppongi",
                        false,
                        true,
                        false,
                        "soooo goood",
                        "created-date",
                        99,
                        1L
                )
        );

        when(photoRepository.createPhotosForRestaurant(anyLong(), anyListOf(NewPhotoUrl.class)))
                .thenReturn(singletonList(new PhotoUrl(999, "http://some-url", 1)));
        when(cuisineRepository.getCuisine("2")).thenReturn(
                Optional.of(
                        new Cuisine(
                                2,
                                "Ramen"
                        )
                )
        );
        when(userRepository.get(anyLong())).thenReturn(
                Optional.of(
                        new User(99L, "jiro@mail.com", "jiro")
                )
        );
        when(mockPriceRangeRepository.getPriceRange(anyLong())).thenReturn(
                Optional.of(
                        new PriceRange(
                                1,
                                "~900"
                        )
                )
        );

        String payload =
                "{" +
                        "\"restaurant\": " +
                        "{" +
                            "\"name\":\"Afuri\", " +
                            "\"address\": \"Roppongi\", " +
                            "\"offers_english_menu\": false, " +
                            "\"walk_ins_ok\": true, " +
                            "\"accepts_credit_cards\": false, " +
                            "\"notes\": \"soooo goood\", " +
                            "\"photo_urls\": [{\"url\": \"http://some-url\"}], " +
                            "\"cuisine_id\": \"2\", " +
                            "\"price_range_id\": \"1\"" +
                        "}" +
                "}";


        mockMvc.perform(
                post("/restaurants")
                        .requestAttr("userId", 99L)
                        .contentType(APPLICATION_JSON_UTF8_VALUE)
                        .content(payload)
        )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Afuri")))
                .andExpect(jsonPath("$.address", is("Roppongi")))
                .andExpect(jsonPath("$.offers_english_menu", is(false)))
                .andExpect(jsonPath("$.walk_ins_ok", is(true)))
                .andExpect(jsonPath("$.accepts_credit_cards", is(false)))
                .andExpect(jsonPath("$.notes", is("soooo goood")))
                .andExpect(jsonPath("$.photo_urls[0].url", is("http://some-url")))
                .andExpect(jsonPath("$.cuisine.name", is("Ramen")))
                .andExpect(jsonPath("$.price_range", is("~900")))
                .andExpect(jsonPath("$.created_by_user_name", is("jiro")));

        assertEquals(99, attributeCreatedByUserId.getValue().longValue());
    }

    @Test
    public void test_create_persistsARestaurantWithoutACuisineId() throws Exception {
        when(restaurantRepository.createRestaurant(anyObject(), anyLong())).thenReturn(
                new Restaurant(
                        1,
                        "Afuri",
                        "Roppongi",
                        false,
                        true,
                        false,
                        "soooo goood",
                        "created-date",
                        1,
                        0L
                )
        );

        Cuisine expectedCuisine = new Cuisine(0, "Not Specified");
        when(cuisineRepository.getCuisine("0")).thenReturn(
                Optional.of(expectedCuisine));
        when(photoRepository.createPhotosForRestaurant(anyLong(), anyListOf(NewPhotoUrl.class)))
                .thenReturn(singletonList(new PhotoUrl(999, "http://some-url", 1)));
        when(userRepository.get(anyLong())).thenReturn(
                Optional.of(
                        new User(1L, "jiro@mail.com", "jiro")
                )
        );
        when(mockPriceRangeRepository.getPriceRange(anyLong())).thenReturn(
                Optional.of(
                        new PriceRange(0L, "Not Specified")
                )
        );

        String payload = "{\"restaurant\": " +
                "{\"name\":\"Afuri\", " +
                "\"address\": \"Roppongi\", " +
                "\"offers_english_menu\": false, " +
                "\"walk_ins_ok\": true, " +
                "\"accepts_credit_cards\": false, " +
                "\"notes\": \"soooo goood\"," +
                "\"photo_urls\": [{\"url\": \"http://some-url\"}]}" +
                "}";

        mockMvc.perform(
                post("/restaurants")
                        .requestAttr("userId", 1L)
                        .contentType(APPLICATION_JSON_UTF8_VALUE)
                        .content(payload)
        )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Afuri")))
                .andExpect(jsonPath("$.address", is("Roppongi")))
                .andExpect(jsonPath("$.offers_english_menu", is(false)))
                .andExpect(jsonPath("$.walk_ins_ok", is(true)))
                .andExpect(jsonPath("$.accepts_credit_cards", is(false)))
                .andExpect(jsonPath("$.notes", is("soooo goood")))
                .andExpect(jsonPath("$.photo_urls[0].url", is("http://some-url")))
                .andExpect(jsonPath("$.cuisine.name", is("Not Specified")))
                .andExpect(jsonPath("$.cuisine.id", is(0)))
                .andExpect(jsonPath("$.price_range", is("Not Specified")));
    }

    @Test
    public void test_create_withInvalidCuisineId() throws Exception {
        when(restaurantRepository.createRestaurant(anyObject(), anyLong())).thenReturn(
                new Restaurant(
                        1,
                        "Afuri",
                        "Roppongi",
                        false,
                        true,
                        false,
                        "soooo goood",
                        "created-date",
                        1,
                        0L
                )
        );

        when(photoRepository.createPhotosForRestaurant(anyLong(), anyListOf(NewPhotoUrl.class)))
                .thenReturn(singletonList(new PhotoUrl(999, "http://some-url", 1)));
        when(cuisineRepository.getCuisine("2")).thenReturn(
                Optional.empty()
        );
        when(userRepository.get(anyLong())).thenReturn(
                Optional.of(
                        new User(1L, "jiro@mail.com", "jiro")
                )
        );
        when(mockPriceRangeRepository.getPriceRange(anyLong())).thenReturn(
                Optional.of(
                        new PriceRange(0L, "Not Specified")
                )
        );

        String payload = "{\"restaurant\": " +
                "{\"name\":\"Afuri\", " +
                "\"address\": \"Roppongi\", " +
                "\"offers_english_menu\": false, " +
                "\"walk_ins_ok\": true, " +
                "\"accepts_credit_cards\": false, " +
                "\"notes\": \"soooo goood\"," +
                "\"photo_urls\": [{\"url\": \"http://some-url\"}], " +
                "\"cuisine_id\": \"2\"}" +
                "}";

        mockMvc.perform(
                post("/restaurants")
                        .requestAttr("userId", 1L)
                        .contentType(APPLICATION_JSON_UTF8_VALUE)
                        .content(payload)
        )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Afuri")))
                .andExpect(jsonPath("$.address", is("Roppongi")))
                .andExpect(jsonPath("$.offers_english_menu", is(false)))
                .andExpect(jsonPath("$.walk_ins_ok", is(true)))
                .andExpect(jsonPath("$.accepts_credit_cards", is(false)))
                .andExpect(jsonPath("$.notes", is("soooo goood")))
                .andExpect(jsonPath("$.photo_urls[0].url", is("http://some-url")))
                .andExpect(jsonPath("$.price_range", is("Not Specified")))
                .andExpect(jsonPath("$.cuisine", isEmptyOrNullString()));
    }

    @Test
    public void test_getRestaurant_returnsRestaurant() throws Exception {
        Restaurant afuriRestaurant = new Restaurant(
                1L,
                "Afuri",
                "Roppongi",
                false,
                true,
                false,
                "",
                "created-date",
                1L,
                0L
        );
        Cuisine expectedCuisine = new Cuisine(1L, "Ramen");
        when(restaurantRepository.get(1)).thenReturn(
                Optional.of(afuriRestaurant)
        );
        when(photoRepository.findForRestaurant(afuriRestaurant)).thenReturn(
                emptyList()
        );
        when(cuisineRepository.findForRestaurant(afuriRestaurant)).thenReturn(
                expectedCuisine
        );
        User hanakoUser = new User(1L, "hanako@email", "hanako");
        when(userRepository.get(anyLong())).thenReturn(
                Optional.of(hanakoUser)
        );
        when(commentRepository.findForRestaurant(afuriRestaurant)).thenReturn(
                singletonList(
                        new SerializedComment(
                                new Comment(
                                        99L,
                                        "comment-content",
                                        "2016-04-01 10:10:10.000000",
                                        1L,
                                        1L
                                ),
                                hanakoUser
                        )
                )
        );
        when(mockLikeRepository.findForRestaurant(afuriRestaurant.getId())).thenReturn(
                asList(
                        new Like(11L, 1L),
                        new Like(12L, 1L)
                )
        );
        when(mockPriceRangeRepository.findForRestaurant(anyObject())).thenReturn(
                new PriceRange(0L, "Not Specified")
        );

        mockMvc.perform(get("/restaurants/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", equalTo(1)))
                .andExpect(jsonPath("$.name", equalTo("Afuri")))
                .andExpect(jsonPath("$.cuisine.name", equalTo("Ramen")))
                .andExpect(jsonPath("$.photo_urls", equalTo(emptyList())))
                .andExpect(jsonPath("$.created_by_user_name", equalTo("hanako")))
                .andExpect(jsonPath("$.comments[0].id", equalTo(99)))
                .andExpect(jsonPath("$.comments[0].user.name", equalTo("hanako")))
                .andExpect(jsonPath("$.num_likes", equalTo(2)))
                .andExpect(jsonPath("$.price_range", is("Not Specified")));
    }

    @Test
    public void test_getRestaurant_returnsRestaurantWithPhotos() throws Exception {
        Restaurant afuriRestaurant = new Restaurant(
                1,
                "Afuri",
                "Roppongi",
                false,
                true,
                false,
                "",
                "created-date",
                1,
                0L
        );
        when(restaurantRepository.get(1)).thenReturn(
                Optional.of(afuriRestaurant)
        );
        when(photoRepository.findForRestaurant(afuriRestaurant)).thenReturn(
                asList(new PhotoUrl(1, "Url1", 1), new PhotoUrl(2, "Url2", 1))
        );
        when(userRepository.get(anyLong())).thenReturn(
                Optional.of(new User(1L, "hanako@email", "hanako"))
        );
        when(mockPriceRangeRepository.findForRestaurant(anyObject())).thenReturn(
                new PriceRange(0, "Not Specified")
        );

        mockMvc.perform(get("/restaurants/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", equalTo(1)))
                .andExpect(jsonPath("$.name", equalTo("Afuri")))
                .andExpect(jsonPath("$.photo_urls[0].url", equalTo("Url1")))
                .andExpect(jsonPath("$.photo_urls[1].url", equalTo("Url2")));
    }

    @Test
    public void test_getRestaurant_returnsRestaurantWithLikeStatus() throws Exception {
        Restaurant afuriRestaurant = new Restaurant(
                1,
                "Afuri",
                "Roppongi",
                false,
                true,
                false,
                "",
                "created-date",
                1,
                0L
        );
        when(restaurantRepository.get(1)).thenReturn(
                Optional.of(afuriRestaurant)
        );
        when(photoRepository.findForRestaurant(afuriRestaurant)).thenReturn(
                emptyList()
        );
        when(userRepository.get(anyLong())).thenReturn(
                Optional.of(new User(1L, "hanako@email", "hanako"))
        );
        when(mockLikeRepository.findForRestaurant(afuriRestaurant.getId())).thenReturn(
                singletonList(new Like(11L, 1L))
        );
        when(mockPriceRangeRepository.findForRestaurant(anyObject())).thenReturn(
                new PriceRange(0L, "Not Specified")
        );

        mockMvc.perform(get("/restaurants/1")
                        .requestAttr("userId", 11L)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.liked", equalTo(true)));
    }

    @Test
    public void test_getRestaurant_returnsRestaurantWithPriceRange() throws Exception {
        Restaurant afuriRestaurant = new Restaurant(
                1,
                "Afuri",
                "Roppongi",
                false,
                true,
                false,
                "",
                "created-date",
                1,
                1L
        );
        when(restaurantRepository.get(1)).thenReturn(
                Optional.of(afuriRestaurant)
        );
        when(photoRepository.findForRestaurant(afuriRestaurant)).thenReturn(
                emptyList()
        );
        when(userRepository.get(anyLong())).thenReturn(
                Optional.of(new User(1L, "hanako@email", "hanako"))
        );
        when(mockLikeRepository.findForRestaurant(afuriRestaurant.getId())).thenReturn(
                singletonList(new Like(11L, 1L))
        );
        when(mockPriceRangeRepository.findForRestaurant(afuriRestaurant)).thenReturn(
                new PriceRange(1, "~900")
        );

        mockMvc.perform(get("/restaurants/1")
                .requestAttr("userId", 11L)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price_range", equalTo("~900")));
    }

    @Test
    public void test_getInvalidRestaurantId_throwsException() throws Exception {
        when(restaurantRepository.get(1)).thenReturn(
                Optional.empty()
        );


        mockMvc.perform(get("/restaurants/1"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("{\"error\":\"Invalid restaurant id.\"}"));
    }

    @Test
    public void test_update_updatesRestaurantInformation() throws Exception {
        Restaurant updatedRestaurant = new Restaurant(
                1,
                "Updated Name",
                "Updated Address",
                false,
                true,
                false,
                "",
                "",
                99,
                0L
        );

        when(restaurantRepository.updateRestaurant(anyLong(), anyObject())).thenReturn(
                updatedRestaurant
        );
        when(photoRepository.findForRestaurant(updatedRestaurant))
                .thenReturn(singletonList(new PhotoUrl(999, "http://some-url", 1)));
        when(cuisineRepository.getCuisine("2")).thenReturn(
                Optional.of(
                        new Cuisine(
                                2,
                                "Ramen"
                        )
                )
        );
        when(userRepository.get(anyLong())).thenReturn(
                Optional.of(
                        new User(99L, "jiro@mail.com", "jiro")
                )
        );
        String updatedRestaurantPayload = "{\"restaurant\": " +
                "{\"name\":\"Updated Name\", " +
                "\"address\": \"Updated Address\", " +
                "\"offers_english_menu\": false, " +
                "\"walk_ins_ok\": true, " +
                "\"accepts_credit_cards\": false, " +
                "\"notes\": \"\"," +
                "\"photo_urls\": [{\"url\": \"http://some-url\"}], " +
                "\"cuisine_id\": \"2\"}" +
                "}";

        mockMvc.perform(
                patch("/restaurants/1")
                        .requestAttr("userId", 99L)
                        .contentType(APPLICATION_JSON_UTF8_VALUE)
                        .content(updatedRestaurantPayload)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated Name")))
                .andExpect(jsonPath("$.address", is("Updated Address")))
                .andExpect(jsonPath("$.offers_english_menu", is(false)))
                .andExpect(jsonPath("$.walk_ins_ok", is(true)))
                .andExpect(jsonPath("$.accepts_credit_cards", is(false)))
                .andExpect(jsonPath("$.notes", is("")))
                .andExpect(jsonPath("$.photo_urls[0].url", is("http://some-url")))
                .andExpect(jsonPath("$.cuisine.name", is("Ramen")))
                .andExpect(jsonPath("$.created_by_user_name", is("jiro")));
    }

}
