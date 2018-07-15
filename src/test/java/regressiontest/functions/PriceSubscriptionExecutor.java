package regressiontest.functions;

import headfront.jetfuel.execute.FunctionState;
import headfront.jetfuel.execute.functions.SubscriptionExecutor;
import headfront.jetfuel.execute.functions.SubscriptionFunctionResponseListener;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Deepak on 19/02/2018.
 */
public class PriceSubscriptionExecutor extends SubscriptionExecutor {

    private AtomicBoolean keepRunning = new AtomicBoolean(true);
    private String callId;
    private SubscriptionFunctionResponseListener result;
    private int stop;
    private int count = 1;

    public PriceSubscriptionExecutor(String callId, SubscriptionFunctionResponseListener result, int stop) {
        this.callId = callId;
        this.result = result;
        this.stop = stop + 1;
    }

    @Override
    public void run() {
        while (keepRunning.get()) {
            try {
                Thread.sleep(1000);
                double price = 100.25 * count;
                result.onSubscriptionUpdate(callId, Optional.empty(), "Sending price " + count, "" + price);
                count++;
                if (count == stop) {
                    keepRunning.set(false);
                    result.onCompleted(callId, Optional.empty(), "Subscription completed as we sent the required prices", "" + price);
                }
            } catch (InterruptedException e) {
                result.onSubscriptionStateChanged(callId, Optional.empty(), "Subscription cancelled by user", FunctionState.SubCancelled);
            }
        }

    }

    @Override
    public void stopSubscriptions() {
        keepRunning.set(false);
        interrupt();
    }
}
