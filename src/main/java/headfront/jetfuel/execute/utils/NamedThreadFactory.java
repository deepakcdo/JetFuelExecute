package headfront.jetfuel.execute.utils;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Deepak on 03/02/2018.
 */
public class NamedThreadFactory implements ThreadFactory {

    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;
    private boolean appendCounter;

    public NamedThreadFactory(String namePrefix) {
        this.namePrefix = namePrefix;
    }

    public NamedThreadFactory(String namePrefix, boolean appendCounter) {
        this.namePrefix = namePrefix;
        this.appendCounter = appendCounter;
    }

    public Thread newThread(Runnable r) {
        String name = namePrefix;
        if (appendCounter) {
            name = name + "-" + threadNumber.getAndIncrement();
        }
        return new Thread(r, name);
    }
}
