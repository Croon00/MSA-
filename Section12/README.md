# 데이터 동기화를 위한 Apache Kafka의 활용 2

## Orders MicroService와 Catalogs Microservice에 Kafka Topic 적용

![1](https://github.com/Croon00/MSA_Study-inflearn-/assets/73871364/eb50c1dc-3cf0-4d59-8758-709370d2416b)

- 사용자가 order-service에서 주문을 하면 catalog-service에서 상품이 하나 마이너스 되어야 한다.
- 각각 독립적인 데이터베이스를 구성했었는데 이를 동기화 해주어야 하는데 이때 kafka사용

```yaml
<dependency>
			<groupId>org.springframework.kafka</groupId>
			<artifactId>spring-kafka</artifactId>
</dependency>
```

- 카프카 dependencies를 각각 서비스에 추가

```java
package com.example.catalogservice.messagequeue;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@EnableKafka
@Configuration
public class KafkaConsumerConfig {
    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> properties = new HashMap<>();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        // 토픽에 쌓여있는 메시지를 가져가는 컨슈머들을 grouping 할 수 있다. 특정한 그룹을 지정할 수 있다.
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "consumerGroupId");
        // 키 타입을 지정해주는데, 데이터를 하나 만들어서 압축화하는 과정 --> Serializer인, 원래 있던 형태로 풀기 위해서 하는 것 Deserializer을 통해 String으로 다시 지정
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        return new DefaultKafkaConsumerFactory<>(properties);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory = new ConcurrentKafkaListenerContainerFactory<>();

        kafkaListenerContainerFactory.setConsumerFactory(consumerFactory());
        return kafkaListenerContainerFactory;
    }
}
```

```java
package com.example.catalogservice.messagequeue;

import com.example.catalogservice.jpa.CatalogEntity;
import com.example.catalogservice.jpa.CatalogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class KafkaConsumer {
    CatalogRepository repository;

    @Autowired
    public KafkaConsumer(CatalogRepository repository){
        this.repository = repository;
    }

    // 토픽이름을 지정 example-catalog-topic에 데이터가 전달되면 밑에 메소드 실행
    @KafkaListener(topics = "example-catalog-topic")
    public void updateQty(String kafkaMessage) throws JsonProcessingException {
        log.info("Kafka Message: -> " + kafkaMessage);

        Map<Object, Object> map = new HashMap<>();
        ObjectMapper mapper = new ObjectMapper();
        try{
           map = mapper.readValue(kafkaMessage, new TypeReference<Map<Object, Object>>() {});

        }catch(JsonProcessingException ex){
            ex.printStackTrace();
        }

        CatalogEntity catalogEntity = repository.findByProductId((String)map.get("productId"));

        if (catalogEntity != null) {
            catalogEntity.setStock(catalogEntity.getStock() - (Integer)map.get("qty"));
            repository.save(catalogEntity);
        }
    }
}
```

- kafka 설정 파일을 만들어준다.
- catalog의 경우 order에서 받아서 해야함으로 Consumer 이다.
- consumer에서 @KafkaListener로 topic의 이름을 지정하여서 repositoy를 이용해서 order의 qty 값 만큼을 빼준 값을 넣은 후 다시 save 해주어서 변경해준다.

```java
package com.example.orderservice.messagequeue;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
public class KafkaProducerConfig {
    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> properties = new HashMap<>();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        // 토픽에 쌓여있는 메시지를 가져가는 컨슈머들을 grouping 할 수 있다. 특정한 그룹을 지정할 수 있다.
        // 키 타입을 지정해주는데, 데이터를 하나 만들어서 압축화하는 과정 --> Serializer인, 원래 있던 형태로 풀기 위해서 하는 것 Deserializer을 통해 String으로 다시 지정
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        return new DefaultKafkaProducerFactory<>(properties);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
```

```java
package com.example.orderservice.messagequeue;

import com.example.orderservice.dto.OrderDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class KafkaProducer {
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    public KafkaProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public OrderDto send(String topic, OrderDto orderDto) {
        ObjectMapper mapper = new ObjectMapper();
        String jsonInString ="";

        try{
            jsonInString = mapper.writeValueAsString(orderDto);
        }catch (JsonProcessingException ex){
            ex.printStackTrace();
        }

        kafkaTemplate.send(topic, jsonInString);
        log.info("Kafka Producer sent data from the Order microService: " + orderDto);

        return orderDto;
    }

}
```

- order의 경우에는 producer이다.
- orderDto의 값으로 Json화 하여서 topic에 send해준다.

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

        // Send this order to the kafka
        kafkaProducer.send("example-catalog-topic", orderDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseOrder);
    }
```

- 이제 post로 orders를 할 때 order 값을 변경 시키는 것 뿐만 아니라 topic으로 orderDto를 send 까지 해주어야 한다.
- `defer-datasource-initialization: true` 를 yml파일에서 설정해주어야 data.sql로 테이블에 추가된다.

```yaml
server:
  port: 0
spring:
  application:
    name: catalog-service
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
    defer-datasource-initialization: true  # 여기에 추가
  datasource:
    driver-class-name: org.h2.Driver
    url: jdbc:h2:mem:testdb
#    hikari:
#      username: sa
#      password: 1234
eureka:
  instance:
    instance-id: ${spring.application.name}:${spring.application.instance_id:${random.value}}
  client:
    register-with-eureka: true
    fetch-registry: true
    service-url:
      defaultZone: http://127.0.0.1:8761/eureka

greeting:
  message: Welcome to the Simple E-commerce.
logging:
  level:
    com.example.catalogservice: DEBUG
```

```yaml
{
    "productId": "CATALOG-001",
    "qty": 10,
    "unitPrice": 1500
}
```

- 위와 같이 catalog를 주문하는 JSON을
- http://127.0.0.1:8000/order-service/baba1b1b-250a-45e6-ae5b-99d61d1d3707/orders 이렇게 POST로 신청하니 CATALOG 데이터베이스에서 10개의 수량이 줄은 것을 확인할 수 있었다. → 127.0.0.1:8000/catalog-service/catalogs (GET)

## Multiple Orders Service

![2](https://github.com/Croon00/MSA_Study-inflearn-/assets/73871364/bd91314e-ceb7-4fd0-99d5-2213228e5cec)

- 사용자 서비스에서 주문 실행하기 위해 주문 방법 요청하면 ORDER SERVICE가 첫 번째 인스턴스도 갈 수 있고 2번째 인스턴스 갈 수도 있다.
- 각각의 요청정보가 어디에 저장 되나? 혹은 상세정보 가져올때 정확히 가져오나?
- 사용자의 Order 정보를 가져오는 get 요청을 할 때마다 한 번은 위에 한 번은 아래 인스턴스에서 정보를 가져와서 보여지는 문제가 생김
- 단일 데이터를 가져오게 해결하기

![3](https://github.com/Croon00/MSA_Study-inflearn-/assets/73871364/fc7ec2eb-5c97-40a0-8422-3e810ba42a68)

- 각각의 데이터베이스를 제거하고 각각 메시지 값을 Queuing Server에 넘긴 후 이 서버에서 단일 데이터베이스에 전달한다.
- Message Queuing Server가 들어왔던 모든 데이터를 순차적으로 가지고 있다가 업데이트 시킨다.
- H2 DB에서 MariaDB 사용하도록 변경
- application.yml에서 datasource 설정 바꾸기
- mariaDB에서 orders 테이블 생성하기

```yaml
datasource:
    url: jdbc:mariadb://localhost:3307/mydb
    driver-class-name: org.mariadb.jdbc.Driver
    username: root
    password: test1357
```

![4](https://github.com/Croon00/MSA_Study-inflearn-/assets/73871364/2789261f-6e36-4a60-b065-8609e5d17103)

- 주문을 요청 했을 때
- database에 저장할 orderDto를 사용하지 않고
- 요청된 주문에 대해서 orderId에서 유효 아이디로 랜덤하게 할당 한 후
- 사용자가 설정했던 수량과 단가를 곱해서 totalprice를 구한다.
- 그 후 orderProducer를 추가 하여 사용자의 주문 정보를 kafka 토픽에 전달하게 한다.
- 토픽에 쌓였던 데이터 —> sink connect에 불려지고 이것이 토픽에 있었던 메시지 내용들을 열어보고 어떻게 저장되어있는지 파악하고 jdbc 커넥터에 저장하게 된다.

![5](https://github.com/Croon00/MSA_Study-inflearn-/assets/73871364/06b9a707-a258-476b-b942-e7c6584f9877)

- schema 부분은 테이블의 구조 → mariadb에서 직접 생성했었다.
- 값을 그대로 Java Object의 값을 JSON으로 바꾸어주어야 한다.
- fileds 값은 그대로 payload는 항상 바뀌는 값

![6](https://github.com/Croon00/MSA_Study-inflearn-/assets/73871364/2c3bd984-3d45-4642-996f-45848b9428da)

![7](https://github.com/Croon00/MSA_Study-inflearn-/assets/73871364/d12f041e-1c0b-4bcc-83bf-757bfd89375d)

- kafkatemplate을 이용해서 필요한 message의 값을 kafka 토픽에 실제적으로 보내게 된다.
- 고정 값인 fields는 구현
- schema 클래스는 위와 같은 형태로 만든다.

![8](https://github.com/Croon00/MSA_Study-inflearn-/assets/73871364/9e3a4bed-c688-475d-be0c-0896e5da87fa)

- 매번 바뀌는 payload는 따로 send라는 메서드를 통해서 만들어 return해준다.

![9](https://github.com/Croon00/MSA_Study-inflearn-/assets/73871364/610d1735-f154-443e-8237-d52598afa414)

- topic의 데이터가 추가 되면 해당 데이터 값을 mariadb에 업데이트 해야한다.
- kafka sink connector가 해당 작용을 한다. —> kafka connector를 실행한 후 위와 같은 sink-coinnect를 추가한다.
- database에 저장 되는 event를 저장하는 파트와 읽어오는 파트를 따로 만드는 cqrs라는 패턴도 존재, 시간 순서로 메시지를 업데이트 시키는 방법도 있음.
- 

"topic.creation.default.replication.factor":1,
"topic.creation.default.partitions" : 1

- 당연히 카프카 커넥트도 실행 시키고 해야한다.
- 좋은 질문, 답변

안녕하세요 강사님, 너무 좋은 강의 잘 수강하고 있어 감사합니다.

kafka 강의를 듣던 중 질문이 생겼는데요,

서로 다른 DB를 사용하는 각각의 마이크로서비스에서 데이터 동기화 문제를 해결하기 위해 Kafka를 사용한다는 것은 잘 알겠습니다.

그런데 동일한 DB를 사용하는 같은 마이크로서비스에서는 각각의 마이크로서비스에 서로다른 h2 DB가 연결되도록 하지 않고  동일한 mariadb로 설정해둔다면 데이터 동기화 문제는 없을 것 같은데 굳이 동일 마이크로서비스 내에서 서로 다른 인스턴스 사이의 데이터 동기화를 위해 kafka를 사용하는 이유가 무엇인지 잘 궁금해서 질문드립니다.

안녕하세요, 이도원입니다.

예를 들어, order-service에 데이터베이스 처리를 위한 코드가 포함되어 있으면, DB에 대한 종속성이 생기게 됩니다. 추후에 DB를 변경하거나, 마이그레이션 등의 작업이 발생할 때, DB에 대한 종속성 부분을 해결해야 합니다. 물론 실무에서는 DB를 변경하는 것은 그렇게 자주 있는 일도 아니고, 대부분의 애플리케이션에서도 DB 엑세스를 직접 처리하고 있습니다. 성능상에 문제도 없고요. 다만, 강의에서는 Kafka에 대한 설명을 드리는 부분에서 order-service에서는 DB를 상대하지 말고, Kafka에게만 정보를 전달해서, Kafka에서 DB나 다른 시스템으로 데이터를 전송해 주는 역할에 대해 설명 드렸었습니다. 추가로,DB에 여러 서비스나 애플리케이션이 커넥션 되어 작업을 처리하는 것는 것보다는, 애플리케이션에서는 Kafka에게 데이터를 전달하고 다른 처리를 할 수 있는 상황에 대해서도 설명을 드렸었습니다. 이해 되지 않는 부분이 있으시면 추가 질문 해 주세요.

감사합니다.
