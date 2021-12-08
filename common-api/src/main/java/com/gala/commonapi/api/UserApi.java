package com.gala.commonapi.api;

import com.gala.commonapi.entity.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

/**
 * @description: 用户相关API
 * @author: GALA
 * @date: 2021.12.3
 */
public interface UserApi {

    /**
     * 用户列表
     */
    @GetMapping("/list")
    List<User> list();

    /**
     * 保存用户
     *
     * @param user
     */
    @PostMapping("/save")
    String save(User user);

}


