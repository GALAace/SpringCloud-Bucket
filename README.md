# SpringCloud-Bucket
## SpringCloud技术栈全家桶Demo

### 项目介绍

#### 包含组件：

- 注册中心：Eureka
- 远程调用：Feign
- 负载均衡：Ribbon
- 断路器：Hystrix
- 网关：Zuul
- 配置中心：SpringCloud Config
- 链路追踪：Sleuth、Zipkin
- 健康检查：SpringBootAdmin

#### 项目结构：

<img src="./doc_img/项目结构.jpg" alt="项目结构" style="zoom: 50%;" />



### 运行

//todo



### 详细搭建步骤

#### Eureka

0.因为是在一台pc上做集群，所以在开始前需要修改一下hosts文件

```
#Eureka
127.0.0.1  euk1.com
127.0.0.1  euk2.com
```

1.使用Spring Initializr创建一个SpringBoot工程，引入eureka-server依赖

```xml
<dependency>
	<groupId>org.springframework.cloud</groupId>
	<artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
</dependency>
```

2.修改application.yml

```yaml
server:
  port: 7901

spring:
  application:
    name: eureka-server

eureka:
  instance:
    #主机名
    hostname: euk1.com
  client:
    #是否将自己注册到Eureka Server,默认为true，表明该服务会向eureka注册自己的信息,单节点则false
    register-with-eureka: true
    #是否从Eureka server获取注册信息，由于多节点，需要同步其他节点数据，用true,单节点则false
    fetch-registry: true
    #设置服务注册中心的URL，用于client和server端交流
    service-url:
      defaultZone: http://euk2.com:7902/eureka/

management:
  endpoint:
    shutdown:
      enabled: true
```

3.启动类EurekaServer1Application增加注解

```java
@EnableEurekaServer
```

4.创建EurekaServer2，步骤同上。修改EurekaServer2的application.yml

```yml
server:
  port: 7902

spring:
  application:
    name: eureka-server

eureka:
  instance:
    #主机名
    hostname: euk2.com
  client:
    #是否将自己注册到Eureka Server,默认为true，表明该服务会向eureka注册自己的信息,单节点则false
    register-with-eureka: true
    #是否从Eureka server获取注册信息，由于多节点，需要同步其他节点数据，用true,单节点则false
    fetch-registry: true
    #设置服务注册中心的URL，用于client和server端交流
    service-url:
      defaultZone: http://euk1.com:7901/eureka/

management:
  endpoint:
    shutdown:
      enabled: true
```

5.分别启动两个工程，访问 http://localhost:7901/或http://localhost:7902/得到下图则配置成功

![Eureka](./doc_img/Eureka.jpg)

#### 配置中心

在单体应用，配置写在配置文件中，没有什么大问题。如果要切换环境 可以切换不同的profile，但在微服务中：

1. 微服务比较多。成百上千，配置很多，需要集中管理；
2. 管理不同环境的配置；
3. 需要动态调整配置参数，更改配置不停服。

1.这里新建一个仓库[SpringCloud-ConfigCenter](https://gitee.com/GALAace/spring-cloud-config-center)存放配置文件，文件命名规则为

> /{name}-{profiles}.properties
> /{name}-{profiles}.yml
> /{name}-{profiles}.json
> /{label}/{name}-{profiles}.yml
>
> name 服务名称
>
> profile 环境名称，开发、测试、生产：dev qa prd
>
> lable 仓库分支、默认master分支

2.使用Spring Initializr创建一个SpringBoot工程，引入config-server依赖

```xml
<dependency>
	<groupId>org.springframework.cloud</groupId>
	<artifactId>spring-cloud-config-server</artifactId>
</dependency>
```

3.修改application.yml

```yaml
server:
  port: 90

spring:
  application:
    name: config-center
  cloud:
    config:
      server:
        git:
          uri: https://gitee.com/GALAace/spring-cloud-config-center.git #配置文件所在的Git仓库地址
          default-label: master #配置文件分支
          search-paths: configs  #配置文件所在目录
#          username: Git账号
#          password: Git密码

eureka:
  client:
    service-url:
      defaultZone: http://euk1.com:7901/eureka/

```

4.启动类ConfigCenterApplication增加注解

```java
@EnableConfigServer
```

5.启动工程，访问http://localhost:90/testconfig-dev.yml 可以得到我们在配置文件仓库中的配置

![SpringCloudConfig测试](./doc_img/SpringCloudConfig测试.jpg)



