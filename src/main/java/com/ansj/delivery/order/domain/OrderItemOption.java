package com.ansj.delivery.order.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "order_item_options")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItemOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false)
    private OrderItem orderItem;

    @Column(nullable = false)
    private String optionName;

    @Column(nullable = false)
    private String optionItemName;

    private int extraPrice;

    @Builder
    public OrderItemOption(OrderItem orderItem, String optionName, String optionItemName, int extraPrice) {
        this.orderItem = orderItem;
        this.optionName = optionName;
        this.optionItemName = optionItemName;
        this.extraPrice = extraPrice;
    }
}
