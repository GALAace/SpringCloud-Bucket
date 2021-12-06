package com.gala.serviceserver.service;

import com.gala.commonapi.api.UserApi;
import com.gala.serviceserver.common.WebError;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @description: 用户Service
 * @author: GALA
 * @date: 2021.12.6
 */

@Service
@FeignClient(name = "microservice-user",fallbackFactory = WebError.class)
public interface UserService extends UserApi {

    @GetMapping("/port")
    String port();

}
