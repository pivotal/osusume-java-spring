package com.tokyo.beach.restaurants.restaurant;

import com.tokyo.beach.restaurants.cuisine.Cuisine;
import com.tokyo.beach.restaurants.cuisine.CuisineDataMapper;
import com.tokyo.beach.restaurants.like.Like;
import com.tokyo.beach.restaurants.like.LikeDataMapper;
import com.tokyo.beach.restaurants.photos.PhotoDataMapper;
import com.tokyo.beach.restaurants.photos.PhotoUrl;
import com.tokyo.beach.restaurants.pricerange.PriceRange;
import com.tokyo.beach.restaurants.pricerange.PriceRangeDataMapper;
import com.tokyo.beach.restaurants.user.User;
import com.tokyo.beach.restaurants.user.UserDataMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@Repository
public class RestaurantRepository {
    private final RestaurantDataMapper restaurantDataMapper;
    private final PhotoDataMapper photoDataMapper;
    private final UserDataMapper userDataMapper;
    private final PriceRangeDataMapper priceRangeDataMapper;
    private final LikeDataMapper likeDataMapper;
    private final CuisineDataMapper cuisineDataMapper;

    @Autowired
    public RestaurantRepository(RestaurantDataMapper restaurantDataMapper, PhotoDataMapper photoDataMapper, UserDataMapper userDataMapper, PriceRangeDataMapper priceRangeDataMapper, LikeDataMapper likeDataMapper, CuisineDataMapper cuisineDataMapper) {
        this.restaurantDataMapper = restaurantDataMapper;
        this.photoDataMapper = photoDataMapper;
        this.userDataMapper = userDataMapper;
        this.priceRangeDataMapper = priceRangeDataMapper;
        this.likeDataMapper = likeDataMapper;
        this.cuisineDataMapper = cuisineDataMapper;
    }

    public List<SerializedRestaurant> getAll(Long userId) {
        List<Restaurant> restaurantList = restaurantDataMapper.getAll();

        List<PhotoUrl> photos = photoDataMapper.findForRestaurants(restaurantList);
        Map<Long, List<PhotoUrl>> restaurantPhotos = photos.stream()
                .collect(groupingBy(PhotoUrl::getRestaurantId));

        List<User> userList = userDataMapper.findForUserIds(
                restaurantList.stream()
                        .map(Restaurant::getCreatedByUserId)
                        .collect(toList())
        );
        Map<Long, User> createdByUsers = userList.stream()
                .collect(
                        Collectors.toMap(
                                User::getId, UnaryOperator.identity()
                        )
                );
        List<PriceRange> priceRangeList = priceRangeDataMapper.getAll();
        Map<Long, PriceRange> priceRangeMap = new HashMap<>();
        priceRangeList.forEach(priceRange -> priceRangeMap.put(priceRange.getId(), priceRange));

        List<Cuisine> cuisineList = cuisineDataMapper.getAll();
        Map<Long, Cuisine> cuisineMap = new HashMap<>();
        cuisineList.forEach(cuisine -> cuisineMap.put(cuisine.getId(), cuisine));

        List<Like> likes = likeDataMapper.findForRestaurants(restaurantList);
        Map<Long, List<Like>> restaurantLikes = likes
                .stream()
                .collect(groupingBy(Like::getRestaurantId));

        return restaurantList
                .stream()
                .map((restaurant) -> new SerializedRestaurant(
                        restaurant,
                        restaurantPhotos.get(restaurant.getId()),
                        cuisineMap.get(restaurant.getCuisineId()),
                        Optional.of(priceRangeMap.get(restaurant.getPriceRangeId())),
                        Optional.of(createdByUsers.get(restaurant.getCreatedByUserId())),
                        emptyList(),
                        restaurantLikes.get(restaurant.getId()) == null ? false : restaurantLikes.get(restaurant.getId()).contains(new Like(userId.longValue(), restaurant.getId())),
                        restaurantLikes.get(restaurant.getId()) == null ? 0 : restaurantLikes.get(restaurant.getId()).size()
                ))
                .collect(toList());
    }
}
