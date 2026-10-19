# THREAD-02 — 시간을 기다리는 스레드

상태: 진행 중 (API 소스 검토 완료, 인터럽트 기초 보충 중, 컴파일·실험 전)

## 1. 질문과 실험 조건

- 날짜 / 환경 ID: 2026-10-19 KST / E01. 이번 단계의 Linux 앱 실행 상태와 PID는 아직 재확인하지 않음.
- 오늘 알아볼 질문 하나: 응답까지 같은 시간이 걸려도 계산 중인 스레드와 시간을 기다리는 스레드는 CPU 사용과 덤프 상태가 같을까?
- 코드 커밋 / 변경 사항: `d311d91`의 로컬 main과 origin/main 일치를 Mac에서 확인. ThreadLabController에서 lab 프로필, GET 매핑, compareAndSet 진입 제한, Thread.sleep(3000L), 인터럽트 표시 복원과 HTTP 503 예외, finally의 running 초기화를 소스로 확인함. 정상 응답 문자열은 현재 `Sleep lab ready`다. 컴파일·실행 확인 전이며 Linux pull 여부는 미확인.
- 실제 버전·자원·JVM 옵션: 기존 E01 기록 참고. 이번 실행 조건은 아직 미확인.
- 요청 수 / 동시성 / 실행 시간 / 입력 크기: 실제 요청 없음. 계획은 `lab` 프로필 전용 `GET /lab/thread/sleep`, sleep 인자 3000밀리초 고정, 동일 JVM 내 동시 sleep 요청 1개, 추가 요청 HTTP 429다. 시간 인자를 외부에서 받지 않는다. Linux 앱 실행 터미널의 Ctrl+C로 실험 앱을 종료할 수 있도록 안내하며, 아직 구현·실행 전이다.

## 2. 필요한 개념과 시작 복습 퀴즈

직전 두 세부 챕터 MEMORY-02와 THREAD-01에서 배운 내용을 질문했다. 아래 보충 사항은 튜터 설명이며 학습자의 최초 답과 구분한다.

| 질문 | 학습자 답 요약 | 보충할 점 |
| --- | --- | --- |
| VIRT, RES와 JVM Heap의 관계는? | VIRT는 가상 메모리 주소, RES는 메모리 사용량이며 Metaspace 등도 포함해 Heap보다 넓은 범위를 본다고 설명. | VIRT 값은 주소 그 자체가 아니라 가상 주소 공간의 크기다. RES는 현재 RAM에 올라온 프로세스 페이지의 크기다. Heap used와 측정 기준이 달라 RES가 Heap used보다 항상 큰 수라고 단정하지 않는다. |
| Heap used와 RES의 차이는? | Heap used는 JVM Heap에서 사용 중인 메모리, RES는 Linux에서 보는 프로세스 메모리라고 설명. | 큰 범위 구분은 맞음. RES는 특히 현재 RAM에 상주하는 부분을 측정한다는 점을 보충한다. |
| 같은 PID와 여러 TID, Sl의 뜻은? | 프로세스에 여러 스레드가 있으므로 PID는 같고 TID는 다르며 S는 잠든 대기 상태, l은 여러 스레드를 가진 표시라고 설명. | 맞음. 명령의 출력 열 이름은 `stats`가 아니라 `stat`이며 화면의 헤더는 `STAT`이다. |
| Acceptor, Poller, exec 역할과 Java/Linux 상태 차이는? | Acceptor는 연결, Poller는 연결 소켓 이벤트를 기다리고 작업을 넘기며 exec은 큐의 작업을 실행한다고 설명. JVM RUNNABLE과 Linux Sl이 함께 나올 수 있다고 설명. | 역할과 관점 구분은 맞음. 이번 Poller/Acceptor는 native 호출 안에서 OS 이벤트를 기다릴 수 있다. Java RUNNABLE을 단순히 “대기하면서 실행할 수 있음”으로 정의하지 않고 상태와 스택을 함께 읽는다. |

다음 설명: `Thread.sleep(3000)`은 현재 스레드를 3000밀리초 동안 재우는 호출이다. 실제 재개 시각은 타이머 정밀도와 스케줄링 등에 영향을 받으며 인터럽트로 중단될 수도 있다. 메서드 소스는 확인했고 실행 결과는 아직 없다. [Java 21 Thread.sleep](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Thread.html#sleep(long))

### 곁가지 질문 — 인터럽트는 누가 요청하는가?

학습자가 인터럽트 요청 주체와 방식에 대한 설명을 요청했다. 아래는 튜터의 보충 설명이며, 실제 인터럽트 재현이나 학습자의 자기 설명은 아직 확인하지 않았다.

- 일반적으로 대상 Thread 객체를 가진 다른 Java 코드가 `targetThread.interrupt()`를 호출한다. 작업 취소를 관리하는 코드가 대표적인 예다. `Future.cancel(true)`도 실행 중인 작업의 스레드에 인터럽트를 요청할 수 있다.
- 인터럽트는 강제 종료가 아니라 협력적인 중단 요청이다. 계산 루프는 표시를 확인하고 종료하도록 작성해야 하며, sleep처럼 인터럽트에 반응하는 대기는 InterruptedException을 던져 빠져나온다.
- sleep이 InterruptedException을 던질 때 인터럽트 상태는 지워진다. catch의 `Thread.currentThread().interrupt()`는 현재 요청을 처리하는 스레드에 그 표시를 다시 남긴다. 이어 HTTP 503 예외를 던지고 finally에서 running을 해제한다. catch가 스레드 자체를 강제로 종료시키는 것은 아니다.
- 현재 실험 컨트롤러에는 다른 스레드에 인터럽트를 보내는 코드가 없다. curl 타임아웃이나 클라이언트 연결 종료만으로 서버의 sleep이 자동으로 인터럽트된다고 가정하지 않는다.

근거: [Java 21 Thread.interrupt](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Thread.html#interrupt()), [Future.cancel](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/Future.html#cancel(boolean)).

## 3. 예상

- 학습자 예상: sleep으로 기다리면 정상 응답까지 3초 이상 걸리겠지만 CPU 사용량은 높지 않을 것이라고 설명했다. 이 문장은 예상이며 아직 측정 결과는 아니다.

## 4. 실행 순서와 재현 절차

이번 단계에서 Linux 명령이나 HTTP 요청은 아직 실행하지 않았다. 앱 실행 상태와 PID를 확인하고, 실험 코드 작성·Git 동기화 후 절차를 구체화한다.

첫 코드 작성 확인: Mac 편집기에서 학습자가 `src/main/java/com/shinkwang/backendsystemlab/thread/ThreadLabController.java`를 만들었다. 실제 파일의 패키지, `@Profile("lab")`, `@RestController`, `AtomicBoolean running = new AtomicBoolean(false)` 필드를 확인했다. 아직 컴파일·실행하지 않았다.

중간 코드에서는 GetMapping 누락, 빈 catch, finally 앞 여분 중괄호를 확인하고 수정 방향을 안내했다. 이후 학습자가 수정한 실제 파일에서 GetMapping 경로, interrupt 표시 복원·HTTP 503 예외, 올바른 try/catch/finally 구조를 확인했다. 정적 소스 검토를 마쳤으며 컴파일·실행 결과는 아직 없다.

## 5. 정상 상태와 변화 관찰

미실행. 이전 THREAD-01 출력은 당시의 기록이며 이번 실행의 정상 상태로 대체하지 않는다.

## 6. 관찰 사실과 가설

- 확인된 사실: 시작 복습 퀴즈에 학습자가 위와 같이 답함. Mac의 main과 origin/main은 `d311d91`을 가리킴.
- 아직 판단할 수 없는 것: sleep API의 실제 CPU와 스레드 덤프 상태. 현재 실행 결과는 없음.

## 7. 검증

미실행. 정상 상태와 요청 진행 중 CPU·덤프를 학습자가 직접 비교할 계획이다.

## 8. 원인과 해결·재검증

해당 없음: API 소스 검토 단계이며 실험 전이다.

## 9. 배운 내용

- 확인된 복습: 프로세스와 스레드 ID, Sl, Acceptor/Poller/worker 역할, JVM과 OS 상태의 관점 차이.
- 보충 설명: VIRT는 가상 주소 공간의 크기, RES는 현재 RAM 상주량, 명령의 출력 열 이름은 stat이다.
- 다음 작은 행동: 인터럽트 보충 설명의 질문을 마친 뒤 Mac에서 compileJava를 수행한다. 컴파일·Git 동기화·Linux 실험은 아직 수행하지 않았다.
- 저장 상태: 이 기록은 로컬에 작성했으며 아직 커밋·푸시하지 않음. 챕터 마무리 퀴즈는 실험 후 진행한다.
