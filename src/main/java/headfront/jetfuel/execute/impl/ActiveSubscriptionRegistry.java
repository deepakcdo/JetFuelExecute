package headfront.jetfuel.execute.impl;

import headfront.jetfuel.execute.functions.SubscriptionExecutor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Deepak on 20/02/2018.
 */
public class ActiveSubscriptionRegistry {

    private static final Map<String, SubscriptionExecutor> activeSubscriptionRequests = new ConcurrentHashMap<>();

    public static String registerActiveSubscription(String id, SubscriptionExecutor subExecutor) {
        final SubscriptionExecutor oldSubscriptionExecutor = activeSubscriptionRequests.get(id);
        if (oldSubscriptionExecutor == null) {
            activeSubscriptionRequests.put(id, subExecutor);
            return null;
        }
        return "We already had an active subscription for " + id;
    }

    public static SubscriptionExecutor getActiveSubscription(String id) {
        return activeSubscriptionRequests.remove(id);
    }

    public static void closeAllActiveSubscription() {
        activeSubscriptionRequests.values().forEach(sub -> {
            sub.stopSubscriptions();
            sub.interrupt();
        });
    }
}

