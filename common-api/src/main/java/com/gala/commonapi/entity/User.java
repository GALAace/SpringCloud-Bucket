package com.gala.commonapi.entity;

import lombok.Data;

/**
 * @description: 用户类
 * @author: GALA
 * @date: 2021.12.3
 */

@Data
public class User {

    private Long id;

    private String account;

    private String username;

    private String password;

    public User(Long id, String account, String username, String password) {
        this.id = id;
        this.account = account;
        this.username = username;
        this.password = password;
    }

}
