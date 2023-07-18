# Spring Cloud Bus

- Actuator refresh는 application 가 한 두개가 아닌 여러개 혹은 수백개의 마이크로 서비스가 있을 때 이걸 다 refresh를 하면 번거롭다.

![1](https://github.com/Croon00/MSA-/assets/73871364/97a382b7-64f7-4b63-b820-ba554eb3fe72)

![2](https://github.com/Croon00/MSA-/assets/73871364/f720a9a3-a254-4571-bdc5-cd2e1936ec44)

- 각각의 마이크로 서비스가 Cloud Config 의 Cloud Bus가 각각에 push를 해준다.
- 변경 사항을 체크하고 이를 config에서 가져다가 Bus를 통해서 전달

### AMQP (Advanced Message Queuing Protocol) 메시지 지향 미들웨어를 위한 개방형 표준 응용 계층 프로토콜

- 메시지 지향, 큐잉, 라우팅(P2P, Publisher-Subcriber), 신뢰성, 보안
- Erlang, RabbitMQ에서 사용

### Kafka 프로젝트

- Apache Software Foundation이 Scalar 언어로 개발한 오픈 소스 메시지 브로커 프로젝트
- 분산형 스트리밍 플랫폼
- 대용량의 데이터를 처리 가능한 메시징 시스템

![3](https://github.com/Croon00/MSA-/assets/73871364/032fc3f0-f4f6-4886-8b0e-8a47a26a7efb)

- kafka는 pub → topic으로 데이터를 보낸다 sub해놓은 사용자에게 자동을 ㅗ보내짐

![4](https://github.com/Croon00/MSA-/assets/73871364/8e38b398-f2d5-4d4f-a117-279bee3f1489)

- 연결되있는 각각의 마이크로 서비스를 외부에서 bushrefresh를 호출하면 된다.
- Spring Cloud Bus가 연결된 그 누구에게도 busrefresh를 하게 되면 변경되어진 사항을 Bus에서 이를 확인하고 또 다른 마이크로 서비스에게도 이를 적용한다.
- Erlang 설치, rabbitmq 설치, 파워쉘을 이용해서 rabbitmq-plugins enable rabitmq_management 명령어를 통해서 설치
- 127.0.0.1:15672 들어가면 guest / guest로 로그인 가능
- 각각의 마이크로서비스가 rabbitmq의 노드로서 연결되는 논리

```yaml
<dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-bus-amqp</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-bootstrap</artifactId>
        </dependency>
```

- config 에 dependencies 추가

```yaml
<dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-bus-amqp</artifactId>
        </dependency>
```

- 위에 bus는 api-gateway와 user-service에도 추가

```yaml
rabbitmq:
    host: 127.0.0.1
    port: 5672
    username: guest
    password: guest
management:
  endpoints:
    web:
      exposure:
        include: health, busrefresh
```

- config 프로젝트에 rabbitmq 설정과 management를 추가한다.
- config에 변경 요청사항이 들어오면 rabbitmq에 요청사항 받았음을 통보한다.
- rabbitmq에 등록된 또 다른 마이크로 서비스에 그 정보를 일괄적으로 push 하게 된다.
