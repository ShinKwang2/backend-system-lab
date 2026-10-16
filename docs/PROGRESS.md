# 학습 진도와 세션 인계

이 파일은 실제 학습 상태의 기준이다. 커리큘럼 작성이나 코드 존재만으로 실험을 완료 처리하지 않는다.

## 지금 위치

- 과정: F00·F01 완료 / F02 Memory 입문 진행 중. CPU 실험 종료와 기록 원격 동기화 상태는 별도 확인 항목으로 유지한다.
- 현재 주제: [Memory 커리큘럼](../src/main/java/com/shinkwang/backendsystemlab/memory/README.md). MEMORY-01 전체 메모리와 MEMORY-02 OS·프로세스·Heap 범위 구분 완료. CPU-04 추적은 Thread 입문 뒤 H01에서 진행한다.
- 현재 세부 단계: MEMORY-03 객체 참조 기초 완료, 제한된 실습 컨트롤러 구현 중. 사용자는 `a = null` 뒤 `b`로 접근 가능하고, `b = null` 뒤 다른 참조가 없으면 GC 대상이 되며 실제 회수는 JVM이 맡는다고 설명함. 이어 `saved` → ArrayList → 내부 배열 요소 → 객체 참조 경로를 설명했고, `clear()`는 목록을 남겨도 요소 참조를 제거한다고 설명함. 최근에는 메서드가 끝난 뒤 지역 컬렉션과 그 요소는 다른 참조가 없다면 회수 가능해지고, 필드 컬렉션은 그것을 가진 객체가 살아 있을 때 요소를 계속 보관할 수 있다고 설명함. 참조를 제거해도 Heap `used`가 즉시 감소한다고 보장할 수 없다는 점도 맞게 설명함. 사용자가 lab 전용 `MemoryLabController`에 최대 4개의 1 MiB 배열을 보관하고 비우는 동기화 메서드를 작성함. 미사용 import는 정리됨. 아직 컴파일·API 실행·Heap 관측 없음.
- 도움 수준: 안내형. 개념·명령·볼 항목을 먼저 설명한다.
- 확인된 작업: CPU-01·02 관찰, CPU-03 구현·컴파일·프로필 404/200·앱 CPU 약 100·회복·추가 요청 429·전체 us 약 25/id 약 75·실측 요청 3.002366초를 기록. 학습자가 CPU 비율·사용자/커널 모드 관계를 설명했고 필요한 범위를 보완함.
- 학습자가 실제 수행한 실습: 2026-10-15 `uname -s` → `Linux`, `pwd` → `/home/skl`, `ls` → 출력 없음, `ls -a` → `.`, `..`, `.bash_history`, `.bash_logout`, `.bashrc`, `.cache`, `.config`, `.lesshst`, `.profile`, `.ssh`, `.viminfo` (사용자 보고). `skl` 사용자로 생성했다고 설명함. 숨김 항목의 내용은 확인하지 않음. 과거 대화에서 언급된 다른 실습은 완료로 추정하지 않음.
- 아직 확인하지 못한 점: 제한된 할당/보관 비교에서 Heap이 어떻게 변하는지와 GC 실제 시점, Heap·Metaspace 내부 동작과 G1 세부 구조, CPU lab 앱·top의 마지막 종료, CPU 부하 중 sy 수치와 장기 정상 범위. CPU 기록의 로컬 커밋 fee11dd를 확인했으며 당시 원격 추적 브랜치보다 1커밋 앞섰다. 푸시·Linux 최신 동기화는 미확인이다. Memory 기록은 이후 변경이다.
- 최근 실험 기록: [MEMORY-03 — 개념 안내 진행 중](experiments/2026-10-16-MEMORY-03-references.md), [MEMORY-01·02 — 범위 읽기 완료](experiments/2026-10-16-MEMORY-01-os-memory.md), [CPU-03 — 관찰 완료](experiments/2026-10-15-CPU-03-compute.md), [CPU-02](experiments/2026-10-15-CPU-02-baseline.md), [CPU-01](experiments/2026-10-15-CPU-01-process.md).
- 다음 작은 행동: 전체 실험 코드를 마친 뒤 Linux VM에서 컴파일하고, 실험 프로필에서 보관·비우기 API를 제한된 횟수로 확인한다. 그 다음 transient allocation을 추가하고 Heap을 관측한다. 아직 API는 실행하지 않았다.

## 과정별 상태

상태: 계획 / 진행 중 / 막힘 / 완료 / 보류. 완료에는 연결된 실험 기록과 자기 말로 설명한 내용이 필요하다.

| 과정 | 주제 | 상태 | 실제 기록 |
| --- | --- | --- | --- |
| F00 | 환경·터미널·기동 확인 | 완료 | Linux·프로젝트·Java 21·Gradle 9.7.1·앱 기동 확인 ([환경 E01](ENVIRONMENT.md), [기동 기록](experiments/2026-10-15-CPU-01-process.md)) |
| F01 | CPU 입문 | 완료 | [CPU-01](experiments/2026-10-15-CPU-01-process.md), [CPU-02](experiments/2026-10-15-CPU-02-baseline.md), [CPU-03](experiments/2026-10-15-CPU-03-compute.md). 실행 종료·기록 동기화는 별도 확인 대기 |
| F02 | Memory 입문 | 진행 중 | [MEMORY-01·02 완료](experiments/2026-10-16-MEMORY-01-os-memory.md), [MEMORY-03 개념 안내 진행 중](experiments/2026-10-16-MEMORY-03-references.md) |
| F03 | Thread 입문 | 계획 | 없음 |
| F04 | Load / Scheduler 입문 | 계획 | 없음 |
| F05 | Disk I/O 입문 | 계획 | 없음 |
| F06 | Network 입문 | 계획 | 없음 |
| H01 | CPU 스레드 추적 | 계획 | 없음 |
| H02 | Memory / GC / Heap OOM | 계획 | 없음 |
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
