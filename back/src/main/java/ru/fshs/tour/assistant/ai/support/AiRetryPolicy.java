package ru.fshs.tour.assistant.ai.support;

import java.util.function.Supplier;
import org.springframework.stereotype.Component;

/**
 * Placeholder retry policy for AI invocations.
 */
@Component
public class AiRetryPolicy {

    public <T> T execute(Supplier<T> supplier) {
        return supplier.get();
    }
}