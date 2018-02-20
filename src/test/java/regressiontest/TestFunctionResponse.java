package regressiontest;

import headfront.jetfuel.execute.functions.FunctionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

/**
 * Created by Deepak on 22/01/2018.
 */
public class TestFunctionResponse implements FunctionResponse {

    private static Logger LOG = LoggerFactory.getLogger(TestFunctionResponse.class);
    protected volatile boolean onCompletedCalled = false;
    protected volatile boolean onErrorCalled = false;
    protected volatile Object message = null;
    protected volatile Object returnValue = null;
    protected volatile Object exception = null;
    protected volatile String id = null;
    protected CountDownLatch latch;
    protected volatile int onCompletedCount = 0;
    protected volatile int onErrorCount = 0;

    public TestFunctionResponse(CountDownLatch latch) {
        this.latch = latch;
    }

    @Override
    public void onCompleted(String id, Object message, Object returnValue) {
        this.id = id;
        this.message = message;
        this.returnValue = returnValue;
        onCompletedCalled = true;
        onCompletedCount++;
        latch.countDown();
        if (LOG.isDebugEnabled()) {
            LOG.debug("onComplete called on " + id);
        }
    }

    @Override
    public void onError(String id, Object message, Object exception) {
        this.id = id;
        this.message = message;
        this.exception = exception;
        onErrorCalled = true;
        onErrorCount++;
        latch.countDown();
        if (LOG.isDebugEnabled()) {
            LOG.debug("onError called on " + id + " message " + message + " exception " + exception);
        }
    }

    public boolean isOnCompletedCalled() {
        return onCompletedCalled;
    }

    public boolean isOnErrorCalled() {
        return onErrorCalled;
    }

    public Object getMessage() {
        return message;
    }

    public Object getReturnValue() {
        return returnValue;
    }

    public Object getException() {
        return exception;
    }

    public int getOnCompletedCount() {
        return onCompletedCount;
    }

    public int getOnErrorCount() {
        return onErrorCount;
    }

    public String getId() {
        return id;
    }
}
