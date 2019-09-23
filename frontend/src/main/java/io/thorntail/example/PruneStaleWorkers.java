package io.thorntail.example;

import org.jboss.logging.Logger;

import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.inject.Inject;
import java.util.Map;

@Singleton
public class PruneStaleWorkers {
    private static final Logger log = Logger.getLogger(PruneStaleWorkers.class);

    @Inject
    private GlobalData globalData;

    @Schedule(second = "*/5", minute = "*", hour = "*", persistent = false)
    public void pruneStaleWorkers() {
        log.debugf("%s: pruning stale workers", globalData.id);

        Map<String, WorkerUpdate> workers = globalData.data.workers;
        long now = System.currentTimeMillis();

        for (Map.Entry<String, WorkerUpdate> entry : workers.entrySet()) {
            String workerId = entry.getKey();
            WorkerUpdate update = entry.getValue();

            if (now - update.getTimestamp() > 10 * 1000) {
                workers.remove(workerId);
                log.infof("%s: pruned stale worker %s", globalData.id, workerId);
            }
        }
    }
}
