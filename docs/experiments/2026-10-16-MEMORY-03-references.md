# MEMORY-03 — 객체 참조와 회수 가능성 기초

상태: 진행 중 (참조 개념 및 제한 실험 수행, GC 전후 비교 완료)

## 1. 질문과 조건

- 날짜 / 환경 ID: 2026-10-16 KST / E01
- 오늘 알아볼 질문 하나: 객체를 만든 뒤 어떤 경우에 GC가 회수할 수 있는가?
- 코드 커밋 / 변경 사항: `MemoryLabController`를 추가해 `lab` 프로필 전용 배열 보관·비우기 API를 작성함. Linux에서 `./gradlew compileJava` 성공을 사용자가 보고함. 아래 참조 예시는 설명용이며 실행하지 않음.
- 앱·도구·DB 등 실제 버전: Linux lab 프로필 앱 PID 55091에서 retained 실험 후 새 코드로 재기동. transient 기준 PID 56469; 새 프로세스라 별도 기준선을 사용.
- vCPU / 메모리 / JVM 옵션 / GC / 스레드 설정: Linux VM E01 기록을 참고. 이번 단계에서 부하 없음.
- 요청 수 / 동시성 / 실행 시간 / 입력 크기: 해당 없음.
- 이번에 바꿀 조건 하나: 먼저 두 참조가 한 객체를 가리키는 예시에서 `a` 참조만 제거한다.
- 실험 프로필 / 자원 상한 / 중단 조건 / 정리 방법: 코드 실행 전 개념 안내이며 자원 사용 없음.

## 2. 필요한 개념과 도구

- 객체: `new Object()`로 만든 대상.
- 참조: 변수에서 객체에 접근할 수 있게 해주는 연결. 입문 모델에서는 객체를 찾는 주소값처럼 생각할 수 있으나, Java가 실제 메모리 주소를 직접 다루도록 보장하는 것은 아니다.
- 도달 가능: 프로그램에 남아 있는 참조 연결을 따라 객체에 접근할 수 있는 상태.
- 명령 / 도구: 없음.

설명용 예시:

```java
Object a = new Object();
Object b = a;
a = null;
```

## 3. 예상

- 바꾸기 전 무엇이 어떻게 달라질 것 같은가: `a = null` 이후에도 `b`가 객체를 가리키므로 객체는 계속 접근 가능하다.
- 그렇게 생각한 이유: `b = a`가 같은 객체를 가리키는 참조를 복사했기 때문이다.

## 4. 실행 순서와 재현 절차

참조 설명 예시 자체는 실행하지 않았다. 아래 추가 실험은 Linux VM에서 수행한다.

| 순서 | 컴퓨터·터미널 / 작업 폴더 | 정확한 명령·키 조작 | 목적·볼 항목 | 실제 결과·확인 상태 |
| --- | --- | --- | --- | --- |
| 1 | 해당 없음 | 설명용 Java 코드의 `a`, `b` 참조를 읽음 | 한 객체에 연결된 참조가 몇 개인지 개념적으로 판단 | 사용자가 `b`에 참조가 남아 접근 가능하므로 GC 대상이 아니라고 설명함 |
| 2 | Linux VM 관찰 터미널 | `jcmd 55091 GC.heap_info` | 배열 보관 전 Heap 기준값 확인 | 사용자가 출력 제공: Heap total 59392K, used 44663K, region size 1024K. API 호출 전 |
| 3 | Linux VM 요청 터미널 | `curl -i --max-time 5 -X POST http://localhost:8080/lab/memory/retained` | 1 MiB 배열 하나를 컬렉션에 보관 | 사용자가 `Retained arrays: 1` 응답을 확인했다고 보고함 |
| 4 | Linux VM 관찰 터미널 | `jcmd 55091 GC.heap_info` | 보관 요청 뒤 전체 Heap 사용량을 다시 읽음 | 사용자가 출력 제공: total 71680K, used 26346K. 기준선보다 used가 18317K 낮았음 |
| 5 | Linux VM 요청 터미널 | 같은 POST를 세 번 더 각각 호출 | 목록에 배열을 2, 3, 4개 보관 | 사용자가 2, 3, 4 응답 단계별로 확인함 |
| 6 | Linux VM 관찰 터미널 | 각 보관 단계 뒤 `jcmd 55091 GC.heap_info` | Heap `used`와 G1 region 상태 비교 | 2개: total 71680K, used 28721K, young 9 regions. 3개: used 30846K, young 10. 4개: used 32971K, young 10. 2→3 및 3→4 각각 +2125K; 1→2는 +2375K |
| 7 | Linux VM 요청 터미널 | 다섯 번째 `curl -i --max-time 5 -X POST http://localhost:8080/lab/memory/retained` | 보관 상한에서 요청이 거절되는지 확인 | 사용자가 HTTP 429 응답을 확인함. 코드의 개수 검사 뒤에 할당이 있어 추가 배열은 만들지 않음 |
| 8 | Linux VM 요청 터미널 | `curl -i --max-time 5 -X DELETE http://localhost:8080/lab/memory/retained` | 목록의 요소 참조를 제거 | 사용자가 HTTP 200, Content-Length 26을 제공함. 붙여넣은 출력에는 body 문구가 보이지 않음; 네 배열 정리 응답일 것으로 예상되나 실제 body는 미확인 |
| 9 | Linux VM 관찰 터미널 | `jcmd 55091 GC.heap_info` | DELETE 뒤 Heap 사용량 확인 | 사용자가 출력 제공: total 71680K, used 35708K, young 12 regions. 배열 4개 때 used 32971K보다 2737K 높음 |
| 10 | Linux VM 관찰 터미널 | `jcmd 56469 GC.heap_info` | transient 실험 새 프로세스의 요청 전 기준선 확인 | 사용자가 출력 제공: total 67584K, used 24476K, region 1024K, young 10, survivors 4. transient API 호출 전 |
| 11 | Linux VM 관찰 터미널 | `jcmd 56469 GC.heap_info` | transient API 호출 뒤, GC 요청 전 상태 확인 | 사용자가 출력 제공: Heap total 67584K, used 31459K, young 15, survivors 4. 기준선보다 used가 6983K 증가. curl 응답은 기록되지 않음 |
| 12 | Linux VM 관찰 터미널 | `jcmd 56469 GC.run` | JVM에 명시적으로 GC를 요청 | `Command executed successfully` 출력. 이 메시지는 명령 실행 성공을 뜻하며, 특정 배열 하나가 회수됐음을 직접 증명하지 않음 |
| 13 | Linux VM 관찰 터미널 | `jcmd 56469 GC.heap_info` | GC 요청 뒤 Heap 상태 비교 | 사용자가 출력 제공: Heap total 61440K, used 17573K, young 1, survivors 0. GC 요청 전보다 used가 13886K 감소 |
| 14 | Linux VM 요청 터미널, 작업 폴더 `/home/skl/backend-system-lab` | `curl -i --max-time 5 -X POST http://localhost:8080/lab/memory/transient` | transient endpoint의 HTTP 응답 확인. `-i`는 응답 헤더를 본문 앞에 함께 출력하고 `--max-time 5`는 요청 시간 제한을 둠 | 2026-10-19 KST 실행(응답 `Date`: Sun, 18 Oct 2026 23:30:21 GMT). 사용자가 HTTP 200, `Content-Length: 30`, 본문 `Temporary array bytes: 1048576`을 제공. API 호출 성공 확인. 이전 PID 56469가 현재 프로세스인지도 미확인 |

## 5. 정상 상태와 변화 관찰

| 시각·관측 구간 | 조건 | 관측값·단위 | 사용한 도구 |
| --- | --- | --- | --- |
| 2026-10-16, 배열 추가 전 | Linux lab 프로필 앱 PID 55091 | Heap total 59392K, used 44663K; Metaspace used 31867K | `jcmd 55091 GC.heap_info` |
| 2026-10-16, 배열 1개 보관 응답 뒤 | 같은 JVM, `Retained arrays: 1` 응답 보고 | Heap total 71680K, used 26346K; Metaspace used 33829K | `jcmd 55091 GC.heap_info` |
| 2026-10-16, 보관 배열 2개 | 같은 JVM, 요청 응답 2 | Heap used 28721K; young 9 regions | `jcmd 55091 GC.heap_info` |
| 2026-10-16, 보관 배열 3개 | 같은 JVM, 요청 응답 3 | Heap used 30846K; young 10 regions | `jcmd 55091 GC.heap_info` |
| 2026-10-16, 보관 배열 4개 | 같은 JVM, 요청 응답 4 | Heap used 32971K; young 10 regions | `jcmd 55091 GC.heap_info` |
| 2026-10-16, DELETE 뒤 | 목록 비우기 응답 HTTP 200 뒤 | Heap used 35708K; young 12 regions. 즉시 감소하지 않고 직전보다 2737K 증가 | `jcmd 55091 GC.heap_info` |
| 2026-10-16, transient 새 앱 baseline | Linux lab 프로필 앱 PID 56469, transient 호출 전 | Heap total 67584K, used 24476K; region 1024K; young 10, survivors 4 | `jcmd 56469 GC.heap_info` |
| 2026-10-16, transient 호출 뒤·GC 요청 전 | 같은 JVM, transient 요청 뒤로 보고됨. curl 응답 미기록 | Heap total 67584K, used 31459K; young 15, survivors 4; Metaspace used 33830K | `jcmd 56469 GC.heap_info` |
| 2026-10-16, `GC.run` 요청 뒤 | 같은 JVM, `Command executed successfully` 출력 | Heap total 61440K, used 17573K; young 1, survivors 0; Metaspace used 33831K | `jcmd 56469 GC.heap_info` |
| 2026-10-19, transient endpoint 응답 확인 | Linux VM, `/home/skl/backend-system-lab`, 현재 앱 PID 미확인 | HTTP 200, Content-Length 30, 본문 `Temporary array bytes: 1048576` | `curl -i --max-time 5 -X POST http://localhost:8080/lab/memory/transient` |

## 6. 관찰 사실과 가설

- 출력에서 직접 확인한 사실: 보관 전 `used` 44663K, 배열 1개 응답 뒤 26346K, 2개 뒤 28721K, 3개 뒤 30846K, 4개 뒤 32971K였다. 2→3과 3→4는 각각 2125K 증가했고, 1→2는 2375K 증가했다. 배열 payload 한 개는 1024K이며 G1 region도 1024K로 출력됐다.
- 그 사실에 대한 해석: `used`는 개별 배열 크기가 아니라 Heap 전체의 현재 사용량이다. 첫 보관 뒤 감소한 것은 다른 회수 가능 객체의 정리 등과 양립하며, 보관된 배열이 회수됐다는 뜻은 아니다. 1MiB 배열은 1MiB region의 절반보다 큰 G1 humongous object로 분류되어 여러 연속 region을 차지할 수 있다. 따라서 약 2MiB씩 증가한 관측은 region 할당과 관련 있을 가능성이 있지만, `GC.heap_info` 스냅샷만으로 정확한 원인을 확정하지 않는다. [Java 21 G1 가이드](https://docs.oracle.com/en/java/javase/21/gctuning/garbage-first-g1-garbage-collector1.html)는 humongous 객체 기준을 region 절반 이상으로 설명한다.
- DELETE 뒤 관찰 사실과 해석: 목록 비우기 응답은 HTTP 200이었고, 다음 `used`는 35708K로 직전 4개 보관 시점 32971K보다 2737K 높았다. `clear()`가 목록 요소 참조를 제거해 배열을 회수 가능하게 하더라도 즉시 GC를 실행하거나 Heap `used`를 즉시 낮추지는 않는다. 다른 객체 할당과 GC 시점도 섞이므로 이 값만으로 배열이 아직 참조 중이라고 결론 내릴 수 없다.
- transient 호출 및 GC 요청 뒤 관찰 사실과 해석: `GC.heap_info`의 Heap `used`는 호출 전 24476K, 호출 뒤/GC 전 31459K, `GC.run` 뒤 17573K였다. GC 요청 뒤 13886K 감소한 사실은 회수 가능한 객체들이 정리된 상황과 일치한다. 다만 스냅샷은 특정 `byte[]` 하나의 크기나 객체 생존 여부를 직접 추적하지 않으므로, 1 MiB 배열 하나가 정확히 회수됐다고 단정할 수 없다. GC 전 증가분에도 요청 처리 중 생긴 다른 객체가 섞일 수 있다. `GC.run`은 `System.gc()`를 호출하는 명령이며, 완료 메시지만으로 회수량을 보장하지 않는다. [JDK 21 jcmd 문서](https://docs.oracle.com/en/java/javase/21/docs/specs/man/jcmd.html). 2026-10-19에 transient endpoint의 HTTP 200과 본문 `Temporary array bytes: 1048576`을 확인했다. `Content-Length: 30`은 이 응답 본문의 바이트 수이며, 배열 크기와는 다른 값이다.
- 가설 A / 근거 / 반증되려면 나와야 할 결과: 객체는 `b`를 통해 접근 가능하다.
- 다른 가능한 설명 B: 해당 없음.
- 아직 판단할 수 없는 것: 약 2125K 증가분 중 humongous region 여유분과 요청 중 다른 할당이 각각 차지한 정확한 크기, GC 전후 감소량 중 transient 배열이 차지한 정확한 크기.

## 7. 검증

- A와 B를 구분하려고 추가로 관찰하거나 바꾼 조건: 설명용 예시에서 마지막 참조 `b`도 `null`로 바꾼다.
- 실제 결과: 코드 실행이 아닌 개념 설명에서 사용자는 다른 참조가 없다면 객체가 GC 대상이 되며, Java는 메모리를 직접 해제하기보다 JVM이 실제 회수를 수행한다고 설명함.
- 지지되거나 약해진 가설 / 남은 불확실성: 회수 가능 상태와 실제 회수는 구분해야 한다. JVM이 회수하는 정확한 시점은 정해져 있지 않다. 이후 설명할 컬렉션 참조 여부는 아직 학습 전.
- transient 및 명시적 GC 비교 뒤 학습자 설명: 메서드 안의 지역 변수 참조는 메서드가 끝나면 끊기므로 객체가 회수 대상이 될 수 있지만, 그 시점에 GC가 실행된다는 보장은 없다. `GC.run`은 JVM에 GC를 요청하며, 요청 뒤 Heap `used`가 크게 줄어든 것은 참조가 끊긴 객체들이 회수됐을 가능성이 높다는 뜻이다. 다만 Heap 전체 수치만으로 특정 배열이 실제 회수됐는지는 눈으로 확인할 수 없다고 설명함. 이해가 정확하다. 엄밀하게는 요청 뒤 감소가 GC 수행의 강한 근거지만, 모든 회수 가능 객체가 전부 회수됐는지 또는 감소분 중 특정 배열이 얼마를 차지하는지는 이 출력만으로 확정할 수 없다.

컬렉션 예시에서 학습자는 `saved`가 목록을 참조한다는 점은 짚었지만, 목록 내부가 요소를 어떻게 보관하는지는 아직 모른다고 질문했다. 설명용 예시의 실제 객체는 `ArrayList`다. OpenJDK의 `ArrayList` 구현은 내부 `Object[]` 배열에 각 요소의 참조를 저장한다. 따라서 `saved.add(item)` 뒤에는 `saved` → ArrayList → 내부 배열의 요소 칸 → 객체라는 참조 경로가 생긴다. `item = null`은 마지막 경로를 지우지 않는다. 필드명은 구현 세부사항이며 List API 자체는 내부 저장 방식을 보장하지 않는다.

이 참조 경로를 다시 설명한 학습자의 설명: `saved`는 ArrayList 객체를 가리키고, ArrayList 내부 배열의 한 요소가 원래 객체를 가리킨다. 그러므로 `item = null`만으로는 그 객체를 GC가 회수할 수 없다. 개념 설명이며 실제 코드 실행은 하지 않았다.

`saved.clear()` 설명 뒤 학습자는 목록 객체와 목록 요소의 객체는 서로 별개이며, `clear()`가 요소 참조를 끊으므로 다른 참조가 없으면 요소 객체는 회수 대상이 된다고 설명했다. 이해가 정확하다. 실제 GC 시점과 Heap 관측값은 아직 실험하지 않았다.

메서드 종료와 필드 보관을 비교하는 질문에 학습자는 다음과 같이 답했다. 클래스 필드 컬렉션을 통해서는 요소에 계속 접근할 수 있고, 메서드 지역 변수에만 있던 컬렉션은 메서드 종료 뒤 회수 가능해질 수 있다. 배열을 목록에서 지운 직후 Heap `used`가 반드시 줄지는 않으며, 실제 회수 시점은 JVM이 정한다. 핵심은 변수 선언 위치만이 아니라 객체까지 이어지는 참조 경로가 남아 있는지다. 필드 컬렉션도 그것을 가진 객체가 살아 있을 때만 보관 경로가 유지되고, 지역 컬렉션도 반환·필드 대입 등으로 밖에 참조를 전달했다면 계속 도달 가능할 수 있다. 이 설명은 개념 안내이며 코드 실행이나 GC 관측은 아직 하지 않았다.

## 8. 원인과 해결·재검증

해당 없음: 장애 재현이 아니라 객체 참조 기초를 배우는 개념 단계다.

## 9. 배운 내용

- 내 말로 설명하는 핵심: `b = a` 뒤에는 두 변수가 같은 객체를 가리키므로 `a = null` 이후에도 `b`로 객체에 접근할 수 있다. `b = null` 뒤 다른 참조가 없다면 GC 대상이 되며 실제 메모리 회수는 JVM이 수행한다. 지역 컬렉션과 그 요소도 메서드가 끝난 뒤 다른 참조가 없다면 회수 가능하고, 살아 있는 객체의 필드 컬렉션은 요소를 계속 보관할 수 있다. transient 메서드가 끝나 참조가 끊겨도 Heap `used`는 늘 수 있고, GC가 실행된다는 보장은 없다. `GC.run`은 수동 GC 요청이며, 요청 뒤 `used`가 31459K에서 17573K로 줄어 회수 작업이 일어난 정황을 관찰했지만 특정 배열이 실제 회수됐는지는 이 전체 Heap 수치로 직접 확인할 수 없다.
- 처음 예상과 달랐던 점: 해당 없음.
- 아직 모르는 점: GC 전후 감소량 중 transient 배열이 차지한 정확한 크기, `GC.run` 요청 후 JVM 내부에서 GC가 수행되는 세부 방식.
- 코드 상태: 학습자가 `@Profile("lab")` 컨트롤러에 배열 1 MiB, 최대 4개 상한, `POST /lab/memory/retained` 보관, `DELETE /lab/memory/retained` 비우기, `POST /lab/memory/transient` 지역 할당 메서드를 작성했다. 보관·비우기 API 버전은 Linux에서 컴파일했고 1~4개 보관, 5번째 요청 429, DELETE 200을 관찰했다. DELETE 뒤 Heap `used`는 즉시 줄지 않았다. transient API 실행 뒤 Heap `used`는 24476K→31459K였고 `jcmd 56469 GC.run` 요청 뒤 17573K로 감소했다. 2026-10-19 요청에서 HTTP 200과 본문 `Temporary array bytes: 1048576`을 확인했다.
- 다음 작은 행동: MEMORY-03 결과를 마무리하고 변경된 기록을 Git에 저장·동기화한다. 이후 다음 Memory 주제로 넘어간다. 실험은 `lab` 프로필에서 1 MiB 단일 배열로 수행했으며 무제한 할당이나 OOM은 유도하지 않았다.
