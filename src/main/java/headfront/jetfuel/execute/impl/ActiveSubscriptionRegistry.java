package headfront.jetfuel.execute.impl;

import headfront.jetfuel.execute.functions.SubscriptionExecutor;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Created by Deepak on 20/02/2018.
 */
public class ActiveSubscriptionRegistry {

    private static final Map<String, SubscriptionExecutor> activeServerRequests = new ConcurrentHashMap<>();
    private static final Set<String> activeClientRequests = new ConcurrentSkipListSet<>();

    public static String registerActiveServerSubscription(String id, SubscriptionExecutor subExecutor) {
        final SubscriptionExecutor oldSubscriptionExecutor = activeServerRequests.get(id);
        if (oldSubscriptionExecutor == null) {
            activeServerRequests.put(id, subExecutor);
            return null;
        }
        return "We already had an active subscription for " + id;
    }

    public static SubscriptionExecutor getAndRemoveActiveServerSubscription(String id) {
        return activeServerRequests.remove(id);
    }

    public static SubscriptionExecutor removeActiveServerSubscription(String id) {
        return activeServerRequests.remove(id);
    }

    public static SubscriptionExecutor getActiveServerSubscription(String id) {
        return activeServerRequests.remove(id);
    }

    public static boolean isActiveServerSubscription(String id) {
        return activeServerRequests.containsKey(id);
    }

    public static boolean isActiveClientSubscription(String id) {
        return activeClientRequests.contains(id);
    }

    public static boolean removeActiveClientSubscription(String id) {
        return activeClientRequests.remove(id);
    }

    public static boolean registerActiveClientSubscription(String id) {
        return activeClientRequests.add(id);
    }

    public static void closeAllActiveSubscription() {
        activeServerRequests.values().forEach(sub -> {
            sub.stopSubscriptions();
            sub.interrupt();
        });
    }
}

