package com.ansj.delivery.restaurant.controller;

import com.ansj.delivery.common.response.ApiResponse;
import com.ansj.delivery.restaurant.domain.RestaurantCategory;
import com.ansj.delivery.restaurant.dto.*;
import com.ansj.delivery.restaurant.service.RestaurantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;

    @GetMapping("/api/restaurants")
    public ApiResponse<List<RestaurantResponse>> getRestaurants(
            @RequestParam(required = false) RestaurantCategory category) {
        return ApiResponse.ok(restaurantService.getOpenRestaurants(category));
    }

    @GetMapping("/api/restaurants/{id}")
    public ApiResponse<RestaurantDetailResponse> getRestaurant(@PathVariable Long id) {
        return ApiResponse.ok(restaurantService.getRestaurantDetail(id));
    }

    @PostMapping("/api/owner/restaurants")
    @PreAuthorize("hasRole('OWNER')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<RestaurantResponse> createRestaurant(
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody CreateRestaurantRequest request) {
        return ApiResponse.ok("가게가 등록되었습니다.", restaurantService.create(UUID.fromString(userId), request));
    }

    @PutMapping("/api/owner/restaurants/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public ApiResponse<RestaurantResponse> updateRestaurant(
            @AuthenticationPrincipal String userId,
            @PathVariable Long id,
            @Valid @RequestBody UpdateRestaurantRequest request) {
        return ApiResponse.ok("가게 정보가 수정되었습니다.", restaurantService.update(UUID.fromString(userId), id, request));
    }

    @PatchMapping("/api/owner/restaurants/{id}/open")
    @PreAuthorize("hasRole('OWNER')")
    public ApiResponse<RestaurantResponse> openRestaurant(
            @AuthenticationPrincipal String userId,
            @PathVariable Long id) {
        return ApiResponse.ok("영업을 시작합니다.", restaurantService.open(UUID.fromString(userId), id));
    }

    @PatchMapping("/api/owner/restaurants/{id}/close")
    @PreAuthorize("hasRole('OWNER')")
    public ApiResponse<RestaurantResponse> closeRestaurant(
            @AuthenticationPrincipal String userId,
            @PathVariable Long id) {
        return ApiResponse.ok("영업을 종료합니다.", restaurantService.close(UUID.fromString(userId), id));
    }

    @GetMapping("/api/owner/restaurants")
    @PreAuthorize("hasRole('OWNER')")
    public ApiResponse<List<RestaurantResponse>> getMyRestaurants(
            @AuthenticationPrincipal String userId) {
        return ApiResponse.ok(restaurantService.getMyRestaurants(UUID.fromString(userId)));
    }
}
