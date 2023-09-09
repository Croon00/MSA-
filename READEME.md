## 데이터 동기화를 위한 Apache Kafak 1

- Apache Software Foundation의 Scalr 언어로 된 오픈 소스 메시지 브로커 프로젝트
- Open Source Message Broker Project
- 링크드인에서 개발, 2011년 오픈 소스화
- 실시간 데이터 피드를 관리하기 위해 통일된 높은 처리량, 낮은 지연 시간을 지닌 플랫폼 제공

![1](https://github.com/Croon00/MSA_Study-inflearn-/assets/73871364/d44c9d32-af9d-40b4-a5e7-ba7f8137e03f)

- 다양한 형태 시스템에 데이터를 전달한다 가정해보면
- 데이터 연동과정이 위와 같이 복잡할 수 밖에 없다.

![2](https://github.com/Croon00/MSA_Study-inflearn-/assets/73871364/8af87227-61ee-4f3a-a135-cb0bbdf768f0)

- kafka 시스템 도입으로 데이터베이스들의 스토리지를 어떠한 시스템을 사용하는지 중요하지 ㅇ낳고 단일 포맷을 사용할 수 있게 된다.
- 메시지를 보내는 쪽 → Producer 메시지를 받는 쪽 → Consumer 각각 분리 가능
- 다양한 형태의 여러개 컨슈머에게 데이터를 전달할 수 있는 장점도 있다
- 높은 처리량, 스케일링이 가능

### Kafka Broker

![3](https://github.com/Croon00/MSA_Study-inflearn-/assets/73871364/88075a68-6afc-400a-917e-5b0502aafc58)

- 실행되는 애플리케이션 서버
- 3대 이상의 Broker로 구성되는 것을 권장 - 멀티 클러스터 구축
- 하나의 Broker가 문제 생겼을 때 대신 사용할 수 있는 Broker를 사용하여 데이터의 안정성 보완
- 서버의 상태, 리더, 장애에 대한 체크 복구 —> 코디네이터(apache Zookeeper)가 사용된다.

## Ecosystem - Kafak Client

![4](https://github.com/Croon00/MSA_Study-inflearn-/assets/73871364/4bbccda0-5910-4511-ac49-f8fe95b39260)

- kafak 클러스터에서 다른 쪽에 있는 kafak의 데이터를 보내고 단순하게 메시지 자체로만 생성하고 전달이고 추가가 되는 consumer 형태

[Clients - Apache Kafka - Apache Software Foundation](https://cwiki.apache.org/confluence/display/KAFKA/Clients)

- 다양한 카프카 기능 지원

### Kafka 서버 기동

![5](https://github.com/Croon00/MSA_Study-inflearn-/assets/73871364/0d76a6fd-6eff-4446-bc26-86b428d2c3ae)

- create로 topic을 생성하고 해당하는 토픽에 메시지를 보내는 것
- —botstrap-server : 단일서버, 9092 —> kafak 서버의 포트번호
- —partitions —> 멀티 클러스터링으로 구성 했을 때 토픽에 전달 한 메시지를 몇 군데로 나눠서 저장할지
- kafak 토픽 목록을 확인하기 위해서 —list
- 상세하게 토픽 정보 확인하려면 —describe , quickstart-events는 토픽의 이름\
- zookiper는

![6](https://github.com/Croon00/MSA_Study-inflearn-/assets/73871364/e9dd01d2-6eb4-49e3-9c00-757542f39bdd)

- .\bin\windows\zookeeper-server-start.bat .\config\zookeeper.properties 로 주키퍼 실행 - 2181 포트로 실행
- .\bin\windows\kafka-server-start.bat .\config\server.properties 로 카프카 실행 -
- .\bin\windows\kafka-topics.bat --bootstrap-server localhost:9092 --list 로 토픽 확인
- .\bin\windows\kafka-topics.bat --bootstrap-server localhost:9092 --create --topic quickstart-events --partitions 1로 토픽 생성
- .\bin\windows\kafka-topics.bat --bootstrap-server localhost:9092 --describe --topic quickstart-events : 토픽 상세 정보
- producer 실행 :  .\bin\windows\kafka-console-producer.bat --broker-list localhost:9092 --topic quickstart-events
- consumer 실행 :  .\bin\windows\kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic quickstart-events --from-beginning
- —from-beginning임으로 처음 보냈던 메시지 부터 전부 뜬다.
- producer가 하는 역할은 consumer 한테 바로 보내는 것이 아닌 consumer가 토픽에 관심있다고 등록하면 등록되어진 토픽한테서 메시지를 받게 되는 것

## Kafka connect

- Data를 자연스럽게 다른곳으로 보내는 법
- Standalone mode와 Distribution mode 지원
- 데이터를 다양한 형태로 저장 가능

![7](https://github.com/Croon00/MSA_Study-inflearn-/assets/73871364/a374c5

- 서로 다른 데이터베이스에 저장할때도 카프카 커넥트 사용

## MariaDB 사용

- mariadb 설치 및 mariadb client를 통해서 mydb라는 데이터베이스 생성

```yaml
<dependency>
			<groupId>org.mariadb.jdbc</groupId>
			<artifactId>mariadb-java-client</artifactId>
		</dependency>
```

- mariaDB 디펜던시 추가
- h2-console에서 mariaDB 사용가능
- JDBC URL 에서 jdbc:mariadb://localhost:3307/mydb로해줘야 한다 mysql이랑 mariadb 같이 있어서 그런가?

## Kafka Connect 설치

![8](https://github.com/Croon00/MSA_Study-inflearn-/assets/73871364/80f8517f-dc5c-4cc3-a7a4-335097ee224e)

![9](https://github.com/Croon00/MSA_Study-inflearn-/assets/73871364/a00138be-a495-4ff4-a303-964b801f4eb9)
90-15cf-4152-a0e8-60389b1b2fc2)
![10](https://github.com/Croon00/MSA_Study-inflearn-/assets/73871364/6035779b-b97e-42f3-9df3-dd523a149833)

![11](https://github.com/Croon00/MSA_Study-inflearn-/assets/73871364/39bbff75-591d-4ccf-a6ef-f7c5422a643b)

[카프카 커넥트 서버 기동시 발생하는 오류 - 인프런 | 질문 & 답변](https://www.inflearn.com/questions/199173/카프카-커넥트-서버-기동시-발생하는-오류)

### Kafka Source Connect 테스트

![12](https://github.com/Croon00/MSA_Study-inflearn-/assets/73871364/7eac27ab-0ba1-48cd-9f6e-762393ad020b)

- postman을 이용해서 테스트 커넥터 추가한다
- 나의 경우 jdbc:mariadb://localhost:3307/mydb
- 127.0.0.1:8083/connectors 로 post 요청하기(커넥터가 8083 포트를 이용한다.)
- kafka-connect는 .\bin\windows\connect-distirbuted .\etc\kafka\connect-distributed.properties로 실행
- 토픽 확인 :

.\bin\windows\kafka-topics.bat --bootstrap-server localhost:9092 --list __consumer_offsets
__consumer_offsets

### Kafka Sink Connect

![13](https://github.com/Croon00/MSA_Study-inflearn-/assets/73871364/ecc0ae35-e1db-4175-ab06-15ee2a1e8b3f)

- sink의 역할 : topic에서 받은 데이터를 어디다 전달할지
- topic을 연결해준다.
- 한쪽의 소스의 데이터에서 가공 추출 등을 이용해서 다른 쪽 타겟 테이블에 추가가 가능하다.
