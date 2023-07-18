## Users Microservice - 사용자 조회

- UserController에서 포트번호 확인할 수 있는 방법 완성
- 이전 까지는 Eureka에 직접 서비스가 바로 등록이 되었지만, 이를 Eureka를 통해서 port 번호를 매번 서비스를 구동할때마다 확인 했어야 한다.—> 계속 변경되기 때문에
- API Gateway를 에 등록을 하면 변경된 포트와 상관 없이 Eureka의 name을 가지고 작업 가능해진다.

```java
- id: user-service
          uri: lb://USER-SERVICE
          predicates:
            - Path=/user-service/**
```

![Gateway-userService](https://github.com/Croon00/MSA-/assets/73871364/f8aafd5f-ba55-4101-8e9e-3a4a29b540a0)

- Gateway-service를 가지고 user-serivce라고 등록한 유저서비스를 들어가서 했을 때 404에러가 뜨는 것을 확인할 수 있다.

![User_gateway다름](https://github.com/Croon00/MSA-/assets/73871364/c5076a9b-fdf3-4c8e-b423-16df0c9da9ec)

- 이유 : User Service의 URI와 API Gateway URI가 다르다.
- user-service/health_check라고 요청을 하여서 Gateway로 이동하게 되어서 UserSerivce로 이동하게 된다. 그러나 여기서 health_check라는 이름으로 GetMapping을 했기 때문에 /user-service/health_check와 다른 URI가 되는 것이다.

![1234445](https://github.com/Croon00/MSA-/assets/73871364/b8cdb515-471d-4678-b9d4-5345b4deed9c)

- 해결법: GetMapping URI에 user-service를 추가해준다.

## User Service의 사용자 조회

- Dto와 vo와 Entity를 전부 사용중인데 이 차이점에 대해서 알아보기
- UserDto, RequestUser, ResponseUser, UserEntity 등이 사용되는 이유는 주로 데이터 전송과 데이터베이스 관리를 위한 개념적인 구분과 데이터의 형태에 따른 목적에 따라 분리된 형태로 사용하기 위함입니다.
1. UserDto:
UserDto는 데이터 전송 객체(Data Transfer Object)입니다. 주로 네트워크를 통해 데이터를 주고받을 때 사용됩니다. 이 객체는 클라이언트와 서버 간에 데이터를 교환하는 용도로 사용됩니다. UserDto는 클라이언트로부터 받은 데이터를 서버에서 처리하기 쉽게 구조화된 형태로 포장하는 역할을 합니다. 예를 들어, 클라이언트가 회원 가입을 요청할 때, UserDto를 사용하여 클라이언트에서 입력한 데이터를 서버로 전달합니다.
2. RequestUser:
RequestUser는 클라이언트로부터 요청된 데이터를 나타내는 객체입니다. 주로 HTTP 요청의 바디(body)에 담겨 전달되는 데이터를 표현합니다. 이 객체는 클라이언트로부터 받은 데이터를 서버에서 처리하기 위해 파싱하거나 유효성 검사 등의 로직을 수행하는 데 사용됩니다. 예를 들어, 클라이언트로부터 회원 가입 요청이 오면 RequestUser 객체를 사용하여 요청된 데이터를 추출하고 검증합니다.
3. ResponseUser:
ResponseUser는 서버에서 클라이언트로 응답할 때 사용되는 객체입니다. 주로 HTTP 응답의 바디(body)에 담겨 전달되는 데이터를 표현합니다. 이 객체는 서버에서 처리된 결과를 클라이언트로 반환하기 위해 사용됩니다. 예를 들어, 회원 가입 요청에 대한 응답으로, 가입이 성공했는지 여부와 함께 응답 데이터를 ResponseUser 객체에 담아 클라이언트에게 전달합니다.
4. UserEntity:
UserEntity는 데이터베이스에서 사용되는 객체입니다. 주로 데이터베이스 테이블과 매핑되는 엔티티로 사용됩니다. UserEntity는 데이터베이스에 저장된 데이터를 표현하고, 데이터베이스와의 상호작용을 처리하는 데 사용됩니다. 예를 들어, 회원 정보를 저장하거나 조회하는 작업을 수행할 때 UserEntity를 사용합니다.

이렇게 UserDto, RequestUser, ResponseUser, UserEntity를 구분하여 사용함으로써 역할과 책임을 분리하고, 데이터의 형태와 용도에 맞게 구조화할 수 있습니다. 이를 통해 코드의 가독성과 유지보수성을 향상시키며, 확장성과 유연성을 높일 수 있습니다. 또한, 클라이언트와 서버 간의 인터페이스 정의에도 도움이 됩니다.

[data:image/svg+xml,%3csvg%20xmlns=%27http://www.w3.org/2000/svg%27%20version=%271.1%27%20width=%2738%27%20height=%2738%27/%3e](data:image/svg+xml,%3csvg%20xmlns=%27http://www.w3.org/2000/svg%27%20version=%271.1%27%20width=%2738%27%20height=%2738%27/%3e)

일반적으로 DTO(Data Transfer Object), VO(Value Object), Entity를 모두 사용하는 것이 좋은 설계 방법입니다. 각각의 개념은 다음과 같은 역할과 목적을 가지고 있습니다.

1. DTO (Data Transfer Object):
- 데이터 전송을 위한 객체입니다.
- 클라이언트와 서버 간의 데이터 교환을 담당합니다.
- 데이터의 속성을 표현하는데에 초점을 둡니다.
- 데이터의 전송을 위해 사용되므로, 주로 네트워크 통신 등에서 활용됩니다.
1. VO (Value Object):
- 값을 포함하는 객체입니다.
- 불변성(Immutable)을 가지고 있으며, 동등성 비교를 위해 동등성 메서드를 구현합니다.
- 도메인 모델의 특정 속성을 나타내기 위해 사용됩니다.
- 주로 도메인 로직에서 사용되고, 비즈니스 규칙과 속성을 표현하는데에 초점을 둡니다.
1. Entity:
- 데이터베이스의 테이블과 매핑되는 객체입니다.
- 데이터베이스의 행을 나타내는 레코드로 사용됩니다.
- 데이터베이스 조작(CRUD)이나 영속성(Persistence) 관련 작업을 담당합니다.
- 주로 데이터베이스와의 상호작용이 필요한 영역에서 사용됩니다.

각각의 개념은 서로 다른 목적과 역할을 가지고 있으며, 적절한 상황에서 사용됩니다. DTO는 데이터 전송을 위한 용도로 사용되고, VO는 도메인 모델의 속성을 표현하고 비즈니스 규칙을 적용하기 위해 사용됩니다. Entity는 데이터베이스와의 상호작용을 처리하고 영속성을 관리하기 위해 사용됩니다.

따라서, DTO, VO, Entity를 모두 사용하는 것은 데이터 전송, 도메인 모델, 데이터베이스 관리의 목적과 책임을 분리하여 코드를 구성하는데 도움을 줄 수 있습니다. 이는 코드의 가독성, 유지보수성, 확장성을 향상시키고, 각각의 객체에 적절한 역할과 책임을 할당하여 애플리케이션을 설계하는 좋은 방법입니다.

[DTO vs VO vs Entity](https://tecoble.techcourse.co.kr/post/2021-05-16-dto-vs-vo-vs-entity/)

- JPA의 기능을 이용해서 Repository에서 getUserByUserId 메서드 생성
- UserSerivceImpl에서 getUserByUserId의 비즈니스 로직과 getUserAll 만들기

```java
@Override
    public UserDto createUser(UserDto userDto) {
        // 랜덤 유효 아이디 생성
        userDto.setUserId(UUID.randomUUID().toString());

        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        UserEntity userEntity = mapper.map(userDto, UserEntity.class);
        userEntity.setEncryptedPwd(passwordEncoder.encode(userDto.getPwd()));

        userRepository.save(userEntity);

        UserDto returnUserDto = mapper.map(userEntity, UserDto.class);

        return returnUserDto;
    }

    @Override
    public UserDto getUserByUserId(String userId) {
        UserEntity userEntity = userRepository.findByUserId(userId);

        // 해당 유저가 없는 경우
        if (userEntity == null){
            throw new UsernameNotFoundException("User not found");
        }

        UserDto userDto = new ModelMapper().map(userEntity, UserDto.class);

        List<ResponseOrder> orders = new ArrayList<>();
        userDto.setOrders(orders);
        return userDto;
    }
```

- UserController에서 요청, 반환 만들기

```java
@GetMapping("/users")
    public ResponseEntity<List<ResponseUser>> getUsers() {
        Iterable<UserEntity> userList = userService.getUserByAll();

        List<ResponseUser> result = new ArrayList<>();
        // userList에
        userList.forEach(v -> {
            result.add(new ModelMapper().map(v, ResponseUser.class));
        });

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<ResponseUser> getUsers(@PathVariable("userId") String userId) {

        UserDto userDto = userService.getUserByUserId(userId);

        ResponseUser returnValue = new ModelMapper().map(userDto, ResponseUser.class);
        // userList에

        return ResponseEntity.status(HttpStatus.OK).body(returnValue);
    }
```

## CatalogService

![CatalogAPI](https://github.com/Croon00/MSA-/assets/73871364/5336f5d6-10e2-4920-af65-8e31438f1021)

```java
h2:
    console:
      enabled: true
      settings:
        web-allow-others: true
      path: /h2-console
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    generate-ddl: true
```

- JPA에서 ddl-auto를 통해서 실행할 때마다 테이블 미어내고 다시 create하기

```java
logging:
  level:
    com.example.catalogservice: DEBUG
```

- 로깅레벨을 지정하여 console에 찍히는 로깅을 원하는대로 할 수 있다.

```java
@JsonInclude(JsonInclude.Include.NON_NULL) --> null 값은 반환하지 않게 한다.
```

- 마찬가지 방법대로 dto, vo, Service, serviceImpl을 이용하여 API들 생성

### Serializable 쓰는 이유

**`Serializable`** 인터페이스는 객체의 상태를 직렬화(Serialization)하여 네트워크를 통해 전송하거나, 파일에 저장하고 다시 복원하는 등의 작업을 할 수 있도록 지원하는 자바 인터페이스입니다. **`Serializable`** 인터페이스를 구현한 클래스는 객체의 상태를 이진 형식으로 변환하여 전송이 가능하며, 이는 네트워크 통신, 데이터 저장 및 전송, 분산 컴퓨팅 등 다양한 시나리오에서 활용됩니다.

**`Serializable`** 인터페이스를 구현하는 이유는 다음과 같습니다:

1. 데이터의 영속성(Persistence): **`Serializable`**을 구현한 클래스는 객체의 상태를 파일이나 데이터베이스에 저장하고 나중에 다시 복원할 수 있습니다. 이를 통해 애플리케이션의 데이터를 영구적으로 보존하고, 다시 불러와서 사용할 수 있습니다.
2. 네트워크 통신: **`Serializable`** 객체는 네트워크를 통해 전송할 수 있습니다. 객체를 직렬화하여 바이트 형태로 변환하고, 이를 전송하고 수신측에서는 다시 역직렬화하여 객체로 복원할 수 있습니다. 이를 통해 분산 시스템에서의 데이터 교환과 클러스터 간의 통신 등에 활용할 수 있습니다.
3. 자바 직렬화 프레임워크 활용: 자바 직렬화 프레임워크에서는 **`Serializable`** 인터페이스를 구현한 클래스를 이용하여 객체를 직렬화 및 역직렬화하는 기능을 제공합니다. 이를 통해 객체를 손쉽게 직렬화하고, 필요에 따라 전송 또는 저장할 수 있습니다.

따라서, **`CatalogEntity`** 등의 클래스가 **`Serializable`** 인터페이스를 구현하는 것은 해당 클래스의 객체를 네트워크를 통해 전송하거나 영속성을 유지하기 위해서입니다. 직렬화를 통해 객체를 이진 형식으로 변환할 수 있으므로, 데이터의 전송과 보존에 활용될 수 있습니다.

## OrderService

![OrderApi](https://github.com/Croon00/MSA-/assets/73871364/6aa4e394-a99e-4669-8049-b75a44923b19)

- 마찬가지로 Controller, Service, Repository, Dto, vo, jpa 등을 통해서 등록과 주문내역 만들기

```java
@PostMapping("/{userId}/orders")
    public ResponseEntity<ResponseOrder> createUser(@PathVariable("userId") String userId, @RequestBody RequestOrder orderDetails){
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        OrderDto orderDto = mapper.map(orderDetails, OrderDto.class);
        // 어떤 유저가 주문한건지 알아야 함으로 set하기 이것을 주소값에서 가져온다.
        orderDto.setUserId(userId);
        OrderDto createdOrder = orderService.createOrder(orderDto);

        // 반환 할때 사용자에게 id와 name과 비밀번호와 같이 만들었다고 알리기 위해서 response 값 만들기
        ResponseOrder responseOrder = mapper.map(createdOrder, ResponseOrder.class);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseOrder);
    }
```

- 등록할 때 user의 Id를 같이 url에서 받아서 해당 orderDto에 userId를 같이 넣어준후 만든다.
- dto에서 totalPrice는 unitPrice * qty로 구해서 넣는다.
