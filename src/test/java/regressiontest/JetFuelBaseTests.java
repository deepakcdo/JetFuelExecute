package regressiontest;

import com.crankuptheamps.client.Command;
import com.crankuptheamps.client.CommandId;
import com.crankuptheamps.client.HAClient;
import com.crankuptheamps.client.Message;
import com.fasterxml.jackson.databind.ObjectMapper;
import headfront.jetfuel.execute.FunctionState;
import headfront.jetfuel.execute.functions.JetFuelFunction;
import headfront.jetfuel.execute.functions.SubscriptionFunctionResponseListener;
import headfront.jetfuel.execute.impl.AmpsJetFuelExecute;
import headfront.jetfuel.execute.utils.FunctionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import static headfront.jetfuel.execute.JetFuelExecuteConstants.*;
import static junit.framework.TestCase.*;

/**
 * Created by Deepak on 23/01/2018.
 */
public class JetFuelBaseTests {

    private static Logger LOG = LoggerFactory.getLogger(JetFuelBaseTests.class);

    public long sleepValueForTest = 5000;
    public long sleepValueForSubTest = 10000;

    @Autowired
    String testInstrument;
    @Autowired
    Function<String, String> getNextFunctionId;
    @Autowired
    HAClient haClient;
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
    JetFuelFunction getNextThreePriceTicksFunction;
    @Autowired
    JetFuelFunction getNextThreePriceTicksInvalidFunction;
    @Autowired
    JetFuelFunction getMarketPriceFunction;

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
        assertTrue("Should be able to unPublish Function " + fullFunctionName,
                jetFuelExecute.unPublishFunction(function));
        latch.await(sleepValueForTest, TimeUnit.MILLISECONDS);
        Set<String> availableFunctions = jetFuelExecute.getAvailableFunctions();
//        assertEquals("We should have right number of functions ", expectedSize, availableFunctions.size());
        assertTrue("We should not have a function with name " + fullFunctionName + " but we had these functions " + availableFunctions,
                !availableFunctions.contains(fullFunctionName));
    }

    protected String callFunctionAndTest(String fullFunctionName, Object[] functionParams, long testWaitTime,
                                         int onErrorCountExpected, boolean errorSetExpected,
                                         int onCompleteCountExpected, boolean completeSetExpected,
                                         int onUdateCountExpected, int onStateChangeExpected,
                                         List<String> updateMessagesExpected, List<String> updateValuesExpected,
                                         String messageExpected, Object returnValueExpected, String exceptionMsgExpected,
                                         boolean skipFunctionExistsTests, String[] expectedStates,
                                         boolean checkMessagesAfterFunctionCall, boolean isSubFunction, int cancelAfter) throws Exception {
        int expectedMessagesForFunction = onErrorCountExpected + onCompleteCountExpected +
                onUdateCountExpected + onStateChangeExpected;
        expectedMessagesForFunction++; // This is for the original Function Request
        if (cancelAfter > 0) {
            expectedMessagesForFunction++; // This is for the cancel request
        }
        CountDownLatch waitLatch = new CountDownLatch(expectedMessagesForFunction);
        if (!skipFunctionExistsTests) {
            final Set<String> availableFunctions = jetFuelExecute.getAvailableFunctions();
            assertTrue("Function with name " + fullFunctionName + " not found we had " + availableFunctions,
                    availableFunctions.contains(fullFunctionName));
            final JetFuelFunction jetFuelFunctionToCall = jetFuelExecute.getFunction(fullFunctionName);
            assertTrue("We have a null jetFuelFunctionToCall", jetFuelFunctionToCall != null);
        }

        // set up subscription to bus to get all updates for this function
        CountDownLatch waitForSubMessages = new CountDownLatch(expectedMessagesForFunction);
        final String id = getNextFunctionId.apply(jetFuelExecute.getConnectionName());
        final String nextId = predictNextId(id);
        final String msgCreationTime = FunctionUtils.getIsoDateTime();
        List<String> messages = new ArrayList<>();
        CommandId subscribeToAllMessagesForThisFunction = null;
        if (checkMessagesAfterFunctionCall) {
            subscribeToAllMessagesForThisFunction = haClient.subscribe(m -> {
                        final String trim = m.getData().trim();
                        if (trim.length() > 0) {
                            messages.add(trim);
                            waitForSubMessages.countDown();
                        }
                    },
                    jetFuelExecute.getFunctionBusTopic(), "/ID='" + nextId + "'", 10000);
        }
        String callID;
        TestFunctionResponseListener response;
        if (isSubFunction) {
            response = new TestSubscriptionFunctionResponseListener(waitLatch);
            callID = jetFuelExecute.executeSubscriptionFunction(fullFunctionName, functionParams, (SubscriptionFunctionResponseListener) response);
            if (cancelAfter > 0) {
                Thread.sleep(cancelAfter);
                jetFuelExecute.cancelSubscriptionFunctionRequest(callID);
            }
        } else {
            response = new TestFunctionResponseListener(waitLatch);
            callID = jetFuelExecute.executeFunction(fullFunctionName, functionParams, response);
        }

        final boolean await = waitLatch.await(testWaitTime, TimeUnit.MILLISECONDS);
        if (!await) {
            LOG.error("Wait time elapsed for " + callID + " we should have for the right number of callacks");
        }

        checkFunctionResponse(response, callID, onErrorCountExpected, errorSetExpected,
                onCompleteCountExpected, completeSetExpected,
                onUdateCountExpected, onStateChangeExpected, updateMessagesExpected, updateValuesExpected,
                messageExpected, returnValueExpected, exceptionMsgExpected);
        if (checkMessagesAfterFunctionCall) {
            waitForSubMessages.await(testWaitTime, TimeUnit.MILLISECONDS);

            //check received messages from subscription
            assertEquals("Should have received " + expectedMessagesForFunction + " messages from normal subscription ",
                    expectedMessagesForFunction, messages.size());

            // do a bookmark subscription
            Command commandToSend = new Command(Message.Command.Subscribe);
            String bookmark = null;
            try {
                if (msgCreationTime.contains(FunctionUtils.NAME_SEPARATOR)) {
                    bookmark = msgCreationTime.substring(0, msgCreationTime.indexOf(FunctionUtils.NAME_SEPARATOR));
                }
            } catch (Exception e) {
                LOG.error("unable to parse " + msgCreationTime);
                throw e;
            }
            bookmark = bookmark.replace("-", "");
            bookmark = bookmark.replace(":", "");
            // go back a few minutes
            bookmark = bookmark.substring(0, bookmark.length() - 4);
            bookmark = bookmark + "0000";
            commandToSend.setBookmark(bookmark);
            commandToSend.addAckType(Message.AckType.Completed);
            commandToSend.setTopic(jetFuelExecute.getFunctionBusTopic());
            commandToSend.setFilter("/ID='" + nextId + "'");
            commandToSend.setTimeout(10000);
            List<String> bookmarkMessages = new ArrayList<>();
            CountDownLatch bookmarkLatch = new CountDownLatch(1);
            final CommandId bookmarkSub = haClient.executeAsync(commandToSend, m -> {
                if (m.getCommand() == Message.Command.Ack && m.getAckType() == Message.AckType.Completed) {
                    bookmarkLatch.countDown();
                }
                final String trim = m.getData().trim();
                if (trim.length() > 0) {
                    bookmarkMessages.add(trim);
                }
            });
            bookmarkLatch.await(testWaitTime, TimeUnit.MILLISECONDS);
            assertEquals("Should have received " + expectedMessagesForFunction + "  messages a bookmark subscription ",
                    expectedMessagesForFunction, bookmarkMessages.size());
            if (LOG.isDebugEnabled()) {
                LOG.debug("Subscription messages " + messages);
                LOG.debug("Bookmark messages " + bookmarkMessages);
            }

            String instanceOwnerFromFirstMessage = null;
            for (int i = 0; i < expectedMessagesForFunction; i++) {
                final Map<String, Object> expectedMessage = createExpectedMessage(i, expectedStates, fullFunctionName,
                        msgCreationTime, nextId, functionParams, returnValueExpected, messageExpected, exceptionMsgExpected,
                        instanceOwnerFromFirstMessage, updateMessagesExpected, updateValuesExpected, isSubFunction);
                final String messageFromSubscription = messages.get(i);
                final String messageFromBookmarkSubscription = bookmarkMessages.get(i);
                Map<String, Object> messageFromSubscriptionMap = jsonMapper.readValue(messageFromSubscription, Map.class);
                // firstmessage so fix AmpsInstanceOwner for now add this to the expected message. but see if you can find a better way
                if (i == 0) {
                    // check contains
                    assertTrue("First message should have a field 'AmpsInstanceOwner' but we had " + messageFromSubscriptionMap,
                            messageFromSubscriptionMap.containsKey("AmpsInstanceOwner"));
                    //
                    instanceOwnerFromFirstMessage = messageFromSubscriptionMap.get("AmpsInstanceOwner").toString();
                    expectedMessage.put("AmpsInstanceOwner", instanceOwnerFromFirstMessage);
                }
                // compare the subscription map
                compareCustomMap(getTextNumber(i) + "SubscriptionMessage", expectedMessage, messageFromSubscriptionMap);

                //compare the bookmark subscription map
                Map<String, Object> messageFromBookmarkSubscriptionMap = jsonMapper.readValue(messageFromBookmarkSubscription, Map.class);
                compareCustomMap(getTextNumber(i) + "BookmarkMessage", expectedMessage, messageFromBookmarkSubscriptionMap);
            }

            // unsubscribe from both subscription
            if (subscribeToAllMessagesForThisFunction != null) {
                haClient.unsubscribe(subscribeToAllMessagesForThisFunction);
            }
            haClient.unsubscribe(bookmarkSub);
        }
        return callID;
    }

    private Map<String, Object> createExpectedMessage(int i, String[] expectedStates, String fullFunctionName,
                                                      String msgCreationTime, String nextId, Object[] functionParams,
                                                      Object returnValueExpected, String messageExpected,
                                                      String exceptionMsgExpected,
                                                      String instanceOwnerFromFirstMessage,
                                                      List<String> updateMessagesExpected,
                                                      List<String> updateValuesExpected, boolean isSubFunction) throws Exception {
        Map<String, Object> message = new HashMap<>();
        message.put(MSG_CREATION_TIME, msgCreationTime);
        message.put(FUNCTION_CALLER_HOSTNAME, InetAddress.getLocalHost().getHostName());
        message.put(FUNCTION_TO_CALL, fullFunctionName);
        message.put(PUBLISH_FUNCTION_ID, nextId);
        message.put(FUNCTION_INITIATOR_NAME, jetFuelExecute.getConnectionName());
        message.put(PARAMETERS, Arrays.asList(functionParams));
        if (i == 0) {
            message.put(CURRENT_STATE, FunctionState.RequestNew.name());
            message.put(MSG_CREATION_NAME, jetFuelExecute.getConnectionName());
        } else {
            final String expectedState = expectedStates[(i - 1)];
            message.put(CURRENT_STATE, expectedState);
            if (expectedState.equalsIgnoreCase(FunctionState.Error.name())) {
                message.put(EXCEPTION_MESSAGE, exceptionMsgExpected);
                message.put(CURRENT_STATE_MSG, messageExpected);
                message.put(MSG_CREATION_NAME, fullFunctionName.substring(0, fullFunctionName.indexOf(".")));
            } else if (expectedState.equalsIgnoreCase(FunctionState.Timeout.name())) {
                message.put(CURRENT_STATE_MSG, exceptionMsgExpected);
                message.put(MSG_CREATION_NAME, instanceOwnerFromFirstMessage);
            } else if (expectedState.equalsIgnoreCase(FunctionState.Completed.name())) {
                message.put(CURRENT_STATE_MSG, messageExpected);
                message.put(RETURN_VALUE, returnValueExpected);
                if (isSubFunction) {
                    message.put(FUNCTION_UPDATE_MESSAGE, updateValuesExpected.get(updateValuesExpected.size() - 1));
                }
                message.put(MSG_CREATION_NAME, fullFunctionName.substring(0, fullFunctionName.indexOf(".")));
            } else if (expectedState.equalsIgnoreCase(FunctionState.SubUpdate.name())) {
                message.put(FUNCTION_UPDATE_MESSAGE, updateValuesExpected.get(i - 2));
                message.put(CURRENT_STATE_MSG, updateMessagesExpected.get(i - 1));
                message.put(MSG_CREATION_NAME, fullFunctionName.substring(0, fullFunctionName.indexOf(".")));
            } else if (expectedState.equalsIgnoreCase(FunctionState.SubActive.name())) {
                message.put(CURRENT_STATE_MSG, updateMessagesExpected.get(i - 1));
                message.put(MSG_CREATION_NAME, fullFunctionName.substring(0, fullFunctionName.indexOf(".")));
            } else if (expectedState.equalsIgnoreCase(FunctionState.RequestCancelSub.name())) {
                message.put(CURRENT_STATE_MSG, CANCEL_REQ_MESSAGE);
                message.put(MSG_CREATION_NAME, jetFuelExecute.getConnectionName());
                message.put(FUNCTION_UPDATE_MESSAGE, updateValuesExpected.get(1));
            } else if (expectedState.equalsIgnoreCase(FunctionState.SubCancelled.name())) {
                message.put(CURRENT_STATE_MSG, updateMessagesExpected.get(updateMessagesExpected.size() - 1));
                message.put(MSG_CREATION_NAME, fullFunctionName.substring(0, fullFunctionName.indexOf(".")));
                message.put(FUNCTION_UPDATE_MESSAGE, updateValuesExpected.get(updateValuesExpected.size() - 1));
            }
            message.put(FUNCTION_AMPS_INSTANCE_OWNER, instanceOwnerFromFirstMessage);
        }
        return message;
    }

    private void checkFunctionResponse(TestFunctionResponseListener response, String id,
                                       int onErrorCountExpected, boolean errorSetExpected,
                                       int onCompleteCountExpected, boolean completeSetExpected,
                                       int onUpdateCountExpected, int onStateChangeExpected,
                                       List<String> updateMessagesExpected, List<String> updateValuesExpected,
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
        // if the return value is of string and json then convert to map and compare
        if (returnValueExpected != null && returnValueExpected instanceof String && ((String) returnValueExpected).trim().startsWith("{")) {
            Map<String, Object> expectedMap = jsonMapper.readValue(returnValueExpected.toString(), Map.class);
            final Object returnValue = response.getReturnValue();
            if (returnValue != null && returnValue instanceof String && ((String) returnValue).trim().startsWith("{")) {
                Map<String, Object> returnedMap = jsonMapper.readValue(returnValue.toString(), Map.class);
                compareCustomMap("JsonReply", expectedMap, returnedMap);
            } else {
                assertTrue("Expected a json message back and got [" + returnValue + "] of type  " + returnValue.getClass().getCanonicalName(),
                        false);
            }
        } else {
            assertEquals("Return was not correct", returnValueExpected, response.getReturnValue());
        }
        assertEquals("Exception Message was not correct", exceptionMsgExpected, response.getException());
        if (response instanceof TestSubscriptionFunctionResponseListener) {
            TestSubscriptionFunctionResponseListener subscriptionFunctionResponse = (TestSubscriptionFunctionResponseListener) response;
            assertEquals("onUpdateCount should have been called " + onUpdateCountExpected + " time/s", onUpdateCountExpected, ((TestSubscriptionFunctionResponseListener) response).getOnSubUpdateCount());
            assertEquals("onStateChange should have been called " + onStateChangeExpected + " time/s", onStateChangeExpected, ((TestSubscriptionFunctionResponseListener) response).getOnSubStateChangeCount());
            assertEquals("Update messages should be equal", updateMessagesExpected, subscriptionFunctionResponse.getAllMessages());
            assertEquals("Update values should be equal", updateValuesExpected, subscriptionFunctionResponse.getSubscriptionUpdates());
        }
    }

    public void setRunningBothClientAndSerer(boolean runningBothClientAndSerer) {
        this.runningBothClientAndSerer = runningBothClientAndSerer;
    }

    protected String getAmpsConnectionNameToUse(String functionName) {
        if (runningBothClientAndSerer) {
            return jetFuelExecute.getConnectionName();
        } else {
            // we need to get a valid functionName so remove characters from the end till you get a name
            // I am not happy with this test as it only handles methods which have extra characters at the end.
            String connectionName = null;
            do {

                final List<String> allFunction = jetFuelExecute.findFunction(functionName);
                final List<String> function = allFunction.stream()
                        .filter(name -> name.toLowerCase().contains("server")).collect(Collectors.toList());
                if (function.size() > 0) {
                    String gotFunction = function.get(0);
                    connectionName = gotFunction.substring(0, gotFunction.indexOf(FunctionUtils.NAME_SEPARATOR));
                }
                functionName = functionName.substring(0, functionName.length() - 1);

            } while (connectionName == null);

            return connectionName;
        }
    }

    private String predictNextId(String oldId) {
        final String[] split = oldId.split(FunctionUtils.ESCAPED_NAME_SEPARATOR);
        final int i = Integer.parseInt(split[1]) + 1;
        return split[0] + FunctionUtils.NAME_SEPARATOR + i;
    }

    private void compareCustomMap(String messagePrefix, Map<String, Object> expectedMap, Map<String, Object> receivedMap) {
        expectedMap.keySet().forEach(keyToCheck -> {
            assertTrue("Expected " + messagePrefix + " map to have a key " + keyToCheck + " but received map that didnt",
                    receivedMap.containsKey(keyToCheck));
            final Object expectedValue = expectedMap.get(keyToCheck);
            final Object receivedValue = receivedMap.get(keyToCheck);
            // handle special fields
            if (keyToCheck.equals("MsgCreationTime")) {
                LocalDateTime expectedDate = LocalDateTime.parse(expectedValue.toString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                LocalDateTime receivedDate = LocalDateTime.parse(receivedValue.toString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                assertTrue("Expected " + messagePrefix + "s MsgCreationTime  '" + expectedValue + "' to be just before or equal to received MsgCreationTime '" + receivedValue + "'",
                        expectedDate.equals(receivedDate) || expectedDate.isBefore(receivedDate));
            } else {
                assertEquals("Value from " + messagePrefix + " for " + keyToCheck + " should be equal", expectedValue, receivedValue);
            }
        });
        final List<String> extraFields = receivedMap.keySet().stream().filter(key -> !expectedMap.containsKey(key)).collect(Collectors.toList());
        assertTrue("Received " + messagePrefix + " map that had extra fields " + extraFields + " this was not expected", extraFields.size() == 0);
        // check size are the sme last as the above give a better failure reason. Its actually not even required.
        assertEquals(messagePrefix + " maps should have same keys Size", expectedMap.keySet(), receivedMap.keySet());
    }

    private String getTextNumber(int i) {
        i = i + 1;
        switch (i) {
            case 1:
                return "First";
            case 2:
                return "Second";
            case 3:
                return "Third";
            case 4:
                return "Fourth";
            case 5:
                return "Fifth";
            case 6:
                return "Sixth";
            case 7:
                return "Seventh";
            case 8:
                return "Eight";
            case 9:
                return "Ninth";
            case 10:
                return "Tenth";
            default:
                return "OverTenth";
        }
    }
}
