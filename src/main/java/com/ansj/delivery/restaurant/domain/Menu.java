package com.ansj.delivery.restaurant.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "menus")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Menu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private int price;

    private String imageUrl;

    private boolean isAvailable;

    private String categoryName;

    private int sortOrder;

    @Builder
    public Menu(Restaurant restaurant, String name, String description, int price,
                String imageUrl, String categoryName, int sortOrder) {
        this.restaurant = restaurant;
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
        this.isAvailable = true;
        this.categoryName = categoryName;
        this.sortOrder = sortOrder;
    }

    public void updateAvailability(boolean isAvailable) {
        this.isAvailable = isAvailable;
    }
}
