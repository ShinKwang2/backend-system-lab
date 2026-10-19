# THREAD-02 — 시간을 기다리는 스레드

상태: 완료 (관찰·자기 설명·마무리 퀴즈 완료, 앱·top 종료 사용자 확인, 기록 커밋·푸시 전)

## 1. 질문과 실험 조건

- 날짜 / 환경 ID: 2026-10-19 KST / E01. Linux 앱이 lab 프로필과 PID 67745로 실행됐다고 학습자가 보고함. 기동 로그 원문은 미제공.
- 오늘 알아볼 질문 하나: 응답까지 같은 시간이 걸려도 계산 중인 스레드와 시간을 기다리는 스레드는 CPU 사용과 덤프 상태가 같을까?
- 코드 커밋 / 변경 사항: `2d97c81 feat(thread): add bounded sleep lab`이 Mac의 main과 origin/main에 반영된 것을 확인. ThreadLabController에서 lab 프로필, GET 매핑, compareAndSet 진입 제한, Thread.sleep(3000L), 인터럽트 표시 복원과 HTTP 503 예외, finally의 running 초기화를 소스로 확인함. 정상 응답 문자열은 현재 `Sleep lab ready`다. Mac 컴파일 결과 원문과 Linux pull·실행 코드의 커밋 해시는 미확인. Linux lab 앱 기동은 사용자 보고로 확인함.
- 실제 버전·자원·JVM 옵션: 기존 E01 기록 참고. 이번 실행 조건은 아직 미확인.
- 요청 수 / 동시성 / 실행 시간 / 입력 크기: sleep API를 단일 요청으로 1회 호출한 출력 확인. 소스로 확인한 제한은 `lab` 프로필 전용 `GET /lab/thread/sleep`, sleep 인자 3000밀리초 고정, 동일 JVM 내 동시 sleep 요청 1개, 추가 요청 HTTP 429다. 시간 인자를 외부에서 받지 않는다. 정상 응답 HTTP 200·3.003086초는 관찰했고, 추가 요청 제한·인터럽트 동작은 이번 실험에서 재현하지 않았다. 실험 후 학습자가 앱·top 종료까지 완료했다고 확인했다.

## 2. 필요한 개념과 시작 복습 퀴즈

직전 두 세부 챕터 MEMORY-02와 THREAD-01에서 배운 내용을 질문했다. 아래 보충 사항은 튜터 설명이며 학습자의 최초 답과 구분한다.

| 질문 | 학습자 답 요약 | 보충할 점 |
| --- | --- | --- |
| VIRT, RES와 JVM Heap의 관계는? | VIRT는 가상 메모리 주소, RES는 메모리 사용량이며 Metaspace 등도 포함해 Heap보다 넓은 범위를 본다고 설명. | VIRT 값은 주소 그 자체가 아니라 가상 주소 공간의 크기다. RES는 현재 RAM에 올라온 프로세스 페이지의 크기다. Heap used와 측정 기준이 달라 RES가 Heap used보다 항상 큰 수라고 단정하지 않는다. |
| Heap used와 RES의 차이는? | Heap used는 JVM Heap에서 사용 중인 메모리, RES는 Linux에서 보는 프로세스 메모리라고 설명. | 큰 범위 구분은 맞음. RES는 특히 현재 RAM에 상주하는 부분을 측정한다는 점을 보충한다. |
| 같은 PID와 여러 TID, Sl의 뜻은? | 프로세스에 여러 스레드가 있으므로 PID는 같고 TID는 다르며 S는 잠든 대기 상태, l은 여러 스레드를 가진 표시라고 설명. | 맞음. 명령의 출력 열 이름은 `stats`가 아니라 `stat`이며 화면의 헤더는 `STAT`이다. |
| Acceptor, Poller, exec 역할과 Java/Linux 상태 차이는? | Acceptor는 연결, Poller는 연결 소켓 이벤트를 기다리고 작업을 넘기며 exec은 큐의 작업을 실행한다고 설명. JVM RUNNABLE과 Linux Sl이 함께 나올 수 있다고 설명. | 역할과 관점 구분은 맞음. 이번 Poller/Acceptor는 native 호출 안에서 OS 이벤트를 기다릴 수 있다. Java RUNNABLE을 단순히 “대기하면서 실행할 수 있음”으로 정의하지 않고 상태와 스택을 함께 읽는다. |

기초 설명: `Thread.sleep(3000)`은 현재 스레드를 3000밀리초 동안 재우는 호출이다. 실제 재개 시각은 타이머 정밀도와 스케줄링 등에 영향을 받으며 인터럽트로 중단될 수도 있다. [Java 21 Thread.sleep](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Thread.html#sleep(long))

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

Mac에서 코드를 작성·검토한 뒤 Linux 앱 기동을 사용자 보고로 확인했다. 아래 표에서 안내와 실제 확인을 구분한다.

| 순서 | 컴퓨터·터미널 / 작업 폴더 | 명령·키 조작 | 목적·볼 항목 | 실제 결과·확인 상태 |
| --- | --- | --- | --- | --- |
| 1 | Linux VM 앱 실행 터미널 / `/home/skl/backend-system-lab` | 기존 앱이 실행 중이면 Ctrl+C 후 `./gradlew bootRun --args='--spring.profiles.active=lab'` 안내 | lab 프로필과 새 PID 확인. 명령은 서버가 실행되는 동안 유지하며 Ctrl+C로 종료 | 학습자가 lab 프로필 활성화와 PID 67745를 보고함. 실행 명령·로그 원문은 미제공. |
| 2 | Linux VM 관찰 터미널 / `/home/skl` (`~` 프롬프트로 확인) | `top -d 1 -p 67745` 실행 확인. Irix mode On 설정 안내는 했으나 키 조작 결과는 미제공. 종료는 q | `-d 1`: 1초 간격, `-p`: 앱 PID 제한. 앱 행의 %CPU 기준값. Irix On은 CPU 하나 사용을 100%로 표시 | 화면 시각 06:50:46(시간대 미확인), PID 67745의 %CPU 0.0, S 값 S. 학습자가 대부분 0.0이고 가끔 2.0까지 잠깐 오른다고 보고함. 현재 Irix 모드는 미확인. |
| 3 | Linux VM 요청 터미널 / `/home/skl/backend-system-lab` (실제 프롬프트로 확인) | `curl --max-time 10 -w '\nHTTP %{http_code}, total %{time_total}s\n' http://localhost:8080/lab/thread/sleep` | top을 유지한 채 GET 1회, 응답 본문·HTTP 코드·전체 소요 시간(초) 및 요청 중 앱 %CPU 관찰. curl은 응답 후 종료하며 클라이언트 대기 상한은 10초 | 실제 출력 `Sleep lab ready` / `HTTP 200, total 3.003086s`. 학습자는 앱 CPU가 잠깐 1.0%로 오른 뒤 바로 0.0%로 돌아왔다고 보고함. |
| 4 | Linux VM 요청 터미널 / 임의 폴더 가능 | 아래 백그라운드 요청·덤프 저장 명령 3줄 | 3초 요청 중 덤프를 포착한 뒤 파일로 천천히 읽기 | 실행 보고 후 실제 덤프 발췌 제공. exec-5/TID 67779가 TIMED_WAITING (sleeping)이며 ThreadLabController.sleep 28행을 확인함. |
| 5 | Linux VM 터미널 / 임의 폴더 가능 | `grep -B 8 -A 5 'ThreadLabController' /tmp/thread-02-sleep-67745.txt` 안내. 학습자는 cat으로도 읽었다고 보고함(정확한 cat 명령은 미제공) | 클래스 이름이 포함된 줄과 앞 8줄·뒤 5줄을 출력해 스레드 이름·상태·스택 확인. 출력 후 종료 | 학습자가 exec-5와 TIMED_WAITING을 스스로 찾았다고 설명하고 아래 실제 스택을 제공함. |
| 6 | Linux VM 관찰 터미널 / `/home/skl/backend-system-lab` (실제 프롬프트로 확인), 추가 HTTP 요청 없이 실행하도록 안내 | `jcmd 67745 Thread.print > /tmp/thread-02-after-67745.txt` 후 `grep -A 20 '"http-nio-8080-exec-5"' /tmp/thread-02-after-67745.txt` | 요청이 끝난 뒤 같은 worker 상태와 스택 비교. -A 20은 일치한 줄 뒤 20줄 표시. 각 명령은 출력·저장 후 종료 | 실제 출력에서 같은 이름·Java ID #33·TID/nid 67779 확인. WAITING (parking), LinkedBlockingQueue.take → TaskQueue.take → ThreadPoolExecutor.getTask가 보임. 학습자가 WAITING (parking)이라고 직접 읽음. |
| 7 | Linux VM top 관찰 터미널·앱 실행 터미널, 확인용 터미널 / jps 작업 폴더는 임의 | top이 열려 있으면 q, 앱 실행 터미널에서 Ctrl+C, 확인용 터미널에서 `jps -l` 안내 | 관찰 도구·앱 종료 후 PID 67745의 앱이 남아 있는지 확인. jps는 출력 후 종료 | 학습자가 “종료까지 끝”이라고 확인함. 종료 완료는 사용자 보고로 기록하며 jps 출력 원문은 미제공. |

덤프 포착용 명령(학습자 실행 보고, 출력 내용 확인 전):

```bash
curl --max-time 10 -w '\nHTTP %{http_code}, total %{time_total}s\n' http://localhost:8080/lab/thread/sleep &
sleep 0.5
jcmd 67745 Thread.print > /tmp/thread-02-sleep-67745.txt
```

`&`는 curl 응답을 기다리지 않고 다음 셸 명령을 실행하도록 백그라운드 작업으로 시작한다. `sleep 0.5`는 서버에 요청이 도착할 여유를 주기 위해 셸을 0.5초 기다리게 하며 Java의 대기 호출과 별개다. `>`는 jcmd 출력을 화면 대신 지정 파일에 저장하고 같은 파일이 있으면 덮어쓴다. curl과 jcmd는 각각 작업이 끝나면 종료한다. 이 간격만으로 서버의 sleep 중 포착을 보장하지 않으므로 덤프의 실제 스택으로 확인한다. 이후 파일 읽기를 별도로 안내한다.

학습자가 0.5초 간격의 이유를 질문함. 보충: 백그라운드 curl과 jcmd는 실행 순서상 서버 도착·대기 진입 시점이 보장되지 않는다. 간격 없이 덤프를 요청하면 서버가 HTTP 요청을 처리하기 전일 수도 있어 짧게 여유를 주었다. 반대로 요청이 끝난 뒤 덤프를 얻으면 작업 큐 대기 모습으로 돌아갈 수 있어 3초 구간 안을 노린다. 0.5초는 TCP나 JVM에 정해진 규칙이 아니라 이번 실험의 포착 간격이다.

첫 코드 작성 확인: Mac 편집기에서 학습자가 `src/main/java/com/shinkwang/backendsystemlab/thread/ThreadLabController.java`를 만들었다. 실제 파일의 패키지, `@Profile("lab")`, `@RestController`, `AtomicBoolean running = new AtomicBoolean(false)` 필드를 확인했다. 해당 중간 단계에서는 컴파일·실행 전이었다.

중간 코드에서는 GetMapping 누락, 빈 catch, finally 앞 여분 중괄호를 확인하고 수정 방향을 안내했다. 이후 학습자가 수정한 실제 파일에서 GetMapping 경로, interrupt 표시 복원·HTTP 503 예외, 올바른 try/catch/finally 구조를 확인했다. 이후 Linux 기동·정상 API 응답 관찰까지 진행했다. Mac 컴파일 출력 원문은 별도로 제공되지 않았다.

## 5. 정상 상태와 변화 관찰

| 구간 | 조건 | 실제 관찰 | 근거 |
| --- | --- | --- | --- |
| 정상 상태 | PID 67745, 별도 sleep 요청 전 | 붙여넣은 top 화면에서 앱 %CPU 0.0. 학습자 보고로는 거의 0.0이며 가끔 2.0으로 잠깐 상승. 1초 갱신, Irix 모드 미확인 | `top -d 1 -p 67745` 출력과 학습자 설명 |
| sleep 요청 중·후 | 요청 1회 | `Sleep lab ready`, HTTP 200, 전체 3.003086초. top의 앱 %CPU는 잠깐 1.0이었다가 바로 0.0으로 돌아왔다는 사용자 보고 | 실제 curl 출력과 학습자 설명 |
| 덤프 포착용 추가 요청 중 | PID 67745, 별도 sleep API 요청 | exec-5/TID 67779, TIMED_WAITING (sleeping), Thread.sleep0 → Thread.sleep → ThreadLabController.sleep 28행 | 학습자가 제공한 덤프 발췌 |
| 요청 완료 후 덤프 | 같은 exec-5/Java ID #33/TID 67779 | WAITING (parking), Unsafe.park·LockSupport.park와 LinkedBlockingQueue.take → TaskQueue.take → ThreadPoolExecutor.getTask 스택. Controller.sleep은 보이지 않음 | 학습자가 제공한 요청 후 덤프 발췌 |

요청 중 실제 덤프 발췌:

```text
"http-nio-8080-exec-5" #33 [67779] daemon prio=5 os_prio=0 cpu=0.99ms elapsed=2554.36s tid=0x000076d650e1f9d0 nid=67779 waiting on condition  [0x000076d5e98f9000]
   java.lang.Thread.State: TIMED_WAITING (sleeping)
    at java.lang.Thread.sleep0(java.base@21.0.12.1/Native Method)
    at java.lang.Thread.sleep(java.base@21.0.12.1/Thread.java:509)
    at com.shinkwang.backendsystemlab.thread.ThreadLabController.sleep(ThreadLabController.java:28)
```

학습자는 exec-5와 TIMED_WAITING을 직접 발견하고, 자신의 Controller 메서드가 스택에 보인다는 점을 확인했다. 보충 설명: exec-5는 이름의 접미사이고 Linux TID는 67779다. 호출 스택은 위쪽이 현재 수행·대기 지점이며 아래쪽에 그 메서드를 호출한 코드가 이어진다. 이 발췌는 Controller.sleep에서 Thread.sleep을 호출한 상태를 보여준다. grep의 -B/-A는 일치한 줄 주변의 출력 범위를 정하며 원본 파일을 수정하지 않는다.

요청 완료 후 실제 덤프 발췌:

```text
"http-nio-8080-exec-5" #33 [67779] daemon prio=5 os_prio=0 cpu=2.29ms elapsed=4882.99s tid=0x000076d650e1f9d0 nid=67779 waiting on condition  [0x000076d5e98fb000]
   java.lang.Thread.State: WAITING (parking)
    at jdk.internal.misc.Unsafe.park(java.base@21.0.12.1/Native Method)
```

동일 블록에서 `LinkedBlockingQueue.take`(435행), `TaskQueue.take`(132행·32행), `ThreadPoolExecutor.getTask`(889행), `runWorker`(936행)도 확인했다. 튜터 해석: 이 worker는 종료된 것이 아니라 다음 작업이 큐에 들어오기를 기다리는 흐름으로 돌아왔다. parking은 실행을 멈추고 깨움 신호 등을 기다리는 방식이며, 여기서는 작업 큐 대기에 사용되고 있다.

### grep 옵션의 긴 이름

학습자가 약자의 긴 옵션 이름을 질문했다. `-B 8`은 `--before-context=8`, `-A 5`는 `--after-context=5`와 같다. Before/After는 일치한 줄의 앞/뒤를 뜻하며 context는 주변 줄을 뜻한다. 따라서 요청 후 명령은 다음처럼 쓸 수 있다.

```bash
grep --after-context=20 '"http-nio-8080-exec-5"' /tmp/thread-02-after-67745.txt
```

이 명령은 스레드 블록의 경계를 해석하지 않고 일치한 줄 뒤 20줄을 출력하므로 다음 exec-6의 일부도 포함됐다. 실제 grep 버전은 미확인이다. [GNU grep Context Line Control](https://www.gnu.org/s/grep/manual/html_node/Context-Line-Control.html)

이전 THREAD-01 출력은 당시의 기록이며 이번 실행의 정상 상태로 대체하지 않는다.

## 6. 관찰 사실과 가설

- 확인된 사실: 시작 복습 퀴즈에 학습자가 위와 같이 답함. Mac의 main과 origin/main은 `2d97c81`을 가리킴. Linux에서 lab 프로필과 PID 67745로 기동했다고 학습자가 보고함. 정상 top에서 앱 %CPU 0.0을 확인하고 대부분 0.0, 가끔 2.0으로 잠깐 오른다는 관찰을 보고함.
- 해석: 약 3초의 응답 시간과 낮은 CPU 관찰은 학습자의 예상과 부합한다. 잠깐 1.0%로 오른 구체적 작업은 이 출력만으로 구분하지 않는다.
- 추가 확인: 요청 진행 중 exec-5/TID 67779가 TIMED_WAITING (sleeping)이며 실제 Controller 28행에서 Thread.sleep을 호출하고 있었다.
- 요청 후 추가 확인: 동일 TID 67779의 worker가 WAITING (parking)이며 작업 큐에서 다음 작업을 기다리는 스택을 확인했다.
- 학습자 최종 설명: sleep 대기가 낮은 CPU의 이유이고 TIMED_WAITING (sleeping)이 이를 뒷받침한다고 설명했다. 동일 TID와 LinkedBlockingQueue.take를 근거로 요청 후 worker가 다음 작업을 기다린다고 설명했다.
- 남은 범위: 현재 Irix 모드·1초 샘플링의 제약을 고려하고 단일 관찰을 장기 특성으로 확대하지 않는다. 인터럽트·추가 요청 HTTP 429를 이번 Linux 실험에서 재현한 것은 아니다.

## 7. 검증

정상 CPU와 sleep 요청 중 CPU·응답 시간은 학습자가 직접 관찰했다. 요청 진행 중 exec-5의 TIMED_WAITING과 실제 Thread.sleep 호출 경로를 확인했다. 요청 후에는 같은 TID 67779가 WAITING (parking)과 작업 큐 대기 스택으로 바뀐 것을 확인했다.

## 8. 원인과 해결·재검증

해당 없음: 장애 해결이 아닌 대기 동작 관찰 실험이다. 정상 상태·요청 중·요청 후 스레드 상태 비교는 완료했다. 학습자가 앱과 top의 종료까지 완료했다고 확인했다. 당시 PID 67745와 TID 67779는 과거 실행의 값이며 다음 실행에서는 다시 식별한다.

## 9. 배운 내용

- 확인된 복습: 프로세스와 스레드 ID, Sl, Acceptor/Poller/worker 역할, JVM과 OS 상태의 관점 차이.
- 보충 설명: VIRT는 가상 주소 공간의 크기, RES는 현재 RAM 상주량, 명령의 출력 열 이름은 stat이다.
- 학습자의 자기 설명: sleep 대기와 낮은 CPU를 연결하고, 같은 TID와 큐 대기 메서드로 worker 생존·재사용을 설명했다. grep의 긴 옵션과 주변 줄 출력 범위도 설명했다.
- 다음 작은 행동: 관찰·퀴즈·종료 기록을 Mac에서 커밋·푸시하고 Linux에서 pull한다. 다음 THREAD-03 시작 전에는 THREAD-01·02 복습 퀴즈를 진행한다.
- 저장 상태: 기존 API·기록 커밋 `2d97c81`은 Mac의 origin/main까지 반영됨. 이번 실제 관찰·퀴즈 답 갱신은 로컬 미커밋 상태다.

## 10. 챕터 마무리 퀴즈

상태: 완료. 세 답 모두 정확하며 학습자의 답 요약을 아래에 남긴다.

1. 이번 API는 응답까지 약 3초가 걸렸는데 CPU 사용이 낮았다. 어떤 동작 때문이며, 덤프의 어떤 상태와 메서드가 그 설명을 뒷받침하는가?
2. 요청 후 exec-5는 종료됐는가, 다른 일을 기다리는가? 같은 스레드임을 보여주는 ID와 대기 중인 메서드를 근거로 설명하라.
3. grep의 -B와 -A를 긴 옵션 이름으로 풀어 쓰고, -A 20 결과에 다음 exec-6까지 나온 이유를 설명하라.

| 문항 | 학습자 답 요약 | 확인 |
| --- | --- | --- |
| 1 | CPU가 낮은 이유는 sleep으로 대기했기 때문이고, exec-5의 TIMED_WAITING (sleeping)을 근거로 제시함. | 맞음. 덤프의 Thread.sleep·Controller.sleep 호출 경로와도 일치함. |
| 2 | 동일 TID 67779로 생존을 확인하고 LinkedBlockingQueue.take에서 다음 작업을 기다린다고 설명함. ps -L과 jcmd Thread.print로 양쪽에서 대조하는 방법도 제안함. | 맞음. 이번 요청 전후 실제 덤프에서 TID/nid 67779가 일치함. ps 교차 확인은 방법 제안이며 이번 PID에 대해 추가로 실행한 결과는 미제공. |
| 3 | -B는 --before-context, -A는 --after-context이며 일치한 exec-5 줄 뒤 20줄을 출력하므로 exec-6까지 포함됐다고 설명함. | 맞음. |

이 관찰·자기 설명·퀴즈 답변으로 THREAD-02 학습 완료 기준을 충족했다. 앱·top 종료는 사용자 보고로 확인했다. 이번 기록의 커밋·푸시는 별도 확인 대기로 남긴다.
