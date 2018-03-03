package headfront.jetfuel.execute;

import headfront.jetfuel.execute.functions.SubscriptionExecutor;

/**
 * Created by Deepak on 03/03/2018.
 */
public interface ActiveSubscriptionRegistry {

    String registerActiveServerSubscription(String id, SubscriptionExecutor subExecutor);

    SubscriptionExecutor getAndRemoveActiveServerSubscription(String id);

    SubscriptionExecutor removeActiveServerSubscription(String id);

    SubscriptionExecutor getActiveServerSubscription(String id);

    boolean isActiveServerSubscription(String id);

    boolean isActiveClientSubscription(String id);

    boolean removeActiveClientSubscription(String id);

    boolean registerActiveClientSubscription(String id);

}
