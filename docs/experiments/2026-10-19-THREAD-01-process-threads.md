# THREAD-01 — 프로세스 안의 Linux 스레드 목록

상태: 완료 (Linux 스레드 목록·JVM 덤프 관찰, 주요 역할 설명, 마무리 퀴즈 완료)

## 1. 질문과 실험 조건

- 날짜 / 환경 ID: 2026-10-19 KST / E01
- 오늘 알아볼 질문 하나: Java 프로세스 안에는 어떤 Linux 스레드들이 보이는가?
- 코드 커밋 / 변경 사항: 코드 변경 없음. 관찰만 수행.
- 앱·도구·DB 등 실제 버전: Linux 학습 VM, Spring Boot 앱 PID 66439, `ps` 버전 미확인. JDK 21 환경은 기존 E01 기록 기준이며 이번 `java -version`은 재확인하지 않음.
- vCPU / 메모리 / JVM 옵션 / GC / 스레드 설정: 이번 관찰에서 재확인하지 않음. 과거 환경 기록의 값이 이번 실행에도 동일한지는 미확인.
- 요청 수 / 동시성 / 실행 시간 / 입력 크기: 스레드 목록 확인만 수행; HTTP 요청 부하는 주지 않음.
- 이번에 바꿀 조건 하나: 없음. 정상 실행 중인 앱의 스레드 목록을 관찰.
- 실험 프로필 / 자원 상한 / 중단 조건 / 정리 방법: 앱의 상태를 변경하지 않는 읽기 전용 관찰. `ps`는 출력 후 종료됨.

## 2. 필요한 개념과 도구

- 프로세스: OS가 실행 중인 프로그램을 식별하고 관리하는 단위. 이 앱 프로세스 ID는 66439.
- 스레드: 프로세스 안에서 실행되는 개별 흐름. Linux는 각 스레드에 TID를 부여하며, 스레드 그룹의 대표 스레드 TID는 프로세스 PID와 같다.
- 명령 / 실행 위치 / 옵션 / 볼 항목 / 단위 / 종료 방법: Linux VM 관찰 터미널, 작업 폴더 `/home/skl/backend-system-lab`에서 `ps -L -p 66439 -o pid,tid,stat,comm` 실행. `-L`은 스레드를 표시하고, `-p`는 PID 66439로 범위를 제한하고, `-o`는 출력 열을 고른다. PID/TID는 식별자(단위 없음), STAT은 Linux 상태 코드, COMMAND/comm은 짧은 작업 이름. 출력 후 종료.
- STAT 해석 참고: `S`는 interruptible sleep, 소문자 `l`은 multithreaded 상태 표식이다. 따라서 `Sl`은 이 순간 Linux에서 대기 가능한 sleep 상태로 보이며, 해당 프로세스가 여러 스레드로 이루어졌다는 표시도 함께 있다. 이것은 Java `Thread.State`와 같은 값이 아니다. [procps ps 매뉴얼](https://man7.org/linux/man-pages/man1/procps.1.html).
- comm 해석 참고: Linux task의 `comm` 이름은 스레드마다 다를 수 있고 TASK_COMM_LEN 제한 때문에 최대 15바이트 이름 뒤가 잘릴 수 있다. `http-nio-8080-e`는 전체 Tomcat 스레드 이름이 아니라 짧게 잘린 이름이다. [proc_pid_comm 매뉴얼](https://man7.org/linux/man-pages/man5/proc_pid_comm.5.html).

## 3. 예상

- 바꾸기 전 무엇이 어떻게 달라질 것 같은가: 정상 앱에도 여러 스레드가 보일 것으로 예상.
- 그렇게 생각한 이유: 요청 처리 외에도 JVM과 웹 서버가 각자 일을 진행하거나 대기해야 한다.

## 4. 실행 순서와 재현 절차

| 순서 | 컴퓨터·터미널 / 작업 폴더 | 정확한 명령·키 조작 | 목적·볼 항목 | 실제 결과·확인 상태 |
| --- | --- | --- | --- | --- |
| 1 | Linux VM, 프로젝트 터미널 `/home/skl/backend-system-lab` | `jps -l` | Java 프로세스 목록에서 앱 PID 식별 | 사용자가 앱 클래스 `com.shinkwang.backendsystemlab.BackendSystemLabApplication`의 PID가 66439라고 제공. 같은 대화에서 앞서 66384라고 했다가 66439로 정정함. 현재 PID는 66439로 기록. |
| 2 | Linux VM 관찰 터미널, `/home/skl/backend-system-lab` | `ps -L -p 66439 -o pid,tid,stat,comm` | 대상 프로세스 내 Linux 스레드 목록, PID/TID/STAT/comm 관찰 | 사용자가 아래 전체 출력을 제공. 35개 행, PID는 모두 66439, TID는 각 행에서 다르고 첫 TID는 66439. 모든 STAT은 `Sl`. `COMMAND`에는 `java`, GC/G1/VM/Compiler/Catalina/`container-0`/`http-nio-8080-*` 이름이 보임. |
| 3 | Linux VM 관찰 터미널, `/home/skl/backend-system-lab` | `jcmd 66439 Thread.print` | Java 수준의 전체 스레드 이름, Java 상태, 스택을 확인하여 Linux `comm`과 대조 | 사용자가 출력 제공. 덤프 시각 `2026-10-19 00:57:21`, OpenJDK `21.0.12.1+1-1-26.04.4-Ubuntu`. executor `http-nio-8080-exec-1`의 native ID 66469가 `ps` TID 66469와 일치하며 Java 상태 `WAITING (parking)`, 스택에 `LinkedBlockingQueue.take`와 `ThreadPoolExecutor.getTask`가 있음. Poller 66479 및 Acceptor 66480은 Java 상태 `RUNNABLE`이나 각각 `EPoll.wait`/`Net.accept` native 호출 스택이 보임. |

실제 `ps` 출력:

```text
    PID     TID STAT COMMAND
  66439   66439 Sl   java
  66439   66440 Sl   java
  66439   66444 Sl   GC Thread#0
  66439   66445 Sl   G1 Main Marker
  66439   66446 Sl   G1 Conc#0
  66439   66447 Sl   G1 Refine#0
  66439   66448 Sl   G1 Service
  66439   66449 Sl   VM Periodic Tas
  66439   66450 Sl   VM Thread
  66439   66451 Sl   Reference Handl
  66439   66452 Sl   Finalizer
  66439   66453 Sl   Signal Dispatch
  66439   66454 Sl   Service Thread
  66439   66455 Sl   Monitor Deflati
  66439   66456 Sl   C1 CompilerThre
  66439   66457 Sl   Notification Th
  66439   66458 Sl   Common-Cleaner
  66439   66461 Sl   GC Thread#1
  66439   66462 Sl   GC Thread#2
  66439   66463 Sl   GC Thread#3
  66439   66466 Sl   Catalina-utilit
  66439   66467 Sl   Catalina-utilit
  66439   66468 Sl   container-0
  66439   66469 Sl   http-nio-8080-e
  66439   66470 Sl   http-nio-8080-e
  66439   66471 Sl   http-nio-8080-e
  66439   66472 Sl   http-nio-8080-e
  66439   66473 Sl   http-nio-8080-e
  66439   66474 Sl   http-nio-8080-e
  66439   66475 Sl   http-nio-8080-e
  66439   66476 Sl   http-nio-8080-e
  66439   66477 Sl   http-nio-8080-e
  66439   66478 Sl   http-nio-8080-e
  66439   66479 Sl   http-nio-8080-P
  66439   66480 Sl   http-nio-8080-A
```

`jcmd` 출력에서 비교한 실제 블록 일부:

```text
"http-nio-8080-exec-1" #29 [66469] daemon prio=5 ... nid=66469 waiting on condition
   java.lang.Thread.State: WAITING (parking)
   at jdk.internal.misc.Unsafe.park(...)
   at java.util.concurrent.LinkedBlockingQueue.take(...)
   at org.apache.tomcat.util.threads.TaskQueue.take(...)
   at org.apache.tomcat.util.threads.ThreadPoolExecutor.getTask(...)

"http-nio-8080-Poller" #39 [66479] daemon prio=5 ... nid=66479 runnable
   java.lang.Thread.State: RUNNABLE
   at sun.nio.ch.EPoll.wait(Native Method)
   at sun.nio.ch.EPollSelectorImpl.doSelect(...)
   at java.nio.channels.Selector.select(...)
   at org.apache.tomcat.util.net.NioEndpoint$Poller.run(...)

"http-nio-8080-Acceptor" #40 [66480] daemon prio=5 ... nid=66480 runnable
   java.lang.Thread.State: RUNNABLE
   at sun.nio.ch.Net.accept(Native Method)
   at sun.nio.ch.ServerSocketChannelImpl.accept(...)
   at org.apache.tomcat.util.net.Acceptor.run(...)
```

덤프에 나온 Java `Thread` 항목(25개)을 역할별로 분류한 표. 역할 설명은 이름과 출력된 스택을 바탕으로 한 해석이며, 내부 구현 세부사항까지 이 출력만으로 확정하지 않는다.

| 그룹 | 덤프의 이름 / Linux TID | Java 상태 | 출력에서 확인한 일·대기 지점 |
| --- | --- | --- | --- |
| JDK | `Reference Handler` / 66451 | RUNNABLE | reference 처리 전용 스레드. 스택에 `Reference.waitForReferencePendingList` native method가 보임. RUNNABLE만으로 CPU 실행 중이라고 판단하지 않음. |
| JDK | `Finalizer` / 66452 | WAITING (on object monitor) | NativeReferenceQueue의 lock/queue에서 대기. |
| JDK | `Signal Dispatcher` / 66453 | RUNNABLE | 스택 없음. 이름상 JVM 신호 처리용. |
| JDK | `Service Thread` / 66454 | RUNNABLE | JVM service thread. 이 순간의 구체적 작업은 출력만으로 확인 안 됨. |
| JDK | `Monitor Deflation Thread` / 66455 | RUNNABLE | JVM monitor 관리용 이름. 이 순간의 구체적 작업은 출력만으로 확인 안 됨. |
| JDK | `C1 CompilerThread0` / 66456 | RUNNABLE | `No compile task`라고 출력됨. JIT C1 컴파일 작업이 이 순간에는 없음. |
| JDK | `Notification Thread` / 66457 | RUNNABLE | JVM notification용 이름. 스택 없음. |
| JDK | `Common-Cleaner` / 66458 | TIMED_WAITING (parking) | Cleaner 작업 대기. 스택에 `ReferenceQueue.remove`가 보임. |
| Tomcat | `Catalina-utility-1` / 66466 | TIMED_WAITING (parking) | `ScheduledThreadPoolExecutor`의 지연 작업 큐에서 대기. |
| Tomcat | `Catalina-utility-2` / 66467 | WAITING (parking) | `ScheduledThreadPoolExecutor` 작업 큐에서 대기. |
| Tomcat | `container-0` / 66468 | TIMED_WAITING (sleeping) | `Thread.sleep` → `StandardServer.await` 스택. Tomcat server lifecycle 대기 흐름. |
| Tomcat worker | `http-nio-8080-exec-1` / 66469 | WAITING (parking) | `LinkedBlockingQueue.take` → `TaskQueue.take` → `ThreadPoolExecutor.getTask`. 다음 HTTP 작업을 기다리는 worker. |
| Tomcat worker | `http-nio-8080-exec-2` / 66470 | WAITING (parking) | `ThreadPoolExecutor.getTask` 계열에서 작업 대기. |
| Tomcat worker | `http-nio-8080-exec-3` / 66471 | WAITING (parking) | `ThreadPoolExecutor.getTask` 계열에서 작업 대기. |
| Tomcat worker | `http-nio-8080-exec-4` / 66472 | WAITING (parking) | `ThreadPoolExecutor.getTask` 계열에서 작업 대기. |
| Tomcat worker | `http-nio-8080-exec-5` / 66473 | WAITING (parking) | `ThreadPoolExecutor.getTask` 계열에서 작업 대기. |
| Tomcat worker | `http-nio-8080-exec-6` / 66474 | WAITING (parking) | `ThreadPoolExecutor.getTask` 계열에서 작업 대기. |
| Tomcat worker | `http-nio-8080-exec-7` / 66475 | WAITING (parking) | `ThreadPoolExecutor.getTask` 계열에서 작업 대기. |
| Tomcat worker | `http-nio-8080-exec-8` / 66476 | WAITING (parking) | `ThreadPoolExecutor.getTask` 계열에서 작업 대기. |
| Tomcat worker | `http-nio-8080-exec-9` / 66477 | WAITING (parking) | `ThreadPoolExecutor.getTask` 계열에서 작업 대기. |
| Tomcat worker | `http-nio-8080-exec-10` / 66478 | WAITING (parking) | `ThreadPoolExecutor.getTask` 계열에서 작업 대기. |
| Tomcat connector | `http-nio-8080-Poller` / 66479 | RUNNABLE | `EPoll.wait` → selector wait → `NioEndpoint$Poller.run`. Linux `ps`에서는 `Sl`; 소켓 이벤트를 기다리는 native 흐름. |
| Tomcat connector | `http-nio-8080-Acceptor` / 66480 | RUNNABLE | `Net.accept` → `ServerSocketChannelImpl.accept` → `Acceptor.run`. Linux `ps`에서는 `Sl`; 새 연결을 기다리는 흐름. |
| JVM lifecycle | `DestroyJavaVM` / 66440 | RUNNABLE | `waiting on condition`, Java stack 없음. 이름만으로 JVM이 지금 종료 중이라고 단정할 수 없음. |
| JVM diagnostic | `Attach Listener` / 66748 | RUNNABLE | `jcmd` 출력에 함께 나타남. elapsed 0.10s. 앞선 `ps` 시점에는 없었던 진단 attach 관련 스레드로 보임. |

HotSpot의 Java `Thread` 목록 밖에서 덤프 마지막에 따로 출력된 native JVM 스레드 10개:

| 그룹 | 덤프 이름 / Linux TID | HotSpot 출력 상태 | 화면에서 확인되는 역할 |
| --- | --- | --- | --- |
| G1 GC | `GC Thread#0` / 66444 | runnable | G1 수집기 작업 스레드 |
| G1 GC | `G1 Main Marker` / 66445 | runnable | G1 표시(marking) 관련 스레드 |
| G1 GC | `G1 Conc#0` / 66446 | runnable | G1 concurrent 작업 스레드 |
| G1 GC | `G1 Refine#0` / 66447 | runnable | G1 refine 작업 스레드 |
| G1 GC | `G1 Service` / 66448 | runnable | G1 service 작업 스레드 |
| JVM | `VM Periodic Task Thread` / 66449 | waiting on condition | JVM 주기 작업 스레드 |
| JVM | `VM Thread` / 66450 | runnable | VM 내부 작업 스레드 |
| G1 GC | `GC Thread#1` / 66461 | runnable | G1 수집기 작업 스레드 |
| G1 GC | `GC Thread#2` / 66462 | runnable | G1 수집기 작업 스레드 |
| G1 GC | `GC Thread#3` / 66463 | runnable | G1 수집기 작업 스레드 |

이 10개는 `java.lang.Thread.State` 행이 없는 HotSpot 내부 thread 정보다. `jcmd` 헤더의 `Threads class SMR info ... length=25`는 Java thread 목록 개수다. Linux `ps`는 OS thread를 보고, JVM dump는 Java Thread 목록과 HotSpot native thread 정보를 나누어 보여준다. 두 명령은 시각도 다르다. 실제로 `ps`에는 TID 66439가 있었지만 dump의 thread 항목에는 대응하는 이름/TID가 없고, dump에는 앞선 `ps`에 없던 Attach Listener 66748이 보인다. 이 두 출력만으로 TID 66439의 정확한 대응 대상을 확정하지 않는다.

## 5. 정상 상태와 변화 관찰

| 시각·관측 구간 | 조건 | 관측값·단위 | 사용한 도구 |
| --- | --- | --- | --- |
| 2026-10-19, 앱 정상 실행 중으로 보고됨 | Spring Boot PID 66439, 별도 부하 미보고 | 35 Linux thread rows; PID 66439 반복, 35개 고유 TID; 모든 STAT `Sl`; 명령 이름은 JVM 내부와 Tomcat 관련으로 보이는 이름을 포함 | `ps -L -p 66439 -o pid,tid,stat,comm` |
| 2026-10-19 00:57:21, JVM thread dump | 같은 Spring Boot PID 66439 | `http-nio-8080-exec-1` native ID/TID 66469, Java `WAITING (parking)` 및 `LinkedBlockingQueue.take` / `ThreadPoolExecutor.getTask`; Poller TID 66479와 Acceptor TID 66480은 Java `RUNNABLE`이면서 각각 `EPoll.wait`, `Net.accept`에서 관찰됨 | `jcmd 66439 Thread.print` |

## 6. 관찰 사실과 가설

- 출력에서 직접 확인한 사실: Linux `ps`가 PID 66439를 가진 여러 행을 표시한다. 각 행의 TID는 다르고, leader row는 PID와 TID 모두 66439다. STAT은 `Sl`이다. `COMMAND` 이름은 `http-nio-8080-e`처럼 짧게 보인다. JVM dump에서 `http-nio-8080-exec-1`의 `[66469]`와 `nid=66469`가 앞서 본 TID 66469와 일치했다.
- 그 사실에 대한 해석: `http-nio-8080-e`는 실제로 `http-nio-8080-exec-1` 접두부였다. 이 executor worker는 `WAITING (parking)` 상태이고 `LinkedBlockingQueue.take` 및 `ThreadPoolExecutor.getTask`에서 작업을 기다리는 것으로 보인다. Poller는 `EPoll.wait`에서 소켓 이벤트를 기다리고, Acceptor는 `Net.accept`에서 새 연결을 기다린다. 사용자는 Acceptor가 새 TCP 연결을 받고, Poller가 연결된 소켓의 이벤트를 기다리며, executor worker가 큐의 처리 작업을 받아 요청을 처리한다고 설명했다. 이어 JVM의 Thread 상태와 Linux의 OS thread 상태는 엄밀히 다른 관점이며, 이 스냅샷에서 Poller/Acceptor는 Java `RUNNABLE`이면서 Linux `Sl`로 보일 수 있다고 설명했다. 보충: Java `RUNNABLE`은 JVM이 “연결을 기다린다”고 표현한 상태라기보다, Java 수준에서 `WAITING`/`BLOCKED` 등으로 표시되지 않은 상태다. `RUNNABLE`만으로 실제 CPU 사용을 단정할 수 없다. [Java 21 Thread.State 문서](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Thread.State.html).
- 아직 판단할 수 없는 것: 마무리 퀴즈를 통한 용어·출력 해석의 재확인, Linux TID 66439가 이 JVM dump에서 직접 대응되지 않은 이유.

## 7. 검증

- A와 B를 구분하려고 추가로 관찰하거나 바꾼 조건: `jcmd 66439 Thread.print`에서 thread 이름·native ID·상태·스택을 읽어 `ps -L` 결과와 대조.
- 실제 결과: `http-nio-8080-exec-1`의 native ID/TID가 66469로 일치했다. `exec-1`은 `WAITING (parking)` 상태로 Tomcat `TaskQueue` / `ThreadPoolExecutor.getTask`에서 대기. `Poller` TID 66479는 `RUNNABLE`이지만 `EPoll.wait`, `Acceptor` TID 66480은 `RUNNABLE`이지만 `Net.accept`에서 관찰됨.
- 지지되거나 약해진 가설 / 남은 불확실성: 이름 접두부가 요청 worker라는 추정이 확인됐다. Java 상태와 Linux 상태는 서로 다른 관점이며, 상태만으로 CPU 사용을 단정할 수 없다. 스냅샷 한 번만으로 평소 동작 전부나 장애 여부를 판정하지 않는다. Linux TID 66439와 jcmd 출력 사이에 직접 대응이 없고, jcmd 시점에 Attach Listener 66748이 새로 나타났다.

## 8. 원인과 해결·재검증

해당 없음: 장애 재현이 아닌 정상 thread 목록 관찰이다.

## 9. 배운 내용

- 내 말로 설명하는 핵심: 리스닝 소켓은 새 연결을 기다리는 역할을 유지하고, 연결이 받아들여질 때 연결 소켓이 별도로 생긴다. TCP 연결 하나는 서버와 클라이언트 쪽 소켓이라는 양 끝점 사이의 1:1 관계다. 3-way handshake는 양쪽 운영체제의 TCP 기능이 SYN, SYN+ACK, ACK를 주고받아 연결을 맺는 과정이며, ACK는 HTTP 요청을 받았다는 뜻이 아니다. THREAD-01 덤프에서 Acceptor는 새 연결을 받고, Poller는 연결된 소켓 이벤트를 기다리며, executor worker는 큐에서 요청 처리 작업을 기다린다고 설명했다. Java의 `RUNNABLE`과 Linux `Sl`은 다른 관점이라 같은 스레드에서 동시에 관찰될 수 있음을 설명했다.
- 처음 예상과 달랐던 점: Java 프로세스 하나 안에 GC/G1/VM, 컴파일러, Tomcat 관련 이름을 가진 많은 Linux thread가 동시에 보였다.
- 아직 모르는 점: Linux TID 66439와 JVM dump의 직접 대응 대상. 이번 출력만으로는 확정하지 않는다.
- 다음 작은 실험: THREAD-02 시작 전 직전 두 세부 챕터의 복습 퀴즈에 답한 뒤 Java 스레드 상태를 배운다.

## 10. 챕터 마무리 퀴즈와 학습자 답

상태: 완료. 아래는 학습자가 답한 내용의 요약이며, 보충 설명은 오답 표시가 아니라 용어를 정확히 다듬기 위해 기록한다.

| 질문 | 학습자 답 요약 | 확인·보충 |
| --- | --- | --- |
| `ps`에서 PID와 TID가 다른 이유, `Sl`의 뜻은? | PID는 프로세스 ID, TID는 스레드 ID이며 한 프로세스에 여러 스레드가 있을 수 있다고 설명. `S`는 잠든 대기 상태, `l`은 여러 스레드가 있는 상태로 기억함. | 맞음. `l`은 Linux `ps`의 multithreaded 표시이지 “멀티프로세스 스레드”라는 뜻은 아니다. `S`는 interruptible sleep을 나타낸다. 스레드를 worker라고 부르는 것은 특정 역할의 비유이며, 모든 스레드의 정의는 아니다. |
| Acceptor, Poller, `exec-1`의 역할은? | Acceptor는 새 연결을 기다리고, Poller는 연결 소켓 이벤트를 확인해 작업 큐로 보내며, `exec-1`은 큐의 작업을 처리한다고 설명. | 역할 구분이 맞음. `exec-1`은 관찰 시점에 큐에서 작업을 기다리고 있었으며, 작업이 오면 가져와 처리한다. Poller는 이벤트를 감지해 소켓 처리 작업을 실행기에 전달한다. |
| JVM `RUNNABLE`과 Linux `Sl`만 보고 CPU 사용을 단정할 수 있는가? | 단정할 수 없다고 답함. Java 상태와 Linux 상태는 서로 다른 관점이며 Linux에서 잠들어 기다릴 수 있다고 설명. | 맞음. `RUNNABLE`은 CPU를 계속 쓰고 있다는 보장이 아니다. `Sl`은 해당 관측 시점의 Linux 상태이며, 단 한 번의 스냅샷만으로 시간 전체의 CPU 사용량을 판단하지 않는다. |
| 잘린 `comm`을 더 자세히 보고 Linux TID와 JVM 스레드를 어떻게 대조하는가? | `ps -L -p <PID> -ww -o pid,tid,stats,comm` 형태를 제안함. | 열 이름은 `stats`가 아니라 `stat`이다. `-ww`는 출력 폭 제한을 완화하지만 Linux `comm`의 15바이트 한계는 바꾸지 않는다. 전체 Java 스레드 이름은 `jcmd <PID> Thread.print`에서 확인하고 Linux TID와 덤프의 native ID(`nid`, 대괄호 안 ID)를 대조한다. 실제 기록에서는 TID/nid 66469가 일치했다. |

퀴즈 확인으로 THREAD-01 완료 기준을 충족했다. Linux TID 66439와 JVM dump의 직접 대응 대상은 이번 출력만으로 확인하지 못한 상태로 남긴다.
