package com.gala.microserviceuser.controller;

import com.gala.commonapi.api.UserApi;
import com.gala.commonapi.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * @description:
 * @author: GALA
 * @date: 2021.12.3
 */

@RestController
public class UserController implements UserApi {

    @Value("${server.port}")
    String port;


    /**
     * 服务端口号
     */
    @GetMapping("/port")
    public String port() {
        return port;
    }

    @Override
    public List<User> list() {
        //实现自己的业务
        User user1 = new User(Long.valueOf(10001), "aaa", "张三", "1234");
        User user2 = new User(Long.valueOf(10002), "bbb", "李四", "1234");
        User user3 = new User(Long.valueOf(10003), "ccc", "王五", "1234");
        List<User> userList = new ArrayList<>();
        userList.add(user1);
        userList.add(user2);
        userList.add(user3);
        return userList;
    }

    @Override
    public String save(User user) {
        //实现自己的业务
        return "ok";
    }
}
