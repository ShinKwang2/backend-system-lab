# THREAD-03 — 덤프 한 조각 읽기

상태: 학습 완료 (2026-10-23 반복 덤프·자기 설명·마무리 퀴즈 확인 및 보충, 앱 종료 사용자 확인). 기록 커밋 3409325 확인, 푸시·Linux pull 미확인.

## 1. 질문과 실험 조건

- 날짜 / 환경 ID: 2026-10-19 시작, 2026-10-23 재개·학습 완료 / E01.
- 오늘의 질문: 스레드 이름·상태·호출 스택을 연결해 무엇을 하는지 어떻게 설명하며, 덤프 한 번으로 알 수 없는 것은 무엇인가?
- 코드 변경: 없음. 기존 lab API를 활용한다. 학습자가 앱 재실행 후 새 PID 69064를 제공함.
- 환경: 직전 PID 67745 앱과 top은 종료했다고 학습자가 확인했다. 2026-10-19 PID는 69064였고 당시 상세 버전·자원·프로필 로그 원문은 미제공이었다. 2026-10-23에는 PID 88723·Java 21.0.12.1·lab 프로필 기동 로그를 확인했다. PID는 모두 당시 값이다.
- Git: THREAD-02 결과 기록 커밋 `a766b39`의 main·origin/main 일치를 확인함. Linux pull은 미확인.

## 2. 시작 복습 퀴즈 — THREAD-01·02

1. Linux ps에서 comm 이름이 잘렸을 때 전체 Java 스레드 이름을 확인할 명령은 무엇인가? ps의 TID와 덤프의 어떤 값을 대조하는가?
2. Poller가 Java RUNNABLE이면서 Linux에서는 S로 보인다면 CPU를 계속 사용 중이라고 결론 낼 수 있는가? 판단을 위해 어떤 출력들을 함께 보는가?
3. 같은 worker가 Thread.sleep의 TIMED_WAITING에서 LinkedBlockingQueue.take의 WAITING으로 바뀌었다. 요청 처리와 기다리는 대상이 어떻게 달라졌는가?

| 문항 | 학습자 답 요약 | 튜터 보충 |
| --- | --- | --- |
| 1 | jcmd Thread.print에서 전체 이름을 보고 ps TID와 덤프 대괄호 안 ID를 비교한다고 답함. ps에서도 긴 이름을 보고 싶다는 의견을 제시함. | 이 JDK 출력에서는 맞음. nid도 같은 native ID다. Linux comm 자체가 최대 15바이트 이름으로 제한되므로 ps 열 폭이나 -ww로 잘린 Java 이름을 복원할 수 없다. args는 프로세스 실행 인자이므로 전체 Java 스레드 이름의 대안이 아니다. |
| 2 | RUNNABLE만으로 계속 CPU를 쓴다고 판단할 수 없다고 설명함. 함께 볼 출력으로 Thread.print를 제안했으나 무엇을 봐야 하는지 확신하지 못함. | 덤프는 현재 코드·대기 지점을 보여준다. 스레드별 CPU 수치는 top -H -p PID로 확인해 같은 TID와 연결한다. -H의 긴 옵션은 --threads-show다. 설명 시점에는 미실행이었으며 이후 제공된 화면은 아래에 별도로 기록했다. |
| 3 | 질문의 표현이 모호하다고 설명하면서 TIMED_WAITING은 sleep 같은 대기, WAITING은 다음 요청 대기로 돌아간 것이라고 답함. | 핵심 해석은 맞음. 요청 처리 도중 정해진 시간을 기다리던 상태에서, 요청 완료 후 Tomcat 큐의 다음 처리 작업을 기다리는 상태로 바뀌었다. TIMED_WAITING 하나만으로 sleep을 확정하지 않고 실제 Thread.sleep 스택도 근거로 쓴다. |

도구 역할 보충: top의 스레드 모드에서는 각 행의 PID 열이 Linux TID를 나타내며 %CPU는 해당 스레드의 사용률이다. q로 종료한다. jcmd Thread.print는 이름·native ID·Java 상태·스택을 출력한 뒤 종료한다. 이 설명을 배우기 전부터 알고 있었다고 기록하지 않는다.

학습자가 짧은 옵션의 긴 이름·영어 뜻을 함께 이해하기를 원하므로 새 옵션 설명에서 가능한 경우 함께 연결한다. 근거: [Linux comm 제한](https://man7.org/linux/man-pages/man5/proc_pid_comm.5.html), [top 스레드 모드](https://man7.org/linux/man-pages/man1/top.1.html).

## 3. 예상

THREAD-02에서 sleep의 응답은 늦어도 CPU 사용은 낮을 것으로 예상하고 확인했다. 이번 재실행에서도 학습자는 sleep 요청을 보냈으나 %CPU가 뚜렷하게 오르지 않는다고 관찰했다. 이후 Poller의 Linux/JVM 대조를 진행했으며 같은 요청 중 반복 덤프는 2026-10-23 재개 후 관찰했다(9절).

반복 관찰의 예상 질문: 3초 sleep 요청을 처리하는 동안 약 1초 차이로 덤프 두 개를 찍으면, 담당 exec 스레드의 상태와 메서드 위치가 어떻게 보일까? 2026-10-23 학습자는 둘 다 WAIT이나 SLEEP처럼 기다리는 상태이며 Thread.sleep이 보일 것으로 예상했다. 튜터는 정확한 Java 상태 이름이 TIMED_WAITING (sleeping)임을 보충했다. 이는 실행 전 예상이며 이후 실제 결과는 9절에 구분해 기록했다.

## 4. 실행 순서와 재현 절차

| 단계 | 실행 위치 | 안내한 명령 | 목적·종료 | 실제 결과 |
| --- | --- | --- | --- | --- |
| 1 | Linux 앱 실행 터미널 / `/home/skl/backend-system-lab` | git pull 후 `./gradlew bootRun --args='--spring.profiles.active=lab'` 안내 | 기존 코드로 lab 앱 재기동, 로그에서 새 PID 확인. 앱 터미널 유지, 종료는 Ctrl+C | 학습자가 새 PID 69064 제공. pull·기동 명령·프로필 로그 원문은 미제공 |
| 2 | Linux 관찰 터미널 / 작업 폴더 미확인 | top의 스레드 모드 안내 후 화면 제공(정확한 실행 명령은 미제공) | 각 행의 PID 열을 TID로 읽고 %CPU·COMMAND 확인. 종료는 q | 스크린샷에 G1/GC/VM 등의 개별 스레드가 보임. 학습자는 -H를 처음 알았다고 설명함. |
| 3 | Linux의 다른 관찰 터미널 / 임의 폴더 가능 | `ps -L -p 69064 -o pid,tid,stat,comm` 안내 | 전체 스레드 목록을 한 번 출력하고 종료. http-nio로 시작하는 행을 찾기 | ps 출력 원문은 미제공. 이후 학습자가 http-nio 행을 찾았다고 설명하며 해당 행들이 포함된 top 스크린샷을 제공함 |
| 4 | Linux 요청 터미널 / `/home/skl/backend-system-lab` | `curl --max-time 10 -w '\nHTTP %{http_code}, total %{time_total}s\n' http://localhost:8080/lab/thread/sleep` | 단일 sleep 요청의 HTTP 응답·전체 시간과 top 스레드별 %CPU 비교. curl은 응답 또는 최대 10초 후 종료 | 실제 출력 Sleep lab ready / HTTP 200, total 3.003121s. 학습자는 %CPU가 뚜렷하게 오르지 않는다고 보고함 |
| 5 | Linux의 다른 관찰 터미널 / 임의 폴더 가능, 추가 요청 없이 실행 | `jcmd 69064 Thread.print > /tmp/thread-03-idle-69064.txt` 후 `grep --after-context=12 'nid=69104 ' /tmp/thread-03-idle-69064.txt` | top의 TID 69104 행을 JVM 이름·상태·스택과 대조. 두 명령은 완료 후 종료 | 학습자가 실제 Poller 블록을 제공하고 RUNNABLE임을 직접 설명함. 전체 이름 http-nio-8080-Poller, TID/nid 69104, EPoll.wait native 스택 확인 |
| 보충 | 실행 중인 Linux top 화면 | 소문자 d → 1 입력 → Enter. 새로 실행한다면 `top -H -d 1 -p 69064` | 갱신 간격 1초 설정. -d의 긴 옵션은 --delay. 종료는 q | 질문에 따라 안내함. 현재 간격·설정 결과는 아직 미확인 |

위 표는 2026-10-19의 절차다. 2026-10-23 재개 후 덤프 저장·반복 관찰 절차와 결과는 9절에 기록했다.

## 5. 정상 상태와 변화 관찰

학습자가 제공한 top 스크린샷은 개별 TID 69064, 69068~69083 등의 행과 69086의 GC Thread#1 행까지 보이는 화면이다. G1 Refine#0/TID 69072는 %CPU 0.3, 나머지 보이는 행은 0.0이며 S 열은 S다. 화면에 http-nio 행은 보이지 않는다. 전체 목록이 담긴 화면인지 확인되지 않았으므로 Tomcat worker가 존재하지 않는다고 단정하지 않는다.

학습자는 exec-1 같은 이름이 보이지 않는다고 질문했다. 이후 더 넓은 top 스크린샷에서 TID 69094~69103의 `http-nio-8080-e`, 69104의 `http-nio-8080-P`, 69105의 `http-nio-8080-A`를 실제로 확인했다. 이 행들의 %CPU는 모두 0.0, S 열은 S였다. 따라서 첫 화면에 없다는 사실만으로 실제 thread 부재를 단정하지 않은 판단을 지지한다. 이후 Poller/TID 69104의 전체 이름을 덤프와 대조했다(아래 참조). 다른 worker 전체 이름의 개별 대조 출력은 미제공이다.

이번 재실행의 sleep 응답은 HTTP 200, 전체 3.003121초였으며 본문은 Sleep lab ready였다. 학습자는 요청에도 %CPU가 오르지 않는 것 같다고 설명했다. top 화면이 요청의 어느 순간에 찍혔는지는 미확인이며, 표시된 0.0%를 모든 시간 동안 CPU 사용이 전혀 없었다는 뜻으로 해석하지 않는다.

보충 설명: %CPU는 갱신 구간의 CPU 사용률이고, top의 S 열은 Linux 상태다. 이번 S 열 값 S는 sleep 상태이며 Java의 WAITING/TIMED_WAITING 등과 같은 분류가 아니다. 낮은 CPU만으로 어떤 요청을 처리 중인 worker인지 확정할 수 없으므로 이번에는 특정 TID 69104의 전체 이름·Java 상태·스택을 연결해 읽는다.

### 동일 TID의 실제 JVM 덤프

학습자가 TID 69104는 Poller이며 Java RUNNABLE이라고 설명하고 다음 출력을 제공했다. 해당 TID를 고른 목적은 Linux S·낮은 %CPU와 JVM RUNNABLE·native 대기 스택의 대조다. API를 처리한 worker를 특정한 것이 아니다. 선택 목적을 먼저 명확히 설명하도록 보완했다.

```text
"http-nio-8080-Poller" #39 [69104] daemon prio=5 os_prio=0 cpu=54.28ms elapsed=592.35s tid=0x00007af858e13840 nid=69104 runnable  [0x00007af8397fe000]
   java.lang.Thread.State: RUNNABLE
    at sun.nio.ch.EPoll.wait(java.base@21.0.12.1/Native Method)
    at sun.nio.ch.EPollSelectorImpl.doSelect(java.base@21.0.12.1/EPollSelectorImpl.java:121)
```

같은 블록에서 SelectorImpl.select와 NioEndpoint$Poller.run(NioEndpoint.java:912)도 확인했다. 두 도구는 서로 다른 순간의 관측이며 완전히 같은 시각에 측정한 것으로 기록하지 않는다.

헤더 읽기 설명: cpu=54.28ms는 그 스레드가 누적 사용한 CPU 시간이고, elapsed=592.35s는 스레드 생성 이후의 경과 시간이다. 현재 %CPU나 HTTP 요청의 응답 시간이 아니다. 이 의미는 아래 학습자의 자기 설명으로 확인했다. 근거: [OpenJDK 21 출력 코드](https://github.com/openjdk/jdk21u/blob/master/src/hotspot/share/runtime/thread.cpp), [스레드 경과 시간 계산](https://github.com/openjdk/jdk21u/blob/master/src/hotspot/share/runtime/threadStatisticalInfo.hpp).

이후 학습자는 elapsed는 스레드 생성 이후 흘러간 시간이며 스레드가 살아 있다고 계속 CPU를 사용하는 것은 아니므로, 이 경우 592초가량 존재하면서 누적 CPU는 약 54밀리초를 썼다고 정확하게 설명했다. 스레드 생성·소멸 비용 때문에 유지·재사용하는 설계도 추론했다. 보충: worker pool 재사용과 지속적인 소켓 감시를 위한 전용 Poller의 역할을 구분한다. 이번 덤프의 시간 값만으로 스레드 유지 설계의 이유까지 입증한 것은 아니다.

이전 실제 덤프는 [THREAD-01](2026-10-19-THREAD-01-process-threads.md)·[THREAD-02](2026-10-19-THREAD-02-timed-wait.md)에 저장되어 있다.

## 6. 관찰 사실과 가설

- 확인: main과 origin/main의 커밋 a766b39 일치. 시작 복습에 학습자가 위와 같이 답함.
- 추가 확인: 새 앱 PID 69064는 사용자 보고로 확인했고 top 스레드 모드의 화면을 읽었다.
- 추가 확인: 이후 top 스크린샷에서 http-nio 행들과 TID 69094~69105가 보임. sleep 요청 HTTP 200·3.003121초 및 뚜렷한 CPU 상승이 없다는 사용자 관찰을 기록함.
- 추가 확인: top의 http-nio-8080-P/TID 69104에 대응하는 JVM 이름은 http-nio-8080-Poller이며 RUNNABLE·EPoll.wait 스택이었다.
- 자기 설명 확인: 학습자가 스레드의 생존 경과 시간 592.35초와 누적 CPU 시간 54.28밀리초를 구분하고, 생존 시간이 연속 CPU 사용을 의미하지 않는다고 설명했다.
- 후속 확인: 같은 요청 구간 반복 덤프 예상과 결과는 2026-10-23에 확인했다(9절).

### 추가 관찰과 GC 가설

- 학습자 관찰: G1 관련 스레드와 VM Periodic Tas의 CPU가 오르는 것을 봤다고 설명함. 사진의 한 장면과는 별도로 실시간 관찰 보고로 기록한다.
- 학습자 가설: 우리 API를 실행하면서 GC가 도는 것 같다고 추정함. 이는 아직 확정된 원인이 아니다.
- 기존 근거: 첫 스크린샷에서도 G1 Refine#0의 %CPU가 0.3이었고 다음 사진에서도 0.3이었다. API 요청이 그 CPU 활동을 발생시켰는지는 이 두 사진만으로 확인할 수 없다.
- 튜터 보충: G1 Refine은 G1 내부 관리 작업을 수행하며 주기적으로 깨어날 수도 있다. VM Periodic Task Thread는 JVM의 주기 작업을 수행하는 흐름이다. 이름과 낮은 CPU 활동만으로 요청 때문에 객체 회수가 실행됐다고 단정하지 않고, 필요할 때 GC 횟수·로그 등의 별도 증거로 검증한다. 이번 단계에서 GC 검증 실험은 아직 수행하지 않았다.
- S 열 보충: %CPU는 갱신 구간의 사용량이고 S 열은 관측 순간의 Linux 상태다. 구간 사이 잠깐 CPU를 사용하고 관측 순간 다시 S일 수도 있다. 또한 Java의 sleep 시간 대기와 작업 큐 대기는 둘 다 Linux에서는 S로 보일 수 있으므로 갱신 간격을 1초로 바꾼다고 두 대기를 구별할 수 있는 것은 아니다.

역할 확인 근거: [OpenJDK 21 G1ConcurrentRefineThread](https://github.com/openjdk/jdk21u/blob/master/src/hotspot/share/gc/g1/g1ConcurrentRefineThread.cpp), [OpenJDK 21 WatcherThread의 주기 작업](https://github.com/openjdk/jdk21u/blob/master/src/hotspot/share/runtime/nonJavaThread.cpp).

## 7. 검증

top의 http-nio-8080-P/TID 69104와 JVM 덤프의 Poller/nid 69104를 실제 출력으로 대조했다. Linux 상태와 Java 상태가 다를 수 있음을 재확인했다. 이후 헤더 해석·반복 덤프·학습자 설명과 퀴즈도 확인했다(9절).

## 8. 원인과 해결·재검증

해당 없음: 도구와 덤프 읽기 학습이며 장애 원인의 진단·수정 실험은 아니다.

## 9. 배운 내용과 다음 행동

- 2026-10-23 재개: 학습자가 출장 후 학습 위치와 Poller 헤더를 출력한 명령을 다시 질문했다. 이전 앱은 꺼두었다고 설명한 뒤 새 기동 로그를 제공했다. 04:37:09Z의 Starting 로그에서 PID 88723·Java 21.0.12.1·프로젝트 경로 `/home/skl/backend-system-lab`·사용자 skl을 확인했고 다음 로그에서 lab 프로필을 확인했다. Started 로그와 HTTP 응답은 이번에 아직 미제공이다. 앞 절의 PID 69064·TID 69104는 2026-10-19 당시 값이다.
- 재개 후 덤프: Linux 관찰 터미널의 임의 폴더에서 `jcmd 88723 Thread.print > /tmp/thread-03-idle-88723.txt` 후 `grep --after-context=12 '"http-nio-8080-Poller"' /tmp/thread-03-idle-88723.txt` 실행을 안내했다. 이에 이어 사용자가 아래 Poller 블록을 제공했다. 정확한 실행 명령과 작업 폴더의 원문은 미제공이며 파일 자체는 AI가 직접 확인하지 않았다.

```text
"http-nio-8080-Poller" #39 [88764] daemon prio=5 os_prio=0 cpu=411.32ms elapsed=5626.96s tid=0x0000761700e3f090 nid=88764 runnable  [0x00007616e49fb000]
   java.lang.Thread.State: RUNNABLE
    at sun.nio.ch.EPoll.wait(java.base@21.0.12.1/Native Method)
    at sun.nio.ch.EPollSelectorImpl.doSelect(java.base@21.0.12.1/EPollSelectorImpl.java:121)
    at sun.nio.ch.SelectorImpl.lockAndDoSelect(java.base@21.0.12.1/SelectorImpl.java:130)
    - locked <0x000000008e82c5a8> (a sun.nio.ch.Util$2)
    - locked <0x000000008e82c3c0> (a sun.nio.ch.EPollSelectorImpl)
    at sun.nio.ch.SelectorImpl.select(java.base@21.0.12.1/SelectorImpl.java:142)
    at org.apache.tomcat.util.net.NioEndpoint$Poller.run(NioEndpoint.java:912)
    at java.lang.Thread.runWith(java.base@21.0.12.1/Thread.java:1596)
    at java.lang.Thread.run(java.base@21.0.12.1/Thread.java:1583)

"http-nio-8080-Acceptor" #40 [88765] daemon prio=5 os_prio=0 cpu=0.20ms elapsed=5626.96s tid=0x0000761700e45870 nid=88765 runnable  [0x00007616e48fb000]
```

- 이번 확인: Poller TID/nid 88764·Java RUNNABLE·EPoll.wait. cpu 411.32ms는 누적 CPU 시간, elapsed 5626.96s는 생성 이후 경과 시간으로 읽는다. 이번 Linux 상태·CPU 사용률과의 직접 대조는 아직 전이다. 마지막 Acceptor 헤더는 다음 스레드 블록이며 Poller 스택의 일부가 아니다.
- 반복 덤프 절차: Linux 관찰 터미널의 임의 폴더에서 아래 블록을 한 번에 실행하도록 안내했다. 이후 학습자가 두 파일의 추출 결과를 제공했다(아래 참조). 블록 자체의 실행 원문과 curl 응답은 미제공이다. 앱 실행 터미널은 유지한다. `&`는 curl을 백그라운드로 실행하고, 셸의 sleep은 다음 관찰 명령까지 기다리는 시간이다. `wait`는 이 셸의 백그라운드 작업 종료를 기다린다. 두 jcmd는 파일 저장 후 종료하며 같은 경로의 파일은 덮어쓴다. 각 덤프 명령의 실행 시간 때문에 실제 측정 간격은 정확히 1초가 아니며 두 번째 덤프가 요청 종료 뒤에 찍힐 수도 있다.

```bash
curl --max-time 10 -w '\nHTTP %{http_code}, total %{time_total}s\n' http://localhost:8080/lab/thread/sleep &
sleep 0.5
jcmd 88723 Thread.print > /tmp/thread-03-sleep-first-88723.txt
sleep 1
jcmd 88723 Thread.print > /tmp/thread-03-sleep-second-88723.txt
wait
```

- 저장 뒤에는 아래 두 명령을 차례대로 실행하도록 안내했고, 학습자가 Linux의 `skl@backend-lab:~$` 프롬프트에서 두 명령과 실제 출력을 제공했다. `--before-context=5`와 `--after-context=3`은 일치한 줄의 앞 5줄·뒤 3줄을 출력한다. 담당 exec 헤더의 TID·Java 상태·Thread.sleep 호출 위치를 비교한다. 일치하는 줄이 없으면 해당 파일에서 요청 처리 스택을 찾지 못한 것으로 기록하고 시점을 확인한다.

```bash
grep --before-context=5 --after-context=3 'ThreadLabController.sleep' /tmp/thread-03-sleep-first-88723.txt
grep --before-context=5 --after-context=3 'ThreadLabController.sleep' /tmp/thread-03-sleep-second-88723.txt
```

### 2026-10-23 반복 덤프 실제 결과

| 항목 | 첫 덤프 | 두 번째 덤프 |
| --- | --- | --- |
| 파일 | `/tmp/thread-03-sleep-first-88723.txt` | `/tmp/thread-03-sleep-second-88723.txt` |
| 이름 / Java ID | http-nio-8080-exec-1 / #29 | http-nio-8080-exec-1 / #29 |
| Linux TID / nid | 88754 | 88754 |
| cpu | 31.13ms | 31.13ms |
| elapsed | 6013.90s | 6015.00s |
| Java 상태 | TIMED_WAITING (sleeping) | TIMED_WAITING (sleeping) |

두 덤프의 최상단 스택은 동일하게 제공되었다.

```text
at java.lang.Thread.sleep0(java.base@21.0.12.1/Native Method)
at java.lang.Thread.sleep(java.base@21.0.12.1/Thread.java:509)
at com.shinkwang.backendsystemlab.thread.ThreadLabController.sleep(ThreadLabController.java:28)
```

관찰 사실: 동일 TID의 두 관측 순간 모두 sleep 대기였고, elapsed는 약 1.10초 증가했지만 cpu는 표시된 정밀도에서 증가하지 않았다. 두 순간의 덤프만으로 그 사이 모든 순간의 상태를 직접 관측한 것은 아니다. 이번 요청의 HTTP 응답·전체 시간은 아직 미제공이다. 실행 파일 자체는 AI가 직접 읽지 않았으며 사용자 제공 출력으로 확인했다.

### 반복 덤프의 학습자 해석

- 학습자는 CPU를 사용하지 않고 스레드 생성 이후 시간만 흐른 것이라고 설명했다. 튜터는 누적 CPU 시간이 표시 정밀도에서 증가하지 않았다는 표현으로 보충한다.
- 학습자는 같은 상태와 스택의 두 순간만으로 중간 모든 순간까지 같다고 확정할 수 없다고 설명했다. 예를 들어 같은 exec-1이 서로 다른 두 요청을 처리해도 두 덤프가 같을 수 있다고 반례를 제시했다.
- 튜터 보충: 스레드 식별자는 요청 식별자가 아니다. 일반적인 반례는 타당하지만 이번 안내 절차에는 curl 한 번이 있으므로 실제 요청이 두 번이었다는 관찰로 기록하지 않는다. 실험 코드와 실행 순서라는 맥락을 덤프와 함께 해석한다.

### 마무리 퀴즈 답변과 보충

| 문항 | 학습자 답 | 튜터 보충 |
| --- | --- | --- |
| 스레드별 CPU와 Java 상태·스택 명령 | `top -L -d 1 -p 88723`, `jcmd 88723 Thread.print` | jcmd는 맞음. top의 스레드 옵션은 `-H`이므로 `top -H -d 1 -p 88723`. ps의 `-L`과 구분한다. 정정 명령의 새 실행 결과를 확인한 것은 아님. |
| Poller RUNNABLE·EPoll.wait와 CPU 확인 | 대기이므로 계속 CPU를 사용한다고 단정하기 어렵고, 덤프를 2~3번 찍어 cpu 시간이 늘어나는지 비교하자고 제안함 | 같은 TID의 누적 cpu 증가량 비교는 유효한 구간 CPU 사용 확인 방법이다. top -H의 갱신 구간 사용률과 함께 상황에 맞게 선택할 수 있다. 덤프에 CPU 사용 정보가 없다는 식으로 역할을 단정하지 않는다. |
| worker WAITING·LinkedBlockingQueue.take | 살아서 작업 큐를 기다리다가 큐에서 작업을 꺼내 처리한다고 설명함. epoll이 요청을 받아 큐에 넣는 흐름으로 표현함 | 대기 대상 해석은 맞음. epoll은 Linux의 소켓 이벤트 통지 기능이며 Tomcat Poller가 이벤트를 받아 처리 작업을 전달한다. |

학습자는 단일 요청이라는 전제가 있으면 같은 대기가 아니냐고 질문했다. 튜터가 덤프만의 한계와 실행 맥락을 섞어 혼란을 주었음을 인정하고 정정한다. 이번에는 단일 요청, 소스의 Thread.sleep(3000L), 안내한 실행 순서와 실제 두 덤프를 함께 보므로 같은 sleep 호출의 대기 구간을 두 번 관찰한 것으로 해석하면 된다. 두 관측 시각 자체는 약 1.10초 차이다. 중간의 모든 내부 동작을 증명하라고 요구하는 것이 이번 학습 목표는 아니다.

- 종료 확인: Linux에서 top은 q, 앱 실행 터미널은 Ctrl+C, 이후 jps -l로 앱 부재를 확인하는 절차를 안내했다. 사용자가 앱 종료를 확인했다. 키 조작·jps 출력 원문 및 top 종료 여부는 별도 미제공이다.
- 후속 진행: 기록 커밋 3409325를 Mac에서 확인했다. F04 시작 복습은 2026-10-24에 답변·보충까지 진행했다. 푸시·Linux pull은 미확인이며 현재 다음 행동은 [진도 문서](../PROGRESS.md)를 따른다. top의 실제 간격 설정 결과는 미확인이다.
- Linux에서 새로 실행하기 전에는 필요한 코드의 Git 동기화를 확인한다.
- 저장 상태: THREAD-03 학습 기록은 커밋 3409325에 포함되어 있다. 2026-11-17의 후속 상태 정정은 로컬 미커밋 변경이다.
