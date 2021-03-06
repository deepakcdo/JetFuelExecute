package regressiontest;

import headfront.jetfuel.execute.JetFuelExecute;
import headfront.jetfuel.execute.functions.JetFuelFunction;
import headfront.jetfuel.execute.utils.FunctionUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static junit.framework.TestCase.assertTrue;

/**
 * Created by Deepak on 21/01/2018.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/client/junitClientTest.xml"})
public class JetFuelBaseClientTest extends JetFuelBaseTests {

    public String callJetFuelFunction(JetFuelExecute jetFuelExecute, String functionName, JetFuelFunction jetFuelFunction,
                                      Object[] functionParams, long testWaitTime,
                                      int onErrorCountExpected, boolean errorSetExpected,
                                      int onCompleteCountExpected, boolean completeSetExpected,
                                      int onUdateCountExpected, int onStateChangeExpected,
                                      List<String> updateMessages, List<String> updateValues,
                                      String messageExpected, Object returnValueExpected,
                                      String exceptionMsgExpected, boolean skipFunctionExistsTests, String[] expectedSates,
                                      boolean checkMessagesAfterFunctionCall, boolean isSubscription,
                                      int cancelAfter) throws Exception {
        if (functionName.startsWith("*")) {
            skipFunctionExistsTests = true;
        }
        String fullFunctionName = FunctionUtils.getFullFunctionName(jetFuelExecute.getConnectionName(), functionName);
        if (runningBothClientAndSerer && jetFuelFunction != null) {
//            unPublishAndCheckFunction(jetFuelFunction);
            if (jetFuelExecute.getFunction(fullFunctionName) == null) {
                publishAndCheckFunction(jetFuelExecute, jetFuelFunction);
            }
        } else {
            final JetFuelFunction function = jetFuelExecute.getFunction(functionName);
            if (function == null && !functionName.startsWith("*")) {
                if (!skipFunctionExistsTests) {
                    final List<String> functions = jetFuelExecute.findFunction(functionName);
                    // Here we might be running several servers so pick a server functions
                    final List<String> serverFunctions = functions.stream().filter(name -> name.startsWith("JunitServerTest")).collect(Collectors.toList());
                    assertTrue("We have atleast function that ends with " + functionName + " but we had " + serverFunctions,
                            serverFunctions.size() >= 1);
                    boolean found = false;
                    final char lastChar = getLastChar(3);
                    String lookFor = "_" + lastChar + "_";
                    for (String existingFunction : functions) {
                        if (existingFunction.endsWith(functionName) && existingFunction.contains(lookFor)) {
                            fullFunctionName = existingFunction;
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        for (String existingFunction : functions) {
                            if (existingFunction.endsWith(functionName)) {
                                fullFunctionName = existingFunction;
                                break;
                            }
                        }
                    }
                } else {
                    final String connectionName = getAmpsConnectionNameToUse(jetFuelExecute, functionName);
                    fullFunctionName = connectionName + FunctionUtils.NAME_SEPARATOR + functionName;
                }
            } else {
                fullFunctionName = functionName;
            }
        }
        final String callID = callFunctionAndTest(jetFuelExecute, fullFunctionName, functionParams, testWaitTime,
                onErrorCountExpected, errorSetExpected,
                onCompleteCountExpected, completeSetExpected, onUdateCountExpected, onStateChangeExpected, updateMessages, updateValues,
                messageExpected, returnValueExpected, exceptionMsgExpected, skipFunctionExistsTests, expectedSates,
                checkMessagesAfterFunctionCall, isSubscription, cancelAfter);
        if (runningBothClientAndSerer) {
            unPublishAndCheckFunction(jetFuelExecute, jetFuelFunction);
        }
        return callID;
    }

    @Test
    public void callGetTradingDateWithNoParamsAndGetPositiveResponse() throws Exception {
        String expectedMsg = "Sending date";
        callJetFuelFunction(getJetFuelExecute(), "getTradingDate", getTradingDateFunction,
                new Object[]{}, sleepValueForTest,
                0, false,
                1, true,
                0, 0, new ArrayList<String>(), new ArrayList<String>(),
                expectedMsg, "20180225", null, false,
                new String[]{"Completed"}, true, false, 0);
    }

    @Test
    public void callUpdateBankStatusAndGetPositiveResponse() throws Exception {
        String expectedMsg = "Deepak is authorised, Bank status is ON";
        callJetFuelFunction(getJetFuelExecute(), "updateBankStatus", updateBankStatusFunction,
                new Object[]{"Deepak", true}, sleepValueForTest,
                0, false,
                1, true,
                0, 0, new ArrayList<String>(), new ArrayList<String>(),
                expectedMsg, true, null, false,
                new String[]{"Completed"}, true, false, 0);
    }

    @Test
    public void callUpdateBankStatusAndGetErrorResponse() throws Exception {
        String expectedMsg = "Jack always throws error.";
        callJetFuelFunction(getJetFuelExecute(), "updateBankStatus", updateBankStatusFunction,
                new Object[]{"Jack", true}, sleepValueForTest,
                1, true,
                0, false,
                0, 0, new ArrayList<String>(), new ArrayList<String>(),
                expectedMsg, null, "Authorisation exception", false,
                new String[]{"Error"}, true, false, 0);
    }

    @Test
    public void callUpdateBankStatusAndGetNullPointer() throws Exception {
        String expectedMsg = "Unable to process Function call";
        String expectedErrorMsg = "null java.lang.NullPointerException";
        callJetFuelFunction(getJetFuelExecute(), "updateBankStatus", updateBankStatusFunction,
                new Object[]{"Amanda", true}, sleepValueForTest,
                1, true,
                0, false,
                0, 0, new ArrayList<String>(), new ArrayList<String>(),
                expectedMsg, null, expectedErrorMsg, false,
                new String[]{"Error"}, true, false, 0);
    }

    @Test
    public void callUpdateBankStatusAndGetNegativeResponse() throws Exception {
        String expectedMsg = "Fred is not authorised";
        callJetFuelFunction(getJetFuelExecute(), "updateBankStatus", updateBankStatusFunction,
                new Object[]{"Fred", true}, sleepValueForTest,
                0, false,
                1, true,
                0, 0, new ArrayList<String>(), new ArrayList<String>(),
                expectedMsg, false, null,
                false, new String[]{"Completed"}, true, false, 0);
    }

    @Test
    public void callUpdatebankStatusAndGetTimeOutResponse() throws Exception {
        String expectedMsg = "Function Timeout";
        String expectedExceptionMsg = "Function publisher that published this function is not available now.";
        callJetFuelFunction(getJetFuelExecute(), "updateBankStatus", updateBankStatusFunction,
                new Object[]{"Lucy", true}, 5000 + sleepValueForTest,
                1, true, 0, false,
                0, 0, new ArrayList<String>(), new ArrayList<String>(),
                expectedMsg, null, expectedExceptionMsg, false, new String[]{"Error"},
                false, false, 0);
    }

    // not sure what to do here @todo review behaviour
    @Test
    public void callFunctionSuccessFullAndGetTimeOutThenCorrectResponse() throws Exception {
//        String expectedMsg = "Function Timeout";
        String expectedMsg = "Sarah is authorised, Bank status is ON";
        String expectedExecptionMsg = "Function publisher that published this function is not available now.";
        callJetFuelFunction(getJetFuelExecute(), "updateBankStatus", updateBankStatusFunction,
                new Object[]{"Sarah", true}, 8000 + sleepValueForTest,
                1, true,
                1, true,
                0, 0, new ArrayList<String>(), new ArrayList<String>(),
                expectedMsg, true, expectedExecptionMsg,
                false, new String[]{"Timeout", "Completed"}, true,
                false, 0);
//                0,false, expectedMsg, null, expectedExecptionMsg, false);
    }

    @Test
    public void callFunctionWithANameThatDoesNotExists() throws Exception {
        String invalidFunction = "updateBankStatusXXX";
        String expectedMsg = "Function " + getAmpsConnectionNameToUse(getJetFuelExecute(), invalidFunction) + ".updateBankStatusXXX(String, Boolean) is not available";
        callJetFuelFunction(getJetFuelExecute(), invalidFunction, updateBankStatusFunction,
                new Object[]{"James", true}, sleepValueForTest,
                1, true, 0, false,
                0, 0, new ArrayList<String>(), new ArrayList<String>(),
                expectedMsg, null, null, true, new String[]{},
                false, false, 0);
    }

    @Test
    public void callFunctionWithAParameterSetThatDoesNotExists() throws Exception {
        String expectedMsg = "Validation failed.";
        String expectedExecptionMsg = "Parameter at index 2 was 55 with type class java.lang.Integer we expected class java.lang.Boolean";
        callJetFuelFunction(getJetFuelExecute(), "updateBankStatus", updateBankStatusFunction,
                new Object[]{"James", 55}, sleepValueForTest,
                1, true, 0,
                false, 0, 0,
                new ArrayList<String>(), new ArrayList<String>(),
                expectedMsg, null, expectedExecptionMsg,
                true, new String[]{}, false, false, 0);
    }

    @Test
    public void callFunctionExecuteOnAFunctionThatIsOfTypeSubscriptionShouldFail() throws Exception {
        callJetFuelFunction(getJetFuelExecute(), "getNextThreePriceTicksInvalid", getNextThreePriceTicksInvalidFunction,
                new Object[]{"DE00012312"}, sleepValueForTest,
                0, false, 0,
                false, 0, 0,
                new ArrayList<String>(), new ArrayList<String>(),
                null, null, null,
                false, new String[]{}, false, false, 0);
    }

    @Test
    public void callFunctionWithANameAndParameterSetThatDoesNotExists() throws Exception {
        String invalidFunction = "updateBankStatusXXX";
        String expectedMsg = "Function " + getAmpsConnectionNameToUse(getJetFuelExecute(), invalidFunction) + ".updateBankStatusXXX(String, Integer) is not available";
        callJetFuelFunction(getJetFuelExecute(), invalidFunction, updateBankStatusFunction,
                new Object[]{"James", 55}, sleepValueForTest,
                1, true, 0,
                false,
                0, 0, new ArrayList<String>(), new ArrayList<String>(),
                expectedMsg, null, null,
                true, new String[]{}, false, false, 0);
    }

    @Test
    public void callFunctionWithWithANullParameter() throws Exception {
        String functionName = "updateBankStatus";
        callJetFuelFunction(getJetFuelExecute(), functionName, updateBankStatusFunction,
                new Object[]{null, 55}, sleepValueForTest,
                1, true, 0,
                false,
                0, 0, new ArrayList<String>(), new ArrayList<String>(),
                "Validation failed.", null,
                "Got a null value for traderName this is not allowed",
                true, new String[]{}, false, false, 0);
    }

    @Test
    public void callFunctionWithUnSupportedParameter() throws Exception {
        String invalidFunction = "updateBankStatusXXX";
        String expectedMsg = "Function " + getAmpsConnectionNameToUse(getJetFuelExecute(), invalidFunction) + ".updateBankStatusXXX(String, BigDecimal) is not available";
        callJetFuelFunction(getJetFuelExecute(), invalidFunction, updateBankStatusFunction,
                new Object[]{"James", new BigDecimal(45)}, sleepValueForTest,
                1, true,
                0, false, 0, 0,
                new ArrayList<String>(), new ArrayList<String>(),
                expectedMsg, null, null, true,
                new String[]{}, false, false, 0);
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
            lastCallId = callJetFuelFunction(getJetFuelExecute(), "updateQuotePrice", updateBidOfferQuoteStatusFunction,
                    new Object[]{"Deepak", testInstrument, lastBid, lastOffer}, sleepValueForTest,
                    0, false, 1, true,
                    0, 0, new ArrayList<String>(), new ArrayList<String>(),
                    expectedMsg, true, null, false, new String[]{"Completed"},
                    true, false, 0);
        }
        // check the last price is correct
        expectedMsg = "Quote found for inst " + testInstrument;
        String functionName = "getLastQuote";
        String responseJson = "{\"FunctionID\":\"" + lastCallId + "\",\"BidPrice\":"
                + lastBid + ",\"Trader\":\"Deepak\",\"ID\":\"" + testInstrument + "\",\"OfferPrice\":" + lastOffer + "}";
        callJetFuelFunction(getJetFuelExecute(), functionName, getLastQuoteFunction,
                new Object[]{testInstrument}, sleepValueForTest,
                0, false,
                1, true,
                0, 0, new ArrayList<String>(), new ArrayList<String>(),
                expectedMsg, responseJson, null, false, new String[]{"Completed"},
                true, false, 0);
    }


    @Test
    public void callGetThreePricesFromMarketAndStops() throws Exception {
        String expectedMsg = "Subscription completed as we sent the required prices";
        String[] states = {"SubActive", "SubUpdate", "SubUpdate", "SubUpdate", "Completed"};
        String[] messages = {"Subscription  for DE123908 is valid", "Sending price 1", "Sending price 2", "Sending price 3", "Subscription completed as we sent the required prices"};
        String[] updates = {"100.25", "200.5", "300.75"};
        callJetFuelFunction(getJetFuelExecute(), "getNextThreePriceTicks", getNextThreePriceTicksFunction,
                new Object[]{"DE123908"}, sleepValueForSubTest,
                0, false,
                1, true,
                3, 1, Arrays.asList(messages), Arrays.asList(updates),
                expectedMsg, "300.75", null, false,
                states, true, true, 0);
    }

    @Test
    public void callMarketPriceThenCancel() throws Exception {
        String expectedMsg = "Subscription cancelled by user";
        String[] states = {"SubActive", "SubUpdate", "SubUpdate", "RequestCancelSub", "SubCancelled"};
        String[] messages = {"Subscription  for DE123908 is valid", "Sending price 1", "Sending price 2", "Subscription cancelled by user"};
        String[] updates = {"100.25", "200.5"};
        callJetFuelFunction(getJetFuelExecute(), "getMarketPrice", getMarketPriceFunction,
                new Object[]{"DE123908"}, sleepValueForSubTest,
                0, false, 0, false,
                2, 2, Arrays.asList(messages), Arrays.asList(updates),
                expectedMsg, null, null, false,
                states, true, true, 2);
    }

    @Test
    public void callGetWeekends() throws Exception {
        String expectedMsg = "Sending weekends";
        ArrayList arrayList = new ArrayList();
        arrayList.add("Saturday");
        arrayList.add("Sunday");
        callJetFuelFunction(getJetFuelExecute(), "getListOfWeekendDays", getListOfWeekendDaysFunction,
                new Object[]{"UK"}, sleepValueForTest,
                0, false,
                1, true,
                0, 0, new ArrayList<String>(), new ArrayList<String>(),
                expectedMsg, arrayList, null, false,
                new String[]{"Completed"}, true, false, 0);
    }

    @Test
    public void callGetNoOfHolidaysPerMonthFunction() throws Exception {
        String expectedMsg = "Sending Holidays";
        Map<String, Integer> holidays = new HashMap<>();
        holidays.put("Dec", 2);
        holidays.put("Jan", 1);
        callJetFuelFunction(getJetFuelExecute(), "getNoOfHolidaysPerMonth", getNoOfHolidaysPerMonthFunction,
                new Object[]{"UK"}, sleepValueForTest,
                0, false,
                1, true,
                0, 0, new ArrayList<String>(), new ArrayList<String>(),
                expectedMsg, holidays, null, false,
                new String[]{"Completed"}, true, false, 0);
    }

    @Test
    public void callPlaceOrderWithMapFunction() throws Exception {
        String expectedMsg = "Placed Buy order for 10 at price 99.94";
        Map<String, Object> orderParameters = new HashMap<>();
        orderParameters.put("side", "Buy");
        orderParameters.put("quantity", 10);
        orderParameters.put("price", 99.94);
        callJetFuelFunction(getJetFuelExecute(), "placeOrderWithMap", placeOrderWithMapFunction,
                new Object[]{orderParameters}, sleepValueForTest,
                0, false,
                1, true,
                0, 0, new ArrayList<String>(), new ArrayList<String>(),
                expectedMsg, "Order_ID_Map_99.94", null, false,
                new String[]{"Completed"}, true, false, 0);
    }

    @Test
    public void callPlaceOrderWithMapAndStringFunction() throws Exception {
        String expectedMsg = "Placed Buy order for 10 at price 99.94";
        Map<String, Object> orderParameters = new HashMap<>();
        orderParameters.put("side", "Buy");
        orderParameters.put("quantity", 10);
        orderParameters.put("price", 99.94);
        callJetFuelFunction(getJetFuelExecute(), "placeOrderWithMapAndString", placeOrderWithMapAndStringFunction,
                new Object[]{orderParameters, "BBG"}, sleepValueForTest,
                0, false,
                1, true,
                0, 0, new ArrayList<String>(), new ArrayList<String>(),
                expectedMsg, "BBG_Order_ID_Map_99.94", null, false,
                new String[]{"Completed"}, true, false, 0);
    }

     @Test
    public void callPlaceOrderWithListFunction() throws Exception {
        String expectedMsg = "Placed Buy order for 10 at price 99.94";
        List<Object> orderParameters = new ArrayList<>();
        orderParameters.add("Buy");
        orderParameters.add(10);
        orderParameters.add(99.94);
        callJetFuelFunction(getJetFuelExecute(), "placeOrderWithList", placeOrderWithListFunction,
                new Object[]{orderParameters}, sleepValueForTest,
                0, false,
                1, true,
                0, 0, new ArrayList<String>(), new ArrayList<String>(),
                expectedMsg, "Order_ID_List_99.94", null, false,
                new String[]{"Completed"}, true, false, 0);
    }

    @Test
    public void callPlaceOrderWithListAndStringFunction() throws Exception {
        String expectedMsg = "Placed Buy order for 10 at price 99.94";
        List<Object> orderParameters = new ArrayList<>();
        orderParameters.add("Buy");
        orderParameters.add(10);
        orderParameters.add(99.94);
        callJetFuelFunction(getJetFuelExecute(), "placeOrderWithListAndString", placeOrderWithListAndStringFunction,
                new Object[]{orderParameters, "TW"}, sleepValueForTest,
                0, false,
                1, true,
                0, 0, new ArrayList<String>(), new ArrayList<String>(),
                expectedMsg, "TW_Order_ID_List_99.94", null, false,
                new String[]{"Completed"}, true, false, 0);
    }

}
