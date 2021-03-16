# Spring Cloud Example

## 背景

在微服务场景下，单体应用被拆分成多个微服务应用来获取更高的开发运维体验，在微服务架构的种种优势下，也有着许多伴随架构而产生的问题需要解决，如服务治理、进程间通信、服务容错保护、事件驱动、链路追踪等等。如何解决这些在特定架构下所必然出现的问题，于是便促生了现行的各种微服务框架及模块，如`Netflix`家族、`SpringCloud`家族、`SpringCloudAlibaba`家族等。

本项目使用`SpringCloud`家族对微服务场景下的各种问题做简单的示例解决方案，各个服务间架构如下图所示。

![1615869239759](README/1615869239759.png)

下文对各个模块的使用过程做详细说明。

## 环境

- `java 8`
- `spring boot 2.3.7.RELEASE`
- `spring cloud Hoxton.SR9`

## 依赖

- `spring-cloud-starter-netflix-eureka-server`
- `spring-cloud-starter-netflix-eureka-client`
- `spring-cloud-starter-openfeign`

## 模块

### 服务发现与注册（Eureka）

·pringCloud使用Eureka作为注册中心来提供服务注册与服务发现功能，Eureka的设计实现了CAP原则中的AP部分，保证了可用性。集群部署的Eureka服务架构如下图所示。

![1615869052054](README/1615869052054.png)

下面介绍Server端与Client端的具体实现。

#### Server端

引入依赖

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

添加注解

```java

@EnableEurekaServer
@SpringBootApplication
public class EurekaServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }

}
```

单机模式下，关闭自注册和相关日志，配置文件为

```properties
spring.application.name=eureka-server
server.port=8761

eureka.client.register-with-eureka=false
eureka.client.fetch-registry=false

logging.level.com.netflix.eureka=OFF
logging.level.com.netflix.discovery=OFF
```

#### Client端

引入依赖

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
	<groupId>org.springframework.cloud</groupId>
	<artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

Spring使用`DiscoveryClient`抽象来和注册中心进行通信，`@EnableDiscoveryClient`注解会激活`Eureka`的相关实现，对于其他类型的注册中心也有自己的相关实现。

```java
@RestController
public class ServiceInstanceRestController {
    @Autowired
    private DiscoveryClient discoveryClient;

    @RequestMapping("/service-instances/{applicationName}")
    public List<ServiceInstance> serviceInstancesByApplicationName(@PathVariable String applicationName) {
        return this.discoveryClient.getInstances(applicationName);
    }
}
```

在配置文件中指定`Eureka`的地址

```properties
spring.application.name=eureka-client
server.port=8000
eureka.client.serviceUrl.defaultZone=http://localhost:8761/eureka
```

访问http://localhost:8761/ 便可以看到`eureka-client`应用已经注册成功。

访问http://localhost:8000/service-instances/eureka-client 可以看到从`Eureka Server`查询到的服务信息。

### 进程间通信（Feign）

`OpenFeign`提供了声明式的`web`服务调用（即声明式的`web`服务客户端），使得开发可以通过接口的方式轻松的调用第三方服务，并提供如服务容错保护（`Hystrix`），超时保护等功能。

使用时`Provider`和`Consumer`服务均需向注册中心中进行注册，`Consumer`的`Feign client`通过`Provider`的服务名向注册中心检索获并取其真实地址，然后完成通信，下面详细介绍二者的使用实现。

#### Provider方

`Provider`方为普通的`web`服务客户端，通过`@RestController`注解提供`REST`端点服务。如

```java
@RestController
public class OpenFeignProviderController {

    @GetMapping("")
    public String sayHi() {
        return "hi, this is from open feign provider";
    }

    @GetMapping("fail")
    public String sayHiWithFail() {
        throw new RuntimeException("hi, this is from open feign provider with fail");
    }

    @GetMapping("sleep")
    public String sayHiWithSleep() throws InterruptedException {
        TimeUnit.SECONDS.sleep(10);
        return "hi, this is from open feign provider with sleep 10s";
    }
}
```

#### Consumer方

添加依赖

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
```

声明`web`服务客户端，并可以在其中指定对应的断路器实现

```java
@FeignClient(value = "${openfeign.provider.name}", fallback = OpenFeignProviderFallback.class)
public interface IOpenFeignProvider {
    @GetMapping("")
    String sayHi();

    @GetMapping("fail")
    String sayHiWithFail();

    @GetMapping("sleep")
    String sayHiWithSleep();
}
```
断路器具体实现

```java
@Component
public class OpenFeignProviderFallback implements IOpenFeignProvider {
    @Override
    public String sayHi() {
        return "hi, this is from open feign customer call back, sayHi";
    }

    @Override
    public String sayHiWithFail() {
        return "hi, this is from open feign customer call back, sayHiWithFail";
    }

    @Override
    public String sayHiWithSleep() {
        return "hi, this is from open feign customer call back, sayHiWithSleep";
    }
}
```

`Feign client`的支持超时时间的配置，这里超时时间分为`connect-timeout`和`read-timeout`两种，单位为毫秒。

-   `connect-timeout`对应客户端与目标服务建立连接的时间
-   `read-timeout`对应建立连接后，等待服务方返回响应的时间

二者使用时可以配置文件中指定，可以指定默认的超时时间（`default`），也可以通过`FeignClient`名称来特别指定某个服务的超时时间。

默认状态下容错保护（`Hystrix`）默认为关闭状态，可以在配置文件中开启，如下。

```properties
spring.application.name=openfeign-consumer
server.port=8020

eureka.client.serviceUrl.defaultZone=http://localhost:8761/eureka

feign.hystrix.enabled=true
feign.client.config.default.connect-timeout=5000
feign.client.config.default.read-timeout=5000

# 取消默认的ribbon实现
spring.cloud.loadbalancer.ribbon.enabled=false

openfeign.provider.name=openfeign-provider
```

启动后，分布请求以下地址，可以观测到对应现象：

- http://localhost:8020  正常返回
- http://localhost:8020/fail 服务提供方出现异常，触发容错保护
- http://localhost:8020/sleep 服务提供方请求超时，触发容错保护

### 客户端负载均衡（Spring Cloud Loadbalancer）

在`SpringCloud`家族中，默认的负载均衡实现为`Rbiion`，但在不幸的是该项目已停止维护，`Spring`官方推荐`Spring Cloud Loadbalancer`来替换它。

在使用`OpenFeign`时，可通过在配置中取消默认实现来使用`Spring Cloud Loadbalancer`

添加依赖

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-loadbalancer</artifactId>
</dependency>
```

修改配置

```properties
# 取消默认的ribbon实现
spring.cloud.loadbalancer.ribbon.enabled=false
```

### 服务容错保护 （Resilience4j）

服务容错保护，目前在`OpenFeign`中默认集成了`Hystrix`的实现，但同样的该项目也停止了维护，`Spring`官方推荐使用`Resilience4j`来替换它。

`Resilience4j`是一个轻量、易用、可组装的高可用框架，支持***熔断***、***高频控制***、***隔离***、***限流***、***限时***、***重试***等多种高可用机制。 

与`Hystrix`相比，它有以下一些主要的区别： 

-  `Hystrix`调用必须被封装到`HystrixCommand`里，`Resilience4j`以装饰器的方式提供对函数式接口、`lambda表达式`等的嵌套装饰，因此可以用简洁的方式组合多种高可用机制
-  `Hystrix`的频次统计采用滑动窗口的方式，`Resilience4j`采用环状缓冲区的方式
-  在熔断器在半开状态时，`Hystrix`仅使用一次执行判定是否进行状态转换，`Resilience4j`采用可配置的执行次数与阈值，来决定是否进行状态转换，这种方式提高了熔断机制的稳定性
-  关于隔离机制，`Hystrix`提供基于线程池和信号量的隔离，`Resilience4j`只提供基于信号量的隔离

