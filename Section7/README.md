## Spring Cloud Config

![1](https://github.com/Croon00/MSA-/assets/73871364/aa6d718b-fef2-4834-a61f-c518c1386189)

- application.yml 파일에서 설정하고 있는 것들을 SpringBoot 외부에서 관리할 수 있게 하는 방법
- 내부에 있을 경우에는 SpringBoot 프로젝트 자체를 다시 빌드해야 되지만 이것이 분리되어 있는 경우 이 설정 파일만 새로 적용하면 된다.
- 배포 파이프라인을 통해서 각 환경에 맞게 설정 정보를 넣을 수 있는 것이 가능해진다.
- git 혹은 현재 로컬에 있는 파일 시스템, Secure File Storage에 저장할 수 있게 가능

![2](https://github.com/Croon00/MSA-/assets/73871364/c7357abe-72ed-423c-986d-4e8e648769a8)

![3](https://github.com/Croon00/MSA-/assets/73871364/33ea1838-a794-4331-a6ee-07f0e259bf58)

- git을 통해서 yaml 파일을 생성해서 놓을 수 있다.
- local repository에 yml파일을 우선순위를 가지고 사용할 수 있다.

`{"name":"ecommerce","profiles":["test"],"label":null,"version":"d04a96f6d845230c8b25ad7662f6576a8a302f60","state":null,"propertySources":[{"name":"file:///MSAgit/MSA-/Section7/git-local-repo/ecommerce.yml","source":{"token.expiration_time":86400000,"token.secret":"user_token","gateway.ip":"172.30.1.30"}}]}`

- 로 나오는 것을 확인할 수 있다.

이 config를 사용하기 위해서 user-service에서 다음과 같은 dependencies를 추가해야 한다.

![4](https://github.com/Croon00/MSA-/assets/73871364/25e0ed50-3c26-4514-8644-d66f204cbb4a)

- 원래 가지고 있었던 application.yml 파일의 특정한 부분을 Spring cloud config에 저장시키는 것 —> spring cloud config를 먼저 실행 시키게 하겠다 —> bootstrap.yml 이다.
- git-load-repo라는 디렉토리 안에 있는 yml 파일 이름을 써준다.

## configure설정이 바뀌었을 때 적용하는 방법 3가지

- 서버 재기동 —> 비효율적
- Acuator refresh
- Spring cloud bus 사용

### Spring Boot Actuator

- Application 상태, 모니터링
- Metric 수집을 위한 Http End point 제공
- user-service에서 actuator레포지터리를 추가해준다.

```yaml
management:
  endpoints:
    web:
      exposure:
        include: refresh, health, beans
```

- yml파일에서 다음과 같이 management를 추가해준다.
- refresh, health, beans를 노출
- acuator/health —> up이라고 나온다 refresh 하게 되면 데이터 값들을 나오게 한다.
- actuator/beans 로 들어가면 현재 user-service의 bean들을 확인할 수 있다.
- actuator/refresh는 post방식으로 사용한다.
- git add , commit 하고나서 refresh를 해주면 정상적으로 설정 값이 다시 적용된다.

### Spring Cloud Gateway

- gateway에서도 마찬가지로 Config를 연동하여 설정할 수 있다.
- 마찬가지로 config, bootstrap, actuator dependencies를 추가하고 bootstrap.yml을 추가한 후

```yaml
- id: user-service
          uri: lb://USER-SERVICE
          predicates:
            - Path=/user-service/actuator/**
            - Method=GET,POST
          filters:
            - RemoveRequestHeader=Cookie
            - RewritePath=/user-service/(?<segment>.*), /$\{segment}
```

를 추가한다.

## Mutiple environments

- application-dev.yml 이렇게 만들어서 사용
- dev, prod 등을 만든다.

```yaml
spring:
  cloud:
    config:
      uri: http://127.0.0.1:8888
      name: ecommerce
  profiles:
    active: prod
```

- 위처럼 profiles에서 active에서 yml 파일에 -안에 있는 이름으로 가져다 쓴다.
- native하게 git을 안쓰고 사용하려면

![5](https://github.com/Croon00/MSA-/assets/73871364/efaac47d-5fdb-426a-a283-d0e6210bc458)

- 해당하는 것처럼 native 지정 후 파일 경로를 설정해 준 후 사용하면 된다 —> config에서
