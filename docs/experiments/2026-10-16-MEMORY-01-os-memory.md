# MEMORY-01·02 — 시스템·프로세스·JVM 메모리 범위 읽기

상태: 완료 (MEMORY-01 기본 출력 해석, MEMORY-02 메모리 범위 구분)

## 질문과 조건

- 날짜: 2026-10-16 KST. 환경: E01, 학습용 Linux VM.
- 질문: Linux 전체 메모리는 얼마나 있고, 지금 사용할 여유는 어떻게 읽는가?
- RAM은 실행 중인 프로그램의 코드와 데이터를 두는 공간으로 소개한다.
- 이번 단계는 전체 시스템 관찰이다. Java 한 프로세스나 JVM Heap과 구분한다.
- 코드 변경·메모리 부하·객체 할당 실험 없음. CPU 실험 앱의 최종 종료 여부는 미확인이다.

## 실행 순서

| 순서 | 컴퓨터·터미널 / 폴더 | 명령·조작 | 목적·볼 항목 | 실제 확인 상태 |
| --- | --- | --- | --- | --- |
| 1 | Linux 관찰용 터미널 / 어느 폴더든 가능 | top이 실행 중이면 q | 셸 입력 상태로 돌아오기 | 이번 종료 여부 미확인 |
| 2 | 같은 터미널 | `free -h` | Linux 전체 메모리 한 번 출력. Mem 행 total·free·available부터 읽기 | 사용자 제공 출력 확인 |
| 3 | Linux VM 프로젝트 터미널 (`~/backend-system-lab`) | `./gradlew bootRun` | Spring Boot 기본 프로필로 실행하고 시작 로그를 기다린다. 서버 터미널은 관찰 중 유지 | 사용자가 앱 실행 및 PID 54006을 보고함. 전체 시작 로그는 미제공 |
| 4 | Linux VM의 다른 터미널 | `jps -l` | Java 프로세스 목록에서 앱 PID를 확인 | 사용자가 앱 PID 54006이라고 보고함. 전체 출력은 미제공 |
| 5 | 같은 관찰 터미널 | `top -p 54006` | 해당 앱의 RES·VIRT·SHR·%MEM을 관찰. `q`로 종료 | 사용자 제공 출력 확인 |
| 6 | Linux VM 관찰 터미널 (`/home/skl`) | `jcmd 54006 GC.heap_info` | 실행 중 JVM에 Heap 진단 정보를 요청 | 사용자 제공 출력 확인 |

`-h`는 사람이 읽기 편한 단위로 출력하는 옵션이다. 파일을 변경하지 않으며 출력 후 자동으로 종료한다. total은 전체 RAM, free는 현재 비어 있는 RAM, available은 새 작업에 사용할 수 있다고 추정하는 RAM이다. 실제 출력이 오면 단위와 수치를 함께 읽고 available과 free 차이를 설명한다.

## 실제 결과와 학습자의 설명

사용자가 제공한 출력(붙여넣기 여백만 정리):

```text
               total        used        free      shared  buff/cache   available
Mem:           7.2Gi       1.0Gi       4.6Gi       5.2Mi       1.8Gi       6.2Gi
Swap:          4.0Gi          0B       4.0Gi
```

첫 해석은 Mem 행의 total 7.2Gi, free 4.6Gi, available 6.2Gi에 집중한다. buff/cache는 1.8Gi이며, available이 free보다 큰 이유를 재사용 가능한 일부 캐시 등과 연결해 설명한다. available은 추정치이고 캐시 전체가 항상 즉시 회수된다는 보장은 아니다. free·available 차이만으로 캐시 중 정확히 얼마가 회수되는지 측정했다고 주장하지 않는다.

현재 출력은 전체 RAM에 비해 available이 큰 관측이다. 장기 추세나 앱·JVM의 메모리 사용량은 아직 확인하지 않았다. Swap은 총 4.0Gi, 사용 0B로 기록하되 개념 설명은 후속 단계에서 진행한다.

학습자 설명: 새 프로그램이 쓸 메모리 여유를 볼 때 `available`이 적절하다. `buff/cache`는 잠시 사용 중이어도 다시 돌려줄 수 있으므로 `free`만 보면 메모리가 부족하다고 오해할 수 있다. 이 설명은 available의 용도를 올바르게 짚었다. available이 추정치라는 점도 함께 기억한다.

가상 주소 공간 설명 뒤 학습자는 이를 OS에서 배운 “메모리 주소 범위를 먼저 잡고, CPU가 접근할 때 매핑해서 들어가는” 내용과 연결했다. 연결 방향은 맞다. 더 정확히는 커널이 주소 범위와 매핑을 관리하고 CPU의 MMU가 페이지 테이블을 통해 가상 주소를 변환한다. 주소 범위 설정이 모든 물리 RAM의 선점이나 즉시 할당을 뜻하지는 않는다.

현재 Spring Boot 앱 PID는 사용자가 54006으로 보고했다. `top -p 54006`으로 프로세스 메모리를 관찰했다. JVM 메모리 영역별 구성은 아직 확인하지 않았다.

프로세스 행의 실제 출력:

```text
PID USER      PR  NI    VIRT    RES    SHR S  %CPU  %MEM     TIME+ COMMAND
54006 skl       20   0 5382468 159168  25692 S   0.0   2.1   0:04.89 java
```

`top`은 이 환경에서 메모리 열을 KiB로 표시했다. 따라서 VIRT 5,382,468 KiB(약 5.13 GiB), RES 159,168 KiB(약 155.4 MiB), SHR 25,692 KiB(약 25.1 MiB)다. %MEM 2.1은 RES를 전체 물리 메모리 기준으로 나타낸 값이다. VIRT는 프로세스의 가상 주소 공간 크기이며 실제 RAM 소비량이 아니다. RES는 현재 RAM에 상주하는 프로세스 페이지의 합으로, Java Heap만의 크기가 아니다. JVM 메모리 영역별 구성은 아직 확인하지 않았다.

같은 JVM에 `jcmd 54006 GC.heap_info`를 실행해 받은 실제 출력:

```text
54006:
 garbage-first heap   total 60416K, used 44105K [0x000000008d000000, 0x0000000100000000)
  region size 1024K, 33 young (33792K), 5 survivors (5120K)
 Metaspace       used 31837K, committed 32512K, reserved 1114112K
  class space    used 4550K, committed 4864K, reserved 1048576K
```

초기 해석에서 볼 값은 Heap의 `total 60416K`와 `used 44105K`, 그리고 Metaspace의 `used`, `committed`, `reserved`다. `jcmd`는 JDK 진단 도구로, `jcmd <PID> <진단 명령>` 형식으로 같은 머신의 실행 중인 JVM에 요청을 보낸다. `GC.heap_info`는 JVM 구현의 일반 Heap 정보를 요청한다. 이번 VM에서 명령 실행이 확인됐으며, 설치 위치와 패키지 이력은 확인하지 않았다.

안내 해석: `K`를 1024바이트 단위로 읽으면 Heap `total` 60,416K는 약 59.0MiB, `used` 44,105K는 약 43.1MiB다. `total`은 이 순간 Heap 용량이며 최대 Heap 크기(`-Xmx`)와 같다고 단정하지 않는다. `used/total`은 약 73%지만 이 비율 하나만으로 메모리 압박이나 누수를 판단하지 않는다. G1 region은 각각 1MiB이고 young 33개 중 survivor 5개를 따로 표시한다.

Metaspace는 Java 클래스 메타데이터 영역으로 Heap 밖의 JVM 메모리다. `used` 31,837K(약 31.1MiB)는 사용량, `committed` 32,512K(약 31.8MiB)는 JVM이 사용할 수 있도록 커밋한 용량, `reserved` 1,114,112K(약 1.06GiB)는 주소 공간에서 예약했지만 모두 커밋한 것은 아닌 용량이다. `class space`는 Metaspace 내 클래스 포인터용 하위 공간이므로 Metaspace 행에 더해 합산하지 않는다. 예약 용량은 실제 RAM 사용량이 아니며 VIRT와 연결되는 주소 공간 관측이다.

따라서 Heap `used` 43.1MiB와 프로세스 `RES` 155.4MiB는 범위가 다르다. RES에는 Heap의 resident page뿐 아니라 Metaspace, 스레드 스택, JIT 코드, JVM Native 영역, 라이브러리와 파일 매핑 등이 들어갈 수 있다. 이 출력들만으로 RES의 나머지 구성이나 VIRT의 모든 영역을 정확히 나눠 더할 수 없다.

학습자 핵심 설명: JVM도 user mode에서 실행되는 프로세스이므로 Java Heap이 프로세스 `RES` 전체를 뜻하지 않는다. 프로세스 RES에는 Metaspace, thread, native memory, code 등 여러 영역의 resident page가 포함될 수 있다. `jcmd <PID> GC.heap_info`로 JVM의 Heap과 Metaspace 정보를 확인할 수 있으며, region·young·survivor는 G1의 세부 정보다. 이 설명은 MEMORY-02의 완료 기준인 OS 전체 → 프로세스 → Heap 범위 구분을 충족한다. G1 세부 구조와 Heap·Metaspace 내부는 후속 주제에서 학습한다.

`jcmd`는 JDK의 `jdk.jcmd` 모듈에 포함된 진단 명령 도구다. JDK 도구 목록에 등록돼 있고, 최소 런타임 이미지나 배포판의 runtime 전용 패키지에는 도구가 빠질 수 있다. 이 VM에서는 실제 `jcmd` 호출이 성공했으므로 설치가 확인됐다. `jcmd <PID> help`로 JVM이 제공하는 명령을 보고, `jcmd <PID> help <명령>`으로 개별 도움말을 볼 수 있다. 명령마다 영향이 다르므로 이름만 보고 실행하지 않는다. 현재 실행한 `GC.heap_info`는 Heap 요약 요청이고, 별도 명령인 `GC.run`은 GC 실행을 요청한다.

### 가상 주소 공간이 필요한 이유

프로그램은 물리 RAM 주소를 직접 다루지 않고 가상 주소를 사용한다. CPU의 메모리 관리 장치와 페이지 테이블이 프로세스의 가상 주소를 물리 페이지로 변환한다. 각 프로세스는 자신의 주소 공간을 가지므로 같은 가상 주소를 서로 다른 물리 페이지로 연결할 수 있고, 프로세스 간 메모리 접근을 분리·보호할 수 있다.

커널은 주소 범위를 용도와 접근 권한에 따라 관리한다. 파일·공유 라이브러리 매핑과 프로세스 간 공유도 주소 공간을 통해 다룬다. 메모리 영역을 먼저 주소 공간에 매핑해 두고 실제 접근 시 필요한 페이지를 RAM에 올리는 수요 페이징도 가능하다. 따라서 VIRT는 관리 중인 가상 주소 공간의 크기를 나타내며, 실제 RAM·swap 사용량이나 앞으로 확보가 보장된 용량과 같지 않다. RES는 그중 현재 RAM에 상주하는 페이지를 요약한다. `top` 한 줄만으로 VIRT의 각 영역이 무엇인지 분해할 수는 없다.
