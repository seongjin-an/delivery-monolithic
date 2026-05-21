package com.ansj.delivery.restaurant.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "menu_options")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MenuOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id", nullable = false)
    private Menu menu;

    @Column(nullable = false)
    private String name;

    private boolean isRequired;

    private int maxSelectCount;

    @OneToMany(mappedBy = "menuOption", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<MenuOptionItem> items = new ArrayList<>();

    @Builder
    public MenuOption(Menu menu, String name, boolean isRequired, int maxSelectCount) {
        this.menu = menu;
        this.name = name;
        this.isRequired = isRequired;
        this.maxSelectCount = maxSelectCount;
    }
}
