# 학습 진도와 세션 인계

이 파일은 실제 학습 상태의 기준이다. 커리큘럼 작성이나 코드 존재만으로 실험을 완료 처리하지 않는다.

## 지금 위치

- 과정: F00·F01·F02 입문 완료 / F03 Thread 입문 시작. MEMORY-03 실험은 심화 H02 내용이지만 개념·제한 실험을 먼저 진행했다. 전체 순서는 [커리큘럼](CURRICULUM.md)을 따른다.
- 현재 주제: [Thread 커리큘럼](../src/main/java/com/shinkwang/backendsystemlab/thread/README.md). THREAD-01 완료 후 THREAD-02 시작 복습 퀴즈에 답했다. 학습자는 sleep의 정상 응답은 3초 이상 걸리지만 CPU 사용은 높지 않을 것이라고 예상했다. ThreadLabController의 GET 매핑·진입 제한·sleep·인터럽트 처리·finally를 실제 소스에서 확인했다. 아직 컴파일·실행하지 않았다. CPU-04 추적은 Thread 입문 뒤 H01에서 다룬다.
- 최근 완료한 세부 실험: MEMORY-03 객체 참조 기초와 제한 실험. Linux lab 프로필 PID 55091에서 보관 배열 1→2→3→4개일 때 Heap `used`는 26346K→28721K→30846K→32971K. 5번째 POST는 HTTP 429, DELETE는 HTTP 200이었고 이후 `used`는 35708K였다. transient API가 포함된 lab 앱 PID 56469는 호출 전 `used` 24476K, transient 호출 뒤 31459K, `jcmd 56469 GC.run` 요청 뒤 17573K를 보였다. 학습자는 회수 대상과 실제 GC 실행을 구분하고 특정 배열의 회수 여부는 전체 Heap 수치만으로 확정할 수 없다고 설명했다. 2026-10-19 transient endpoint 응답 HTTP 200, `Temporary array bytes: 1048576`을 확인함. 자세한 절차와 관측값은 [MEMORY-03 기록](experiments/2026-10-16-MEMORY-03-references.md)에 있음.
- 도움 수준: 안내형. 개념·명령·볼 항목을 먼저 설명한다.
- 확인된 작업: CPU-01·02 관찰, CPU-03 구현·컴파일·프로필 404/200·앱 CPU 약 100·회복·추가 요청 429·전체 us 약 25/id 약 75·실측 요청 3.002366초를 기록. 학습자가 CPU 비율·사용자/커널 모드 관계를 설명했고 필요한 범위를 보완함.
- 학습자가 실제 수행한 실습: 2026-10-15 `uname -s` → `Linux`, `pwd` → `/home/skl`, `ls` → 출력 없음, `ls -a` → `.`, `..`, `.bash_history`, `.bash_logout`, `.bashrc`, `.cache`, `.config`, `.lesshst`, `.profile`, `.ssh`, `.viminfo` (사용자 보고). `skl` 사용자로 생성했다고 설명함. 숨김 항목의 내용은 확인하지 않음. 과거 대화에서 언급된 다른 실습은 완료로 추정하지 않음.
- 확인된 THREAD-01 이해: 학습자는 Acceptor가 새 연결을 받고, Poller가 연결 소켓 이벤트를 감지해 작업을 전달하며, `exec-*`가 큐에서 작업을 받아 처리한다고 설명했다. Java `RUNNABLE`과 Linux `Sl`이 다른 관점인 것도 설명했다. 마무리 퀴즈에서 PID/TID, `Sl`, 각 스레드 역할, JVM과 Linux 상태의 차이, `jcmd`와 TID/nid 대조를 다시 확인했다. `l`은 Linux의 multithreaded 표시이며 `-ww`도 `comm`의 고정 이름 길이 제한을 해제하지 않는다고 보충했다. THREAD-01 완료.
- 아직 확인하지 못한 점: 이번 GC 전후 감소량 중 transient 배열이 차지한 정확한 크기, Heap·Metaspace 내부 동작과 G1 세부 구조, CPU lab 앱·top의 마지막 종료, CPU 부하 중 sy 수치와 장기 정상 범위, Linux TID 66439와 JVM dump의 직접 대응 대상. CPU 기록의 로컬 커밋 fee11dd를 확인했으며 당시 원격 추적 브랜치보다 1커밋 앞섰다. Memory 기록 커밋 `6b175a6`과 진도 순서 정정 커밋 `c8a7106`이 `origin/main`까지 반영된 것을 Mac에서 확인했고, Linux pull은 사용자 보고로 확인함.
- 최근 실험 기록: [MEMORY-03 — 안내 실험 완료](experiments/2026-10-16-MEMORY-03-references.md), [MEMORY-01·02 — 범위 읽기 완료](experiments/2026-10-16-MEMORY-01-os-memory.md), [CPU-03 — 관찰 완료](experiments/2026-10-15-CPU-03-compute.md), [CPU-02](experiments/2026-10-15-CPU-02-baseline.md), [CPU-01](experiments/2026-10-15-CPU-01-process.md).
- THREAD-02 시작 복습: MEMORY-02·THREAD-01의 네 질문에 답함. Heap used/프로세스 메모리, PID/TID, Sl, Tomcat 스레드 역할을 설명했다. VIRT는 주소 자체가 아닌 크기, RES는 현재 RAM 상주량, 명령의 출력 열은 `stat`임을 보충했다. 답과 보충은 [THREAD-02 기록](experiments/2026-10-19-THREAD-02-timed-wait.md)에 구분해 저장했다.
- Git 상태: 기존 THREAD-01·퀴즈 규칙 커밋 `d311d91`은 Mac의 main과 origin/main에 반영된 것을 확인했다. 해당 커밋의 Linux pull은 미확인. 이번 THREAD-02 시작 기록과 진도 갱신은 로컬 저장 상태이며 아직 커밋 전이다.
- 다음 작은 행동: 학습자가 요청한 인터럽트의 요청 주체·표시·sleep의 예외 반응을 보충 설명한다. 해당 내용의 자기 설명·재현은 아직 미확인이다. 질문을 마친 뒤 Mac에서 compileJava와 Git 동기화를 진행한다. Linux 실험 전 코드 커밋의 push·pull과 앱 PID를 확인한다.

## 과정별 상태

상태: 계획 / 진행 중 / 막힘 / 완료 / 보류. 완료에는 연결된 실험 기록과 자기 말로 설명한 내용이 필요하다.

| 과정 | 주제 | 상태 | 실제 기록 |
| --- | --- | --- | --- |
| F00 | 환경·터미널·기동 확인 | 완료 | Linux·프로젝트·Java 21·Gradle 9.7.1·앱 기동 확인 ([환경 E01](ENVIRONMENT.md), [기동 기록](experiments/2026-10-15-CPU-01-process.md)) |
| F01 | CPU 입문 | 완료 | [CPU-01](experiments/2026-10-15-CPU-01-process.md), [CPU-02](experiments/2026-10-15-CPU-02-baseline.md), [CPU-03](experiments/2026-10-15-CPU-03-compute.md). 실행 종료·기록 동기화는 별도 확인 대기 |
| F02 | Memory 입문 | 완료 | [MEMORY-01·02](experiments/2026-10-16-MEMORY-01-os-memory.md). 심화 H02의 [MEMORY-03](experiments/2026-10-16-MEMORY-03-references.md)은 앞서 제한 실험함 |
| F03 | Thread 입문 | 진행 중 | [THREAD-01](experiments/2026-10-19-THREAD-01-process-threads.md) 완료. [THREAD-02](experiments/2026-10-19-THREAD-02-timed-wait.md) API 소스 검토 완료, 인터럽트 기초 보충, 컴파일·실험 전 |
| F04 | Load / Scheduler 입문 | 계획 | 없음 |
| F05 | Disk I/O 입문 | 계획 | 없음 |
| F06 | Network 입문 | 계획 | 없음 |
| H01 | CPU 스레드 추적 | 계획 | 없음 |
| H02 | Memory / GC / Heap OOM | 계획 | MEMORY-03을 앞서 일부 진행함. 정규 순서는 F06·H01 뒤 GC-01부터 재개 |
| H03 | 락 경쟁 / Deadlock | 계획 | 없음 |
| H04 | 외부 API 지연 / Timeout / TCP 종료 | 계획 | 없음 |
| H05 | DB 쿼리 / 인덱스 / 트랜잭션 / MVCC | 계획 | 없음 |
| H06 | HikariCP 고갈 | 계획 | 없음 |
| H07 | 동시성 정합성 / 다중 인스턴스 | 계획 | 없음 |
| H08 | 메시지 중복 / 멱등성 / Outbox | 계획 | 없음 |
| H09 | Retry / 장애 격리 | 계획 | 없음 |
| H10 | 캐시 장애 | 계획 | 없음 |
| H11 | Container / cgroup / OOM | 계획 | 없음 |
| H12 | Kubernetes에서 재실험 | 계획 | 없음 |
| H13 | 관측 체계 통합 | 계획 | 없음 |

## 세션 종료 시 갱신할 항목

```text
날짜:
환경 ID:
오늘 진행한 세부 단계 ID:
실제로 실행하고 관찰한 것 / 기록 링크:
학습자가 자기 말로 설명한 것:
틀렸던 예상과 수정한 이해:
아직 모르는 점 / 막힌 지점:
도움 수준: 안내형 / 힌트형 / 독립형
다음에 할 작은 행동 하나:
```

다음 세션은 마지막 행동부터 이어간다. 최근 이해 수준을 확인할 필요가 있으면 지난 출력의 항목 하나를 다시 읽어보는 정도로 시작하고, 이미 끝낸 전체 과정을 반복하지 않는다.
