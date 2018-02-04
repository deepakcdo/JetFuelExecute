package regressiontest;

import headfront.jetfuel.execute.functions.JetFuelFunction;
import headfront.jetfuel.execute.utils.FunctionUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import static junit.framework.TestCase.assertTrue;

/**
 * Created by Deepak on 21/01/2018.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/client/junitClientTest.xml"})
public class JetFuelBaseClientTest extends JetFuelBaseTests {

    public void callJetFuelFunction(String functionName, JetFuelFunction jetFuelFunction,
                                    Object[] functionParams, long testWaitTime,
                                    int onErrorCountExpected, boolean errorSetExpected,
                                    int onCompleteCountExpected, boolean completeSetExpected,
                                    String messageExpected, Object returnValueExpected,
                                    String exceptionMsgExpected, boolean skipFunctionExistsTests) throws Exception {
        String fullFunctionName = FunctionUtils.getFullFunctionName(jetFuelExecute.getConnectionName(), functionName);
        if (runningBothClientAndSerer) {
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
        callFunctionAndTest(fullFunctionName, functionParams, testWaitTime,
                onErrorCountExpected, errorSetExpected,
                onCompleteCountExpected, completeSetExpected,
                messageExpected, returnValueExpected, exceptionMsgExpected, skipFunctionExistsTests);
        if (runningBothClientAndSerer) {
            unPublishAndCheckFunction(jetFuelFunction);
        }
    }

    @Test
    public void callGetTradingDateWithNoParamsAndGetPositiveResponse() throws Exception {
        String expectedMsg = "Sending date";
        callJetFuelFunction("getTradingDate", getTradingDateFunction,
                new Object[]{}, sleepValueForTest,
                0, false,
                1, true,
                expectedMsg, "20180225", null, false);
    }

    @Test
    public void callUpdateBankStatusAndGetPositiveResponse() throws Exception {
        String expectedMsg = "Deepak is authorised, Bank status is ON";
        callJetFuelFunction("updateBankStatus", updateBankStatusFunction,
                new Object[]{"Deepak", true}, sleepValueForTest,
                0, false,
                1, true,
                expectedMsg, true, null, false);
    }

    @Test
    public void callUpdateBankStatusAndGetNullPointer() throws Exception {
        String expectedMsg = "Unable to process Function call";
        String expectedErrorMsg = "null java.lang.NullPointerException";
        callJetFuelFunction("updateBankStatus", updateBankStatusFunction,
                new Object[]{"Amanda", true}, sleepValueForTest,
                1, true,
                0, false,
                expectedMsg, null, expectedErrorMsg, false);
    }

    @Test
    public void callUpdateBankStatusAndGetNegativeResponse() throws Exception {
        String expectedMsg = "Fred is not authorised";
        callJetFuelFunction("updateBankStatus", updateBankStatusFunction,
                new Object[]{"Fred", true}, sleepValueForTest,
                0, false,
                1, true,
                expectedMsg, false, null, false);
    }

    @Test
    public void callUpdatebankStatusAndGetTimeOutResponse() throws Exception {
        String expectedMsg = "Function Timeout";
        String expectedExceptionMsg = "Function publisher that published this function is not available now.";
        callJetFuelFunction("updateBankStatus", updateBankStatusFunction,
                new Object[]{"Lucy", true}, 5000 + sleepValueForTest,
                1, true, 0, false,
                expectedMsg, null, expectedExceptionMsg, false);
    }

    // not sure what to do here @todo review behaviour
    @Test
    public void callFunctionSuccessFullAndGetTimeOutThenCorrectResponse() throws Exception {
        String expectedMsg = "Function Timeout";
//        String expectedMsg = "Sarah is authorised, Bank status is ON";
        String expectedExecptionMsg = "Function publisher that published this function is not available now.";
        callJetFuelFunction("updateBankStatus", updateBankStatusFunction,
                new Object[]{"Sarah", true}, 8000 + sleepValueForTest,
                1, true,
//                1, true,expectedMsg, true, expectedExecptionMsg, false);
                0,false, expectedMsg, null, expectedExecptionMsg, false);
    }

    @Test
    public void callFunctionWithANameThatDoesNotExists() throws Exception {
        String invalidFunction = "updateBankStatusXXX";
        String expectedMsg = "Function " + getAmpsConnectionNameToUse(invalidFunction) + ".updateBankStatusXXX(String, Boolean) is not available";
        callJetFuelFunction(invalidFunction, updateBankStatusFunction,
                new Object[]{"James", true}, sleepValueForTest,
                1, true, 0,
                false, expectedMsg, null, null, true);
    }

    @Test
    public void callFunctionWithAParameterSetThatDoesNotExists() throws Exception {
        String expectedMsg = "Validation failed.";
        String expectedExecptionMsg = "Parameter at index 2 was 55 with type class java.lang.Integer we expected class java.lang.Boolean";
        callJetFuelFunction("updateBankStatus", updateBankStatusFunction,
                new Object[]{"James", 55}, sleepValueForTest,
                1, true, 0,
                false, expectedMsg, null, expectedExecptionMsg, true);
    }

    @Test
    public void callFunctionWithANameAndParameterSetThatDoesNotExists() throws Exception {
        String invalidFunction = "updateBankStatusXXX";
        String expectedMsg = "Function " + getAmpsConnectionNameToUse(invalidFunction) + ".updateBankStatusXXX(String, Integer) is not available";
        callJetFuelFunction(invalidFunction, updateBankStatusFunction,
                new Object[]{"James", 55}, sleepValueForTest,
                1, true, 0,
                false, expectedMsg, null, null, true);
    }

    @Test
    public void callFunctionWithUnSupportedParameter() throws Exception {
        String invalidFunction = "updateBankStatusXXX";
        String expectedMsg = "Function " + getAmpsConnectionNameToUse(invalidFunction) + ".updateBankStatusXXX(String, BigDecimal) is not available";
        callJetFuelFunction(invalidFunction, updateBankStatusFunction,
                new Object[]{"James", new BigDecimal(45)}, sleepValueForTest,
                1, true, 0,
                false, expectedMsg, null, null, true);
    }

    @Test
    public void callUpateQuoteFunctions10Times() throws Exception {
        String expectedMsg = "Quote update was successful";
        // publish 10 prices
        String instID = "DE012333444";
        double lastBid = 0;
        double lastOffer = 0;
        double offset = 56;
        for (int i = 0; i < 10; i++) {
            lastBid = offset + i;
            lastOffer = offset + 1 + i;
            callJetFuelFunction("updateQuotePrice", updateBidOfferQuoteStatusFunction,
                    new Object[]{"Deepak", instID, lastBid, lastOffer}, sleepValueForTest,
                    0, false, 1, true,
                    expectedMsg, true, null, false);
        }
        // check the last price is correct
        expectedMsg = "Quote found for inst " + instID;
        String funtionName = "getLastQuote";
        String responseJson = "{\"FunctionID\":\"" + jetFuelExecute.getConnectionName() + "\",\"BidPrice\":"
                + lastBid + ",\"Trader\":\"Deepak\",\"ID\":\"DE012333444\",\"OfferPrice\":" + lastOffer + "}";
        callJetFuelFunction(funtionName, getLastQuoteFunction,
                new Object[]{instID}, sleepValueForTest,
                0, false,
                1, true,
                expectedMsg, responseJson, null, false);
    }


    private String getAmpsConnectionNameToUse(String functionName) {
        if (runningBothClientAndSerer) {
            return jetFuelExecute.getConnectionName();
        } else {
            // we need to get a valid functionName so remove characters from the end till you get a name
            // I am not happy with this test as it only handles methods which have extra characters at the end.
            String connectionName = null;
            do {

                final List<String> function = jetFuelExecute.findFunction(functionName);
                if (function.size() > 0) {
                    String gotFunction = function.get(0);
                    connectionName = gotFunction.substring(0, gotFunction.indexOf(FunctionUtils.NAME_SEPARATOR));
                }
                functionName = functionName.substring(0, functionName.length() - 1);

            } while (connectionName == null);

            return connectionName;
        }
    }

}