package com.tokyo.beach.restaurant;

import com.tokyo.beach.restaurants.cuisine.Cuisine;
import com.tokyo.beach.restaurants.pricerange.PriceRange;
import com.tokyo.beach.restaurants.restaurant.Restaurant;
import com.tokyo.beach.restaurants.restaurant.SerializedRestaurant;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SerializedRestaurantTest {
    @Test
    public void test_getCreatedDate() throws Exception {
        Restaurant restaurant = new RestaurantFixture()
                .withId(1L)
                .withName("Afuri")
                .withCreatedAt("2016-04-13 16:01:21.094")
                .build();

        SerializedRestaurant serializedRestaurant = new SerializedRestaurant(
                restaurant, null, null, null, null, null, false, 0L
        );

        assertEquals("2016-04-13T16:01:21.094Z", serializedRestaurant.getCreatedDate());
    }

    @Test
    public void test_getCuisine_returnsTheCuisine() {
        Cuisine cuisine = new Cuisine(1, "Mexican");
        Restaurant restaurant = new RestaurantFixture()
                .withId(1L)
                .withName("Afuri")
                .build();
        SerializedRestaurant serializedRestaurant = new SerializedRestaurant(
                restaurant, null, cuisine, null, null, null, false, 0L
        );

        assertEquals(cuisine, serializedRestaurant.getCuisine());
    }

    @Test
    public void test_getCuisine_returnsNullWhenThereIsNoCuisine() {
        Restaurant restaurant = new RestaurantFixture()
                .withId(1L)
                .withName("Afuri")
                .build();
        SerializedRestaurant serializedRestaurant = new SerializedRestaurant(
                restaurant, null, null, null, null, null, false, 0L
        );

        assertEquals(null, serializedRestaurant.getCuisine());
    }

    @Test
    public void test_getPriceRange_returnsThePriceRange() {
        PriceRange priceRange = new PriceRange(1, "0-1000");
        Restaurant restaurant = new RestaurantFixture()
                .withId(1L)
                .withName("Afuri")
                .build();
        SerializedRestaurant serializedRestaurant = new SerializedRestaurant(
                restaurant, null, null, priceRange, null, null, false, 0L
        );

        assertEquals(priceRange, serializedRestaurant.getPriceRange());
    }

    @Test
    public void test_getPriceRange_reutrnsNullWhenThereIsNoPriceRange() {
        Restaurant restaurant = new RestaurantFixture()
                .withId(1L)
                .withName("Afuri")
                .build();
        SerializedRestaurant serializedRestaurant = new SerializedRestaurant(
                restaurant, null, null, null, null, null, false, 0L
        );

        assertEquals(null, serializedRestaurant.getPriceRange());
    }
}