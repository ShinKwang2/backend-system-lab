package com.shinkwang.backendsystemlab.memory;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Profile("lab")
@RestController
public class MemoryLabController {

    private static final int ARRAY_SIZE_BYTES = 1024 * 1024;
    private static final int MAX_RETAINED_ARRAYS = 4;

    private final List<byte[]> retainedArrays = new ArrayList<>();

    @PostMapping("/lab/memory/retained")
    public synchronized String retain() {
        if (retainedArrays.size() >= MAX_RETAINED_ARRAYS) {
            throw new ResponseStatusException(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "Maximum retained arrays reached"
            );
        }

        retainedArrays.add(new byte[ARRAY_SIZE_BYTES]);

        return "Retained arrays: " + retainedArrays.size();
    }

    @DeleteMapping("/lab/memory/retained")
    public synchronized String clear() {
        int removed = retainedArrays.size();
        retainedArrays.clear();

        return "Cleared retained arrays: " + removed;
    }

}
