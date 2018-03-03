package headfront.jetfuel.execute.impl;

import headfront.jetfuel.execute.ActiveSubscriptionRegistry;
import headfront.jetfuel.execute.functions.SubscriptionExecutor;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Created by Deepak on 20/02/2018.
 */
public class AmpsActiveSubscriptionRegistry implements ActiveSubscriptionRegistry {

    private final Map<String, SubscriptionExecutor> activeServerRequests = new ConcurrentHashMap<>();
    private final Set<String> activeClientRequests = new ConcurrentSkipListSet<>();

    public String registerActiveServerSubscription(String id, SubscriptionExecutor subExecutor) {
        final SubscriptionExecutor oldSubscriptionExecutor = activeServerRequests.get(id);
        if (oldSubscriptionExecutor == null) {
            activeServerRequests.put(id, subExecutor);
            return null;
        }
        return "We already had an active subscription for " + id;
    }

    public SubscriptionExecutor getAndRemoveActiveServerSubscription(String id) {
        return activeServerRequests.remove(id);
    }

    public SubscriptionExecutor removeActiveServerSubscription(String id) {
        return activeServerRequests.remove(id);
    }

    public SubscriptionExecutor getActiveServerSubscription(String id) {
        return activeServerRequests.remove(id);
    }

    public boolean isActiveServerSubscription(String id) {
        return activeServerRequests.containsKey(id);
    }

    public boolean isActiveClientSubscription(String id) {
        return activeClientRequests.contains(id);
    }

    public boolean removeActiveClientSubscription(String id) {
        return activeClientRequests.remove(id);
    }

    public boolean registerActiveClientSubscription(String id) {
        return activeClientRequests.add(id);
    }

    public void closeAllActiveSubscription() {
        activeServerRequests.values().forEach(sub -> {
            sub.stopSubscriptions();
            sub.interrupt();
        });
    }
}

