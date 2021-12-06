package com.gala.serviceserver.service;

import com.gala.commonapi.api.UserApi;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @description:
 * @author: YBW
 * @date: 2021.12.6
 */

@Service
@FeignClient(name = "microservice-user")
public interface UserService extends UserApi {

    @GetMapping("/port")
    String port();

}
