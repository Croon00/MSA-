# Microservice간의 통신

![1](https://github.com/Croon00/MSA-/assets/73871364/f0047947-7d4d-457a-84f1-bfcb315b2ae1)

- 서버포트(랜덤포트 등)을 이용해서 ORDER-SERVER (라운드 로빈) 으로 순차적으로 요청
- 기능에 따라서 특정한 지역 , 시간대, 리소스에 따라서 요청을 분산 시킬 수도 있다.

![2](https://github.com/Croon00/MSA-/assets/73871364/73c96280-35cb-4a5e-9018-c8f0c4d6a3b7)

- 전통적으로 Rest Template 방법으로
- http protocol 을 이용해서 get 방식 post방식으로 또 다른 서비스에서 원하는 요청을 하는 방식
- Rest Template 객체를 이용해서 서비스를 대신 처리 해줄 수 있는 대리자
- api gateway에 http://127.0.0.1:8000/user-service/user/{userId} 로 클라이언트가 요청을 했을 때
- User Service는 REST template은 Eureka 서비스에서 얻어왔던 order service에 요청을 통해 정보를 얻어 와서 사용하게 할 것이다.

```java
// 다른 컨트롤러나 repository에서 주입 받아서 사용 가능하게
    @Bean
    public RestTemplate getRestTemnplate() {
        return new RestTemplate();
    }
```

- user-service에서 RestTemplate 사용하기 위한 빈 주입

```java
@Override
    public UserDto getUserByUserId(String userId) {
        UserEntity userEntity = userRepository.findByUserId(userId);

        // 해당 유저가 없는 경우
        if (userEntity == null){
            throw new UsernameNotFoundException("User not found");
        }

        UserDto userDto = new ModelMapper().map(userEntity, UserDto.class);

//        List<ResponseOrder> orders = new ArrayList<>();
        /* Using as resttemplate */
        // 밑에 포트 번호는 변경될 수 있다. (하드코딩 대신 별도의 구성 파일에 만들어놓는 것이 좋다)
//        String orderUrl = "http://127.0.0.1:8000/order-service/%s/orders";
        String orderUrl = String.format(env.getProperty("order_service.url"), userId);
        // exchange에 파라미터 --> url, method, 요청 파라미터, 전달 받고자하는 타입
        ResponseEntity<List<ResponseOrder>> orderListResponse = restTemplate.exchange(orderUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<ResponseOrder>>() {
        });

        List<ResponseOrder> orderList = orderListResponse.getBody();
        userDto.setOrders(orderList);

        return userDto;
    }
```

```yaml
order-service:
  url: http://127.0.0.1:8000/order-service/%s/orders
```

- user-service용 yml 파일에서 order-service로 보낼 수 있는 url 설정을 해주고
- getUserByUserId를 위와 같이 수정하여서 orderList를 RestTemplate를 통해서 받아올 수 있게 해준다.

![3](https://github.com/Croon00/MSA-/assets/73871364/da84ff7c-5ede-4f8c-9de6-1abea7c46b61)

- 위와 같이 url 방식으로 마이크로 서비스를 연결하는 것은 불편한 방법으로 마이크로 서비스의 네임으로 연결하는 방법
- @LoadBalanced를 사용

## Feign Web Service Client 방법

- FeignClient → HTTP Client
- REST Call을 추상화 한 Spring Cloud Netflix 라이브러리
- 사용방법
- 호출 하려는 HTTP Endpoint에 대한 Interface를 생성
- @FeignClient 선언
- Load balanced 지원
- Spring Cloud Netflix 라이브러리 추가
- @FeignClient Interface 생성
- `@EnableFeignClients` 를 사용할 프로젝트 최상단 Application에 추가

```java
@FeignClient(name="order-service")
public interface OrderServiceClient {

    @GetMapping("/order-service/{userId}/orders")
    List<ResponseOrder> getOrders(@PathVariable String userId);
}
```

- OrderServiceClient를 위처럼 만들어준다.
- name에서는 마이크로서비스의 이름을
- @GetMapping으로는 해당하는 url을 설정해준다.

```java
@Override
    public UserDto getUserByUserId(String userId) {
        UserEntity userEntity = userRepository.findByUserId(userId);

        // 해당 유저가 없는 경우
        if (userEntity == null){
            throw new UsernameNotFoundException("User not found");
        }

        UserDto userDto = new ModelMapper().map(userEntity, UserDto.class);

//
////        List<ResponseOrder> orders = new ArrayList<>();
//        /* Using as resttemplate */
//        // 밑에 포트 번호는 변경될 수 있다. (하드코딩 대신 별도의 구성 파일에 만들어놓는 것이 좋다)
////        String orderUrl = "http://127.0.0.1:8000/order-service/%s/orders";
//        String orderUrl = String.format(env.getProperty("order_service.url"), userId);
//        // exchange에 파라미터 --> url, method, 요청 파라미터, 전달 받고자하는 타입
//        ResponseEntity<List<ResponseOrder>> orderListResponse = restTemplate.exchange(orderUrl,
//                HttpMethod.GET,
//                null,
//                new ParameterizedTypeReference<List<ResponseOrder>>() {
//        });
//
//        List<ResponseOrder> orderList = orderListResponse.getBody();

        /* Using a feign client */
        List<ResponseOrder> orderList = orderServiceClient.getOrders(userId);
        userDto.setOrders(orderList);

        return userDto;
    }
```

- 위와 같이 짧은 코드량으로 orderList를 받아서 userDto에 set 할 수 있다.
- 반대로 직관적으로 파악이 어려울 수 있다 —> RestTemplate의 장점이 좀 더 코드가 직관적이라 보기 좋다.

## Feign Client에서 로그 사용 및 예외 처리

![4](https://github.com/Croon00/MSA-/assets/73871364/22a28ef7-d5c0-45cb-b828-1ec42551e59a)

- 로깅 정보들이 나오게 한다.

![5](https://github.com/Croon00/MSA-/assets/73871364/743daead-4251-4202-b05f-e7299b17e660)

- 404 에러는 클라이언트의 uri 요청이 잘 못된 것인데 500에러가 발생한 것을 알 수 있다. 이 문제점을 해결해야 한다.

```yaml
logging:
  level:
    com.example.userservice.client: DEBUG
```

- client를 포함한 정보로 DEBUG

```java
@Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }
```

- User-service 최상단 UserServiceApplication에 위에 같이 빈 추가

```java
List<ResponseOrder> orderList = null;
                try {
                orderList = orderServiceClient.getOrders(userId);

                }catch (FeignException ex){
                    log.error(ex.getMessage());
                }
```

- try-catch를 통해서 FeginException에 대해서 받아서 넣기

## FeignErrorDecoder 로 에러 받기

```java
package com.example.userservice.error;

import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class FeignErrorDecoder implements ErrorDecoder {

    Environment env;

    @Autowired
    public FeignErrorDecoder(Environment env) {
        this.env = env;
    }

    @Override
    public Exception decode(String methodKey, Response response) {
        switch (response.status()){
            case 400:
                break;
            case 404:
                if(methodKey.contains("getOrders")){
                    return new ResponseStatusException(HttpStatus.valueOf(response.status()),
                        env.getProperty("order_service.excpetion.orders_is_empty"));
                }
                break;

            default:
                return new Exception(response.reason());
        }
        return null;
    }
}
```

- 위와 같이 FeignErrorDecoder를 만들어준다
- 여기서 env로 에러에 띄워줄 메시지를 yml에서 설정 해놓았다.

```java
order-service:
  url: http://ORDER-SERVICE/order-service/%s/orders
  exception:
    order_is_empty: User's orders is empty.
```

```java
/* ErrorDecoder */
        List<ResponseOrder> ordersList = orderServiceClient.getOrders(userId);
        userDto.setOrders(ordersList);
```

- 기존에 썼던 방식을 사용하면 된다.

## 데이터 동기화 문제

![6](https://github.com/Croon00/MSA-/assets/73871364/fd917cf9-3367-46b1-9150-5af5b8d037db)

- 여러가지 인스턴스를 부하분산을 위해 띄웠을 때
- 하나는 60001번 하나는 60002번에 order-service를 띄웠을 때 각자 데이터베이스를 구현 해놓았을 때 주문을 했을 때 이렇게 데이터가 분산되어서 저장된는 동기화 문제
- 사용자가 주문 했을 때 주문 정보를 했을 때 어떤 때에는 첫 번째 인스턴스 어떤 때에는 두 번째 인스턴에 저장, 그러면 주문 정보를 가져올 때 어떤 때에는 60001 번 인스턴스에 들어간 정보를 가져오고 어떤 때에는 60002 번 인스턴스에 들어간 정보를 가져오는 문제 발생

1. order-service를 하나의 마이크로서비스로 보고 하나의 database에 저장하는 법

![7](https://github.com/Croon00/MSA-/assets/73871364/8805633e-a749-4792-b10e-b8312da85810)

- 만약에 물리적으로 다른 서비스를 같은 데이터베이스를 사용하려면 동시성이나 트랜잭션 문제가 일어날 수 있다.
- 거기다가 완전 개발환경 부터 다른 서비스를 사용할 경우 문제가 발생할 수 도 있다.
1. 데이터베이스를 서로 동기화 하여서 사용

![8](https://github.com/Croon00/MSA-/assets/73871364/611834fb-2e12-4d90-9781-f825336c9e3d)

- apache kafka, rabbitmq 를 이용하여 데이터 변경 사항을 Message Queing 서버에 알려주면 두 번째 order-service가 변경된 사항을 가져가는 방법(구독해놓았을 경우)

1. 첫 번째와 두 번째 방법을 둘다 사용하는 방법

![9](https://github.com/Croon00/MSA-/assets/73871364/32776a02-4aae-4379-8f87-9d55a2c24233)
72e13f3a-991a-4268-9635-650c28c808ed)

- Messaging Queuing 에 메시지를 계속 보내게 되면 이러한 처리를 하기위해서 특화 되어있는 middle ware 임으로 아무리 많은 데이터가 들어와도 1초 안에 수만건을 처리 —> 동시성이나 시간적인 문제를 해결할 수 있다.
- 이 데이터를 하나의 단일 데이터베이스에 저장하면 조회했을 때 하나의 데이터베이스를 사용함으로 동기화 문제는 일어나지 않는다.
