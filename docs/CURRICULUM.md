# 전체 커리큘럼

## 목표

Spring Boot의 요청 하나를 Application → JVM → Linux → CPU·Memory·Disk·Network → DB·외부 서비스까지 연결해서 이해한다. 느리거나 잘못된 결과가 나왔을 때 증거로 가설을 좁히고, 해결 후 같은 조건으로 검증하는 능력을 기른다.

CS 과목을 먼저 모두 완독하는 과정은 아니다. 필요한 기초를 설명받고 작은 현상을 관찰한 뒤, 그 현상을 이해하는 데 필요한 이론을 더 깊게 배운다. 실습과 직접 관련되지 않는 이론을 첫 수업에 모두 넣지 않는다.

## 두 번의 깊이로 진행

첫 6개 주제는 CPU → Memory → Thread → Load/Scheduler → Disk I/O → Network 순서로 **관찰 입문**을 한다. 여기서 각 분야를 끝내는 것이 아니다. 이후 익힌 도구로 같은 주제의 **심화 장애 분석**을 진행한다.

F는 기초 과정, H는 심화 과정이다. 세부 단계는 CPU-01처럼 주제별 ID를 사용한다. 예전 대화의 Lab 번호와 억지로 일대일 대응시키지 않고, 이 문서의 ID를 진도 기준으로 사용한다.

| 순서 | 과정 | 실제 세부 단계 | 이번 과정의 결과 |
| --- | --- | --- | --- |
| 0 | F00 환경 확인 | [START-HERE](START-HERE.md) | 실행 머신·JDK·기동 상태를 확인하고 기록 |
| 1 | F01 CPU 입문 | [CPU-01~03](../src/main/java/com/shinkwang/backendsystemlab/cpu/README.md) | 앱 PID 찾기, CPU 값 읽기, 제한된 계산 부하 비교 |
| 2 | F02 Memory 입문 | [MEMORY-01~02](../src/main/java/com/shinkwang/backendsystemlab/memory/README.md) | 전체 메모리·프로세스 RSS·Heap의 범위 구분 |
| 3 | F03 Thread 입문 | [THREAD-01~03](../src/main/java/com/shinkwang/backendsystemlab/thread/README.md) | 실행·대기와 스택을 덤프에서 읽기 |
| 4 | F04 Load / Scheduler | [SCHEDULER-01~03](../src/main/java/com/shinkwang/backendsystemlab/scheduler/README.md) | CPU·실행 대기·load·context switch 비교 |
| 5 | F05 Disk I/O | [DISK-01~03](../src/main/java/com/shinkwang/backendsystemlab/disk/README.md) | 용량과 I/O 구분, 버퍼·page cache·동기화 연결 |
| 6 | F06 Network 입문 | [NETWORK-01~02](../src/main/java/com/shinkwang/backendsystemlab/network/README.md) | HTTP 결과와 TCP 연결·포트의 구분 |
| 7 | H01 CPU 추적 | [CPU-04](../src/main/java/com/shinkwang/backendsystemlab/cpu/README.md) | Linux TID → JVM 스레드 → 코드 추적 |
| 8 | H02 Memory / GC | [MEMORY-03~04](../src/main/java/com/shinkwang/backendsystemlab/memory/README.md), [GC-01~04](../src/main/java/com/shinkwang/backendsystemlab/gc/README.md) | 할당·보관·회수·지연과 Heap OOM 분석 |
| 9 | H03 락과 Deadlock | [CONCURRENCY-01~02](../src/main/java/com/shinkwang/backendsystemlab/concurrency/README.md) | 락 소유·대기·순환 관계와 해결 검증 |
| 10 | H04 Network 심화 | [NETWORK-03~05](../src/main/java/com/shinkwang/backendsystemlab/network/README.md) | 외부 API 지연·timeout·TCP 종료 분석 |
| 11 | H05 Database | [DATABASE-01~04](../src/main/java/com/shinkwang/backendsystemlab/database/README.md) | 쿼리·인덱스·락·격리 수준·MVCC 실험 |
| 12 | H06 HikariCP | [POOL-01~03](../src/main/java/com/shinkwang/backendsystemlab/pool/README.md) | 연결 획득 대기와 연결 보유 원인의 구분 |
| 13 | H07 동시성 정합성 | [CONCURRENCY-03~05](../src/main/java/com/shinkwang/backendsystemlab/concurrency/README.md) | 쿠폰 문제, 해결책 비교, 두 인스턴스 검증 |
| 14 | H08 Messaging | [MESSAGING-01~03](../src/main/java/com/shinkwang/backendsystemlab/messaging/README.md) | 재처리·멱등성·Dual Write·Outbox 보장 범위 |
| 15 | H09 Resilience | [RESILIENCE-01~03](../src/main/java/com/shinkwang/backendsystemlab/resilience/README.md) | retry 증폭, backoff·jitter, 장애 격리 |
| 16 | H10 Cache | [CACHE-01~03](../src/main/java/com/shinkwang/backendsystemlab/cache/README.md) | Redis 실패, fallback 부하, 동시 만료 분석 |
| 17 | H11 Container | [CONTAINER-01~03](../src/main/java/com/shinkwang/backendsystemlab/container/README.md) | cgroup·throttling·Heap OOM과 cgroup OOM 구분 |
| 18 | H12 Kubernetes | [KUBERNETES-01~03](../src/main/java/com/shinkwang/backendsystemlab/kubernetes/README.md) | 이전 장애와 Pod 자원·상태·재시작 연결 |
| 19 | H13 Observability | [OBSERVABILITY-01~03](../src/main/java/com/shinkwang/backendsystemlab/observability/README.md) | 지표·로그·trace와 직접 관찰로 통합 진단 |

H02 권장 순서: MEMORY-03 → GC-01 → MEMORY-04 → GC-02~04. 객체 보관과 회수를 먼저 익힌 뒤 누수 가설과 GC 압박을 분석한다. SCHEDULER-03의 I/O 대기 비교는 F05 이후 다시 돌아와 보완한다.

## 각 주제의 세부 안내

주제별 README에는 시작 조건, 쉬운 첫 설명, 도구 사용법, 각 단계의 배울 것·실습·질문·완료 기준, 해석할 때 주의할 경계를 둔다. 디렉터리마다 전체 수업을 한 번에 진행하지 않고 현재 과정에 해당하는 단계만 읽는다.

| 위치 | 상세 주제 |
| --- | --- |
| [cpu](../src/main/java/com/shinkwang/backendsystemlab/cpu/README.md) | Process, PID/PPID, Core, user/system CPU, thread CPU tracking |
| [memory](../src/main/java/com/shinkwang/backendsystemlab/memory/README.md) | free/available, virtual memory/RSS, Heap/Stack/Metaspace/Native, 객체 보관 |
| [thread](../src/main/java/com/shinkwang/backendsystemlab/thread/README.md) | Linux/Java 스레드, Java 상태, stack trace, 반복 덤프 |
| [scheduler](../src/main/java/com/shinkwang/backendsystemlab/scheduler/README.md) | runnable, run queue, load average, context switch |
| [disk](../src/main/java/com/shinkwang/backendsystemlab/disk/README.md) | filesystem, df/du, 장치 I/O, page cache, flush/fsync |
| [network](../src/main/java/com/shinkwang/backendsystemlab/network/README.md) | socket, IP/port, TCP, 지연, timeout, TIME_WAIT/CLOSE_WAIT |
| [gc](../src/main/java/com/shinkwang/backendsystemlab/gc/README.md) | 할당·도달 가능성, 실제 수집기, GC 로그, pause, Heap OOM |
| [database](../src/main/java/com/shinkwang/backendsystemlab/database/README.md) | SQL, index/B-tree, buffer/page, EXPLAIN, transaction/lock, isolation/MVCC |
| [pool](../src/main/java/com/shinkwang/backendsystemlab/pool/README.md) | HikariCP, acquire wait, active/pending, 연결 보유 원인 |
| [concurrency](../src/main/java/com/shinkwang/backendsystemlab/concurrency/README.md) | critical section/monitor, deadlock, race condition, atomicity, DB lock, 다중 앱 |
| [messaging](../src/main/java/com/shinkwang/backendsystemlab/messaging/README.md) | 전달 보장, offset commit, idempotency, deduplication, Outbox, 선택 CDC |
| [resilience](../src/main/java/com/shinkwang/backendsystemlab/resilience/README.md) | timeout/retry budget, backoff/jitter, circuit breaker, bulkhead |
| [cache](../src/main/java/com/shinkwang/backendsystemlab/cache/README.md) | cache aside, TTL, Redis 실패, local cache, stampede |
| [container](../src/main/java/com/shinkwang/backendsystemlab/container/README.md) | 자원 범위, cgroup CPU/memory, throttling, OOM 증거 |
| [kubernetes](../src/main/java/com/shinkwang/backendsystemlab/kubernetes/README.md) | requests/limits, probes, restart, Service, worker 복구, 선택 HPA |
| [observability](../src/main/java/com/shinkwang/backendsystemlab/observability/README.md) | Actuator/Micrometer, Prometheus/Grafana, 로그/trace, 가설 트리 |

## 도움을 줄이는 방법

| 단계 | 튜터의 안내 | 학습자의 연습 |
| --- | --- | --- |
| 안내형 | 기초·도구·항목·읽는 예시, 필요 시 가설 후보 | 직접 실행, 값 읽기, 자기 말로 설명 |
| 힌트형 | 질문과 작은 힌트, 부족한 기초 보충 | 변화 비교, 가설 선택·작성, 검증 후보 제안 |
| 독립형 | 가설 리뷰와 반례, 필요할 때만 안내 | 도구 선택, 검증 설계, 해결·재검증 |

모르는 명령을 숨기거나 추측하게 하지 않는다. 독립형에서도 새로운 도구는 설명한다. 진단 실험으로 넘어가도 막히면 필요한 설명 수준으로 돌아간다. 도움 수준은 진도에 함께 기록한다.

## 공통 실험 조건

- 먼저 정상 상태를 기록한다. 작업 시간·동시성·데이터량 중 한 조건씩 바꾼다.
- 최초 부하는 동시성 1과 수 초의 제한된 실행부터 시작한다. CPU·요청 부하의 첫 실행은 보통 5초 이내로 설계하고, 더 긴 관찰은 자원과 중단 방법을 확인한 뒤 조정한다. timeout 실험 등 목적에 따라 상한을 명시적으로 변경한다.
- 메모리·디스크는 절대량 상한을 실험 환경의 여유와 함께 정한다. OOM은 별도 Heap/컨테이너 한계로 만들고 호스트 자원 고갈을 목표로 하지 않는다.
- 실험 코드를 만들 때 전용 프로필, 입력 상한, 종료 시간, 정리 방법을 먼저 구현한다. 취소된 HTTP 요청만으로 서버의 계산이 중단된다고 가정하지 않는다.
- 관측값에는 단위·측정 대상·시각·구간을 남긴다. 예상 숫자와 실제 측정값을 섞지 않는다.
- 해결은 동일 조건으로 재검증한다. 정확성 실험은 성공 응답뿐 아니라 최종 데이터도 확인한다.

이 조건은 향후 구현의 요구사항이며, 현재 앱에 실험 제한 기능이 이미 구현됐다는 뜻은 아니다.

## 기술을 추가하는 시점

| 필요가 생긴 단계 | 추가할 것 |
| --- | --- |
| 기본 입문 | 기존 Spring Boot, Linux CLI, 필요한 JDK 도구 |
| F06~H04 | 두 번째 작은 Spring 앱, 실제 사용할 HTTP 클라이언트 |
| H05~H06 | PostgreSQL, 최소 JDBC, HikariCP와 필요한 Actuator 지표 |
| H08 | Kafka와 작은 worker; DB 업무는 최소 크기로 유지 |
| H09 | 기본 실험 후 필요한 resilience 라이브러리 |
| H10 | Redis; 필요할 때 Caffeine 비교 |
| H11~H12 | 컨테이너 런타임, 이후 격리 Kubernetes 환경 |
| H13 | Micrometer 연계, Prometheus/Grafana, 필요한 trace 도구 |

Actuator는 이미 의존성으로 선언돼 있다. 관측 능력을 마지막까지 미루는 것이 아니라 필요한 지표는 중간에 사용하고 큰 스택을 뒤에 통합한다. 특정 HTTP 클라이언트·DB·브로커·Kubernetes 버전은 도입 시 공식 문서와 실제 설정으로 확정한다.

## 이론을 보완하는 위치

- OS: 프로세스·스레드, 스케줄링, 가상 메모리·page, 파일시스템·I/O를 해당 Lab에서 연결한다.
- JVM: 메모리 영역·GC·스레드 상태·monitor를 관찰 도구와 연결한다.
- 네트워크: TCP 연결·종료와 timeout을 먼저 배우고, DNS·TLS는 실제 문제에 필요할 때 별도 보완한다.
- DB: 인덱스·실행 계획·트랜잭션·격리·MVCC를 실제 두 세션과 요청으로 확인한다.
- 분산 시스템: 실패 지점·재시도·중복·일관성 보장을 실제 저장 결과로 확인한다.

이 커리큘럼만으로 CS 전체를 다뤘다고 표시하지 않는다. 자료구조·알고리즘, Java Memory Model, 네트워크 세부 이론처럼 부족한 선수 지식이 드러나면 관련된 작은 보충 수업을 진행하고 진도에 남긴다.

## 기록과 완료 기준

단계 완료는 명령 암기가 아니라 다음 기준으로 판단한다.

1. 무엇을 확인하려고 어떤 도구를 썼는지 설명할 수 있다.
2. 실제 출력에서 필요한 값을 찾고 범위·단위를 말할 수 있다.
3. 관찰 사실과 자신의 해석·가설을 구분할 수 있다.
4. 장애 분석 단계에서는 추가 검증과 변경 전후 비교의 근거가 있다.
5. 직접 확인한 내용과 남은 의문을 기록했다.

실제 기록은 [실험 템플릿](EXPERIMENT-TEMPLATE.md)으로 `docs/experiments/`에 남기고 [PROGRESS.md](PROGRESS.md)에 연결한다. 입문 기록은 짧게 남겨도 되며 모든 실습을 장문의 사고 보고서로 만들 필요는 없다.

## 장기 확장과 학습 속도

약 6개월은 이전 대화의 목표 리듬이며 기한이나 실력 보장이 아니다. 현재 OS·CS 이해 수준과 학습 시간에 맞춰 진도를 조정하고, 어려운 주제는 반복한다.

이메일 도메인 적용은 기존 주제를 익힌 뒤 연결한다: TCP → SMTP 연결, Disk → EML 저장, Messaging → 전달 큐와 중복 처리, Resilience → 일시 실패 재시도, Observability → Message-ID 기반 추적. 별도의 SMTP 엔진을 지금 구현하지 않는다.

AI 서비스 백엔드는 Network·Resilience·DB·Observability를 익힌 뒤 LLM API, timeout/retry, RAG·검색, 평가로 확장할 수 있다. 현재는 AI를 기초 설명, 가설 검토, 반례 제시, 실험 설계와 문서 정리에 사용한다.
