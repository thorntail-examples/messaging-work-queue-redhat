package io.thorntail.example;

import javax.enterprise.context.ApplicationScoped;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@ApplicationScoped
public class GlobalData {
    final String id = "worker-" + UUID.randomUUID().toString().substring(0, 4);

    final AtomicInteger requestsProcessed = new AtomicInteger(0);
    final AtomicInteger processingErrors = new AtomicInteger(0);
}
