# 실제 실험 기록

[기록 템플릿](../EXPERIMENT-TEMPLATE.md)을 복사해 `YYYY-MM-DD-<단계-ID>-<짧은-이름>.md`로 저장한다. 예: `YYYY-MM-DD-CPU-01-process.md`. 같은 실험을 다른 환경에서 반복하면 파일명에 환경 ID나 순번을 붙인다.

첫 입문 실습은 질문, 명령, 관찰값, 자기 설명, 다음 행동만 짧게 남겨도 된다. 의미 있는 장애 분석은 가설·검증·재검증까지 채운다. 불확실한 원인은 불확실하다고 기록한다.

대형 덤프·바이너리·민감 데이터는 Git에 넣지 않고 필요한 요약과 재현 조건을 남긴다. 기록 파일의 상대 링크를 `docs/PROGRESS.md`에 연결한다.

## 현재 기록

| 단계 | 상태 | 내용 |
| --- | --- | --- |
| [CPU-01](2026-10-15-CPU-01-process.md) | 완료 | 환경 준비, PID/PPID, 실행 인자·jps, 재시작·재부모화 |
| [CPU-02](2026-10-15-CPU-02-baseline.md) | 완료 | 정상 CPU, Irix 모드, HTTP 요청, 전체 요약·갱신 간격 |
| [CPU-03](2026-10-15-CPU-03-compute.md) | 관찰 완료 / 정리 확인 대기 | 계산 부하, 프로필 404/200, 추가 요청 429, 전체 CPU, 3.002366초 |
| [MEMORY-01·02](2026-10-16-MEMORY-01-os-memory.md) | 완료 | 전체 RAM, top RSS/virtual, JVM Heap·Metaspace 범위 |
| [MEMORY-03](2026-10-16-MEMORY-03-references.md) | 안내 실험 완료 | Java 객체 참조, 컬렉션 보관·해제, 일시 할당과 GC 요청 전후 관찰 |
| [THREAD-01](2026-10-19-THREAD-01-process-threads.md) | 완료 | PID/TID, Linux와 JVM 스레드 목록 대조, Acceptor/Poller/worker, 마무리 퀴즈 |
| [THREAD-02](2026-10-19-THREAD-02-timed-wait.md) | 진행 중 / 시작 복습 답변 확인 | MEMORY-02·THREAD-01 복습, 계산과 sleep의 비교 준비. 실험 미실행 |

## 기록 기준

각 문서는 터미널 역할·작업 폴더·명령과 키 조작의 순서·옵션 의미·실제 결과·학습자의 설명을 담는다. 재현용 안내, 실제 출력, 사용자 보고, 미확인을 구분한다. 파일명의 날짜는 시작일이며 여러 날 진행하면 문서 안에 실제 관찰일과 UTC/KST 기준을 적는다.

파일을 저장한 것과 GitHub에 반영한 것은 다르다. 최신 커밋·푸시·Linux 동기화 여부는 [진도 문서](../PROGRESS.md)의 Git 상태에서 확인한다.
