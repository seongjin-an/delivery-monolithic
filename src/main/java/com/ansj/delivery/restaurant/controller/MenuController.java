package com.ansj.delivery.restaurant.controller;

import com.ansj.delivery.common.response.ApiResponse;
import com.ansj.delivery.restaurant.dto.CreateMenuRequest;
import com.ansj.delivery.restaurant.dto.MenuResponse;
import com.ansj.delivery.restaurant.dto.UpdateMenuRequest;
import com.ansj.delivery.restaurant.service.RestaurantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/owner")
@PreAuthorize("hasRole('OWNER')")
@RequiredArgsConstructor
public class MenuController {

    private final RestaurantService restaurantService;

    @PostMapping("/restaurants/{restaurantId}/menus")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<MenuResponse> createMenu(
            @AuthenticationPrincipal String userId,
            @PathVariable Long restaurantId,
            @Valid @RequestBody CreateMenuRequest request) {
        return ApiResponse.ok("메뉴가 등록되었습니다.",
                restaurantService.createMenu(UUID.fromString(userId), restaurantId, request));
    }

    @PutMapping("/menus/{menuId}")
    public ApiResponse<MenuResponse> updateMenu(
            @AuthenticationPrincipal String userId,
            @PathVariable Long menuId,
            @Valid @RequestBody UpdateMenuRequest request) {
        return ApiResponse.ok("메뉴가 수정되었습니다.",
                restaurantService.updateMenu(UUID.fromString(userId), menuId, request));
    }

    @DeleteMapping("/menus/{menuId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMenu(
            @AuthenticationPrincipal String userId,
            @PathVariable Long menuId) {
        restaurantService.deleteMenu(UUID.fromString(userId), menuId);
    }

    @PatchMapping("/menus/{menuId}/availability")
    public ApiResponse<MenuResponse> updateMenuAvailability(
            @AuthenticationPrincipal String userId,
            @PathVariable Long menuId,
            @RequestParam boolean available) {
        return ApiResponse.ok(restaurantService.updateMenuAvailability(UUID.fromString(userId), menuId, available));
    }
}
