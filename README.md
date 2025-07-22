# 스프링 동시성 문제

분산 서버에서 동시성 문제 해결

## 시나리오

- 3개의 서버를 띄우고, 티켓 수를 1 감소시키는 총 300번의 request를 전송
![서버 구성](./readme-image/Spring%20동시성%20테스트.png)

- 프로젝트 루트 경로에서 실행 (Spring 빌드 + Redis 초기화 + 3개 서버 스케일링)
```shell
docker compose --profile init up --build --scale app=3 -d
```

- 기본(v1), 비관적 락(v2), 낙관적 락(v3), 분산 락(v4, v5) 테스트
- k6로 메서드마다 3개의 서버에 100번의 request를 보내 총 300번의 request를 보내는 테스트 실행
```shell
# 기본, 비관적 락, 낙관적 락 (DB)
k6 run -e TICKET_NAME=test1 -e API_VERSION=v1 script.js
k6 run -e TICKET_NAME=test2 -e API_VERSION=v2 script.js
k6 run -e TICKET_NAME=test3 -e API_VERSION=v3 script.js

# 분산 락 (redis)
k6 run -e TICKET_NAME=test4 -e API_VERSION=v4 script.js
k6 run -e TICKET_NAME=test5 -e API_VERSION=v5 script.js
```

- [TicketService](./src/main/java/dev/typhoon/spring_concurrency/service/TicketService.java)

## 결과

| API 버전 | 구현 방식 | 응답 성공 횟수 | 평균 실행 시간 | 남은 티켓 수 |
|---------|----------|-----------|-------------|-------------|
| v1 | 기본 | 300 / 300 | 624ms | 284 / 300 |
| v2 | 비관적 락 | 300 / 300 | 368ms | 0 / 300 |
| v3 | 낙관적 락 | 164 / 300 | 3690ms | 136 / 300 |
| v4 | redis (DECR) | 300 / 300 | 32ms | 0 / 300 |
| v5 | redis (Lua 스크립트) | 300 / 300 | 61ms | 0 / 300 |

#### v1 - 기본 (동시성 제어 없음)
- Race Condition 발생으로 인해, 요청은 모두 성공했지만, 284개의 티켓이 남음

#### v2 - 비관적 락 (Pessimistic Lock)
- 데이터의 일관성을 유지하면서 동시성 문제를 해결
- 성능 문제와 교착 상태 발생 가능성의 문제가 있음

#### v3 - 낙관적 락 (Optimistic Lock)
- 성공 횟수 만큼 티켓이 소모됨 -> 데이터 일관성 유지
- 충돌 발생 시 재시도 요청으로 인해 응답 시간이 지연됨
- 동시에 수정 요청이 많은 경우에는 적합하지 않은 방법

#### v4 - Redis DECR
- Redis의 원자적 연산으로 동시성 문제 해결
- 구현 시 음수 티켓에 대한 처리를 Service 클래스에처 처리 -> 불완전한 방법

#### v5 - Redis Lua 스크립트
- Redis의 원자적 연산으로 동시성 문제 해결
- 음수 티켓에 대한 처리도 redis에서 함으로써 v4의 문제 해결
