package com.gala.commonapi.entity;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @description: 订单
 * @author: GALA
 * @date: 2021.12.3
 */

@Data
public class Order {

    private Long id;

    private String orderNumber;

    private BigDecimal totalPrice;

    private Long userId;

    public Order(Long id, String orderNumber, BigDecimal totalPrice, Long userId) {
        this.id = id;
        this.orderNumber = orderNumber;
        this.totalPrice = totalPrice;
        this.userId = userId;
    }
}
