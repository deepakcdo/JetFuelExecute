package regressiontest;

import com.fasterxml.jackson.databind.ObjectMapper;
import headfront.jetfuel.execute.functions.JetFuelFunction;
import headfront.jetfuel.execute.impl.AmpsJetFuelExecute;
import headfront.jetfuel.execute.utils.FunctionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static junit.framework.TestCase.*;

/**
 * Created by Deepak on 23/01/2018.
 */
public class JetFuelBaseTests {

    private static Logger LOG = LoggerFactory.getLogger(JetFuelBaseTests.class);

    public long sleepValueForTest = 3000;

    @Autowired
    AmpsJetFuelExecute jetFuelExecute;
    @Autowired
    JetFuelFunction updateBankStatusFunction;
    @Autowired
    JetFuelFunction updateBidOfferQuoteStatusFunction;
    @Autowired
    JetFuelFunction updateBidQuoteStatusFunction;
    @Autowired
    JetFuelFunction getLastQuoteFunction;
    @Autowired
    JetFuelFunction updateQuoteStatusFunction;
    @Autowired
    JetFuelFunction getTradingDateFunction;
    @Autowired
    ObjectMapper jsonMapper;

    protected boolean runningBothClientAndSerer = true;

    protected void publishAndCheckFunction(JetFuelFunction function) throws Exception {
        int expectedSize = jetFuelExecute.getAvailableFunctions().size() + 1;
        final String fullFunctionName = FunctionUtils.getFullFunctionName(jetFuelExecute.getConnectionName(), function.getFunctionName());
        CountDownLatch latch = new CountDownLatch(1);
        jetFuelExecute.setOnFunctionAddedListener(newFunctionCreated -> {
            if (fullFunctionName.equalsIgnoreCase(newFunctionCreated)) {
                latch.countDown();
            }
        });
        assertTrue("Should be able to create Function " + fullFunctionName, jetFuelExecute.publishFunction(function));
        latch.await(sleepValueForTest, TimeUnit.MILLISECONDS);
        Set<String> availableFunctions = jetFuelExecute.getAvailableFunctions();
        //Commented as other test might be adding and removing functions
//        assertTrue("We should have right number of function ", availableFunctions.size() == expectedSize);
        assertTrue("We should have a function with name " + fullFunctionName + " but we had these functions" + availableFunctions,
                availableFunctions.contains(fullFunctionName));
    }

    protected void checkDuplicateFunctionsCantBePublished(JetFuelFunction function) throws Exception {
        Set<String> availableFunctions = jetFuelExecute.getAvailableFunctions();
        assertTrue("We should have some function published  and we have " + availableFunctions.size() + " functions",
                availableFunctions.size() > 0);
        final String fullFunctionName = FunctionUtils.getFullFunctionName(jetFuelExecute.getConnectionName(), function.getFunctionName());
        assertTrue("We should have a function with name " + fullFunctionName + " but we only had these functions" + availableFunctions,
                availableFunctions.contains(fullFunctionName));
        assertFalse("Should not able to create Function " + fullFunctionName, jetFuelExecute.publishFunction(function));
        availableFunctions = jetFuelExecute.getAvailableFunctions();
        assertTrue("We should have some function published  and we have " + availableFunctions.size() + " functions",
                availableFunctions.size() > 0);
    }

    protected void unPublishAndCheckFunction(JetFuelFunction function) throws Exception {
        int expectedSize = jetFuelExecute.getAvailableFunctions().size() - 1;
        CountDownLatch latch = new CountDownLatch(1);
        final String fullFunctionName = FunctionUtils.getFullFunctionName(jetFuelExecute.getConnectionName(), function.getFunctionName());
        jetFuelExecute.setOnFunctionRemovedListener(functionRemoved -> {
            if (fullFunctionName.equalsIgnoreCase(functionRemoved)) {
                latch.countDown();
            }
        });
        assertTrue("Should be able to unpublish Function " + fullFunctionName,
                jetFuelExecute.unPublishFunction(function));
        latch.await(sleepValueForTest, TimeUnit.MILLISECONDS);
        Set<String> availableFunctions = jetFuelExecute.getAvailableFunctions();
//        assertEquals("We should have right number of functions ", expectedSize, availableFunctions.size());
        assertTrue("We should not have a function with name " + fullFunctionName + " but we had these functions " + availableFunctions,
                !availableFunctions.contains(fullFunctionName));
    }

    protected void callFunctionAndTest(String fullFunctionName, Object[] functionParams, long testWaitTime,
                                       int onErrorCountExpected, boolean errorSetExpected,
                                       int onCompleteCountExpected, boolean completeSetExpected,
                                       String messageExpected, Object returnValueExpected, String exceptionMsgExpected,
                                       boolean skipFunctionExistsTests) throws Exception {
        CountDownLatch waitLatch = new CountDownLatch(onErrorCountExpected + onCompleteCountExpected);
        TestFunctionResponse response = new TestFunctionResponse(waitLatch);
        if (!skipFunctionExistsTests) {
            final Set<String> availableFunctions = jetFuelExecute.getAvailableFunctions();
            assertTrue("Function with name " + fullFunctionName + " not found we had " + availableFunctions,
                    availableFunctions.contains(fullFunctionName));
            final JetFuelFunction jetFuelFunctionToCall = jetFuelExecute.getFunction(fullFunctionName);
            assertTrue("We have a null jetFuelFunctionToCall", jetFuelFunctionToCall != null);
        }
        final String callID = jetFuelExecute.executeFunction(fullFunctionName, functionParams, response);
        final boolean await = waitLatch.await(testWaitTime, TimeUnit.MILLISECONDS);
        if (!await) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Wait time elapsed for " + callID);
            }
        }
        checkResponse(response, callID, onErrorCountExpected, errorSetExpected, onCompleteCountExpected, completeSetExpected,
                messageExpected, returnValueExpected, exceptionMsgExpected);
    }

    private void checkResponse(TestFunctionResponse response, String id, int onErrorCountExpected, boolean errorSetExpected,
                               int onCompleteCountExpected, boolean completeSetExpected,
                               String messageExpected, Object returnValueExpected, String exceptionMsgExpected) throws Exception {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Checking response for " + id);
        }
        assertEquals("ID was not correct", id, response.getId());
        assertEquals("OnError should have been called " + onErrorCountExpected + " time/s", onErrorCountExpected, response.getOnErrorCount());
        assertEquals("OnError was Set incorrectly", errorSetExpected, response.isOnErrorCalled());
        assertEquals("OnComplete should have been called " + onCompleteCountExpected + " time/s", onCompleteCountExpected, response.getOnCompletedCount());
        assertEquals("OnComplete was Set incorrectly", completeSetExpected, response.isOnCompletedCalled());
        assertEquals("Message was not correct", messageExpected, response.getMessage());
        // if the return value is of strign and json then convert to map and compare
        if (returnValueExpected != null && returnValueExpected instanceof String && ((String) returnValueExpected).trim().startsWith("{")) {
            Map<String, Object> expectedaMap = jsonMapper.readValue(returnValueExpected.toString(), Map.class);
            final Object returnValue = response.getReturnValue();
            if (returnValue != null && returnValue instanceof String && ((String) returnValue).trim().startsWith("{")) {
                Map<String, Object> returnedMap = jsonMapper.readValue(returnValue.toString(), Map.class);
                assertEquals("Return json was not correct", expectedaMap, returnedMap);
            } else {
                assertTrue("Expected a json message back and got [" + returnValue + "] of type  " + returnValue.getClass().getCanonicalName(),
                        false);
            }
        } else {
            assertEquals("Return was not correct", returnValueExpected, response.getReturnValue());
        }
        assertEquals("Exception Message was not correct", exceptionMsgExpected, response.getException());
    }

    public void setRunningBothClientAndSerer(boolean runningBothClientAndSerer) {
        this.runningBothClientAndSerer = runningBothClientAndSerer;
    }
}
