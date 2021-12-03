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

}
