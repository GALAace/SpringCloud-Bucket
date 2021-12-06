package com.gala.serviceserver.common;

import com.gala.commonapi.entity.User;
import com.gala.serviceserver.service.UserService;
import com.netflix.hystrix.exception.HystrixTimeoutException;
import feign.hystrix.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @description: 使用fallbackFactory检查具体错误
 * @author: GALA
 * @date: 2021.12.6
 */

@Component
public class WebError implements FallbackFactory<UserService> {
    @Override
    public UserService create(Throwable throwable) {

        return new UserService() {
            @Override
            public String port() {
                System.out.println(throwable);
                if (throwable instanceof HystrixTimeoutException) {
                    System.out.println("InternalServerError");
                    return "远程服务报错";
                } else if (throwable instanceof RuntimeException) {
                    return "请求时异常：" + throwable;
                } else {
                    return null;
                }
            }

            @Override
            public List<User> list() {
                return null;
            }

            @Override
            public String save(User user) {
                return null;
            }
        };
    }
}
