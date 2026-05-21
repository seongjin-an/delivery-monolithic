package com.ansj.delivery.restaurant.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "menu_option_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MenuOptionItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_option_id", nullable = false)
    private MenuOption menuOption;

    @Column(nullable = false)
    private String name;

    private int extraPrice;

    @Builder
    public MenuOptionItem(MenuOption menuOption, String name, int extraPrice) {
        this.menuOption = menuOption;
        this.name = name;
        this.extraPrice = extraPrice;
    }
}
