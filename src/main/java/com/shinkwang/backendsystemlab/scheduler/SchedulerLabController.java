package com.shinkwang.backendsystemlab.scheduler;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Profile("lab")
@RequestMapping("/lab/scheduler")
@RestController
public class SchedulerLabController {

    private static final int MAX_THREADS = 8;
    private static final long DURATION_NANOS = TimeUnit.SECONDS.toNanos(3);

    private final AtomicBoolean running = new AtomicBoolean(false);

    @PostMapping("/cpu")
    public RunResult cpu(
            @RequestParam(name = "threads", defaultValue = "1") int threads
    ) {
        // 입력값을 먼저 검사한다.
        if (threads < 1 || MAX_THREADS < threads) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "threads must be between 1 and " + MAX_THREADS
            );
        }

        // 실험 전체는 한 번에 하나만 실행한다
        if (!running.compareAndSet(false, true)) {
            throw new ResponseStatusException(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "Scheduler lab is already running"
            );
        }

        long startedAt = System.nanoTime();

        try (ExecutorService executor = Executors.newFixedThreadPool(
                threads,
                Thread.ofPlatform()
                        .name("scheduler-cpu-", 1)
                        .factory()
            )
        ) {
            List<Callable<WorkerResult>> tasks = new ArrayList<>();

            // 스레드 수와 같은 개수의 계산 작업을 준비한다.
            for (int i = 0; i < threads; i++) {
                tasks.add(this::calculate);
            }

            // 작업들을 실행하고 모두 끝날 때까지 기다린다.
            List<Future<WorkerResult>> futures = executor.invokeAll(tasks);

            List<WorkerResult> results = new ArrayList<>();

            for (Future<WorkerResult> future : futures) {
                results.add(future.get());
            }

            long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(
                    System.nanoTime() - startedAt
            );

            return new RunResult(
                    threads,
                    elapsedMillis,
                    List.copyOf(results)
            );


        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Scheduler lab interrupted",
                    e
            );
        } catch (ExecutionException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Scheduler worker failed",
                    e.getCause()
            );
        } finally {
            // executor의 종료 정리를 거친 뒤 다음 실험을 허용한다.
            running.set(false);
        }
    }

    private WorkerResult calculate() throws InterruptedException {
        long startedAt = System.nanoTime();
        long iterations = 0;
        long value = 1;

        while (System.nanoTime() - startedAt < DURATION_NANOS) {
            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException("Scheduler worker interrupted");
            }

            value = value * 31 + 7;
            iterations++;
        }

        return new WorkerResult(
                Thread.currentThread().getName(),
                iterations,
                value
        );
    }

    public record WorkerResult(
            String threadName,
            long iterations,
            long value
    ) {}

    public record RunResult(
            int threads,
            long elapsedMillis,
            List<WorkerResult> workers
    ) {}
}
