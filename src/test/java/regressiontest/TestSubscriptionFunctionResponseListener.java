package regressiontest;

import headfront.jetfuel.execute.FunctionState;
import headfront.jetfuel.execute.functions.SubscriptionFunctionResponseListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

/**
 * Created by Deepak on 22/01/2018.
 */
public class TestSubscriptionFunctionResponseListener extends TestFunctionResponseListener implements SubscriptionFunctionResponseListener {

    private static Logger LOG = LoggerFactory.getLogger(TestSubscriptionFunctionResponseListener.class);
    protected volatile int onSubUpdateCount = 0;
    protected volatile int onSubStateChangeCount = 0;
    protected volatile List<String> subscriptionUpdates = new ArrayList<>();
    protected volatile List<String> subscriptionStateChange = new ArrayList<>();
    protected volatile List<String> allMessages = new ArrayList<>();
    protected volatile List<String> updateMessages = new ArrayList<>();
    protected volatile List<String> stateChnagedMessages = new ArrayList<>();
    private Consumer<Integer> updateListener;

    public TestSubscriptionFunctionResponseListener(CountDownLatch latch, Consumer<Integer> updateListener) {
        super(latch);
        this.updateListener = updateListener;
    }

    @Override
    public void onSubscriptionUpdate(String id, Object message, String update) {
        this.id = id;
        this.message = message;
        allMessages.add((String) message);
        updateMessages.add((String) message);
        onSubUpdateCount++;
        subscriptionUpdates.add(update);
        if (updateListener != null) {
            updateListener.accept(onSubUpdateCount);
        }
        latch.countDown();
        if (LOG.isDebugEnabled()) {
            LOG.debug("onSubscriptionUpdate called on " + id + " message " + message + " update " + update);
        }
    }

    @Override
    public void onSubscriptionStateChanged(String id, Object message, FunctionState state) {
        this.id = id;
        this.message = message;
        allMessages.add((String) message);
        stateChnagedMessages.add((String) message);
        onSubStateChangeCount++;
        subscriptionStateChange.add(state.name());
        latch.countDown();
        if (LOG.isDebugEnabled()) {
            LOG.debug("onSubscriptionStateChanged called on " + id + " message " + message + " FunctionState " + state);
        }
    }

    public int getOnSubUpdateCount() {
        return onSubUpdateCount;
    }

    public int getOnSubStateChangeCount() {
        return onSubStateChangeCount;
    }

    public List<String> getSubscriptionUpdates() {
        return subscriptionUpdates;
    }

    public List<String> getSubscriptionStateChange() {
        return subscriptionStateChange;
    }

    public List<String> getAllMessages() {
        return allMessages;
    }

    public List<String> getUpdateMessages() {
        return updateMessages;
    }

    public List<String> getStateChnagedMessages() {
        return stateChnagedMessages;
    }

    protected void addMessage(Object message) {
        allMessages.add((String) message);
    }
}
