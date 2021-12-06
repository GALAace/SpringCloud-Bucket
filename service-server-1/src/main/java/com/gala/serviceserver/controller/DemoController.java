package com.gala.serviceserver.controller;

import com.gala.commonapi.entity.User;
import com.gala.serviceserver.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @description: Demo Controller
 * @author: GALA
 * @date: 2021.12.3
 */


@RefreshScope
@RestController
public class DemoController {

    @Autowired
    UserService userService;

    @GetMapping("/list")
    public List<User> list(){
        return userService.list();
    }

    @GetMapping("/port")
    public String port(){
        return userService.port();
    }

}
