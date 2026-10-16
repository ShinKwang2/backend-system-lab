# 실행 환경 기록

환경이 바뀌면 이전 기록을 지우지 말고 아래 표를 복사해 새 환경 ID로 추가한다. 실험 기록에는 사용한 환경 ID와 그 실험의 실제 JVM 옵션·자원 제한을 함께 적는다. 비밀번호, SSH 개인 키, 토큰은 저장하지 않는다.

## 저장소에서 확인된 설정

| 항목 | 값 | 근거 |
| --- | --- | --- |
| Java toolchain | 21 | `build.gradle` |
| Spring Boot 플러그인 | 4.1.1 | `build.gradle` |
| 앱 이름 | backend-system-lab | `application.yaml` |
| 패키지 | com.shinkwang.backendsystemlab | 앱 진입점 |
| 의존성 | Web MVC, Actuator, 테스트 스타터 | `build.gradle` |
| 실험 API / 외부 서비스 | CPU 컨트롤러의 약 3초 계산·동시 진입 제한 구현. Mac 컴파일과 Linux 기본/lab 실행·404/200·추가 요청 429 확인. 외부 서비스 없음 | 소스와 사용자 제공 출력·설명 |

선언된 버전과 실제 실행 버전은 구분하며, 실제 확인 근거는 아래 환경 기록과 실험 문서에 남긴다.

## 환경 E01 — 학습용 Linux VM과 Mac 작업 공간

| 항목 | 현재 기록 |
| --- | --- |
| 확인 날짜 | 2026-10-15~16 KST. 사용자 실행 결과·로그·설명 기준 |
| 코드 작성 컴퓨터 / OS | macOS 작업 공간이 제공됨; 실습용 상세 정보 미확인 |
| Linux 실험 머신 / OS / 커널 | 학습용으로 별도 생성한 VM이며 실제 서비스는 운영하지 않음 (사용자 확인). `uname -s` → `Linux`; Gradle 출력의 OS는 `Linux 7.0.0-38-generic amd64` (사용자 보고). 배포판 버전·VM 자원 할당 설정은 미확인 |
| Linux 사용자 / 현재 폴더 | `skl` 사용자로 생성했다고 설명함. `pwd` 결과 `/home/skl` (사용자 보고) |
| Linux 프로젝트 경로 / 접속 방법 | HTTPS clone 후 `cd backend-system-lab` 실행, 프롬프트 `skl@backend-lab:~/backend-system-lab` (사용자 보고). 경로 `/home/skl/backend-system-lab`. `ls`에서 `AGENTS.md`, `README.md`, `build.gradle`, `docs`, `gradle`, `gradlew`, `gradlew.bat`, `settings.gradle`, `src` 확인 (사용자 보고). 커밋·접속 방법은 미확인 |
| Linux Git | `git --version` → `git version 2.53.0` (사용자 보고) |
| Git 작업 방식 | Mac에서 수정·commit/push, Linux에서 clone/pull. Public 저장소의 HTTPS clone·pull 성공을 사용자 보고. 자격 증명 파일·키 내용은 조사하지 않음 |
| 로컬 원격 저장소 | `origin`: `https://github.com/ShinKwang2/backend-system-lab.git`; Public. 로컬 코드 커밋 fe35463 확인. 사용자 push·Linux pull 성공 보고. Linux 해시 출력과 추가 실험 기록의 커밋·푸시는 미확인 |
| JDK 배포판 / 실제 버전 | `java -version`: OpenJDK `21.0.12.1`, Runtime build `21.0.12.1+1-1-26.04.4-Ubuntu`, 64-Bit Server VM (사용자 보고). 컴파일러·진단 도구 존재는 아직 미확인. 빌드 문자열만으로 Linux 배포판 버전을 확정하지 않음 |
| Gradle / 실행 JVM | Linux `./gradlew --version` 성공: Gradle 9.7.1, Launcher JVM `21.0.12.1`, Daemon JVM 경로 `/usr/lib/jvm/java-21-openjdk-amd64` (사용자 보고). Linux 기본 앱 기동 확인. CPU 컨트롤러의 Mac compileJava 성공도 사용자 출력으로 확인 |
| vCPU / 메모리 / 실험 디스크 여유 | Linux에서 `nproc` → `4` (사용자 보고). 현재 실행 환경의 사용 가능한 처리 장치 수이며 물리 코어 수·VM 할당 설정·메모리·디스크 여유는 미확인 |
| JVM 옵션 / GC 종류 | 미확인 |
| 플랫폼 / 가상 스레드 설정 | 미확인; 첫 스레드 추적 실험은 플랫폼 스레드 기준 |
| 앱 기동 명령 / 포트 | 기본: `./gradlew bootRun`. 실험: `./gradlew bootRun --args='--spring.profiles.active=lab'`. HTTP 8080, context path `/` 확인. 마지막 관측은 lab 실행이며 최종 종료는 미확인 |
| `ps`, `top`, `free`, `vmstat`, `ss` | ps·top 실행 확인. CPU-02에서 procps-ng 4.0.4, Delay 3.0 secs, Cumulative Off, Irix On 전환 확인. CPU-03은 -d 1·Irix On 기준으로 안내 후 관측 보고. free·vmstat·ss는 미확인 |
| `jps`, `jcmd`, `jstack` | `jps`와 `jps -l` 실행 성공. `command -v jps` → `/usr/bin/jps` (사용자 보고). 링크 대상·설치 이력, jcmd·jstack 실행은 미확인 |
| `iostat` | 필요 시 sysstat 도구 확인; 아직 미확인 |
| 빌드 / 기동 성공 여부 | 2026-10-15T00:23:28.176Z 로그에 `Started BackendSystemLabApplication in 0.842 seconds` 확인 (사용자 제공). 해당 실행의 앱 PID는 46883; 재시작하면 다시 확인해야 함. 별도 `build`·테스트 작업은 미수행 |
| 최근 앱 재실행 관찰 | 중단 후 앱·Wrapper가 jps 목록에서 사라짐. 재실행 후 앱 47487, Wrapper 47427, Gradle 데몬 46752 확인 (사용자 제공 jps 출력). PID는 해당 실행에만 사용 |
| CPU-03 기본 프로필 관찰 | 앱 PID 51980. UTC 2026-10-15T22:57:11.216Z 로그의 default 확인. Tomcat 기동·/lab/cpu HTTP 404를 사용자 보고. 이후 lab으로 재시작함 |
| CPU-03 lab 프로필 관찰 | 앱 PID 52288. 요청 전 앱 CPU 주로 0.0·가끔 0.6, 계산 중 약 100·이후 회복, 전체 us 약 25·id 약 75를 사용자 보고. 실제 HTTP 200·추가 요청 429·time_total 3.002366s 확인. sy 구체적 값과 종료 여부는 미확인 |
| PID 1 / 재부모화 관찰 | `ps -p 1 -o pid,ppid,comm` → PID 1, PPID 0, systemd. Wrapper 종료 후 데몬 46752의 PPID가 1로 바뀜 (사용자 보고) |
| 실험 프로필 / 자원 상한 / 정리 방법 | lab 전용, 약 3초 경과 시간 제한, 같은 JVM에서 동시 계산 1개·초과 429, finally 복원. 컴파일·프로필 차이·계산·거절·회복을 실제 확인. top q·실행 터미널 Ctrl+C로 정리하도록 안내하며 최종 수행은 미확인 |

PID는 실행할 때마다 달라질 수 있으므로 영구 환경 설정으로 재사용하지 않는다. DB·Redis·Kafka·컨테이너 등의 버전은 해당 주제를 시작할 때 추가한다.
