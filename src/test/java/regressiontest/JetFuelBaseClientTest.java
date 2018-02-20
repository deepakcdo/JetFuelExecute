package regressiontest;

import headfront.jetfuel.execute.functions.JetFuelFunction;
import headfront.jetfuel.execute.utils.FunctionUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import static junit.framework.TestCase.assertTrue;

/**
 * Created by Deepak on 21/01/2018.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/client/junitClientTest.xml"})
public class JetFuelBaseClientTest extends JetFuelBaseTests {

    public String callJetFuelFunction(String functionName, JetFuelFunction jetFuelFunction,
                                    Object[] functionParams, long testWaitTime,
                                    int onErrorCountExpected, boolean errorSetExpected,
                                    int onCompleteCountExpected, boolean completeSetExpected,
                                    String messageExpected, Object returnValueExpected,
                                    String exceptionMsgExpected, boolean skipFunctionExistsTests, String[] expectedSates,
                                    boolean checkMessagesAfterFunctionCall) throws Exception {
        String fullFunctionName = FunctionUtils.getFullFunctionName(jetFuelExecute.getConnectionName(), functionName);
        if (runningBothClientAndSerer) {
//            unPublishAndCheckFunction(jetFuelFunction);
            if (jetFuelExecute.getFunction(fullFunctionName) == null) {
                publishAndCheckFunction(jetFuelFunction);
            }
        } else {
            if (!skipFunctionExistsTests) {
                final List<String> functions = jetFuelExecute.findFunction(functionName);
                // Here we might be running several servers so pick a server functions
                final List<String> serverFunctions = functions.stream().filter(name -> name.startsWith("JunitServerTest")).collect(Collectors.toList());
                assertTrue("We have atleast function that ends with " + functionName + " but we had " + serverFunctions,
                        serverFunctions.size() >= 1);
                fullFunctionName = serverFunctions.get(0);
            } else {
                final String connectionName = getAmpsConnectionNameToUse(functionName);
                fullFunctionName = connectionName + FunctionUtils.NAME_SEPARATOR + functionName;
            }
        }
        final String callID = callFunctionAndTest(fullFunctionName, functionParams, testWaitTime,
                onErrorCountExpected, errorSetExpected,
                onCompleteCountExpected, completeSetExpected,
                messageExpected, returnValueExpected, exceptionMsgExpected, skipFunctionExistsTests, expectedSates, checkMessagesAfterFunctionCall);
        if (runningBothClientAndSerer) {
            unPublishAndCheckFunction(jetFuelFunction);
        }
        return callID;
    }

    @Test
    public void callGetTradingDateWithNoParamsAndGetPositiveResponse() throws Exception {
        String expectedMsg = "Sending date";
        callJetFuelFunction("getTradingDate", getTradingDateFunction,
                new Object[]{}, sleepValueForTest,
                0, false,
                1, true,
                expectedMsg, "20180225", null, false,
                new String[]{"StateDone"}, true);
    }

    @Test
    public void callUpdateBankStatusAndGetPositiveResponse() throws Exception {
        String expectedMsg = "Deepak is authorised, Bank status is ON";
        callJetFuelFunction("updateBankStatus", updateBankStatusFunction,
                new Object[]{"Deepak", true}, sleepValueForTest,
                0, false,
                1, true,
                expectedMsg, true, null, false, new String[]{"StateDone"}, true);
    }

    @Test
    public void callUpdateBankStatusAndGetNullPointer() throws Exception {
        String expectedMsg = "Unable to process Function call";
        String expectedErrorMsg = "null java.lang.NullPointerException";
        callJetFuelFunction("updateBankStatus", updateBankStatusFunction,
                new Object[]{"Amanda", true}, sleepValueForTest,
                1, true,
                0, false,
                expectedMsg, null, expectedErrorMsg, false, new String[]{"StateError"}, true);
    }

    @Test
    public void callUpdateBankStatusAndGetNegativeResponse() throws Exception {
        String expectedMsg = "Fred is not authorised";
        callJetFuelFunction("updateBankStatus", updateBankStatusFunction,
                new Object[]{"Fred", true}, sleepValueForTest,
                0, false,
                1, true,
                expectedMsg, false, null, false, new String[]{"StateDone"}, true);
    }

    @Test
    public void callUpdatebankStatusAndGetTimeOutResponse() throws Exception {
        String expectedMsg = "Function Timeout";
        String expectedExceptionMsg = "Function publisher that published this function is not available now.";
        callJetFuelFunction("updateBankStatus", updateBankStatusFunction,
                new Object[]{"Lucy", true}, 5000 + sleepValueForTest,
                1, true, 0, false,
                expectedMsg, null, expectedExceptionMsg, false, new String[]{"StateError"},
                false);
    }

    // not sure what to do here @todo review behaviour
    @Test
    public void callFunctionSuccessFullAndGetTimeOutThenCorrectResponse() throws Exception {
//        String expectedMsg = "Function Timeout";
        String expectedMsg = "Sarah is authorised, Bank status is ON";
        String expectedExecptionMsg = "Function publisher that published this function is not available now.";
        callJetFuelFunction("updateBankStatus", updateBankStatusFunction,
                new Object[]{"Sarah", true}, 8000 + sleepValueForTest,
                1, true,
                1, true, expectedMsg, true, expectedExecptionMsg,
                false, new String[]{"StateTimeout", "StateDone"}, true);
//                0,false, expectedMsg, null, expectedExecptionMsg, false);
    }

    @Test
    public void callFunctionWithANameThatDoesNotExists() throws Exception {
        String invalidFunction = "updateBankStatusXXX";
        String expectedMsg = "Function " + getAmpsConnectionNameToUse(invalidFunction) + ".updateBankStatusXXX(String, Boolean) is not available";
        callJetFuelFunction(invalidFunction, updateBankStatusFunction,
                new Object[]{"James", true}, sleepValueForTest,
                1, true, 0,
                false, expectedMsg, null, null, true, new String[]{},
                false);
    }

    @Test
    public void callFunctionWithAParameterSetThatDoesNotExists() throws Exception {
        String expectedMsg = "Validation failed.";
        String expectedExecptionMsg = "Parameter at index 2 was 55 with type class java.lang.Integer we expected class java.lang.Boolean";
        callJetFuelFunction("updateBankStatus", updateBankStatusFunction,
                new Object[]{"James", 55}, sleepValueForTest,
                1, true, 0,
                false, expectedMsg, null, expectedExecptionMsg, true, new String[]{}, false);
    }

    @Test
    public void callFunctionExecuteOnAFunctionThatIsOfTypeSubscriptionShouldFail() throws Exception {
        callJetFuelFunction("getNextThreePriceTicksInvalid", getNextThreePriceTicksInvalid,
                new Object[]{"DE00012312"}, sleepValueForTest,
                0, false, 0,
                false, null, null, null,
                false, new String[]{}, false);


//
//
//        JetFuelFunction jetFuelFunction = getNextThreePriceTicksInvalid;
//        String functionName = getNextThreePriceTicks.getFunctionName();
//        String fullFunctionName = FunctionUtils.getFullFunctionName(jetFuelExecute.getConnectionName(), functionName);
//        if (runningBothClientAndSerer) {
////            unPublishAndCheckFunction(jetFuelFunction);
//            if (jetFuelExecute.getFunction(fullFunctionName) == null) {
//                publishAndCheckFunction(jetFuelFunction);
//            }
//        } else {
//                final List<String> functions = jetFuelExecute.findFunction(functionName);
//                // Here we might be running several servers so pick a server functions
//                final List<String> serverFunctions = functions.stream().filter(name -> name.startsWith("JunitServerTest")).collect(Collectors.toList());
//                assertTrue("We have atleast function that ends with " + functionName + " but we had " + serverFunctions,
//                        serverFunctions.size() >= 1);
//                fullFunctionName = serverFunctions.get(0);
//        }
//        final String callID = jetFuelExecute.executeFunction(fullFunctionName, new Object[]{"James"}, new TestFunctionResponse(new CountDownLatch(0)));
//        assertTrue("This should return null", callID == null);
//        if (runningBothClientAndSerer) {
//            unPublishAndCheckFunction(jetFuelFunction);
//        }
    }

    @Test
    public void callFunctionWithANameAndParameterSetThatDoesNotExists() throws Exception {
        String invalidFunction = "updateBankStatusXXX";
        String expectedMsg = "Function " + getAmpsConnectionNameToUse(invalidFunction) + ".updateBankStatusXXX(String, Integer) is not available";
        callJetFuelFunction(invalidFunction, updateBankStatusFunction,
                new Object[]{"James", 55}, sleepValueForTest,
                1, true, 0,
                false, expectedMsg, null, null, true, new String[]{}, false);
    }

    @Test
    public void callFunctionWithUnSupportedParameter() throws Exception {
        String invalidFunction = "updateBankStatusXXX";
        String expectedMsg = "Function " + getAmpsConnectionNameToUse(invalidFunction) + ".updateBankStatusXXX(String, BigDecimal) is not available";
        callJetFuelFunction(invalidFunction, updateBankStatusFunction,
                new Object[]{"James", new BigDecimal(45)}, sleepValueForTest,
                1, true, 0,
                false, expectedMsg, null, null, true,
                new String[]{}, false);
    }

    @Test
    public void callUpdateQuoteFunctions10Times() throws Exception {
        String expectedMsg = "Quote update was successful";
        // publish 10 prices
        double lastBid = 0;
        double lastOffer = 0;
        double offset = 56;
        String lastCallId = null;
        for (int i = 0; i < 10; i++) {
            lastBid = offset + i;
            lastOffer = offset + 1 + i;
            lastCallId = callJetFuelFunction("updateQuotePrice", updateBidOfferQuoteStatusFunction,
                    new Object[]{"Deepak", testInstrument, lastBid, lastOffer}, sleepValueForTest,
                    0, false, 1, true,
                    expectedMsg, true, null, false, new String[]{"StateDone"}, true);
        }
        // check the last price is correct
        expectedMsg = "Quote found for inst " + testInstrument;
        String functionName = "getLastQuote";
        String responseJson = "{\"FunctionID\":\"" + lastCallId + "\",\"BidPrice\":"
                + lastBid + ",\"Trader\":\"Deepak\",\"ID\":\"" + testInstrument + "\",\"OfferPrice\":" + lastOffer + "}";
        callJetFuelFunction(functionName, getLastQuoteFunction,
                new Object[]{testInstrument}, sleepValueForTest,
                0, false,
                1, true,
                expectedMsg, responseJson, null, false, new String[]{"StateDone"}, true);
    }

}
