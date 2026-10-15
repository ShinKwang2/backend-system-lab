package com.shinkwang.backendsystemlab.cpu;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.concurrent.atomic.AtomicBoolean;

@Profile("lab")
@RestController
public class CpuLabController {

    private final AtomicBoolean running = new AtomicBoolean(false);

    @GetMapping("/lab/cpu")
    public String cpu() {
        if (!running.compareAndSet(false, true)) {
            throw new ResponseStatusException(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "CPU lab is already running"
            );
        }

        try {
            return Long.toString(calculateForThreeSeconds());
        } finally {
            running.set(false);

        }
    }

    private long calculateForThreeSeconds() {
        long start = System.nanoTime();
        long result = 1;

        while (System.nanoTime() - start < 3_000_000_000L) {
            result = result * 31 + 7;
        }

        return result;
    }
}
