# SCHEDULER-02 — 일하는 흐름 수를 늘리기

상태: 진행 중 (2026-11-20 Mac 컴파일·푸시, Linux PID 4571·lab 프로필 사용자 확인). 부하 전 CPU 기준값 확인, worker 1개 HTTP 200·3.071397초·us 25% 실제 확인, worker 2개 HTTP 200·3.004848초·us 50%·r 2 실제 확인, worker 4개 HTTP 200·3.004689초·r 4·us 100%·id 0~1% 실제 확인. worker 8개 HTTP 200·3.011410초·r 8·us 99~100%·worker당 반복량 감소 실제 확인. cs 설명, 입력 상한·중복 실행 제한·재진입 검증을 진행했다. 마무리 퀴즈 답변과 보충을 확인했다.

## 1. 질문과 실험 조건

- 날짜 / 환경 ID: 2026-11-19 / E01.
- 오늘의 질문: 동시에 계산하려는 작업을 늘리면 CPU 상태와 응답 시간은 어떻게 달라질까?
- 코드 커밋: 시작 시 Mac main·origin/main 9a5c3b9, 작업 트리 깨끗함 확인. Linux pull은 미확인이다.
- 자원·앱: 직전 nproc 4 확인. 현재 앱 실행·PID는 미확인이다.
- 변경 사항: 없음. 기존 CpuLabController는 lab 전용, 같은 JVM에서 동시 계산 1개, 추가 요청 429, 약 3초 경과 시간 동안 계산한다. 그대로 요청만 늘리면 동시 계산 수를 늘리는 실험이 되지 않는다.
- 계획: 복습과 개념 설명 뒤 별도 실험의 작업량·동시성·실행 시간 상한·정리 방법을 먼저 정한다. 동일 작업량의 비교와 일정 시간 CPU를 사용하는 비교는 구분한다. 제한을 임의로 제거하지 않는다.

## 2. 필요한 개념과 도구

### 시작 복습 — THREAD-03·SCHEDULER-01

1. THREAD-03: Poller가 Java RUNNABLE이면 CPU를 계속 사용한다고 판단할 수 있을까? 판단을 보완할 스택 위치나 CPU 관찰 방법을 하나 말한다.
2. SCHEDULER-01: 논리 CPU 4개 환경에서 r=8을 봤다. 이 8에는 어떤 작업들이 포함되고, CPU가 계속 부족한지 판단하려면 무엇을 함께 볼까?

| 문항 | 학습자 답 | 보충 |
| --- | --- | --- |
| THREAD-03 | RUNNABLE만으로 확신할 수 없다고 답했다. jcmd에서 TIME_WAIT 또는 WAIT 상태를 찾고, CPU는 top -L -p PID로 확인하자고 제안했다. | 판단 유보는 맞음. Poller는 EPoll.wait에서 기다리는 동안에도 Java RUNNABLE일 수 있으므로 상태 이름과 스택을 함께 읽는다. TIME_WAIT은 TCP 상태이며 Java Thread.State가 아니다. top의 스레드 옵션은 -H, ps는 -L이다. CPU는 top -H -p PID 또는 동일 TID의 누적 cpu 증가량 비교로 확인한다. |
| SCHEDULER-01 | r에는 실행 중과 실행 차례 대기 작업이 포함되고 CPU%를 함께 보겠다고 답했다. | 맞음. r 증가와 CPU 사용률이 지속되는지도 함께 확인한다. |

회상 답변: 학습자는 `top -H -p <PID>`를 정확히 답했고 ps의 -L도 기억했다. 특정 앱을 선택하는 정확한 형식은 `ps -L -p <PID>`라고 보충한다. 실제 명령 실행 결과가 아니라 회상 답변이다. 학습자는 top은 CPU·메모리 상태, ps는 프로세스를 보는 도구인지 질문했다. 두 도구 모두 프로세스·스레드 정보를 볼 수 있고, top은 주기적으로 화면을 갱신하며 서버 전체 요약과 각 작업의 자원 사용을 함께 보여주고 ps는 실행 시점의 목록·상태 등을 한 번 출력한다는 차이를 설명한다. ps의 모든 열이 순간 사용률이라는 의미는 아니다. 시작 복습·도구 보충을 마친 뒤 아래 첫 실험 설명으로 이어간다. 처리량·문맥 전환의 상세 설명은 아직 전이다.

## 3. 예상

새 실험의 예상은 아직 묻지 않았다. 필요한 기초와 제한 조건을 설명한 뒤 예상한다.

## 4. 실행 순서와 재현 절차

부하 실행 명령은 아직 안내하지 않았다. 학습자의 요청으로 top·ps·jcmd 연결 방법과 예시를 아래에 보충한다. 안내 예시를 실제 실행으로 기록하지 않는다.

## 5. 정상 상태와 변화 관찰

새 Linux 관측값 없음. 이전 정상 출력은 [SCHEDULER-01](2026-10-23-SCHEDULER-01-run-queue.md)에 보존한다.

## 6. 관찰 사실과 가설

- 확인: 기존 CPU API의 동시 계산 1개 제한과 고정 경과 시간 계산.
- 미확인: 새 실험 결과, 현재 앱 상태·Linux 동기화.

## 7. 검증

2026-11-20 AI의 Mac compileJava 성공(최종 수정 후 906ms)을 확인했다. 컴파일 검증이며 Linux 런타임·프로필·입력 거절·중복 거절·스레드 생성과 종료·성능 관찰 검증은 아니다.

## 8. 원인과 해결·재검증

해당 없음: 현재 복습·실험 준비 단계다. 부하 실험 전 전용 lab 프로필·작업 상한·중단·정리 방법을 먼저 마련한다.

## 9. 배운 내용과 다음 행동

- 다음 작은 행동: SCHEDULER-02 마무리 퀴즈 4문항에 답한다. 학습자 답·보충 확인 후 Linux 앱 종료 및 AI의 기록 커밋·푸시를 진행한다.
- 저장 상태: 이 시작 기록은 로컬 미커밋 상태다. 사용자는 AI의 커밋·푸시 수행을 명시했다. Linux 명령 실행과 관찰은 학습자가 수행한다.


## 10. 보충 — top·ps·jcmd를 같은 TID로 연결하기

학습자는 top -H 실행 결과와 다른 스택에서 무엇을 읽는지 정확한 예시를 요청했다. 아래 PID·TID·CPU 수치는 모두 설명용이며 실제 측정값이 아니다. 새 실습·앱 실행을 확인한 것이 아니다. 현재 소스의 계산 루프는 CpuLabController.java:38~39, sleep 호출은 ThreadLabController.java:28임을 AI가 확인했다.

### 명령과 터미널 역할

Linux 관찰 터미널 A에서 `top -H -d 1 -p <PID>`로 앱의 스레드별 사용률을 관찰한다. -H는 --threads-show, -d는 --delay, -p는 --pid다. 1초 간격으로 갱신하고 q로 닫는다. PID 열은 이 스레드 모드에서 Linux TID로 읽는다. Irix On에서는 한 논리 CPU를 계속 사용하면 한 스레드가 약 100%로 표시될 수 있다. 상단 %Cpu(s)는 특정 앱만이 아니라 시스템 전체 요약이다.

필요한 열만 남긴 설명용 예시:

```text
  PID S %CPU COMMAND
12360 R 98.7 http-nio-8080-e
12361 S  0.0 http-nio-8080-e
12362 S  0.0 http-nio-8080-P
12363 S  0.0 http-nio-8080-e
```

Linux 관찰 터미널 B에서 `ps -L -p <PID> -o pid,tid,stat,comm`으로 앱 PID와 TID·Linux 상태를 확인한다. -L은 스레드 표시, -p는 앱 선택, -o는 출력 열 지정이다. 한 번 출력하고 종료한다.

```text
  PID   TID STAT COMMAND
12345 12360 Rl   http-nio-8080-e
12345 12361 Sl   http-nio-8080-e
12345 12362 Sl   http-nio-8080-P
12345 12363 Sl   http-nio-8080-e
```

이 역시 독립된 시점의 예시다. R은 실행 중 또는 CPU 실행 차례 대기, S는 interruptible sleep, 소문자 l은 multithreaded 표시다. comm은 Java 이름 전체를 보장하지 않는다.

같은 터미널 B에서 `jcmd <PID> Thread.print > /tmp/thread-dump.txt`로 앱 전체 덤프를 저장한다. 앱 PID를 넣으며 TID를 넣는 것이 아니다. >는 같은 파일을 덮어쓴다. 명령은 저장 후 종료하고 앱은 유지된다. 다음은 예시 TID 12360의 블록을 꺼내는 명령이다. `grep --after-context=25 'nid=12360 ' /tmp/thread-dump.txt`에서 12360은 실제 관측 TID로 바꾼다. grep도 출력 후 종료한다. 숫자 25는 뒤쪽 줄 수이지 스레드 블록 경계가 아니다.

### 스택 읽기 예시(헤더의 일부 필드는 생략)

```text
"http-nio-8080-exec-1" #29 [12360] ... nid=12360 ...
   java.lang.Thread.State: RUNNABLE
    at com.shinkwang.backendsystemlab.cpu.CpuLabController.calculateForThreeSeconds(CpuLabController.java:39)
    at com.shinkwang.backendsystemlab.cpu.CpuLabController.cpu(CpuLabController.java:27)
```

TID를 먼저 대조하고 상태·최상단 프레임·내 코드 위치를 읽는다. 높은 스레드 CPU와 계산 루프가 함께 관찰되면 계산에 CPU를 쓰고 있다는 근거가 된다. RUNNABLE 한 단어만으로 판단하지 않는다.

```text
"http-nio-8080-exec-2" #30 [12361] ... nid=12361 ...
   java.lang.Thread.State: TIMED_WAITING (sleeping)
    at java.lang.Thread.sleep0(Native Method)
    at java.lang.Thread.sleep(Thread.java:509)
    at com.shinkwang.backendsystemlab.thread.ThreadLabController.sleep(ThreadLabController.java:28)
```

현재 위치는 sleep 내부이고 내 코드 28행이 호출한 경로다. 긴 응답 시간과 낮은 CPU가 함께 나타날 수 있다.

```text
"http-nio-8080-Poller" #39 [12362] ... nid=12362 ...
   java.lang.Thread.State: RUNNABLE
    at sun.nio.ch.EPoll.wait(Native Method)
    at sun.nio.ch.EPollSelectorImpl.doSelect(EPollSelectorImpl.java:121)
```

소켓 이벤트를 기다리는 native 코드 위치다. Java RUNNABLE과 Linux S·낮은 CPU가 함께 보일 수 있다. 이는 과거 실제 Poller 관찰과 같은 원리를 설명하는 예시다.

요청이 끝나 다음 작업을 기다리는 worker는 WAITING (parking)과 Unsafe.park / LockSupport.park / ConditionObject.await / LinkedBlockingQueue.take / Tomcat TaskQueue.take 등의 경로로 보일 수 있다. 스택 최상단과 아래 호출 경로를 함께 읽는다. WAITING이면 모두 다음 HTTP 작업 대기라고 단정하는 것이 아니라 해당 큐 경로가 근거다.

주의: top의 %CPU는 갱신 구간의 사용률, S는 관측 순간의 Linux 상태, 덤프는 찍힌 순간의 Java 상태·스택이다. 서로 다른 시점이며 3초 실험이 끝나면 같은 TID도 다른 위치에서 관찰될 수 있다. 덤프 헤더 cpu는 누적 CPU 시간(ms), elapsed는 생성 이후 경과 시간(s)이다. 동일 TID의 cpu 증가량을 구간 경과 시간과 연결해 CPU 사용을 추가 확인할 수도 있다. 이번 보충의 실제 재실행은 미확인이다.

근거: [top 매뉴얼](https://man7.org/linux/man-pages/man1/top.1.html), [Java 21 Thread.State](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Thread.State.html), 기존 THREAD-02·03 실제 관찰과 현재 프로젝트 소스.


## 11. 첫 실험 설계와 클래스 틀 안내

동시성은 처리 중인 작업이 겹쳐 있는 정도이며 동시에 CPU를 사용하는 것과 구분한다. 논리 CPU 하나에서도 여러 작업을 번갈아 실행할 수 있다.

첫 비교는 약 3초 동안 계산하는 스레드 수를 1→2→4로 늘려 top·vmstat의 CPU 사용·실행 가능 작업 수를 관찰하는 것으로 계획한다. CPU보다 많은 작업의 실행 대기는 이후 최대 8개까지 제한해서 비교할 수 있으나 아직 실행하지 않는다. 이 시간 고정 실험을 같은 작업량의 처리량·응답 시간 성능 비교로 해석하지 않는다. 작업량 고정 비교는 이후 별도 조건으로 진행한다.

안전 조건(구현 전 계획): lab 전용·전용 Linux VM, 배치 동시 실행 1개, 계산 스레드 최대 8개, 계산 루프 경과 시간 약 3초 상한과 인터럽트 확인, 종료·정리 처리. 기존 CpuLabController의 제한은 유지한다. API·스레드 생성·중단 코드는 설명 후 학습자와 작은 단위로 작성할 예정이다.

현재 작은 행동은 Mac의 `src/main/java/com/shinkwang/backendsystemlab/scheduler/SchedulerLabController.java` 클래스 틀 작성이다. 안내 코드:

```java
package com.shinkwang.backendsystemlab.scheduler;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Profile("lab")
@RestController
@RequestMapping("/lab/scheduler")
public class SchedulerLabController {
}
```

2026-11-20 학습자가 클래스 작성 완료를 알렸고 AI가 실제 파일을 읽어 패키지·lab 프로필·RestController·경로 접두어를 확인했다. 애너테이션 순서는 안내와 다르지만 이 구성에서는 문제없다. 클래스 내부는 비어 있고 호출 가능한 API·부하 실행은 아직 없다. 빌드·Linux 실행은 미확인이다. AI가 Java 소스를 대신 생성하거나 수정한 것은 아니다.


## 12. 배치 실행 상태 필드 안내 — 2026-11-20

학습자에게 import `java.util.concurrent.atomic.AtomicBoolean`과 클래스 안의 `private final AtomicBoolean running = new AtomicBoolean(false);` 추가를 안내한다. false는 실험 미실행, true는 진행 중을 표현할 예정이다. 이 값은 계산 worker 개수가 아니라 실험 배치 전체의 중복 진입을 제한하는 데 사용할 계획이다. 이후 API 진입의 compareAndSet(false, true)와 종료·정리 후 finally의 set(false)를 연결한다. 필드를 선언하는 것만으로는 중복 실행을 막지 않으며 검사·복원 로직은 아직 없다. 새 API·스레드 생성 코드도 아직 작성하지 않는다. 필드 추가의 실제 결과 대기 중이다.

## 13. 코드 안내 방식 변경·전체 구현안 — 2026-11-20

사용자는 코드 작성이 너무 느리므로 기능 전체를 한 번에 보여주고 모르는 부분을 질문하겠다고 요청했다. AI는 이를 AGENTS.md에 기록했고 이후 import·필드마다 완료를 기다리지 않는다. 실제 소스를 확인하여 AtomicBoolean import와 running 필드는 학습자가 추가한 것을 확인했다.

컨트롤러 전체 구현안을 대화에 제시한다. 이 구현안은 아직 실제 Java 파일에 적용하거나 빌드·실행한 것으로 기록하지 않는다.

- API: POST /lab/scheduler/cpu?threads=1, 기본값 1, 허용 범위 1~8, 범위 밖은 400. 기존 CPU API는 유지한다.
- 배치 단위 guard: compareAndSet(false, true) 실패 시 429. executor 종료·worker 정리 이후 finally에서 false로 복원.
- Java 21 플랫폼 스레드: 요청한 크기의 fixed thread pool을 배치마다 만들고 scheduler-cpu-1 등의 이름을 붙인다. Callable 작업 수는 요청한 스레드 수와 같고 최대 8개다.
- 각 worker: 자기 시작 시점 이후 약 3초간 계산하면서 interrupt를 확인하고, 이름·반복 횟수·계산 결과를 WorkerResult record로 반환한다.
- invokeAll로 전체 작업 완료를 기다리고 Future.get으로 결과를 모은다. RunResult record는 요청한 threads, elapsedMillis, worker 결과 목록을 반환한다.
- ExecutorService를 try-with-resources로 닫아 종료를 기다린 뒤 guard를 복원한다. InterruptedException은 현재 스레드를 재인터럽트하고 503, ExecutionException은 500으로 전달한다.
- 시간은 worker별 경과 시간 기준이며 시작 시각이 완전히 같거나 HTTP 응답이 정확히 3초라고 보장하지 않는다. 시간 고정 관찰이며 동일 작업량 성능 비교가 아니다.
- 다음 행동: 학습자가 전체 코드를 작성·질문한다. 작성 후 AI가 전체 검토·빌드를 확인하고 커밋·푸시, 이후 학습자가 Linux에서 실행·관찰한다. 현재는 필드까지만 실제 확인했다.

Java 21 동작 근거: [ExecutorService 문서](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/ExecutorService.html). invokeAll의 완료 대기와 interrupt 시 미완료 작업 취소, close의 종료 대기를 확인했다.


## 14. 전체 코드 작성 확인·Callable/Future 보충 — 2026-11-20

학습자가 전체 코드를 작성했다고 보고하고 Callable·invokeAll·Future·결과 수집 구간의 상세 설명을 요청했다. AI가 실제 SchedulerLabController.java 전체를 읽어 구현이 추가된 것을 확인했다. 새 Java 코드 적용이나 자동 수정은 하지 않았다. 빌드·Linux 실행은 아직 미확인이다.

코드 검토에서 `@RequestParam(name = "threads", defaultValue = 1)`을 발견했다. defaultValue는 String이므로 `defaultValue = "1"`로 수정해야 한다. 현재 InterruptedException 응답은 안내안의 503 대신 500이며 응답 record 필드는 elapsedMillis 대신 elapseMillis로 작성되어 있다. 후자는 각각 의미·응답 이름 차이이고 annotation의 int 값은 컴파일 오류다. 자동으로 고치거나 수정 완료로 기록하지 않는다.

보충 설명 내용:

- Callable<V>는 call()로 V를 반환하거나 예외를 던질 수 있는 작업 표현이며 스레드가 아니다. this::calculate와 () -> calculate()는 여기서 같은 역할을 하고, 목록에 넣는 순간 calculate를 실행하지 않는다.
- tasks에는 같은 메서드를 실행할 작업을 요청한 수만큼 넣는다. 같은 컨트롤러 객체의 메서드를 호출하더라도 각 호출의 지역 변수 startedAt·iterations·value는 별개다.
- ExecutorService가 작업을 플랫폼 worker 스레드들에서 실행한다. tasks만 생성해서는 계산이 시작되지 않는다.
- Future<V>는 작업의 완료 상태와 성공 결과·실패를 확인할 수 있는 핸들이다. 일반적인 submit은 전체 완료를 기다리지 않고 Future를 받을 수 있다. get은 필요하면 완료를 기다리며 결과를 얻고 실패 시 ExecutionException을 던진다.
- 이번 invokeAll(tasks)는 모든 작업이 완료된 뒤 Future 목록을 반환한다. 완료에는 예외로 종료된 작업도 포함된다. Future 목록 순서는 입력 작업 순서이며 완료 순서가 아니다.
- 따라서 뒤의 for/future.get은 작업을 순서대로 새로 실행하는 것이 아니며, 이미 끝난 작업의 결과를 순서대로 모은다. 정상 완료라면 추가 계산 완료 대기는 없다. 작업 예외는 get에서 확인한다.
- HTTP 요청을 처리하는 Tomcat 스레드가 목록 준비·invokeAll 대기·get 수집·응답 생성을 수행하고, scheduler-cpu-* worker가 calculate를 수행한다. 요청 처리는 전체 완료를 기다리므로 HTTP 응답을 즉시 돌려주는 비동기 API는 아니다.
- elapsedMillis는 시작부터 계산·대기·결과 수집 뒤 측정까지의 경과 시간이다. CPU 시간의 합계가 아니며 executor close·JSON 직렬화·통신까지 포함한 최종 HTTP 응답 시간도 아니다.
- List.copyOf는 결과 목록의 변경 불가 사본을 만들며 Future가 아니라 실제 WorkerResult 목록을 RunResult에 담는다.

설명은 작업 2개·worker 2개의 가상 타임라인으로 제시하며 실제 관측 결과로 기록하지 않는다. 추가 테스트나 계산 부하는 실행하지 않았다. 다음은 학습자 질문과 annotation 문자열 수정 후 코드 검토·컴파일 확인이다.

근거: Java 21 [Callable](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/Callable.html), [Future](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/Future.html), [ExecutorService](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/ExecutorService.html).


## 15. InterruptedException·ExecutionException 보충 — 2026-11-20

학습자가 스레드 예외, 특히 InterruptedException의 의미와 재인터럽트 코드가 감이 오지 않는다고 질문했다. 아래는 개념 설명이며 실제 인터럽트 실험이나 이해 완료 확인이 아니다. 소스 수정·빌드도 수행하지 않는다.

- 인터럽트는 대상 Thread에 중단·대기 취소를 요청하는 협력적 통지다. 대상 참조의 interrupt()를 다른 관리 코드 등이 호출할 수 있다. CPU 계산의 임의 위치에 자동으로 InterruptedException을 던져 강제 종료하는 기능이 아니다.
- 대상에 인터럽트 상태가 설정된다. sleep 같은 인터럽트 가능한 대기 메서드는 대기를 끝내고 InterruptedException을 던지며 상태를 해제한다. 단순히 new InterruptedException을 직접 던지는 것 자체가 상태를 해제하는 것은 아니다.
- 계산 중에는 코드가 isInterrupted()를 확인하고 종료·정리하거나 예외를 던지도록 작성해야 한다. 이번 calculate는 이 검사를 하고 직접 InterruptedException을 던진다. isInterrupted()는 상태를 지우지 않는다.
- Thread.currentThread().interrupt()는 catch를 실행 중인 현재 스레드의 인터럽트 상태를 다시 설정한다. InterruptedException을 HTTP 예외로 바꿔 전달하는 이 코드에서는 중단 요청 정보를 잃지 않도록 보존한다. 이 한 줄이 스레드를 즉시 죽이거나 작업을 재시작하는 것은 아니다. 항상 모든 catch에서 기계적으로 넣는 규칙이 아니라, 예외를 상위로 그대로 던지거나 요청을 처리하는 등 취소 정책에 맞춘다.
- 이 컨트롤러의 invokeAll은 Tomcat 요청 스레드가 전체 완료를 기다리는 호출이다. 그 대기 스레드가 인터럽트되면 invokeAll은 미완료 작업을 취소하고 InterruptedException을 던질 수 있다. try-with-resources의 executor 정리를 거쳐 catch로 전달된다. 이 catch의 currentThread는 계산 worker가 아니라 요청 스레드다.
- 반대로 worker의 calculate에서 발생한 예외는 Future에 작업 실패로 보관되고, 요청 스레드가 get에서 ExecutionException으로 받는다. getCause는 worker의 원래 예외다. worker의 원래 예외가 InterruptedException일 수도 있지만 그 사실만으로 요청 스레드 자신도 인터럽트된 것은 아니다. 따라서 ExecutionException catch에서 무조건 현재 스레드를 재인터럽트하지 않는다.
- 중단 요청을 보내는 코드의 예는 Thread 참조의 interrupt(), Future.cancel(true), executor의 shutdownNow() 등이다. API가 자동으로 예외를 던지는 조건과 직접 확인해야 하는 조건을 구분한다. 클라이언트 curl timeout이 servlet worker 인터럽트를 자동 보장하는 것은 아니다.
- 별도의 짧은 sleep·interrupt 예시를 설명용으로 제시한다. 실제 프로젝트나 Linux에서 실행한 결과로 기록하지 않는다.

근거: Java 21 Thread.interrupt/isInterrupted, InterruptedException, ExecutorService.invokeAll, ExecutionException 공식 문서. 다음은 질문·이해 확인과 annotation 문자열 수정 후 빌드 확인이다.


## 16. 전체 구현 검토·컴파일 확인 — 2026-11-20

학습자가 작성을 마쳤다고 확인했다. AI는 실제 전체 소스를 읽었고 defaultValue="1" 수정 완료를 확인했다. 1~8 입력 검사·400, AtomicBoolean 배치 제한·429, lab 프로필, 플랫폼 worker 이름·개수 상한, worker별 약 3초 계산과 인터럽트 확인, invokeAll 완료 대기, Future.get 실패 확인, executor close 후 guard 복원을 검토했다. 현재 InterruptedException 응답은 500이며 자동으로 503으로 바꾸지 않았다.

AI는 응답 record의 elapseMillis 오타를 elapsedMillis로 수정했다. CpuLabController 변경은 학습자가 정리한 빈 줄 한 줄 삭제뿐이며 동작 변경이 없음을 diff로 확인했다. 코드 교육 방식 변경(기능 전체 제시)과 도구·동시성 예외 보충 기록도 함께 커밋할 예정이다.

Mac에서 ./gradlew compileJava를 실행했다. 처음 제한된 실행에서 ~/.gradle 캐시 lock 생성 권한 오류가 났고, 승인된 권한으로 다시 실행하여 3초에 성공했다. 응답 필드명 정정 후 다시 실행하여 BUILD SUCCESSFUL in 906ms, 1 actionable task: 1 executed를 확인했다. CPU 부하를 Mac에서 실행하지 않았으며 실제 실험은 별도 Linux에서 학습자가 수행한다. 테스트·실제 HTTP 결과를 확인한 것은 아니다.

다음 안내: Linux 앱 실행 터미널에서 cd /home/skl/backend-system-lab → git pull → ./gradlew bootRun --args='--spring.profiles.active=lab'. pull 후 기동 로그의 새 PID와 lab 프로필을 확인한다. 앱 터미널 유지, 종료 Ctrl+C. Linux pull·기동·새 PID는 아직 미확인이다. 이후 별도 터미널에서 관측 준비·예상·첫 단일 worker 요청을 진행할 예정이다.

Git 후속 확인: 2026-11-20 구현·학습 기록 커밋 72ccda2를 생성하고 origin/main 정상 푸시를 확인했다. 첫 자동 승인 거절 후 사용자 명시 지시와 실제 지침을 재확인하여 동일 작업 재검토로 성공했다. Linux pull은 미확인이다. 이 Git 상태 갱신은 별도 문서 커밋에 포함한다.


## 17. Linux 기동 확인·기준값 관찰 안내 — 2026-11-20

학습자가 `PID 4571 | The following 1 profile is active: "lab"`을 제공했다. 이번 실행의 PID와 lab 프로필은 사용자 보고로 확인했다. 이전 PID는 재사용하지 않는다. Linux pull 원문·실행 커밋·Started 로그·실험 API 응답은 미제공이다.

앱 실행 터미널은 유지하고 별도 Linux 관찰 터미널의 임의 폴더에서 `vmstat -w 1 5`를 실행하도록 안내한다. -w는 넓은 출력, 1은 초 단위 간격, 5는 보고서 수다. 자동 종료하며 중간 중단 Ctrl+C. 첫 자료 행의 CPU 값은 부팅 이후 평균이므로 두 번째 이후 행의 r·us·id를 부하 전 기준값으로 읽는다. 서버 전체 관측이며 앱 PID로 필터하는 명령이 아니다. 아직 새 출력과 부하 실행 결과는 미확인이다.


## 18. Linux 부하 전 기준값 — 2026-11-20

PID 4571·lab 기동 확인 뒤 vmstat -w 1 5를 안내했고 학습자가 헤더와 5개 자료 행을 텍스트로 제공했다. 이번 명령 프롬프트·실행 시각 원문은 별도 미제공이다. 필요한 CPU 항목과 이후 비교용 cs를 아래에 보존한다. cs는 아직 새로 설명하거나 원인 판단에 사용하지 않았다.

| 자료 행 | r (개) | us (%) | sy (%) | id (%) | cs (회/초) | CPU·rate 관측 구간 |
| --- | --- | --- | --- | --- | --- | --- |
| 1 | 0 | 0 | 0 | 100 | 0 | 부팅 이후 평균 |
| 2 | 0 | 0 | 0 | 99 | 877 | 직전 1초 |
| 3 | 0 | 1 | 0 | 99 | 862 | 직전 1초 |
| 4 | 0 | 0 | 0 | 99 | 872 | 직전 1초 |
| 5 | 0 | 0 | 0 | 99 | 785 | 직전 1초 |

r은 각 행의 순간 값이다. 두 번째 이후 CPU 구간에서 us=0~1%, id=99%, 각 관측 순간 r=0이었다. 튜터는 이번 기준 구간의 CPU에 여유가 있다고 해석했다. 다른 앱 상태나 모든 순간 CPU 사용 0을 확정한 것은 아니다. 어떤 worker 부하 요청도 아직 실행 확인하지 않았다.

첫 부하 예상 질문(답변 대기): 현재 확인한 논리 CPU 4개 환경에서 worker 1개가 약 3초 계산하면 서버 전체 us·id는 기준값에서 어떻게 바뀔까? 가능하면 대략적인 비율과 이유를 설명한다. 예상 확인 후 실험 관측·요청 명령을 안내한다.


## 19. worker 1개 예상·실행 안내 — 2026-11-20

학습자는 us 약 25%, id 약 75%를 예상했다. 스레드 하나가 3초 동안 CPU를 계속 쓰기 때문이라고 설명했고 Irix 모드가 아닌 것 같다고 덧붙였다. 숫자와 CPU 한 개 분량이라는 해석은 맞다. Irix 모드는 top의 작업별 %CPU 표시 방식이며 vmstat에는 그 설정이 없다. vmstat는 전체 CPU 시간 합계를 기준으로 CPU 비율을 표시한다. 다른 작업과 측정 구간 때문에 실측은 정확히 25/75일 필요는 없다.

Linux 앱 터미널(PID 4571, lab)은 유지한다. 별도 관찰·요청 터미널의 임의 폴더에서 아래 블록을 한 번에 실행하도록 안내한다(아직 결과 미확인).

```bash
vmstat -w 1 8 > /tmp/scheduler-02-threads-1-vmstat.txt &
sleep 1
curl --max-time 10 -w '\nHTTP %{http_code}, total %{time_total}s\n' -X POST 'http://localhost:8080/lab/scheduler/cpu?threads=1'
wait
cat /tmp/scheduler-02-threads-1-vmstat.txt
```

vmstat는 넓은 출력·1초 간격·8개 보고서이며 자동 종료한다. >는 파일 덮어쓰기, &는 백그라운드 실행이다. sleep 1은 관측 시작 후 잠깐 기다리는 셸 명령이다. curl은 최대 10초, worker 1개 POST를 한 번 보내며 응답·HTTP 코드·전체 시간을 표시한다. wait는 이 셸의 백그라운드 작업 종료를 기다리고 cat은 저장한 관측을 출력한다. 관측·요청이 끝나면 프롬프트로 돌아온다. 계산 중단이 필요하면 앱 터미널 Ctrl+C로 종료한다. curl 중단·timeout은 서버 계산 중단을 보장하지 않으며 worker는 자체 약 3초 제한을 가진다.

첫 CPU 행은 부팅 이후 평균이고 이후 행에는 부하 전·중·후가 섞여 있다. r·us·id와 응답 JSON의 worker 수·이름·elapsedMillis를 확인할 예정이다. 응답과 전체 관측 출력 대기 중이다.


## 20. curl 옵션 읽기 보충 — 2026-11-20

첫 부하 실행 결과를 받기 전 학습자가 curl -X·-w가 익숙하지 않고 --max-time 10은 최대 10초 기다리는 것인지 질문했다. 첫 실행의 실제 수행·응답은 여전히 미확인이다.

- -X / --request는 HTTP 메서드 지정이다. POST·DELETE뿐 아니라 GET도 지정할 수 있다. 이번에는 컨트롤러가 @PostMapping이므로 POST를 지정한다. 별도 설정 없이 URL만 요청하는 일반적인 curl 사용의 기본 메서드는 GET이다.
- -w / --write-out은 요청이 끝난 뒤 curl이 추가 출력할 형식이다. 서버로 보내는 본문이 아니다. %{http_code}는 HTTP 상태 코드, %{time_total}은 초 단위 전체 소요 시간, \n은 출력 줄바꿈이다. HTTP와 total, s는 사용자가 적은 글자다.
- --max-time / -m은 전송 전체 시간의 상한(초)이다. 응답을 받은 뒤 추가로 10초 기다리는 것이 아니고 10초가 되면 curl이 요청 진행을 중단한다. 서버 계산의 중단까지 보장하지 않는다.
- vmstat의 -w(--wide)와 curl의 -w(--write-out)은 서로 다른 명령의 옵션이다. 긴 이름으로 먼저 읽도록 안내한다.

같은 명령의 긴 옵션 표현(설명용, 별도 실행을 확인한 것이 아님):

```bash
curl --max-time 10 \
  --write-out '\nHTTP %{http_code}, total %{time_total}s\n' \
  --request POST \
  'http://localhost:8080/lab/scheduler/cpu?threads=1'
```

추가 출력 예시 HTTP 200, total 3.012345s는 설명용이며 실측이 아니다. 근거: [curl 공식 매뉴얼](https://curl.se/docs/manpage.html). 다음은 옵션 질문 확인 후 기존 19절의 첫 실행·응답·vmstat 결과를 제공받는다.


## 21. worker 1개 실제 결과 — 2026-11-20

학습자가 vmstat 출력과 이어서 응답·HTTP 코드·전체 시간을 제공했다. 앞 메시지에 같은 관측 블록이 중복되어 있었지만 이를 별도 두 번 실행한 것으로 세지 않는다.

```json
{"threads":1,"elapsedMillis":3001,"workers":[{"threadName":"scheduler-cpu-1","iterations":141956656,"value":688669666028171009}]}
```

```text
HTTP 200, total 3.071397s
[1]+ Done vmstat -w 1 8 > /tmp/scheduler-02-threads-1-vmstat.txt
```

| 자료 행 | r (개) | us (%) | sy (%) | id (%) | cs (회/초) |
| --- | --- | --- | --- | --- | --- |
| 1 | 1 | 0 | 0 | 100 | 0 |
| 2 | 0 | 0 | 0 | 99 | 855 |
| 3 | 1 | 25 | 1 | 74 | 1024 |
| 4 | 1 | 25 | 0 | 75 | 871 |
| 5 | 1 | 25 | 0 | 75 | 782 |
| 6 | 0 | 4 | 0 | 96 | 1052 |
| 7 | 0 | 0 | 0 | 100 | 757 |
| 8 | 1 | 0 | 0 | 99 | 775 |

첫 CPU 자료 행은 부팅 이후 평균이며 이후는 직전 1초 구간이다. r은 각 시점의 값이다.

학습자는 중간의 us 25%가 예상과 일치했다고 설명했고 worker 1개·3001ms는 약 3초임을 읽었다. iterations와 value를 보지 않아도 되는지 질문했다.

보충: 이번 우선 항목은 worker 수·이름·경과 시간과 r/us/id다. JVM 전체 스레드 수가 1이라는 뜻은 아니다. elapsedMillis=3001은 3.001초의 경과 시간이며 CPU 시간이 아니다. curl total 3.071397초는 HTTP 전체로 측정 범위가 다르다. iterations=141956656은 루프 반복 횟수이며 worker별·합계 계산량을 비교할 추가 자료로 보존한다. 짧은 한 번의 값으로 처리 성능을 단정하지 않는다. value는 계산 결과로 숫자 자체에 업무 의미는 없으며 이번 CPU 상태 관찰에서 값을 해독할 필요는 없다.

다음 예상 질문: worker 2개일 때 us·id 변화와 경과 시간이 약 3초인지 약 6초인지 이유를 설명한다. threads=2는 아직 실행하지 않았다.


## 22. worker 2개 예상·실행 안내 — 2026-11-20

학습자는 us 약 50%·id 약 50%, 전체 경과 시간 약 3초를 예상했다. 논리 CPU 4개에서 계산 worker 2개를 실행하는 조건과 맞는 예상이다. 두 작업이 동시에 시작하도록 장벽을 둔 코드는 아니며 시작·응답 처리와 다른 부하에 따라 실제 수치는 달라질 수 있다. 시간 고정 루프를 2개 실행하는 비교이고 동일 작업량의 처리량 비교는 아니다.

기존 앱 PID 4571·lab 터미널은 유지한다. 같은 Linux 관찰·요청 터미널의 임의 폴더에서 아래 블록을 한 번에 실행하도록 안내한다. worker 1개와 바꾸는 조건은 threads=2이며 관측 파일을 별도 이름으로 저장한다. curl은 익숙해지도록 긴 옵션 이름을 사용한다.

```bash
vmstat -w 1 8 > /tmp/scheduler-02-threads-2-vmstat.txt &
sleep 1
curl --max-time 10 --write-out '\nHTTP %{http_code}, total %{time_total}s\n' --request POST 'http://localhost:8080/lab/scheduler/cpu?threads=2'
wait
cat /tmp/scheduler-02-threads-2-vmstat.txt
```

관측은 1초 간격 8개 보고서 후 자동 종료, curl은 최대 10초다. 같은 파일은 덮어쓰고 &는 백그라운드 실행, wait는 종료 대기다. 부하 중단이 필요하면 앱 터미널 Ctrl+C로 종료한다. curl 중단만으로 서버 계산 중단이 보장되지는 않으며 각 worker는 약 3초 제한을 가진다.

실제 JSON·HTTP 코드·전체 시간·vmstat 전체 출력을 요청한다. 응답 worker 결과 2개, 경과 시간과 부하 중 r·us·id 및 회복을 비교할 예정이다. 아직 두 worker 실행·결과는 미확인이다.


## 23. worker 2개 실제 결과 — 2026-11-20

학습자가 아래 응답과 HTTP 시간, vmstat 8개 보고서 및 자동 종료 표시를 제공했다.

```json
{"threads":2,"elapsedMillis":3000,"workers":[{"threadName":"scheduler-cpu-1","iterations":142204471,"value":1801735661660739398},{"threadName":"scheduler-cpu-2","iterations":142241730,"value":-102374890308570975}]}
```

```text
[1] 5016
HTTP 200, total 3.004848s
[1]+ Done vmstat -w 1 8 > /tmp/scheduler-02-threads-2-vmstat.txt
```

[1] 5016은 백그라운드 작업 시작 표시로 제공되었으며 앱 PID나 계산 worker TID로 해석하지 않는다.

| 자료 행 | r (개) | us (%) | sy (%) | id (%) | cs (회/초) |
| --- | --- | --- | --- | --- | --- |
| 1 | 1 | 0 | 0 | 100 | 0 |
| 2 | 0 | 0 | 1 | 99 | 851 |
| 3 | 2 | 50 | 1 | 50 | 885 |
| 4 | 2 | 50 | 1 | 49 | 834 |
| 5 | 2 | 50 | 1 | 49 | 754 |
| 6 | 0 | 1 | 1 | 99 | 792 |
| 7 | 0 | 1 | 0 | 99 | 740 |
| 8 | 0 | 0 | 0 | 99 | 735 |

첫 CPU 행은 부팅 이후 평균이며 r은 각 시점의 값이다. 예상한 us 50%·id 50%·약 3초와 대체로 일치하고 실험 뒤 r=0·id=99%로 회복했다. CPU 비율은 표시 반올림으로 합계가 100이 아닌 행이 있을 수 있다.

| 비교 항목 | worker 1개 | worker 2개 |
| --- | --- | --- |
| HTTP | 200 | 200 |
| 서버 내부 경과 시간 | 3001ms | 3000ms |
| curl 전체 시간 | 3.071397s | 3.004848s |
| 계산 중 3개 구간의 r | 1 | 2 |
| 계산 중 3개 구간의 us | 25% | 50% |
| 계산 중 3개 구간의 id | 74~75% | 49~50% |

두 worker는 각각 약 3초 계산한다. CPU 사용 분량은 늘었지만 요청 경과 시간은 거의 같았다. 시간 고정 비교이며 동일 작업량 처리 속도나 HTTP 처리량이 2배라는 결론으로 확대하지 않는다. 다음은 worker 4개에서 us·id·경과 시간을 예상한다. threads=4는 아직 실행하지 않았다.


## 24. worker 4개 예상·실행 안내 — 2026-11-20

학습자는 us 100%·id 0%, 전체 경과 시간은 여전히 약 3초를 예상했다. 논리 CPU 4개에서 계산 worker 4개가 실행되는 조건과 맞는 예상이다. 실제로는 커널 코드·다른 작업·측정 경계 때문에 us가 정확히 100일 필요는 없고, 작업 시작과 정리 차이로 응답 시간도 정확히 3초는 아니다.

앱 PID 4571·lab 터미널은 유지한다. 같은 Linux 관찰·요청 터미널의 임의 폴더에서 아래 블록을 한 번에 실행하도록 안내한다.

```bash
vmstat -w 1 8 > /tmp/scheduler-02-threads-4-vmstat.txt &
sleep 1
curl --max-time 10 --write-out '\nHTTP %{http_code}, total %{time_total}s\n' --request POST 'http://localhost:8080/lab/scheduler/cpu?threads=4'
wait
cat /tmp/scheduler-02-threads-4-vmstat.txt
```

앞 실험과 달라진 조건은 threads=4와 관측 파일명이다. vmstat는 1초 간격 보고서 8개 후 자동 종료하고 curl은 최대 10초다. 같은 파일은 덮어쓴다. &는 백그라운드 실행, wait는 종료 대기다. 계산 중단은 앱 터미널 Ctrl+C이며 curl 종료가 서버 계산 취소를 보장하지는 않는다. 각 worker에는 약 3초 경과 시간 제한이 있다.

worker 결과 4개·경과 시간·HTTP 코드와 계산 구간 r·us·id 및 이후 회복을 확인할 예정이다. 실제 실행·결과 대기 중이다.


## 25. worker 4개 실제 결과 — 2026-11-20

학습자는 실행 블록을 제시한 뒤 아래 실제 결과를 제공하고 예상과 같다고 설명했다. 앞 메시지의 블록 제시만을 별도 부하 실행으로 세지 않는다.

```json
{"threads":4,"elapsedMillis":3000,"workers":[{"threadName":"scheduler-cpu-1","iterations":142305059,"value":6319339296355030406},{"threadName":"scheduler-cpu-2","iterations":142261222,"value":286411094241496033},{"threadName":"scheduler-cpu-3","iterations":142258000,"value":-5747448887500371711},{"threadName":"scheduler-cpu-4","iterations":142358133,"value":7958538082338038758}]}
```

```text
HTTP 200, total 3.004689s
[1]+ Done vmstat -w 1 8 > /tmp/scheduler-02-threads-4-vmstat.txt
```

| 자료 행 | r (개) | us (%) | sy (%) | id (%) | cs (회/초) |
| --- | --- | --- | --- | --- | --- |
| 1 | 1 | 0 | 0 | 100 | 0 |
| 2 | 3 | 0 | 1 | 99 | 834 |
| 3 | 4 | 100 | 0 | 1 | 858 |
| 4 | 4 | 100 | 0 | 0 | 759 |
| 5 | 4 | 100 | 0 | 0 | 765 |
| 6 | 0 | 1 | 0 | 98 | 831 |
| 7 | 0 | 0 | 0 | 100 | 738 |
| 8 | 0 | 0 | 0 | 100 | 726 |

첫 CPU 행은 부팅 이후 평균, 이후는 직전 1초 구간이며 r은 각 시점의 값이다. CPU 항목들은 표시 반올림 때문에 합계가 정확히 100이 아닐 수 있다. 부하 직전 행의 r=3과 us=0%는 서로 다른 관측 범위(순간과 직전 구간)이므로 모순으로 단정하지 않는다.

| 비교 항목 | worker 1개 | worker 2개 | worker 4개 |
| --- | --- | --- | --- |
| HTTP | 200 | 200 | 200 |
| 서버 경과 시간(ms) | 3001 | 3000 | 3000 |
| curl 전체 시간(s) | 3.071397 | 3.004848 | 3.004689 |
| 계산 중 3개 구간 r | 1 | 2 | 4 |
| 계산 중 3개 구간 us(%) | 25 | 50 | 100 |
| 계산 중 3개 구간 id(%) | 74~75 | 49~50 | 0~1 |
| iterations 합계 | 141956656 | 284446201 | 569182414 |

4개 worker 각각 약 1억 4,200만 회 반복했고 전체 CPU는 거의 유휴 없이 사용했다. 실행 후 r=0·id=98~100%로 회복했다. 시간 고정 관찰이므로 HTTP 처리량이나 동일 작업량 처리 시간이 선형으로 개선됐다고 결론 내리지 않는다.

다음 예상 질문(답변 대기): 논리 CPU 4개에서 worker를 최대 허용치인 8개로 늘리면 서버 전체 us, 실행 가능 작업 수 r, 각 worker의 iterations는 4개일 때와 비교해 어떻게 될까? 각 작업은 여전히 자기 시작부터 약 3초 경과 시간으로 끝내는 코드다. 아직 8개 실행은 안내·확인 전이며 기존 lab·단일 배치·최대 8개 제한을 유지한다.


## 26. worker 8개 예상·경과 시간과 CPU 시간 보충 — 2026-11-20

학습자의 실행 전 예상: us는 100%에 머무르되 약 6초간 유지될 수 있고, r은 8 근처에서 점차 줄며, worker마다 calculate()를 3초 실행하므로 iterations는 비슷할 것이라고 생각했다. 동시에 OS가 번갈아 실행한다면 while 도중에도 다른 스레드가 CPU를 쓸 수 있어 iterations가 줄 수 있지 않느냐고 질문했다. 이 예상과 의문을 실제 결과와 구분해 보존한다.

보충: while나 메서드 호출은 CPU 독점 구간이 아니다. 일반 플랫폼 worker는 메서드·반복문 실행 도중에도 Linux 스케줄링에 의해 실행 차례를 넘기고 나중에 이어갈 수 있다. System.nanoTime 차이는 그 호출이 시작된 뒤의 경과 시간이며 해당 스레드의 누적 CPU 사용 시간이 아니다. CPU 차례를 기다리는 중에도 경과 시간은 흐른다. 다시 CPU를 받아 조건을 검사할 때 3초가 지났으면 루프를 끝낸다. 정리·시작 편차와 재스케줄 대기로 전체 시간은 3초를 조금 넘을 수 있다.

설명용 이상화 예시(실측 아님): CPU 4개에서 worker 4개는 3초 경과 동안 각각 약 3초 CPU를 사용할 여지가 있다. worker 8개가 공평하게 나눠 쓰면 3초 경과 동안 각각 약 1.5초 CPU를 쓰고 나머지를 실행 차례 대기로 보낼 수 있다. 따라서 개별 iterations가 대략 감소하고 합계는 4개일 때와 비슷할 수 있다는 검증 가설을 세운다. 정확한 분배나 반복 횟수는 보장하지 않는다. 6초 예상은 각 작업에 실제 CPU 3초 또는 같은 작업량을 반드시 보장하는 다른 실험에서는 합리적일 수 있으나 현재 코드는 경과 시간 제한이다.

pool 크기와 작업 수 모두 8이므로 4개 작업을 모두 끝낸 뒤 다음 4개를 시작하는 큐 구성으로 해석하지 않는다. 부하 중 r은 8 근처일 수 있고 다른 작업·시작·종료 시점 때문에 정확한 상수는 아니다.

기존 lab 앱 PID 4571과 전용 Linux 환경, 단일 배치·최대 8개·각 worker 약 3초 제한을 유지한다. 설명 후 아래 제한된 비교를 안내한다. 현재 실제 8개 결과는 아직 미확인이다.

```bash
vmstat -w 1 8 > /tmp/scheduler-02-threads-8-vmstat.txt &
sleep 1
curl --max-time 10 --write-out '\nHTTP %{http_code}, total %{time_total}s\n' --request POST 'http://localhost:8080/lab/scheduler/cpu?threads=8'
wait
cat /tmp/scheduler-02-threads-8-vmstat.txt
```

같은 Linux 관찰·요청 터미널의 임의 폴더에서 한 번에 실행한다. 바뀐 것은 threads=8과 저장 파일명이며 같은 파일이 있으면 덮어쓴다. vmstat는 8개 보고서 뒤 자동 종료, curl 최대 10초. 중단 필요 시 앱 터미널 Ctrl+C, curl 종료가 서버 계산 종료를 보장하지 않음은 이전과 같다. 응답 worker 8개·elapsedMillis·각 iterations와 vmstat r·us·id·cs를 기록할 예정이다. cs 설명과 해석은 실제 결과를 받은 뒤 진행한다.

근거: Java 21 System.nanoTime(경과 시간 측정), 이전 스레드 덤프의 elapsed·cpu 구분. 부하 실행과 이해 완료는 아직 확인하지 않았다.


## 27. worker 8개 실제 결과·학습자 해석 — 2026-11-20

```json
{"threads":8,"elapsedMillis":3008,"workers":[{"threadName":"scheduler-cpu-1","iterations":75598192,"value":-7138084852027542783},{"threadName":"scheduler-cpu-2","iterations":75384427,"value":-6740047657680888058},{"threadName":"scheduler-cpu-3","iterations":67829482,"value":948690848901770529},{"threadName":"scheduler-cpu-4","iterations":75487531,"value":-5937669301310733562},{"threadName":"scheduler-cpu-5","iterations":67828322,"value":7908996246562095777},{"threadName":"scheduler-cpu-6","iterations":67920091,"value":4229228089780452358},{"threadName":"scheduler-cpu-7","iterations":69956415,"value":-1447706185531681594},{"threadName":"scheduler-cpu-8","iterations":69803325,"value":-8442177289905097370}]}
```

```text
HTTP 200, total 3.011410s
[1]+ Done vmstat -w 1 8 > /tmp/scheduler-02-threads-8-vmstat.txt
```

| 자료 행 | r (개) | us (%) | sy (%) | id (%) | cs (회/초) |
| --- | --- | --- | --- | --- | --- |
| 1 | 1 | 0 | 0 | 100 | 0 |
| 2 | 0 | 0 | 0 | 99 | 850 |
| 3 | 8 | 99 | 0 | 1 | 1867 |
| 4 | 8 | 100 | 0 | 0 | 1991 |
| 5 | 8 | 100 | 0 | 0 | 1998 |
| 6 | 0 | 2 | 0 | 98 | 916 |
| 7 | 0 | 1 | 0 | 99 | 752 |
| 8 | 0 | 0 | 0 | 99 | 746 |

r은 각 시점의 값, 첫 CPU·rate 행은 부팅 이후 평균, 이후 행은 직전 1초 구간이다. worker 결과 8개와 전체 경과 시간 약 3초, 계산 중 r=8·us=99~100%·id=0~1%, 이후 회복을 실제 출력으로 확인했다.

| 비교 항목 | worker 4개 | worker 8개 |
| --- | --- | --- |
| 서버 내부 경과 시간 | 3000ms | 3008ms |
| curl 전체 시간 | 3.004689s | 3.011410s |
| iterations 합계 | 569182414 | 569807785 |
| worker당 iterations 평균 | 142295603.5 | 71225973.125 |
| 계산 중 r | 4 | 8 |
| 계산 중 cs | 759~858회/초 | 1867~1998회/초 |

합계는 이번 한 번의 비교에서 약 0.11% 차이로 거의 같고 worker당 평균은 약 절반이었다. 이를 엄밀한 벤치마크나 일반적인 처리량 보장으로 확대하지 않는다.

학습자는 반복 횟수가 거의 절반이 된 것을 읽고, 경과 시간은 계속 흐르며 기능 실행 내내 CPU를 쓰는 것은 아니라고 자기 말로 설명했다. CPU 수보다 많은 작업이 들어오면 번갈아 CPU를 사용하면서 같은 시간의 성능이 나빠질 수 있다고 해석했다. 튜터 보충: 이번처럼 CPU 계산이 주된 작업에서 CPU가 포화되면 개별 작업의 몫은 줄 수 있지만, 전체 처리량까지 반드시 감소하는 것은 아니다. 이번 합계는 거의 같았다. I/O 대기가 많은 작업은 동시성을 늘려 CPU 유휴를 활용할 수도 있으므로 모든 작업에 일반화하지 않는다. 작업량 고정 실험에서는 실행 대기로 완료 시간이 늘 수 있고, 현재 시간 고정 실험에서는 개별 반복 횟수가 줄었다.

### cs 첫 설명

cs는 context switch(문맥 전환)의 초당 횟수이며 서버 전체 지표다. CPU가 실행하던 작업의 상태를 보관하고 다른 작업의 실행 상태로 바꾸는 동작으로 설명한다. 이번 4개·8개 계산 구간에서 각각 759~858회/초, 1867~1998회/초가 관찰되어 증가했다. 모든 증가분이 이 앱만의 활동이라고 확정하지 않고, cs 하나만으로 병목이나 성능 악화를 단정하지 않는다. 근거: [vmstat 매뉴얼](https://man7.org/linux/man-pages/man8/vmstat.8.html).

다음은 cs 설명에 대한 질문·이해 확인, 입력 상한·중복 실행 제한의 실제 동작 확인과 마무리 퀴즈다. API의 400·429는 소스 검토로만 확인한 상태이며 해당 새 엔드포인트의 실제 응답은 아직 미확인이다. 학습 전체 완료·앱 종료는 아직 전이다.


## 28. 입력 상한 검증 안내

기존 lab 앱 PID 4571을 유지한 상태에서 별도 Linux 관찰 터미널의 임의 폴더에서 아래 명령을 실행하도록 안내한다. `--request POST`는 HTTP 메서드, `-i`/`--include`는 응답 헤더 포함, `--max-time 5`는 curl 전체 요청 제한 5초다. 한 번 출력 후 종료한다. `threads=9`는 최대 허용 8개를 넘으므로 소스상 400을 예상하며 실제 응답과 실행 시점은 아직 미확인이다. 계산을 시작하기 전 입력 검사로 거절하도록 작성되어 있다.

```bash
curl --include --max-time 5 --request POST 'http://localhost:8080/lab/scheduler/cpu?threads=9'
```

다음은 학습자가 실제 상태 코드·응답을 제공한 뒤 중복 실행 거절을 확인한다.


### 입력 상한 실제 결과

학습자가 Linux `root@backend-lab:/home/skl#`에서 안내한 POST threads=9 명령과 결과를 제공했다. 2026-11-20T04:28:31.884Z 응답은 HTTP/1.1 400, `{"timestamp":"2026-11-20T04:28:31.884Z","status":400,"error":"Bad Request","path":"/lab/scheduler/cpu"}`였다. 예상한 400이 확인되었고 응답 본문에 상세 사유 문자열은 표시되지 않았다. 응답만으로 worker 생성 여부를 직접 측정하지는 않았다. 소스에서는 범위 검사가 executor 생성보다 앞선다.

## 29. 중복 실행 제한 검증 안내

Linux의 같은 관찰·요청 터미널에서 다음 블록을 한 번에 실행하도록 안내한다. 첫 요청 threads=1의 응답은 파일에 저장해 다른 출력과 구분하고 백그라운드로 시작한다. 셸 sleep 0.5 이후 두 번째 요청을 보내 중복 실행 시 429를 기대한다. wait 후 첫 응답을 출력한다. 0.5초는 요청 시작을 보장하는 동기화가 아니므로 실제 결과로 중첩 여부를 판단한다. 같은 파일은 덮어쓴다. 앱 자체의 계산 제한은 약 3초이고 curl 중단은 서버 중단을 보장하지 않는다. 예상과 안내 단계이며 실제 요청·429·첫 응답은 아직 미확인이다.

```bash
curl --include --max-time 10 --request POST 'http://localhost:8080/lab/scheduler/cpu?threads=1' > /tmp/scheduler-first-response.txt &
sleep 0.5
curl --include --max-time 5 --request POST 'http://localhost:8080/lab/scheduler/cpu?threads=1'
wait
cat /tmp/scheduler-first-response.txt
```

두 번째 응답이 먼저 화면에 보이고 첫 응답은 cat으로 마지막에 출력된다. 다음은 두 결과를 받아 실제 429와 첫 요청 성공을 확인한다.


### 중복 실행 실제 결과

학습자는 Linux `root@backend-lab:/home/skl#`에서 안내한 두 요청 블록을 실행한 출력을 제공했다. 두 번째 요청에서 `HTTP/1.1 429`를 확인했고, 첫 요청 파일에서는 `HTTP/1.1 200`과 `{"threads":1,"elapsedMillis":3000,"workers":[{"threadName":"scheduler-cpu-1","iterations":142183908,"value":-1873728736005638335}]}`를 확인했다. 두 번째 응답 본문의 일부는 curl 진행 표시와 섞여 흐트러져 전체를 확인할 수 없었다. 서로 다른 curl의 출력·진행 표시가 같은 터미널에서 겹친 것으로 보충하며 완전한 두 번째 응답 본문을 추측해 복원하지 않는다.

## 30. 실험 종료 후 재진입 검증 안내

첫 요청이 끝난 뒤 running이 복원되어 새로운 요청이 성공하는지 확인한다. 동일 Linux 관찰·요청 터미널에서 `curl --silent --show-error --include --max-time 10 --request POST 'http://localhost:8080/lab/scheduler/cpu?threads=1'`을 실행하도록 안내한다. --silent는 진행 표시를 숨기고, --show-error는 그 상태에서도 오류를 보여준다. 나머지 옵션은 이전과 같다. worker 1개가 약 3초 계산한 후 HTTP 200을 예상하며 실제 요청·응답은 미확인이다. 앱 종료는 아직 전이다.


### 실험 종료 후 재진입 실제 결과

학습자가 단일 재요청 후 아래 JSON을 제공하고 성공했다고 보고했다.

```json
{"threads":1,"elapsedMillis":3000,"workers":[{"threadName":"scheduler-cpu-1","iterations":142190673,"value":4837466348109809446}]}
```

앞선 중복 실험이 끝난 뒤 새로운 worker 결과가 반환되어 정상 재진입을 확인했다. HTTP 상태 줄은 제공되지 않아 상태 코드 자체는 미확인이다. 안내한 --silent --show-error 옵션의 실제 명령 원문도 이번에는 제공되지 않았다.

## 31. SCHEDULER-02 마무리 퀴즈

1. 논리 CPU 4개 환경에서 worker를 4개에서 8개로 늘렸을 때 전체 us와 각 worker의 iterations가 어떻게 바뀌었고 왜 그런가?
2. worker 8개도 약 3초 만에 끝난 이유는 무엇이며, 3초의 경과 시간과 각 worker의 CPU 시간은 어떻게 다른가?
3. vmstat cs의 의미와 단위는 무엇이며, cs가 증가한 것만으로 무엇을 확정할 수 없는가?
4. threads=9 요청과 실험 중 중복 요청의 실제 HTTP 상태는 각각 무엇이었고, 실험 종료 후 다시 요청한 결과는 어떠했는가?

2026-11-20 재개 시 위 네 문항을 다시 제시한다. 다음 행동은 학습자가 자기 말로 답하는 것이다. 챕터는 진행 중이며 Linux 앱 종료와 Linux 작업 공간의 동기화 상태는 미확인이다.

### 마무리 퀴즈 답변과 보충 — 2026-11-20

학습자는 worker 4개와 8개 비교에서 전체 us가 약 100%로 유지되고 worker당 iterations가 거의 절반으로 줄었다고 답했다. calculate가 경과 시간으로 종료하기 때문에 8개도 약 3초에 끝나며 각 worker의 CPU 사용 시간은 줄었을 것으로 설명했다. CPU 시간 자체는 직접 측정하지 않았음을 보충했다.

cs는 context switching 횟수이며 증가만으로 성능 저하를 단정할 수 없다고 설명했다. I/O 대기와 관련된 문맥 전환도 예로 들었다. 튜터는 vmstat cs의 단위가 초당 횟수임을 보충했다.

threads=9는 HTTP 400, 중복 요청은 HTTP 429라고 답했다. 재요청의 상태를 200이라고 답했으나 당시 상태 줄은 제공되지 않았고 worker 결과 JSON으로 재진입만 확인했음을 보충했다. 학습자는 finally에서 running을 false로 복원하기 때문에 새 계산이 가능하다고 설명했다. 마무리 퀴즈 답변과 보충을 확인했다. Linux 앱 종료는 미확인이다.
