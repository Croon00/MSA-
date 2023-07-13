## API Gateway

![apigateway](https://github.com/Croon00/Persona/assets/73871364/9e333c9c-fcef-415e-9e95-fd4d983d50f4)

- 마이크로 서비스를 제공함으로서 프론트단에서 필요한 요청을 할때 해당 서비스로 요청하기 위해 필요한 것이 API Gateway
- 인증 및 권한 부여 가능
- 서비스 검색 통합 가능
- 응답 캐싱
- 정책, 회로 차단기 및 QoS 다시 시도
- 속도 제한
- 부하 분산
- 로깅, 추적, 상관 관계 (ELK 등을 이용한)
- 헤더, 쿼리, 문자열 및 청구 변환
- IP 허용 목록에 추가

## Netflix Ribbon

### Spring Cloud에서 MSA간 통신

1. RestTEmplate
2. Feign Client

```java
RestTemplate restTemplate = new RestTemplate();
restTemplate.getForObject("http://localhost:8080/", User.class,200);
```

```java
@FeginClient("stores")
public interface StoreClient {
	@RequestMapping(method = RequestMethod.GET, value = "/stores")
	List<Stroe>getStores();
```

### Ribbon: Client side Load Balancer

- 클라이언트 사이드에서 Ribbon이 존재
- 서비스 이름으로 호출
- Health Check - 서비스가 정상 작동 중인가?
- 비동기(x) 이여서 최근에 잘 사용 안함 (functional api or 리액트 자바)
- ip:port 번호가 필요 없이 MSA의 name을 이용해서 사용
- 다음 버전부턴 없을 가능성
  
![zull](https://github.com/Croon00/Persona/assets/73871364/b9ffd73e-cb78-4509-852c-34e5b68ab935)

## Netflix Zuul

- First Service
- Second Service
- Netflix Zuul → GateWay의 기능을 해준다.

![zuul](https://github.com/Croon00/Persona/assets/73871364/ed715f37-2ee2-498f-bc70-c41812eea26b)

## Spring Cloud Gateway

- GateWay 기능

```yaml
cloud:
	gateway:
				routes:
					- id: first-service
						url: http://localhost:8081/
						predicates:
							- Path=/first-service/**
					- id: first-service
						url: http://localhost:8082/
						predicates:
							- Path=/second-service/**
```

- id : 고유한 값
- url : 어디로 포워딩 시킬 것인지
- predicates : 조건 (어떤 Path가 있으면 이 url로 포워딩 하는지)
- Netty started on port 8000 에서 볼 수 있듯이 tomcat이 아닌 Netty서버가 Api gateway가 작동한다 (비동기 방식이 사용가능)
- - Path=/second-service/** 임으로 service의 controller 단에서 RequestMapping으로 “/second-service”로 설정해주어야 한다 —> 요청이 http://localhost:8082/second-service 를 받음으로

@RestController와 @Controller의 차이는 RequestBody와 ResponseBody를 구현 하느냐 혹은 제공하는 것을 사용하느냐의 차이

![gateway](https://github.com/Croon00/Persona/assets/73871364/d5681527-484c-4320-9d88-47459c4811be)

- Gateway Handler Mapping : 어떤 요청이 들어왔는지 정보를 받기
- Predicate : 요청에 대한 사전 조건 (조건에 대한 분기)
- Pre Filter(사전 필터)  : 작업이 일어나기 전
- Post Filter(사후 필터) : 작업이 일어난 후
- 필터를 통한 요청정보를 구성할 수 있다.
- Property와 Java Code 두 방법으로 구성할 수 있다.
- Lamdar —> 익명 클래스(클래스나 인터페이스를 직접 선언하지 않은 상태에서 인스턴스를 생성하고 소멸하는 과정을 할 수 있다.)

```java
@Configuration
public class FilterConfig {
    @Bean
    public RouteLocator gatewayRoutes(RouteLocatorBuilder builder){
        return builder.routes()
                .route(r -> r.path("/first-serivce/**")
                            .filters(f -> f.addRequestHeader("first-request", "first-request-header")
                                           .addResponseHeader("first-response", "first-response-header"))
                            .uri("http://localhost:8081"))
                .route(r -> r.path("/first-serivce/**")
                        .filters(f -> f.addRequestHeader("second-request", "second-request-header")
                                .addResponseHeader("second-response", "second-response-header"))
                        .uri("http://localhost:8082"))
                .build();
    }
}
```

- 위처럼 yml 파일 대신 config에서 설정할 수도 있다.

```yaml
spring:
  application:
    name: apigateway-service
  cloud:
    gateway:
      routes:
        - id: first-service
          uri: http://localhost:8081/
          predicates:
            - Path=/first-service/**
          filters:
            - AddRequestHeader=first-request, first-requests-header2
            - AddResponseHeader=first-response, first-response-header2
        - id: second-service
          uri: http://localhost:8082/
          predicates:
            - Path=/second-service/**
          filters:
            - AddRequestHeader=second-request, second-requests-header2
            - AddResponseHeader=second-response, second-response-header2
```

- yml 방식으로 filter 처리하는 방법

## CustomFilter 방법

```java
package com.example.apigatewayservice.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class CustomFilter extends AbstractGatewayFilterFactory<CustomFilter.Config> {
    public CustomFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        // Custom Pre Filter
        return (exchange, chain) -> {
         ServerHttpRequest request = exchange.getRequest();
         ServerHttpResponse response = exchange.getResponse();

         log.info("Custom PRE filter: request id -> {}", request.getId());

         // Custom Post Filter
         return chain.filter(exchange).then(Mono.fromRunnable(() -> {
             log.info("Custom POST filter: response code -> {}", response.getStatusCode());
         }));
        };
    }

    public static class Config {
        // Put the configuration properties
    }
}
```

- AbstractGatewayFilterFactory 를 상속해서 사용해야 한다.
- apply라는 메서드를 구현해주면 된다. (GatewayFilter를 반환하여 어떠한 작업을 할 것인지 알려줌)
- 사용자 인증 했을 때 서버로 부터 토큰을 받고 계속 들고다니는 토큰 (JWT)를 가지고 이를 prefilter에서 토큰을 가지고 있는지 확인하게 할 수 있다.
- Nettiy를 이용하여 비동기 방식 서버이다. —> .exchange 매개변수로 부터 ServerHttpRequest와 ServerHttpResponse 객체를  사용하게 된다.
- PRE filter와 POST filter 적용하여서 return을 chain에 가능
- `import org.springframework.http.server.reactive.ServerHttpRequest;` 에서 reactive가 rx java라고 해서 webflux를 지원 해주는 Spring5의 기능이다. 이것으로 import 해두어야 한다.
- Mono 라는 객체는 Webflux에서 Spring5에서 추가된 기능, 비동기 방식으로 단일 값 전달할 때에는 Mono라는 객체를 이용해서 한다.

```yaml
filters:
#            - AddRequestHeader=first-request, first-requests-header2
#            - AddResponseHeader=first-response, first-response-header2
             - CustomFilter
        - id: second-service
          uri: http://localhost:8082/
          predicates:
            - Path=/second-service/**
          filters:
#            - AddRequestHeader=second-request, second-requests-header2
#            - AddResponseHeader=second-response, second-response-header2
             - CustomFilter
```

- 만든 커스텀 필터 클래스의 이름으로 filters에 추가한다.
- Yaml에서 내가 넣을 라우팅 정보마다 지정을 해주어야 한다.

## GlobalFilter 방법

- CustomFilter와 다르게 모든 라우팅 정보에 Filter를 추가하게 한다.
- yml 파일에서 필요한 설정 arguments를 지정할 수 있다.(Config정보를 지정)
- 모든 Filter의 가장 첫 번째로 시작 하고 마지막에 종료될 때 실행된다.

```java
package com.example.apigatewayservice.filter;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class GlobalFilter extends AbstractGatewayFilterFactory<GlobalFilter.Config> {
    public GlobalFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        // Custom Pre Filter
        return (exchange, chain) -> {
         ServerHttpRequest request = exchange.getRequest();
         ServerHttpResponse response = exchange.getResponse();

         log.info("Global Filter baseMessage : {}", config.getBaseMessage());

         if (config.isPreLogger()) {
             log.info("Global Filter Start: request id -> {}", request.getId());
         }
         // Custom Post Filter
         return chain.filter(exchange).then(Mono.fromRunnable(() -> {

             if (config.isPostLogger()) {
                 log.info("Global Filter End: response code -> {}", response.getStatusCode());
             }
         }));
        };
    }

    @Data
    public static class Config {
        // Put the configuration properties
        private String baseMessage;
        private boolean preLogger;
        private boolean postLogger;
    }
}
```

## LoggingFilter

![logging](https://github.com/Croon00/Persona/assets/73871364/3c89ea0e-c6e5-4172-a26d-cc642f8fa63f)

```java
package com.example.apigatewayservice.filter;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.OrderedGatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class LoggingFilter extends AbstractGatewayFilterFactory<LoggingFilter.Config> {
    public LoggingFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        // Custom Pre Filter
//        return (exchange, chain) -> {
//         ServerHttpRequest request = exchange.getRequest();
//         ServerHttpResponse response = exchange.getResponse();
//
//         log.info("Global Filter baseMessage : {}", config.getBaseMessage());
//
//         if (config.isPreLogger()) {
//             log.info("Global Filter Start: request id -> {}", request.getId());
//         }
//         // Custom Post Filter
//         return chain.filter(exchange).then(Mono.fromRunnable(() -> {
//
//             if (config.isPostLogger()) {
//                 log.info("Global Filter End: response code -> {}", response.getStatusCode());
//             }
//         }));
//        };

        GatewayFilter filter = new OrderedGatewayFilter((exchange, chain) ->{
            ServerHttpRequest request = exchange.getRequest();
            ServerHttpResponse response = exchange.getResponse();

         log.info("Logging Filter baseMessage : {}", config.getBaseMessage());

         if (config.isPreLogger()) {
             log.info("Logging PRE Filter: request id -> {}", request.getId());
         }
         // Custom Post Filter
         return chain.filter(exchange).then(Mono.fromRunnable(() -> {

             if (config.isPostLogger()) {
                 log.info("Logging POST Filter: response code -> {}", response.getStatusCode());
             }
         }));
         // 실행 순서를 정할 수 있다 
        }, Ordered.HIGHEST_PRECEDENCE);

        return filter;
    }

    @Data
    public static class Config {
        // Put the configuration properties
        private String baseMessage;
        private boolean preLogger;
        private boolean postLogger;
    }
}
```

- Ordered.에서 HIGHST_PREDENCE인지 LOWEST_PRECEDENCE 인지에 따라서 Filter의 실행되는 순서를 정할 수 있다.

```yaml
filters:
#            - AddRequestHeader=second-request, second-requests-header2
#            - AddResponseHeader=second-response, second-response-header2
             - name: CustomFilter
             - name: LoggingFilter
               args:
                 baseMessage: Hi, there.
                 preLogger: true
                 postLogger: true
```

- 위와 같이 여러개의 필터가 붙을 때에는 name을 따로 따로 지정 해주어야 한다.
- args를 붙여서 각각의 필터에 파라미터를 넘길 수 있다.

## Spring Cloud Gateway - Eureka 연동

![eureka연동](https://github.com/Croon00/Persona/assets/73871364/7bd0b5f9-077c-4ad5-bbb2-47838de33c96)

- 이제 8000 포트의 Eureka 서버로 요청을 했을 때 Api Gateway에서 받아서 이를 각각 마이크로 서비스로 보내주어야 함으로 Api Gateway와 각각 서비스들을 등록을 해주고,

![eureka](https://github.com/Croon00/Persona/assets/73871364/7feaa5c7-37e4-4fac-9246-9ec4f11d6241)

```yaml
cloud:
    gateway:
      routes:
        - id: first-service
          uri: lb: //MY-FIRST-SERVICE

```

- 위와 같이 Eureka 서버에서 나오는 인스턴스의 이름을 가지고 uri에서 적용을 해주어야 한다.

## 각각 서비스들을 각각 2개씩 기동

![여러개기동](https://github.com/Croon00/Persona/assets/73871364/6ac696d6-cfaa-4512-a757-c7bb4313b969)

- 방법은 3가지 존재

```yaml
server:
  port: 0

spring:
  application:
    name: my-first-service

eureka:
  client:
    register-with-eureka: true
    fetch-registry: true
    service-url:
      defaultZone: http://localhost:8761/eureka

  instance:
    instance-id: ${spring.application.name}:${spring.application.instance_id:${random.value}}
```

- 위 인스턴스 이름을 각 포트마다 Eureka에 다르게 뜨게 하기 위해 port에 0으로 넣어주고
- instance-id 를 지정해준다.

```java
package com.example.firstservice;

import com.netflix.discovery.converters.Auto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/first-service")
@Slf4j
public class FirstServiceController {

    Environment env;

    @Autowired
    public FirstServiceController(Environment env){
        this.env = env;
    }

 

    @GetMapping("/check")
    public String check(HttpServletRequest request) {
        log.info("Server port={}", request.getServerPort());
        return String.format("Hi, there. This is a message from First Service on PORT %s", env.getProperty("local.server.port"));

    }
}
```

- 위와 같이 firstService에서  Environment를 객체로 주입 받는다.
- check로 url 요청 왔을 때 request로 부터 getServerPort를 받아서 어떤 port로 요청 한 것인지 화인이 가능하게 할 수 있다.
- 번갈아 가면서 요청을 다른 포트로 받게 된다.
