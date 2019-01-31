package headfront.jetfuel.execute.impl;

import headfront.jetfuel.execute.FunctionAccessType;
import headfront.jetfuel.execute.FunctionExecutionType;
import headfront.jetfuel.execute.FunctionState;
import headfront.jetfuel.execute.JetFuelExecute;
import headfront.jetfuel.execute.functions.FunctionParameter;
import headfront.jetfuel.execute.functions.FunctionResponseListener;
import headfront.jetfuel.execute.functions.JetFuelFunction;
import headfront.jetfuel.execute.functions.SubscriptionFunctionResponseListener;
import headfront.jetfuel.execute.impl.functions.AverageCalc;
import headfront.jetfuel.execute.impl.functions.PricePublisher;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;


public class DefaultJetFuelExecuteServiceTest {

    private static DefaultJetFuelExecuteService jetFuelExecuteInSameThread = null;
    private static DefaultJetFuelExecuteService jetFuelExecuteInDifferentThread = null;
    private static AtomicInteger idCounter = new AtomicInteger(0);


    @BeforeClass
    public static void setUp() {
        jetFuelExecuteInSameThread = new DefaultJetFuelExecuteService();
        jetFuelExecuteInSameThread.setFunctionIDGenerator(in -> in + "-" + idCounter.get());
        jetFuelExecuteInDifferentThread = new DefaultJetFuelExecuteService();
        jetFuelExecuteInDifferentThread.setFunctionIDGenerator(in -> in + "-" + idCounter.get());
        jetFuelExecuteInDifferentThread.setExecuteOnDifferentThread(true);
        createAverageFunction();
        createPricePublisherFunction(jetFuelExecuteInSameThread);
        createPricePublisherFunction(jetFuelExecuteInDifferentThread);
    }

    private static void createAverageFunction() {
        FunctionParameter paramA = new FunctionParameter("firstParam", Integer.class, "First Number to Add");
        FunctionParameter paramB = new FunctionParameter("secondParam", Integer.class, "Second Number to Add");
        List<FunctionParameter> parameters = new ArrayList<>();
        parameters.add(paramA);
        parameters.add(paramB);
        JetFuelFunction function = new JetFuelFunction("Average", "Calculates Average of two numbers", parameters,
                Integer.class, "Returns the average of the two numbers", new AverageCalc(), FunctionAccessType.Refresh,
                FunctionExecutionType.RequestResponse);
        jetFuelExecuteInSameThread.publishFunction(function);
        jetFuelExecuteInDifferentThread.publishFunction(function);
    }

    private static void createPricePublisherFunction(DefaultJetFuelExecuteService jetFuelExecute) {
        FunctionParameter paramA = new FunctionParameter("instID", String.class, "Instrument ID");
        List<FunctionParameter> parameters = new ArrayList<>();
        parameters.add(paramA);
        final PricePublisher<Object> objectPricePublisher = new PricePublisher<>();
        JetFuelFunction function = new JetFuelFunction("PriceUpdates", "Sends Price updates for Instruments", parameters,
                String.class, "Returns the lastPrice", objectPricePublisher, FunctionAccessType.Refresh,
                FunctionExecutionType.Subscription);
        objectPricePublisher.setActiveSubscriptionFactory(jetFuelExecute.getSubscriptionRegistry());
        jetFuelExecute.publishFunction(function);
    }


    @Test
    public void testDefaultJetFuelExecuteFunctionCall() throws InterruptedException {
        //same Thread JetFuelExecutor
        idCounter.set(1);
        testAverageFunction(99, 88, "Average is 93", "MOCK-1", jetFuelExecuteInSameThread);
        idCounter.set(1999);
        testAverageFunction(2, 8, "Average is 5", "MOCK-1999", jetFuelExecuteInSameThread);
        idCounter.set(45);
        testAverageFunction(1000, 3, "Average is 501", "MOCK-45", jetFuelExecuteInSameThread);

        //same Thread Different JetFuelExecutor
        idCounter.set(11);
        testAverageFunction(99, 88, "Average is 93", "MOCK-11", jetFuelExecuteInDifferentThread);
        idCounter.set(19991);
        testAverageFunction(2, 8, "Average is 5", "MOCK-19991", jetFuelExecuteInDifferentThread);
        idCounter.set(451);
        testAverageFunction(1000, 3, "Average is 501", "MOCK-451", jetFuelExecuteInDifferentThread);
    }

    private void testAverageFunction(int a, int b, String returnValue, String id, JetFuelExecute jetFuelExecute) throws InterruptedException {
        AtomicReference completedValue = new AtomicReference();
        AtomicBoolean completed = new AtomicBoolean();
        CountDownLatch latch = new CountDownLatch(1);
        String functionID = jetFuelExecute.executeFunction(jetFuelExecute.getConnectionName() + ".Average",
                new Object[]{a, b}, new FunctionResponseListener() {
                    @Override
                    public void onCompleted(String id, Map<String, Object> responseMap, Object message, Object returnValue) {
                        completedValue.set(returnValue);
                        completed.set(true);
                        latch.countDown();
                    }

                    @Override
                    public void onError(String id, Map<String, Object> responseMap, Object message, Object exception) {
                        completed.set(false);
                        latch.countDown();
                    }
                });
        assertEquals("Function Id is incorrect for parameters " + a + " and " + b, id, functionID);
        latch.await(1, TimeUnit.SECONDS);
        assertEquals("Function Returned successfully", true, completed.get());
        assertEquals("Function Returned right value", returnValue, completedValue.get());
    }

    @Test
    public void testDefaultJetFuelExecuteSubscriptionFunctionCall() throws InterruptedException {

        //same Thread JetFuelExecutor
        testSubscriptionFunctionCall(333, jetFuelExecuteInSameThread);

        //same Thread Different JetFuelExecutor
        testSubscriptionFunctionCall(3331, jetFuelExecuteInDifferentThread);
    }

    private void testSubscriptionFunctionCall(int id,  JetFuelExecute jetFuelExecute)throws InterruptedException{
        idCounter.set(id);
        String expectedId = "MOCK-" + id;
        AtomicReference<List> updatesValue = new AtomicReference<>();
        updatesValue.set(new ArrayList());
        AtomicBoolean subscribed = new AtomicBoolean();
        AtomicBoolean cancelled = new AtomicBoolean();
        AtomicInteger updateCount = new AtomicInteger();
        CountDownLatch updateLatch = new CountDownLatch(10);
        CountDownLatch cancelLatch = new CountDownLatch(10);

        String functionID = jetFuelExecute.executeSubscriptionFunction(jetFuelExecute.getConnectionName() + ".PriceUpdates",
                new Object[]{"FTSE100"}, new SubscriptionFunctionResponseListener() {
                    @Override
                    public void onSubscriptionUpdate(String id, Map<String, Object> responseMap, Object message, String update) {
                        updatesValue.get().add(update);
                        updateCount.incrementAndGet();
                        updateLatch.countDown();
                    }

                    @Override
                    public void onSubscriptionStateChanged(String id, Map<String, Object> responseMap, Object message, FunctionState state) {
                        if (state == FunctionState.SubActive) {
                            subscribed.set(true);
                        }
                        if (state == FunctionState.SubCancelled) {
                            cancelled.set(true);
                            cancelLatch.countDown();
                        }
                    }

                    @Override
                    public void onCompleted(String id, Map<String, Object> responseMap, Object message, Object returnValue) {
                    }

                    @Override
                    public void onError(String id, Map<String, Object> responseMap, Object message, Object exception) {

                    }
                });
        assertEquals("Function Id is correct", expectedId, functionID);
        updateLatch.await(750, TimeUnit.MILLISECONDS);
        jetFuelExecute.cancelSubscriptionFunctionRequest(functionID);
        cancelLatch.await(1, TimeUnit.SECONDS);
        List<String> expectedUpdates = Arrays.asList("100.25", "200.5", "300.75");
        assertEquals("Function Returned right Id", expectedId, functionID);
        assertEquals("Function Subscribed successfully", true, subscribed.get());
        assertEquals("Function returned right updates ", expectedUpdates, updatesValue.get());
        assertEquals("Function Cancelled successfully", true, cancelled.get());
    }

}