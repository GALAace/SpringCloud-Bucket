package com.gala.commonapi.api;

import com.gala.commonapi.entity.Order;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

/**
 * @description: 订单API
 * @author: GALA
 * @date: 2021.12.3
 */
public interface OrderApi {

    /**
     * 订单列表
     */
    @GetMapping("/list")
    List<Order> list();

    /**
     * 保存订单
     */
    @PostMapping("/save")
    String save(Order order);
}
