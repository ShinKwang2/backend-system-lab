# Backend Systems Lab

Java/Spring으로 만든 기능이 JVM과 Linux 위에서 어떻게 실행되는지, 장애가 생기면 무엇을 관찰하고 어떻게 원인을 좁히는지 배우는 실험실이다.

목표는 코드를 빨리 작성하는 것을 넘어 문제를 정의하고, 시스템을 이해하고, AI가 만든 결과를 검증하고, 근거를 가지고 장애를 분석하는 백엔드 엔지니어로 성장하는 것이다. OS·CS 기초가 약한 상태를 출발점으로 삼는다.

## 학습 방식

Spring Boot에서 작은 현상을 만들고 필요한 아래 계층으로 내려간다.

```text
HTTP 요청 → Spring MVC / Tomcat → Java Thread → JVM
                                            ↓
                             Linux → CPU / Memory / Disk / TCP
                                            ↓
                               DB / 외부 서비스 / 메시지 처리
```

처음에는 튜터가 개념, 명령어, 출력에서 볼 항목을 설명한다. 학습자는 직접 실행하고 값을 읽는다. 익숙해지면 가설 후보 선택, 검증 설계, 관찰 도구 선택까지 스스로 하는 범위를 넓힌다.

매 실험은 다음 순서로 진행한다.

1. 오늘의 질문과 필요한 기초를 배운다.
2. 도구 사용법과 출력 읽는 예시를 익힌다.
3. 정상 상태를 직접 관찰한다.
4. 조건을 바꾸면 어떻게 될지 예상한다.
5. 제한된 실험을 실행하고 변화와 시각을 기록한다.
6. 관찰 사실과 가설을 구분하고 추가 관찰로 검증한다.
7. 필요한 해결을 적용하고 같은 조건으로 재검증한다.
8. 배운 내용과 남은 질문을 자기 말로 남긴다.

첫 실험부터 원인을 혼자 맞힐 필요는 없다. 안내 실험에서 원리를 배운 뒤 진단 실험으로 적용한다. 이해에 시간이 걸리면 같은 주제를 반복한다.

## 문서 지도

| 파일 | 역할 |
| --- | --- |
| [AGENTS.md](AGENTS.md) | AI 튜터의 수업·코드·기록 원칙 |
| [docs/START-HERE.md](docs/START-HERE.md) | 첫 수업과 컴퓨터 변경 시 환경 확인 |
| [docs/CURRICULUM.md](docs/CURRICULUM.md) | 전체 순서, 선수 지식, 주제별 안내 링크 |
| [docs/PROGRESS.md](docs/PROGRESS.md) | 현재 위치와 다음에 할 작은 행동 |
| [docs/ENVIRONMENT.md](docs/ENVIRONMENT.md) | 실행 환경의 확인된 사실과 미확인 항목 |
| [docs/EXPERIMENT-TEMPLATE.md](docs/EXPERIMENT-TEMPLATE.md) | 예상·관찰·가설·검증 기록 양식 |
| [docs/experiments/README.md](docs/experiments/README.md) | 실제 실험 기록의 저장 규칙 |

## 주제별 위치

```text
src/main/java/com/shinkwang/backendsystemlab/
├── BackendSystemLabApplication.java
├── cpu/README.md           # 프로세스, CPU, 스레드 CPU 추적
├── memory/README.md        # OS/JVM 메모리, 객체 보관
├── thread/README.md        # 스레드와 덤프, 대기 상태
├── scheduler/README.md     # 실행 대기, load, 문맥 전환
├── disk/README.md          # 용량, I/O, page cache, fsync
├── network/README.md       # 소켓, TCP, 외부 API, timeout
├── gc/README.md            # 할당, GC, Heap OOM
├── database/README.md      # 쿼리, 인덱스, 트랜잭션, MVCC
├── pool/README.md          # HikariCP와 커넥션 대기
├── concurrency/README.md   # 락, deadlock, 쿠폰 정합성
├── messaging/README.md     # 중복 처리, 멱등성, Outbox
├── resilience/README.md    # retry, backoff, 장애 격리
├── cache/README.md         # 캐시 장애와 fallback
├── container/README.md     # cgroup, 자원 제한, OOM
├── kubernetes/README.md    # 기존 장애를 Pod에서 재현
└── observability/README.md # Actuator부터 지표·로그·추적까지
```

각 디렉터리는 학습 안내와 해당 실험 코드를 가까이 두기 위한 위치다. CPU 주제에는 `lab` 프로필 전용 계산 API를 작성했고, 다른 주제는 커리큘럼 단계다. Markdown은 Java 컴파일 대상이 아니다. 실험 기록은 코드 디렉터리 대신 `docs/experiments/`에 모은다.

## 현재 프로젝트와 실행 환경

- 빌드 설정: Java 21 toolchain, Spring Boot 4.1.1, Gradle Wrapper.
- 선언된 의존성: Web MVC, Actuator, 관련 테스트 스타터.
- 현재 코드: Spring Boot 진입점, 기본 테스트, `lab` 프로필 전용 `GET /lab/cpu`. 계산은 약 3초로 고정하고, 같은 JVM의 동시 계산을 1개로 제한하며 추가 요청은 HTTP 429로 거절하도록 작성했다. DB·Redis·Kafka는 아직 없다.
- 코드 작성은 Mac에서, 관찰 실험은 별도 학습용 Linux VM에서 진행한다. CPU-01~03에서 프로세스 식별, 정상 CPU, 프로필별 API 등록, 제한된 계산 부하와 회복, 추가 요청 429, 요청 시간 3.002366초를 관찰했다. 명령과 실행 순서는 [실험 기록](docs/experiments/README.md)에 남긴다.
- macOS의 명령과 Linux 명령은 출력·옵션이 다를 수 있다. `free`, `ss` 등의 Linux 실습을 macOS에서 그대로 진행하지 않는다.

첫 실행은 [환경 확인 안내](docs/START-HERE.md)를 따른다. 빌드 설정이 있다는 사실은 해당 컴퓨터에서 빌드나 실행이 검증됐다는 뜻이 아니다.

## 새로운 컴퓨터·세션에서 이어가기

Git으로 이 문서와 실험 기록을 함께 옮긴다. 새 세션에서는 다음과 같이 요청하면 된다.

> AGENTS.md, README.md, docs/PROGRESS.md, docs/ENVIRONMENT.md와 현재 주제 README를 읽고 이어가자. 먼저 현재 위치를 짧게 확인하고 다음 작은 단계 하나만 안내해줘. 처음 쓰는 명령어는 실행 방법과 볼 항목부터 설명해줘.

컴퓨터가 바뀌면 PID, 경로, 자원량, 도구 버전과 실행 상태를 다시 확인한다. 이전 실험 결과는 보존하되 현재 환경의 사실로 복사하지 않는다. 세션 종료 전에는 실제로 진행한 내용과 다음 행동을 진도 문서에 남긴다.

## 확장 방향

DB, Redis, Kafka, 컨테이너, Kubernetes, 관측 스택은 필요한 실험에서 순차적으로 추가한다. Actuator는 이미 의존성이 있으므로 필요한 지표부터 사용한다. 처음부터 전체 인프라를 설치하지 않는다.

장기적으로는 배운 원리를 이메일 도메인의 TCP 연결, EML 저장, 전달 큐, 재시도, 중복 처리, worker 복구에 적용한다. 이후 LLM API의 timeout·retry, RAG, 검색, 평가와 운영으로 확장할 수 있다. 두 확장은 당장 완료해야 할 필수 과정이 아니다.
