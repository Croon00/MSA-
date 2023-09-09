# 장애 처리와 Microservice 분산 추적

## Microservice 통신 시 연쇄 오류

![1](https://github.com/Croon00/MSA_Study-inflearn-/assets/73871364/a7d8ab56-ac1b-44dd-9aba-1f051630e9de)

- getUser 와 같은 USER-SERVICE를 사용하였을 때 getOrders를 받아와야 하는데 OREDER SERVICE에서 문제가 발생하였을 때
- 해당하는 마이크로 서비스로 요청을 더 하지 못하게 해야한다.
- Fegin Client —> 임시로 에러가 발생했을 때 나타낼 수 있는 디폴트 값이나 우회 할 수 있게 해주어 정상적인 값을 반환 해주어야 한다.

![2](https://github.com/Croon00/MSA_Study-inflearn-/assets/73871364/a3a2c556-4f48-4453-b688-f8674297d283)

- 200 반환 하면서 orders의 부분이 비어있게 보여주게 해야한다.

## CircuitBreaker

![3](https://github.com/Croon00/MSA_Study-inflearn-/assets/73871364/b69ecf2e-42ae-46de-959f-12123f51ca2d)

- circuit브레이커를 통해서 timeout이 자주 만나면 데이터를 전달하게 하지 않고 클라이언트 요청을 알아서 회피 시키는 방법

## Resilience4j

- 서킷브레이커를 제공

![4](https://github.com/Croon00/MSA_Study-inflearn-/assets/73871364/cf4a637a-4e5d-4c4f-9f5a-bed40fc56ac5)

- fault tolerance 에러가 발생해도 정상적인 처리를 할 수 있게 하는 라이브러리

![5](https://github.com/Croon00/MSA_Study-inflearn-/assets/73871364/c7d624e6-f9d5-4587-ac19-8a5514b5afe5)

- 서킷브레이커를 이용하여 order의 메서드를 호출 할 때 문제가 생길 때의 new ArrayList<> 기본 생성자 값을 반환하여 보여준다. → 주문 내역이 없는 것처럼 보이기

![6](https://github.com/Croon00/MSA_Study-inflearn-/assets/73871364/e9787f39-e0ca-4472-9e0e-55a5eac345e8)

![7](https://github.com/Croon00/MSA_Study-inflearn-/assets/73871364/9ceddae8-d5ec-46ed-9067-b1d2d1471e90)

- 커스텀 하게 서킷브레이커를 설정하는 방법

## Zipkin - 분산환경 추적 시스템

![8](https://github.com/Croon00/MSA_Study-inflearn-/assets/73871364/32892dca-7f90-46af-a066-4d71f7ace66c)

![9](https://github.com/Croon00/MSA_Study-inflearn-/assets/73871364/663e0389-5afc-42f5-a3f9-07bf13a2aa85)

- Trace와 Span Ids를 이용한 추적 가능

![10](https://github.com/Croon00/MSA_Study-inflearn-/assets/73871364/fb7acf17-b1ee-49d8-8a1f-a26517511baf)

- 요청 할때 Trace ID가 할당된다. 처음 할당 될 때 Span ID도 같은 것이 할당
- Trace ID는 그대로 유지, Span ID(세부적인 트랜잭션)는 새로운 요청일 때마다 다른 것이 할당
- 새로운 요청 —> DDD 이용, Span은 각 서비스에서 요청 할때 새로 요청
- 서킷 브레이커 —> 잘못된 것을 우회 하는 것
- 이번에 얘기 —> 추적을 해서 누가 누구를 호출 했고, 시간이 얼마나 걸리면서 정상상태, 비정상 상태 등을 시각화 하여 보여준다.

[Quickstart · OpenZipkin](https://zipkin.io/pages/quickstart.html)

- 설치 법이 여러 방법 docker, window, mac 사용방법

![11](https://github.com/Croon00/MSA_Study-inflearn-/assets/73871364/fd524b01-7d2e-4300-b6e5-8c0170f030d0)

![12](https://github.com/Croon00/MSA_Study-inflearn-/assets/73871364/41157c85-6047-4ea5-91a5-90e9cbebf1a7)

![13](https://github.com/Croon00/MSA_Study-inflearn-/assets/73871364/7d9f64c0-6a2a-4b6b-b9cb-baca19498064)

- 같은 요청에 같은 TraceId를 확인할 수 있고 SpanID는 요청마다 각각 다른 것을 확인
- 

![14](https://github.com/Croon00/MSA_Study-inflearn-/assets/73871364/1f3716ec-72ad-41b9-943d-c98c4f5d9702)

- 성공 확인 가능

![15](https://github.com/Croon00/MSA_Study-inflearn-/assets/73871364/c456a3b9-5d30-487a-8946-deb24a2d6994)

![16](https://github.com/Croon00/MSA_Study-inflearn-/assets/73871364/02480583-448d-4ac2-ac32-0da3ccb3b0ba)

![17](https://github.com/Croon00/MSA_Study-inflearn-/assets/73871364/131869a2-2866-4b09-bc32-01ba2b9505f7)

- 장애 확인 가능하다.
