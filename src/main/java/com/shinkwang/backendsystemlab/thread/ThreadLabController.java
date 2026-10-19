package com.shinkwang.backendsystemlab.thread;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.concurrent.atomic.AtomicBoolean;

@Profile("lab")
@RestController
public class ThreadLabController {

    private final AtomicBoolean running = new AtomicBoolean(false);


    @GetMapping("/lab/thread/sleep")
    public String sleep() {
        if (!running.compareAndSet(false, true)) {
            throw new ResponseStatusException(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "Sleep lab is already running"
            );
        }

        try {
            Thread.sleep(3000L);
            return "Sleep lab ready";
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Sleep lab interrupted",
                    e
            );

        } finally {
            running.set(false);
        }
    }
}
