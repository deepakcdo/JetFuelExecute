package headfront.jetfuel.execute.example.subscription;

import headfront.jetfuel.execute.FunctionState;
import headfront.jetfuel.execute.functions.SubscriptionExecutor;
import headfront.jetfuel.execute.functions.SubscriptionFunctionResponseListener;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Deepak on 19/02/2018.
 */
public class PriceSubscriptionExecutor extends SubscriptionExecutor {

    private AtomicBoolean keepRunning = new AtomicBoolean(true);
    private String callId;
    private SubscriptionFunctionResponseListener result;
    private int count = 1;

    public PriceSubscriptionExecutor(String callId, SubscriptionFunctionResponseListener result) {
        this.callId = callId;
        this.result = result;
    }

    @Override
    public void run() {
        while (keepRunning.get()) {
            try {
                Thread.sleep(1000);
                System.out.println("sending " + callId);
                result.onSubscriptionUpdate(callId, "Sending price " + count, "" + (100.25 * count));
                count++;
            } catch (InterruptedException e) {
                result.onSubscriptionStateChanged(callId, "Subscription cancelled by user" , FunctionState.SubCancelled);
            }
        }

    }

    @Override
    public void stopSubscriptions() {
        System.out.println("stopping " + callId);
        keepRunning.set(false);
        interrupt();
    }
}
