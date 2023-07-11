### Spring Cloud netflix Eureka

- Netflix가 가지고 있는 클라우드 기술을 Java Spring에 공유

![Untitled](https://s3-us-west-2.amazonaws.com/secure.notion-static.com/5ae392ab-6251-4a79-9751-e18644260c2e/Untitled.png)

- ServiceDiscovery (어떠한 서버가 어느 위치에 있는지 등록, 검색 등을 해주는 역할) 웹 서비스 처럼 기동
- 각 마이크로 서비스를 Discovery에 등록을 하여서 사용
- 만약 pc가 1대이다 —> 각각 서비스를 포트 번호를 다르게 실행, pc가 여러대 이다 —> 포트는 갖게하고 url을 다르게
- 클라이언트의 요청정보를 API gate(로드 밸런서)에 보내게 된다.
- 요청 정보에 따라서 Discovery에서 Key-value값으로 요청 정보가 어디로 갈지 로드밸런스 한테 다시 알려준다
- 그러면 해당 서비스로 요청이 간다

### EcommerceApplication.java

- @SpringBootApplication : Spring Boot에서의 Main() 역할
- @EnableEurekaServer :  이 SpringBoot프로젝트는 Eureka 서버로 등록을 해준다. —> 위에서 사용한 ServiceDiscovery로서 사용된다.

### application.yml

- Eureka 라이브러리가 포함된채 SpringBoot가 기동되면 Eureka 클라이언트 역할로 어딘가에 등록하는 기능이 실행된다.
- eureka : client: register-with-eureka와 fetch-registry 는 기본적으로 true로 되어있다.
- register-with-eureka : 현재 작업하고 있는 것을 클라이언트에 정보를 저장한다
- fetch-registry : 현재 작업하고 있는 것을 클라이언트에 정보를 저장한다
- 하지만 이 Springboot 자체가 Discovery의 역할을 하는 프로젝트이기 때문에 여기서는 자기 자신을 등록하게 됨으로 false로 해주어야 한다.
- 추가 해야하는 서비스 프로젝트에서는 true로 설정

### UserServiceApplication의 yml

```java
server:
  port: 0
spring:
  application:
    name: user-service

eureka:
  client:
    register-with-eureka: true
    fetch-registry: true
    service-url:
      defaultZone: http://127.0.0.1:8761/eureka
```

- service의 클라이언트 역할을 등록해야 한다.
- eureka서버의 url을 등록해주어야 한다 —> defaultZone에 서버의 ip 등록

### 중복 기동하는 방법

1. Run 버튼 클릭
2. Edit Configuration을 눌러서 UserServiceApplication을 copy하여서 item을 등록(environment에서 VM options에다가 -Dserver.port=9002 입력, 이것을 run (port를 바꾸어서) -D의 뜻 자바 클래를 실행할 때 부가적인 파라미터 옵션을 넣을 때 쓰이는 방법
3. 이 application을 빌드 하여 패키징 한다음에 command라인에서 실행하는 방법 : terminal에서 (혹은 파워쉘이나 커맨드) 작업하는 디렉토리 안에서 (src와 target이 보이는), pc에 java와 maven이 설치되어 있어야 한다 **mvn spring-boot:run -Dspring-boot.run.jvmArguments=’-Dserver.port=9003’**
4. 독립적인 명령 프롬프트(powerShell or 명령 프롬프트) 프로젝트의 dir로 이동 한 후 (target과 src가 보이는) 빌드 되어 있는 정보를 삭제하기 위해서 **mvn clean 빌드를 하기 위한 mvn compile package 후 target이 생성 target 안에 .jar파일이 생성된다. java -jar -Dserver.port=9004 ./target/user-service-0.0.1-SNAPSHOT.jar 로 실행 완료, (파워 쉘에서는 따옴표 넣어주기)**

### Spring에서 지원해주는 랜덤포트

```java
server:
	port: 0
```

- port에 0을 쓰면 랜덤포트를 사용하겠다는 의미
- Eureka server에 url:서비스이름:포트 로 뜨는데 0이 하나면 중복으로 실행해도 0 하나만 뜬다.

```java
eureka:
  instance:
		instance-id: ${spring.cloud.client.hostname}:${spring.application.instance_id:${random.value}}
```

- 위를 추가하여서 Eureka server에서 인스턴스 호스트네임과 랜덤포트를 넣어주어서 보이게 해준다.
