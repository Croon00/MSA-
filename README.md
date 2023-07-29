### 인프런 Spring Cloud 강의

[Spring Cloud로 개발하는 마이크로서비스 애플리케이션(MSA) - 인프런 | 강의](https://www.inflearn.com/course/스프링-클라우드-마이크로서비스/dashboard)

Resilient / ANTIFRAGILE(불확실성과 혼돈으로 부터 이익을 얻는 성질) 키워드의 증가 2010년 이후

- 서비스 간의 결합도를 낮춤
- 각 서비스를 독립적으로 확장 가능하게 설계한다.
- 서비스 간의 상호작용을 비동기 적으로 처리하여 시스템의 유연성을 높이는 방법
- 지속적인 변경이 있어도 시스템의 탄력적인 변경이 가능하게 만든다.

![그래프](https://github.com/Croon00/MSA-/assets/73871364/06496d31-1b2c-4034-8c1a-931fb4847b06)


- Anti-Fragile을 통해서 위에 그래프를 통해서 알 수 있는 것은 낮은 비용과 시스템 변화가 적고 화에 바로 적용할 수 있다는 것을 확인 가능


- Anti-Fragile의 주요 점으로 DevOps와 Cloud Native 기술들이 사용된다.

## Antifragile의 특징

![antiflagile](https://github.com/Croon00/MSA-/assets/73871364/c1672ceb-f0cd-4792-b073-644bb51ef1d1)

### 1. Auto scaling (자동 확장성)

- 시스템을 구성하고 있는 인스턴스를 하나의 오토 스케일링 그룹으로 묶는다.
- 그룹에서 유지되어야 하는 최소의 인스턴스를 지정 가능
- 사용량에 따라 자동으로 인스턴스를 증가한다.
- ex) 온라인 쇼핑몰에서 5월이나 11월 같이 성수기에 서버의 운영 개수를 늘리고 다시 줄이는 작업

### 2. Microservices

![netflix의 마이크로서비스](https://github.com/Croon00/MSA-/assets/73871364/a1fed421-fc1c-4663-98ac-ca0470cd33b0)

- netflix의 복잡한 서비스를 그래프와 노드로 보여준 경우
- Cloud Service를 가장 잘 이용하는 netflix에서 보여준다.
- Spring Cloud의 기능을 잘 적용하여서 사용
- 기존 서비스는 하나의 거대한 시스템으로 구축 됨 —> 전체의 서비스를 독립적인 모듈로 기능하고 배포하고 운영하는 특징이 MSA

### 3. Chaos engineering

![카오스 엔지니어링](https://github.com/Croon00/MSA-/assets/73871364/850379ca-e302-4335-a644-fbd2d65d94c2)

- 시스템의 실행하는 방법이라던가 규칙(불확실성에 대한 안정적인 서비스를 제공해야 한다)

### 4. CI/CD 지속적인 통합,배포

![CICD](https://github.com/Croon00/MSA-/assets/73871364/26fd9274-5c02-4fa7-93c2-f79eb8321f0a)

- 수십 수백개의 서비스를 pipeline으로 연계시켜 놓으면 빠르게 적용하여 빌드하고 배포 가능 (이전 부터 사용)

## Cloud Native Architecture의 특징

### 확장 가능한 아키텍처

- 시스템의 수평적 확정에 유연 (scale up—> 하드웨어적 향상, scale out —>인스턴스를 증가(서버를 증가))
- 확장된 서버로 시스템의 부하 분산, 가용성 보장
- 시스템 또는, 서비스 애플리케이션 단위의 패키지(컨테이너 기반 패키지)’
- 모니터링

### 탄력적 아키텍처

- 서비스 생성 - 통합 - 배포, 비즈니스 환경 변화에 대응 시간 단축
- 분활 된 서비스 구조
- 무상태  통신 프로토콜
- 서비스의 추가와 삭제 자동으로 감지
- 변경된 서비스 요청에 따라 사용자 요청 처리 (동적 처리)

### 장애 격리 (Fault isolation)

- 특정 서비스에 오류가 발생해도 다른 서비스에 영향 주지 않음

### Cloud native의 가이드

[The Twelve-Factor App](https://12factor.net/)

1. 코드 통합 - 각 서비스의 단일 코드 베이스(버전을 제어, 형상 관리) 코드를 통일적으로 관리
2. 종속성의 배제 - 각 마이크로 서비스는 자체 종속성을 가져서 다른 서비스 환경에 영향 X
3. 환경설정의 외부 관리 - 시스템 코드 관리 외부에서 환경 설정을 관리
4. 백업서비스의 분리 - 보조서비스(DB, 캐싱, 메시징 서비스) 등을 이용해서 MSA를 지원 (응용프로그램을 이용함으로 서로 상호 작용 가능한 서비스를 해서 종속을 안함)
5. 개발환경과 테스트환경의 분리 - 빌드와 release와 실행환경을 각각 분리
6. 상태관리 - 각각의 MSA는 다른 서비스와 분리된채 자체 서비스에서 독립적 실행 가능해야 함 (캐시등을 이용한 데이터 동기화)
7. 포트바인딩 - 각각의 서비스는 자체 포트를 가짐
8. 동시성 - 하나의 서비스가 동일한 인스턴스로 나눠서 제공 가능함으로 동시성을 가진다.
9. 서비스의 올바른 상태 유지 - 서비스 인스턴스 자체가 삭제가 가능해야 하고 정상적인 종료가 가능해야한다 (컨테이너의 도커를 통한 서비스의 실행 종료 삭제가 쉬어진다.)
10. 개발과 운영환경의 통일 - 수명주기 전체에 걸쳐서 서비스에 직접 access하는 상황을 제외 하여 종속적이지 않은 상태를 유지
11. 로깅 시스템 - 기존의 application과 분리되어서 작동하지 않더라도 log를 찍어내야 한다. log 관리를 위한 도구나 시스템을 이용해서 분석하는 용으로 사용 가능
12. 관리프로세스 - 현재 운영되고 있는 모든 MSA에 대해서 파악하기 위한 관리 도구 필요

+3개 더 고민해야 할 수 있다.

- API first - API를 구축함으로 사용자 측의 사용법에 대한 고민 부터
- Telemetry - 모든 지표는 시각화 되어서 구분해야한다.
- Authentication and authorization - API를 사용하는데에 있어서 인증 인가 기능을 가져야 한다.

### SOA와 MSA의 차이

![Untitled](https://github.com/Croon00/MSA-/assets/73871364/9ab5456b-9a75-4400-82c5-974b699e3680)

### RESTful

![asdf](https://github.com/Croon00/MSA-/assets/73871364/41d5c457-78d7-4832-a9fe-76e5c3cc1b67)

- 0단계에서는 그냥 url을 통해서 요청
- 1단계에는 resource를 uri에 합침
- 2단계에는 적절하게 HTTP Methods에 합침(get, post, put, delete)
- 3단계에는 하나의 리소스를 사용함에 있어서 다른 작업으로 넘어가는  것에 대해서 작용

 

### Spring Cloud 공식 홈페이지

[Spring Cloud](https://spring.io/projects/spring-cloud)

- 환경설정을 위한 SprongCloudConfigServer
- 마이크로서비스의 확인 검색등을 위한 Eureka 사용
- 로드 밸런싱을 위한 Spring Cloud Gateway
- FeginClient를 이용한 REST 요청 사용
- 외부 모니터링 서비스, 로그 추적 - ELK
- 장애 추적 및 빠르게 확인 Hystrix
