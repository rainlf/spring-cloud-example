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

在项目中，可通过在配置关闭`Ribbon`来启用`Spring Cloud Loadbalancer`

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

`Resilience4j`是一个轻量、易用、可组装的高可用框架，支持***熔断***、***高频控制***、***隔离***、***限流***、***限时***、***重试***等多种高可用机制。 目前实现上述服务容错的方案大致可分为以下五种：

1.  超时：给每个请求配置一个超时时间，如果超过配置时间，则释放线程资源
2.  限流：为服务设置最大并发数，放置线程生成过多而导致资源耗尽
3.  仓壁模式：每个服务使用独立的线程池，相互之间隔离，互不影响
4.  断路器模式：当一个服务触发断路条件时，开启断路器
5.  重试：进行多次尝试来容错

在断路器模式中，设计断路器状态的变化，断路器会在关闭、开启、半开三个模式之间转换，具体如下图：

![1615947858284](README/1615947858284.png)

下文详细介绍每种模式的具体配置以及使用方式。

#### 依赖

```xml
<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-spring-cloud2</artifactId>
    <version>1.7.0</version>
</dependency>
```

#### 限流模式

使用`@RateLimiter`注解，` Resilicence44j `的限流实现有

-    `io.github.resilience4j.ratelimiter.internal.AtomicRateLimiter` 默认，基于令牌桶算法 
-   `io.github.resilience4j.ratelimiter.internal.SemaphoreBasedRateLimiter` 基于`Semaphore`类 

```java
@RateLimiter(name = "testRateLimiter", fallbackMethod = "fallbackMethod")
@GetMapping("testRateLimiter")
public String testRateLimiter(){
    return "ok";
}
```

相关配置

```properties
# 在刷新周期内，请求的最大频次
resilience4j.ratelimiter.instances.testRateLimiter.limit-for-period=1
# 刷新周期时长
resilience4j.ratelimiter.instances.testRateLimiter.limit-refresh-period=1s
# 线程等待许可的时间，0表示线程不等待则直接抛异常
resilience4j.ratelimiter.instances.testRateLimiter.timeout-duration=0
```

#### 仓壁模式

` Resilicence44j `的仓壁模式的实现有

-   ` Semaphore`方式：默认，每个请求去获取信号量，如果没有获取到，则拒绝请求
-   `ThreadPool`方式：每个请求去获取线程，如果没有获取到，则进入等待队列，如果队列已满，则执行拒绝策略

从性能角度来看，基于`Semaphore`要优于基于`ThreadPool`要好，在基于`ThreadPool`时，可能会导致过多的小型的隔离线程池，会导致整个微服务的线程数过多，而线程数过多会导致线程上下文切换过多，影响性能。

```java
@Bulkhead(name = "testBulkhead", fallbackMethod = "fallbackMethod")
@GetMapping("testBulkhead")
public String testBulkhead(){
    return "ok";
}

@Bulkhead(name = "testThreadPoolBulkhead", fallbackMethod = "fallbackMethod", type = Bulkhead.Type.THREADPOOL)
@GetMapping("testThreadPoolBulkhead")
public String testThreadPoolBulkhead(){
    return "ok";
}
```

相关配置
```properties
# 最大并发请求数
resilience4j.bulkhead.instances.testBulkhead.max-concurrent-calls=1
# 仓壁饱和时的最大等待时间，默认0
resilience4j.bulkhead.instances.testBulkhead.max-wait-duration=10ms
# 事件缓冲区大小
resilience4j.bulkhead.instances.testBulkhead.event-consumer-buffer-size=1

# 最大线程池大小
resilience4j.thread-pool-bulkhead.instances.testThreadPoolBulkhead.max-thread-pool-size=1
# 核心线程数
resilience4j.thread-pool-bulkhead.instances.testThreadPoolBulkhead.core-thread-pool-size=1
# 队列容量，默认100
resilience4j.thread-pool-bulkhead.instances.testThreadPoolBulkhead.queue-capacity=10
# 当线程数大于内核数时，多余空闲线程存活时间，默认20ms
resilience4j.thread-pool-bulkhead.instances.testThreadPoolBulkhead.keep-alive-duration=10ms
# 事件缓冲区大小
resilience4j.thread-pool-bulkhead.instances.testThreadPoolBulkhead.event-consumer-buffer-size=100
```

#### 断路器模式

` Resilicence44j `的断路器模式使用` io.github.resilience4j.circuitbreaker.internal.CircuitBreakerStateMachine `基于有限状态机来实现。

```java
@CircuitBreaker(name = "testCircuitBreaker", fallbackMethod = "fallbackMethod")
@GetMapping("testCircuitBreaker")
public String testCircuitBreaker(){
    if (i++ % 2 == 0) {
        throw new RuntimeException("random exception");
    }
    return "ok";
}
```

相关配置

```properties
# 滑动窗口大小，默认100
resilience4j.circuitbreaker.instances.testCircuitBreaker.sliding-window-size=100
# 滑动窗口类型，默认COUNT_BASED
resilience4j.circuitbreaker.instances.testCircuitBreaker.sliding-window-type=COUNT_BASED
# 断路器半开时，允许的请求尝试的个数，默认10
resilience4j.circuitbreaker.instances.testCircuitBreaker.permitted-number-of-calls-in-half-open-state=10
# 启动断路器的最小请求数
resilience4j.circuitbreaker.instances.testCircuitBreaker.minimum-number-of-calls=10
# 断路器从打开切换到半开的时间
resilience4j.circuitbreaker.instances.testCircuitBreaker.wait-duration-in-open-state=60s
# 错误率阈值
resilience4j.circuitbreaker.instances.testCircuitBreaker.failure-rate-threshold=50
# 慢请求率阈值
resilience4j.circuitbreaker.instances.testCircuitBreaker.slow-call-rate-threshold=100
# 慢请求时间阈值
resilience4j.circuitbreaker.instances.testCircuitBreaker.slow-call-duration-threshold=60s
# 记录异常的Predicate，java.util.function.Predicate的实现类
resilience4j.circuitbreaker.instances.testCircuitBreaker.record-failure-predicate=
# 纳入调用失败率统计的异常列表
resilience4j.circuitbreaker.instances.testCircuitBreaker.record-exceptions=java.lang.Exception
# 不会纳入调用失败率统计的异常列表
resilience4j.circuitbreaker.instances.testCircuitBreaker.ignore-exceptions=
# 是否将断路器监控信息注册到/actuator/health
resilience4j.circuitbreaker.instances.testCircuitBreaker.register-health-indicator=true
# 事件缓冲区大小
resilience4j.circuitbreaker.instances.testCircuitBreaker.event-consumer-buffer-size=10
```

#### 重试模式

使用`@Retry`注解

```java
@Retry(name = "testRetry", fallbackMethod = "fallbackMethod")
@GetMapping("testRetry")
public String testRetry(){
    if (true) {
        throw new RuntimeException("exception");
    }
    return "ok " + i++;
}
```

相关配置

```properties
# 最大重试次数，默认3
resilience4j.retry.instances.testRetry.max-attempts=3
# 多次重试的间隔
resilience4j.retry.instances.testRetry.wait-duration=500ms
# 是否开启指数退避，默认false
resilience4j.retry.instances.testRetry.enable-exponential-backoff=true
# 时间间隔乘数，配置enable-exponential-backoff使用
resilience4j.retry.instances.testRetry.exponential-backoff-multiplier=2
# 是否开启随机重试时间
resilience4j.retry.instances.testRetry.enable-randomized-wait=false
# 重试间隔随机因子，配合enable-randomized-wait使用
resilience4j.retry.instances.testRetry.randomized-wait-factor=2
# 记录异常的Predicate，java.util.function.Predicate的实现类
resilience4j.retry.instances.testRetry.retry-exception-predicate=
# 需要重试的异常
resilience4j.retry.instances.testRetry.retry-exceptions=java.lang.Exception
# 不需要重试的异常
resilience4j.retry.instances.testRetry.ignore-exceptions=
# 事件缓冲区大小
resilience4j.retry.instances.testCircuitBreaker.event-consumer-buffer-size=10
```

#### Fallback方法

`fallback`方法名称保持和注解中的`fallbackMethod`属性值一致以及和原方法返回值一致，并且在参数中增加`Throwable throwable`

```java
public String fallbackMethod(Throwable throwable) {
    log.error("Fallback Happened", throwable);
    return "ok fallback";
}
```

#### 默认配置中

在使用默认配置时，需要在相应注解中取消`name`属性，并在配置文件中执行`default`属性即可，例如
```properties
resilience4j.ratelimiter.configs.default.limit-for-period=1
```

以上各个模式的测试场景可参考项目中`Resilience4jTest.java`