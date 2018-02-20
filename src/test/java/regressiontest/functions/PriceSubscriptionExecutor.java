package regressiontest.functions;

import headfront.jetfuel.execute.FunctionState;
import headfront.jetfuel.execute.functions.SubscriptionExecutor;
import headfront.jetfuel.execute.functions.SubscriptionFunctionResponse;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Deepak on 19/02/2018.
 */
public class PriceSubscriptionExecutor extends SubscriptionExecutor {

    private AtomicBoolean keepRunning = new AtomicBoolean(true);
    private String callId;
    private SubscriptionFunctionResponse result;
    private int stop;
    private int count = 1;

    public PriceSubscriptionExecutor(String callId, SubscriptionFunctionResponse result, int stop) {
        this.callId = callId;
        this.result = result;
        this.stop = stop + 1;
    }

    @Override
    public void run() {
        while (keepRunning.get()) {
            try {
                Thread.sleep(1000);
                result.onSubscriptionUpdate(callId, "Sending price " + count, "" + (100.25 * count));
                count++;
                if (count == stop){
                    keepRunning.set(false);
                    result.onSubscriptionStateChanged(callId, "Subscription completed as we sent the required prices" , FunctionState.StateDone);
                }
            } catch (InterruptedException e) {
                result.onSubscriptionStateChanged(callId, "Subscription cancelled by user" , FunctionState.StateSubCancelled);
            }
        }

    }

    @Override
    public void stopSubscriptions() {
        keepRunning.set(false);
        interrupt();
    }
}
