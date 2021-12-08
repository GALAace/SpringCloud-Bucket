# SpringCloud技术栈全家桶Demo

## 项目介绍

### 包含组件：

- 注册中心：Eureka
- 远程调用：Feign
- 负载均衡：Ribbon
- 断路器：Hystrix
- 网关：Zuul
- 配置中心：SpringCloud Config
- 链路追踪：Sleuth、Zipkin
- 健康监控：SpringBootAdmin

### 项目结构：

<img src="./doc_img/项目结构.jpg" alt="项目结构" style="zoom: 50%;" />



## 运行

//todo



## 详细搭建步骤

### 注册中心（Eureka）

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
    #应用名称 其他服务在Eureka中根据这个名字找到对应的服务
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
```

3.启动类EurekaServerApplication增加注解

```java
@EnableEurekaServer
```

4.创建EurekaServer2，步骤同上。修改EurekaServer2的application.yml

```yml
server:
  port: 7902

spring:
  application:
    #应用名称 其他服务在Eureka中根据这个名字找到对应的服务
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
```

5.分别启动两个工程，访问 http://localhost:7901/或http://localhost:7902/得到下图则配置成功

![Eureka](./doc_img/Eureka.jpg)

### 配置中心（SpringCloud Config）

在单体应用，配置写在配置文件中，没有什么大问题。如果要切换环境 可以切换不同的profile，但在微服务中：

1. 微服务比较多。成百上千，配置很多，需要集中管理；
2. 管理不同环境的配置；
3. 需要动态调整配置参数，更改配置不停服。

1.这里新建一个仓库[SpringCloud-ConfigCenter](https://gitee.com/GALAace/spring-cloud-config-center)存放配置文件，文件命名规则为

> ```
> /{name}-{profiles}.properties
> /{name}-{profiles}.yml
> /{name}-{profiles}.json
> /{label}/{name}-{profiles}.yml
> ```
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



### 服务

服务分为业务服务（service-server）下面称Consumer，和具体执行功能的服务称Provider（microservice-xxx）；

由Consumer作为客户端调用作为服务端的Provider，他们都需要注册在Eureka上；

使用声明式服务调用，Provider方提供公用API包，Feign通过SpringMVC的注解来加载URI

所有我们先创建一个项目作为API

#### Api

1.使用Spring Initializr创建一个SpringBoot工程，引入依赖

```xml
<!--web 提供web服务-->
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-web</artifactId>
</dependency>
<!--lombok 简化代码 （可选）-->
<dependency>
	<groupId>org.projectlombok</groupId>
	<artifactId>lombok</artifactId>
</dependency>
```

注意：SpringBoot默认打build配置可能引起打包后其他项目无法依赖的问题，需要修改pom.xml

```xml
	<build>
        <plugins>
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-compiler-plugin</artifactId>
                    <configuration>
                        <source>1.8</source>
                        <target>1.8</target>
                    </configuration>
                </plugin>
        </plugins>
    </build>
```

2.创建实体和API interface，具体代码参考[common-api](https://github.com/GALAace/SpringCloud-Bucket/tree/main/common-api)

3.使用maven install打包到本地仓库，待Consumer和Provider使用

#### Provider

这里举例的4个Provider结构都是类似的，这里以microservice-user-1为例

1.使用Spring Initializr创建一个SpringBoot工程，引入依赖

```xml
<!--eureka-client 注册eureka-->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>

<!--config-client 连接配置中心-->
<dependency>
	<groupId>org.springframework.cloud</groupId>
	<artifactId>spring-cloud-config-client</artifactId>
</dependency>

<!--sleuth 链路追踪-->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-sleuth</artifactId>
</dependency>

<!--zipkin 链路追踪 UI-->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-zipkin</artifactId>
</dependency>

<!--actuator 健康监控-->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>

<!--SpringBootAdmin 健康监控 UI-->
<dependency>
    <groupId>de.codecentric</groupId>
    <artifactId>spring-boot-admin-starter-client</artifactId>
    <version>2.2.1</version>
</dependency>

<!--自定义API-->
<dependency>
    <groupId>com.gala</groupId>
    <artifactId>common-api</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>

<!--lombok 简化代码 （可选）-->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>
```

2.修改application.yml

```java
server:
    port: 9000
```

3.新建一个bootstrap.yml

bootstrapd加载的优先级比application高，我们在这里配置注册中心和配置中心的信息

```yaml
eureka:
  client:
    service-url:
      defaultZone: http://euk1.com:7901/eureka/

spring:
  application:
    name: microservice-user
  cloud:
    config:
      discovery:
        #通过注册中心查找配置中心
        enabled: true
        #配置中心的服务id
        service-id: config-center
      #环境
      profile: dev
      #分支
      label: master
```

2.新建一个UserController，实现引入的自定义依赖common-api的UserApi接口

```java
@RestController
public class UserController implements UserApi {
    @Override
    public List<User> list() {
        //实现自己的业务
        return null;
    }

    @Override
    public String save(User user) {
        //实现自己的业务
        return null;
    }
}
```

#### Consumer

1.使用Spring Initializr创建一个SpringBoot工程，引入依赖

```xml
<!--eureka-client 注册eureka-->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>

<!--openfeign 远程调用-->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>

<!--config-client 连接配置中心-->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-config-client</artifactId>
</dependency>

<!--actuator 健康监控-->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>

<!--SpringBootAdmin 健康监控 UI-->
<dependency>
    <groupId>de.codecentric</groupId>
    <artifactId>spring-boot-admin-starter-client</artifactId>
    <version>2.2.1</version>
</dependency>

<!--hystrix 断路器-->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>
        spring-cloud-starter-netflix-hystrix
    </artifactId>
</dependency>

<!--hystrix-dashboard 熔断监控 UI-->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>
        spring-cloud-starter-netflix-hystrix-dashboard
    </artifactId>
</dependency>

<!--自定义API-->
<dependency>
    <groupId>com.gala</groupId>
    <artifactId>common-api</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>

<!--lombok 简化代码 （可选）-->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>
```

##### 远程调用（Feign）

1.启动类ConfigCenterApplication增加注解

```java
@EnableFeignClients
```

2.创建接口，增加注解@FeignClient(name = "{微服务id}") 

```java
@Service
@FeignClient(name = "microservice-user")
public interface UserService extends UserApi {
    
}
```

3.创建Controller，引入UserService

```java
@RestController
public class DemoController {

    @Autowired
    UserService userService;

    @GetMapping("/list")
    public List<User> list(){
        return userService.list();
    }

}
```

4.访问http://localhost:8000/list 可以看到Provider端写的测试数据

![list](./doc_img/list.jpg)

##### Ribbon

###### 负载均衡

默认的负载均衡策略是ZoneAvoidanceRule（区域权衡策略）：复合判断Server所在区域的性能和Server的可用性，轮询选择服务器。

除此以外还有：

BestAvailableRule（最低并发策略）：会先过滤掉由于多次访问故障而处于断路器跳闸状态的服务，然后选择一个并发量最小的服务。逐个找服务，如果断路器打开，则忽略。

RoundRobinRule（轮询策略）：以简单轮询选择一个服务器。按顺序循环选择一个server。

RandomRule（随机策略）：随机选择一个服务器。

AvailabilityFilteringRule（可用过滤策略）：会先过滤掉多次访问故障而处于断路器跳闸状态的服务和过滤并发的连接数量超过阀值得服务，然后对剩余的服务列表安装轮询策略进行访问。

WeightedResponseTimeRule（响应时间加权策略）：据平均响应时间计算所有的服务的权重，响应时间越快服务权重越大，容易被选中的概率就越高。刚启动时，如果统计信息不中，则使用RoundRobinRule(轮询)策略，等统计的信息足够了会自动的切换到WeightedResponseTimeRule。响应时间长，权重低，被选择的概率低。反之，同样道理。此策略综合了各种因素（网络，磁盘，IO等），这些因素直接影响响应时间。

RetryRule（重试策略）：先按照RoundRobinRule(轮询)的策略获取服务，如果获取的服务失败则在指定的时间会进行重试，进行获取可用的服务。如多次获取某个服务失败，就不会再次获取该服务。主要是在一个时间段内，如果选择一个服务不成功，就继续找可用的服务，直到超时。

切换负载均衡策

注解方式

```java
@Bean
public IRule myRule(){
	//return new RoundRobinRule();
	//return new RandomRule();
	return new RetryRule(); 
}
```

配置文件方式(优先级高于注解)

```yaml
microservice-user: #微服务id
  ribbon:
    NFLoadBalancerRuleClassName: com.netflix.loadbalancer.RandomRule #随机策略
```

验证

1.在microservice-user-1和microservice-user-2的UserController中增加一个方法，返回当前服务的端口号

```java
@Value("${server.port}")
    String port;
    
	/**
     * 返回服务端口号
     */
    @GetMapping("/port")
    public String port() {
        return "调用端口:" + port;
    }
```

2.在service-server的UserService中增加方法

```java
@GetMapping("/port")
    String port();
```

3.在service-server的DemoController中增加方法

```java
@GetMapping("/port")
    public String port(){
        return userService.port();
    }
```

4.多次访问http://localhost:8000/port查看响应结果

###### 超时重试

Feign默认支持Ribbon；Ribbon的重试机制和Feign的重试机制有冲突，所以源码中默认关闭Feign的重试机制,使用Ribbon的重试机制

1.修改service-server的配置文件

```yaml
#ribbon
microservice-user: #微服务id
  ribbon:
#    NFLoadBalancerRuleClassName: com.netflix.loadbalancer.RandomRule #随机策略
    #连接超时时间(ms)
    ConnectTimeout: 1000
    #业务逻辑超时时间(ms)
    ReadTimeout: 5000
    #同一台实例最大重试次数,不包括首次调用
    MaxAutoRetries: 1
    #重试负载均衡其他的实例最大重试次数,不包括首次调用
    MaxAutoRetriesNextServer: 1
    #是否所有操作都重试
    OkToRetryOnAllOperations: false
```

2.修改microservice-user-1的UserController，microservice-user-2不变用于验证

```java
/**
     * 返回服务端口号
     */
    @GetMapping("/port")
    public String port() {
        try {
            System.out.println("调用" + port + "端口，进入sleep");
            Thread.sleep(6000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "调用端口:" + port;
    }
```

3.访问http://localhost:8000/port，当访问到microservice-user-1时，也就是9000端口服务时，在等待6秒后会重试一次，

可以看到microservice-user-1的控制台会打印2次“调用9000端口，进入sleep”，但此时9000服务依然超时，ribbon会去调用9001然后页面响应“调用端口:9001”

##### 断路器（Hystrix）

1.修改配置文件,启用hystrix

```yaml
#feign
feign:
  hystrix:
    enabled: true
```

2.创建一个WebError类，实现FallbackFactory,对每个接口编写对应的处理方法

```java
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
```

3.修改UserService的注解

```java
@FeignClient(name = "microservice-user",fallbackFactory = WebError.class)
```

4.启动类ServiceServerApplication增加注解

```java
@EnableHystrixDashboard
```

5.访问http://localhost:8000/actuator/hystrix.stream查看监控端点，如果报错则在启动类增加一个Bean

```java
//解决/actuator/hystrix.stream无法访问问题
    @Bean
    public ServletRegistrationBean hystrixMetricsStreamServlet() {
        ServletRegistrationBean registration = new ServletRegistrationBean(new HystrixMetricsStreamServlet());
        registration.addUrlMappings("/actuator/hystrix.stream");
        return registration;
    }
```

6.访问http://localhost:8000/hystrix查看图形化页面

![hystrix](./doc_img/hystrix.jpg)

#### 网关（Zuul）

zuul默认集成了：ribbon和hystrix

1.使用Spring Initializr创建一个SpringBoot工程，引入依赖

```xml
<!--eureka-client 注册eureka-->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>

<!--zuul 网关-->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-zuul</artifactId>
</dependency>
```

2.修改配置文件

```yaml
server:
  port: 80

eureka:
  client:
    service-url:
      defaultZone: http://euk1.com:7901/eureka/

spring:
  application:
    name: zuulserver
```

3.启动类增加注解

```java
@EnableZuulProxy
```

4.访问 http://{ip}:{端口}/{服务id}/{接口uri} 如 http://localhost/service-server/list

![zuul](./doc_img/zuul.jpg)

#### 链路追踪（Sleuth、Zipkin）

如果能跟踪每个请求，中间请求经过哪些微服务，请求耗时，网络延迟，业务逻辑耗时等。我们就能更好地分析系统瓶颈、解决系统问题。因此链路跟踪很重要

##### Sleuth

Sleuth是Spring cloud的分布式跟踪解决方案。

1. span(跨度)，基本工作单元。一次链路调用，创建一个span，

   span用一个64位id唯一标识。包括：id，描述，时间戳事件，spanId,span父id。

   span被启动和停止时，记录了时间信息，初始化span叫：root span，它的span id和trace id相等。

2. trace(跟踪)，一组共享“root span”的span组成的树状结构 称为 trace，trace也有一个64位ID，trace中所有span共享一个trace id。类似于一颗 span 树。

3. annotation（标签），annotation用来记录事件的存在，其中，核心annotation用来定义请求的开始和结束。

   - CS(Client Send客户端发起请求)。客户端发起请求描述了span开始。
   - SR(Server Received服务端接到请求)。服务端获得请求并准备处理它。SR-CS=网络延迟。
   - SS（Server Send服务器端处理完成，并将结果发送给客户端）。表示服务器完成请求处理，响应客户端时。SS-SR=服务器处理请求的时间。
   - CR（Client Received 客户端接受服务端信息）。span结束的标识。客户端接收到服务器的响应。CR-CS=客户端发出请求到服务器响应的总时间。

其实数据结构是一颗树，从root span 开始。

使用：

1.每个需要监控的系统都需要引入依赖

```xml
<!--sleuth 链路追踪-->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-sleuth</artifactId>
</dependency>
```

2.在zuul-server、service-server，microservice里都加入依赖,修改一下日志配置

```yaml
logging:
  level:
    root: INFO
    org.springframework.web.servlet.DispatcherServlet: DEBUG
    org.springframework.cloud.sleuth: DEBUG
```

3.访问一次http://localhost/service-server/list接口，查看日志

zuul-server:

```java
2021-12-07 11:53:11.519 DEBUG [zuulserver,6228db7f8ae94ab0,6228db7f8ae94ab0,false] 14644 --- [p-nio-80-exec-4] o.s.web.servlet.DispatcherServlet        : GET "/service-server/list", parameters={}
2021-12-07 11:53:12.023 DEBUG [zuulserver,6228db7f8ae94ab0,6228db7f8ae94ab0,false] 14644 --- [p-nio-80-exec-4] o.s.web.servlet.DispatcherServlet        : Completed 200 OK
```

service-server:

```java
2021-12-07 11:53:11.867 DEBUG [service-server,6228db7f8ae94ab0,9b859b488d9eff12,false] 17912 --- [oservice-user-1] o.s.c.s.i.async.SleuthContextListener    : Context refreshed or closed [org.springframework.context.event.ContextRefreshedEvent[source=SpringClientFactory-microservice-user, started on Tue Dec 07 11:53:11 CST 2021, parent: org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext@31e72cbc]]
2021-12-07 11:53:11.877 DEBUG [service-server,6228db7f8ae94ab0,9b859b488d9eff12,false] 17912 --- [oservice-user-1] o.s.c.s.i.w.c.f.LazyTracingFeignClient   : Sending a request via tracing feign client [org.springframework.cloud.sleuth.instrument.web.client.feign.TracingFeignClient@38724594] and the delegate [feign.Client$Default@3767e736]
```

microservice-user:

```java
2021-12-07 11:53:11.963 DEBUG [microservice-user,6228db7f8ae94ab0,292c12ffaa37d7de,false] 9976 --- [nio-9000-exec-2] o.s.web.servlet.DispatcherServlet        : GET "/list", parameters={}
2021-12-07 11:53:11.991 DEBUG [microservice-user,6228db7f8ae94ab0,292c12ffaa37d7de,false] 9976 --- [nio-9000-exec-2] o.s.web.servlet.DispatcherServlet        : Completed 200 OK
```

可以看出traceId， 是一样的

```
 [服务名称，traceId（一条请求调用链中 唯一ID），spanID（基本的工作单元，获取数据等），是否让zipkin收集和展示此信息]
```

##### Zipkin

Sleuth的日志已经能将调用链路等信息打印出来，但看起来还是比较费事的，我们使用zipkin收集系统的时序数据，从而追踪微服务架构中系统延时等问题。还有一个友好的界面。

原理：

sleuth收集跟踪信息通过http请求发送给zipkin server，zipkin将跟踪信息存储，以及提供RESTful API接口，zipkin ui通过调用api进行数据展示。

默认内存存储，可以用mysql，ES等存储。

使用：

1.每个需要监控的系统都需要引入依赖

```xml
<!--zipkin 链路追踪 UI-->
<dependency>
	<groupId>org.springframework.cloud</groupId>
	<artifactId>spring-cloud-starter-zipkin</artifactId>
</dependency>
```

2.每个需要监控的系统都需要修改配置文件

```yaml
spring:
  #zipkin
  zipkin:
    base-url: http://localhost:9411/
    #采样比例1
  sleuth:
    sampler:
      rate: 1  
```

3.下载、启动zipkin

​	Windows：

​		1.访问官网https://zipkin.io/ 

​		2.点击[Quickstart](https://zipkin.io/pages/quickstart.html)，

​		3.点击[latest release](https://search.maven.org/remote_content?g=io.zipkin&a=zipkin-server&v=LATEST&c=exec)下载jar包

​		4.用cmd启动

​	Liunx：

```shell
curl -sSL https://zipkin.io/quickstart.sh | bash -s
java -jar zipkin.jar
```

​	Docker：

```shell
docker run -d -p 9411:9411 openzipkin/zipkin
```

下图为Windows启动后的截图

![zipkin](./doc_img/zipkin.jpg)

4.请求一次http://localhost/service-server/list接口，然后访问http://localhost:9411/，可以查看调用链路、依赖等等信息

![zipkin_ui](./doc_img/zipkin_ui.jpg)

#### 健康监控（SpringBootAdmin）

1.使用Spring Initializr创建一个SpringBoot工程，引入依赖

```xml
<!-- Admin-server 服务 -->
<dependency>
    <groupId>de.codecentric</groupId>
    <artifactId>spring-boot-admin-starter-server</artifactId>
</dependency>
<!-- Admin UI -->
<dependency>
    <groupId>de.codecentric</groupId>
    <artifactId>spring-boot-admin-server-ui</artifactId>
</dependency>
```

2.启动类增加注解

```
@EnableAdminServer
```

3.在需要监控的微服务上添加依赖

```xml
<!-- Admin-client -->
<dependency>
    <groupId>de.codecentric</groupId>
    <artifactId>spring-boot-admin-starter-client</artifactId>
    <version>2.2.1</version>
</dependency>
<!--actuator 健康监控-->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

4.修改需要监控的微服务的配置（可以将这部分配置放在配置中心统一管理）

```yaml
spring:
  boot:
    admin:
      client:
        #SpringBootAdmin 地址
        url: http://localhost:81

#监控端点
management:
  endpoints:
    web:
      exposure:
        include: '*'
  endpoint:
    health:
      show-details: always
```

5.访问http://localhost:81 查看监控信息

![admin_wallboard](./doc_img/admin_wallboard.jpg)

![admin_details](./doc_img/admin_details.jpg)

同时也可以配置邮箱、钉钉等服务上下线通知
