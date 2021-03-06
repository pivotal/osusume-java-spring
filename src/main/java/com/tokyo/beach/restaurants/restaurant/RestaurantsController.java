package com.tokyo.beach.restaurants.restaurant;

import com.tokyo.beach.restaurants.photos.PhotoDataMapper;
import com.tokyo.beach.restaurants.photos.PhotoUrl;
import com.tokyo.beach.restaurants.s3.S3StorageRepository;
import com.tokyo.beach.restutils.RestControllerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@CrossOrigin
@RestController
@RequestMapping("/restaurants")
public class RestaurantsController {
    private RestaurantRepository restaurantRepository;
    private final PhotoDataMapper photoDataMapper;
    private final S3StorageRepository s3StorageRepository;

    @Autowired
    public RestaurantsController(
            RestaurantRepository restaurantRepository,
            PhotoDataMapper photoDataMapper,
            S3StorageRepository storageRepository
    ) {
        this.restaurantRepository = restaurantRepository;
        this.photoDataMapper = photoDataMapper;
        this.s3StorageRepository = storageRepository;
    }

    @RequestMapping(value = "", method = GET)
    public List<SerializedRestaurant> getAll() {
        Number userId = getCurrentUserId(RequestContextHolder.getRequestAttributes());
        return restaurantRepository.getAll(userId.longValue());
    }

    @RequestMapping(value = "{id}", method = GET)
    public SerializedRestaurant getRestaurant(@PathVariable String id) {
        Number userId = getCurrentUserId(RequestContextHolder.getRequestAttributes());

        Optional<SerializedRestaurant> maybeRestaurant = restaurantRepository.get(Long.parseLong(id), userId.longValue());
        maybeRestaurant.orElseThrow(() -> new RestControllerException("Invalid restaurant id."));
        return maybeRestaurant.get();
    }

    @RequestMapping(value = "", method = POST)
    @ResponseStatus(CREATED)
    public SerializedRestaurant create(@RequestBody NewRestaurantWrapper restaurantWrapper) {
        Number userId = getCurrentUserId(RequestContextHolder.getRequestAttributes());

        return restaurantRepository.create(restaurantWrapper.getRestaurant(), userId.longValue());
    }

    @RequestMapping(value = "{id}", method = PATCH)
    @ResponseStatus(OK)
    public SerializedRestaurant updateRestaurant(
            @PathVariable String id,
            @RequestBody NewRestaurantWrapper restaurantWrapper
    ) {
        return restaurantRepository.update(new Long(id), restaurantWrapper.getRestaurant());
    }

    @RequestMapping(value = "{restaurantId}/photoUrls/{photoUrlId}", method = DELETE)
    @ResponseStatus(HttpStatus.OK)
    public void deletePhotoUrl(@PathVariable String restaurantId, @PathVariable String photoUrlId) {
        Optional<PhotoUrl> maybePhotoUrl = photoDataMapper.get(Long.parseLong(photoUrlId));

        if (maybePhotoUrl.isPresent()) {
            photoDataMapper.delete(Long.parseLong(photoUrlId));
            s3StorageRepository.deleteFile(maybePhotoUrl.get().getUrl());
        }

    }

    @RequestMapping(value = "{restaurantId}", method = DELETE)
    @ResponseStatus(HttpStatus.OK)
    public void deleteRestaurant(@PathVariable String restaurantId) {
        Number userId = getCurrentUserId(RequestContextHolder.getRequestAttributes());
        restaurantRepository.delete(Long.parseLong(restaurantId), userId.longValue());
    }

    private Number getCurrentUserId(RequestAttributes requestAttributes) {
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        return (Number) request.getAttribute("userId");
    }
}
