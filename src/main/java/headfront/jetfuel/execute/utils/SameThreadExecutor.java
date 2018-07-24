package headfront.jetfuel.execute.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Deepak on 23/07/2018.
 */
public class SameThreadExecutor {

    private static Logger LOG = LoggerFactory.getLogger(SameThreadExecutor.class);

    private ExecutorService[] executor;
    private int noOfExecutors;
    private Map<String, ExecutorService> cachedExecutor = new ConcurrentHashMap<>();
    private int currentCount = 0;

    public SameThreadExecutor() {
        this(1);
    }


    public SameThreadExecutor(int noOfExecutors) {
        executor = new ExecutorService[noOfExecutors];
        this.noOfExecutors = noOfExecutors;
        for (int i = 0; i < noOfExecutors; i++) {
            executor[i] = Executors.newSingleThreadExecutor(new NamedThreadFactory("JetFuelExecuteProcessorThread-" + i));
        }
    }

    public void processTask(String id, Runnable runnable) {
        final ExecutorService executorService = cachedExecutor.get(id);
        if (executorService != null) {
            executorService.submit(runnable);
        } else {
            final ExecutorService selectedExecutor = executor[currentCount];
            final ExecutorService oldExecutor = cachedExecutor.putIfAbsent(id, selectedExecutor);
            if (oldExecutor == null) {
                selectedExecutor.submit(runnable);
                currentCount++;
                if (currentCount == noOfExecutors) {
                    currentCount = 0;
                }
            } else {
                oldExecutor.submit(runnable);
            }
        }

    }

    public void removeCompletedTask(String id) {
        cachedExecutor.remove(id);
    }

}
