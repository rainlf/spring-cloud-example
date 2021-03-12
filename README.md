# Spring Cloud Example

## 背景

在微服务场景下，单体应用被拆分成多个微服务应用来获取更高的开发运维体验，在微服务架构的种种优势下，也有着许多伴随架构而产生的问题需要解决，如服务治理、进程间通信、服务容错保护、事件驱动、链路追踪等等。于是已促生了种种微服务框架及模块，如SpringCloud体系，SpringCloudAlibaba体系等。

本项目使用SpringCloud体系下的相关模块对微服务场景下的各种问题做简单的示例解决方案，所使用到模块有：

- `spring-cloud-starter-netflix-eureka-server`
- `spring-cloud-starter-netflix-eureka-client`

下文对各个模块的搭建过程做详细说明。

## 环境

- `java 8`

- `spring boot 2.3.7.RELEASE`

- `spring cloud Hoxton.SR9`

## 模块

### 服务发现与注册（Eureka）

SpringCloud使用Eureka作为注册中心来提供服务注册与服务发现功能，Eureka的设计实现了CAP原则中的AP部分，保证了可用性。

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

### 进程间通信



