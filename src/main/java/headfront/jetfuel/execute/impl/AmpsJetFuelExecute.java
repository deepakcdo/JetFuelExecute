package headfront.jetfuel.execute.impl;

import com.crankuptheamps.client.CommandId;
import com.crankuptheamps.client.ConnectionStateListener;
import com.crankuptheamps.client.HAClient;
import com.crankuptheamps.client.Message;
import com.crankuptheamps.client.exception.DisconnectedException;
import com.fasterxml.jackson.databind.ObjectMapper;
import headfront.jetfuel.execute.*;
import headfront.jetfuel.execute.functions.*;
import headfront.jetfuel.execute.utils.FunctionUtils;
import headfront.jetfuel.execute.utils.NamedThreadFactory;
import headfront.jetfuel.execute.utils.SameThreadExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static headfront.jetfuel.execute.JetFuelExecuteConstants.CANCEL_REQ_MESSAGE;
import static headfront.jetfuel.execute.utils.AssertChecks.notNull;

/**
 * Created by Deepak on 10/01/2018.
 */
public class AmpsJetFuelExecute implements JetFuelExecute {

    private static Logger LOG = LoggerFactory.getLogger(AmpsJetFuelExecute.class);

    private final HAClient ampsClient;
    private final ObjectMapper jsonMapper;
    private final String ampsConnectionName;
    private String hostName;
    private boolean checkFunctionOwner = false;
    private AtomicBoolean initialised = new AtomicBoolean(false);
    private final Map<String, JetFuelFunction> functionsReceivedFromAmps = new ConcurrentHashMap<>();
    private final Map<String, JetFuelFunction> functionsPublishedToAmps = new ConcurrentHashMap<>();
    private final Map<String, FunctionResponseListener> callBackBackLog = new ConcurrentHashMap<>();
    private final Map<String, CommandId> activePublishedFunctions = new ConcurrentHashMap<>();
    private final Set<CommandId> jetFuelActiveSubscriptions = new HashSet<>();
    private SameThreadExecutor processingThreadFactory = null;
    private final String AMPS_OPTIONS = Message.Options.SendKeys + Message.Options.NoEmpties;
    private final String AMPS_OPTIONS_WITH_OOF = Message.Options.SendKeys + Message.Options.NoEmpties + Message.Options.OOF;
    private final ActiveSubscriptionRegistry subscriptionRegistry = new AmpsActiveSubscriptionRegistry();
    private final List<OwnConnectionListener> ownConnectionListeners = new CopyOnWriteArrayList<>();
    private final ExecutorService jetFuelOwnConnectionProcessorThread = Executors.newSingleThreadExecutor(
            new NamedThreadFactory("JetFuelOwnConnectionProcessorThread"));

    private Consumer<String> onFunctionAddedListener = name -> {
    };

    private Consumer<String> onFunctionRemovedListener = name -> {
    };
    //JetFuelExecute Defaults. These can be overridden by setters before the initialise() is called
    private int noOfProcessingThreads = 10;
    private String functionTopic = "JETFUEL_EXECUTE";
    private String functionBusTopic = "JETFUEL_EXECUTE_BUS";
    private String loginTopic = "/AMPS/ClientStatus";
    private Function<String, String> functionIDGenerator = FunctionUtils::getNextID;

    public AmpsJetFuelExecute(HAClient ampsClient, ObjectMapper jsonMapper) {
        notNull(ampsClient, "ampsClient cannot be null");
        notNull(jsonMapper, "jsonMapper cannot be null");
        //@todo Extra validation check amps is connected
        this.ampsClient = ampsClient;
        this.jsonMapper = jsonMapper;
        this.ampsConnectionName = ampsClient.getName();
        try {
            this.hostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            LOG.warn("Cant get hostname so using unknown");
            this.hostName = "Unknown";
        }
    }

    @Override
    public boolean unPublishFunction(JetFuelFunction jetFuelFunction) {
        checkInitialised();
        try {
            String deleteKey = "/ID='" + jetFuelFunction.getFullFunctionName() + "'";
            ampsClient.sowDelete(getFunctionTopic(), deleteKey, 1000);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Published a delete function for " + deleteKey);
            }
            final CommandId functionSubscription = activePublishedFunctions.get(jetFuelFunction.getFullFunctionName());
            if (functionSubscription != null) {
                ampsClient.unsubscribe(functionSubscription);
            }
            functionsPublishedToAmps.remove(jetFuelFunction.getFullFunctionName());
        } catch (Exception e) {
            LOG.error("Unable to un publish Function " + jetFuelFunction.getFunctionName() + " with ID " + jetFuelFunction.getFullFunctionName(), e);
            return false;
        }
        return true;
    }

    @Override
    public void initialise() {
        if (!initialised.get()) {
            try {

                // Initialise executors
                processingThreadFactory = new SameThreadExecutor(noOfProcessingThreads);

                // Add disconnection and reconnection for our selves
                ampsClient.addConnectionStateListener(new AmpsConnectionListener());

                // Listen for all active Functions
                subscribeToPublishedFunctions();

                // Listen for function callbacks
                subscribeToFunctionCallbacks();

                initialised.set(true);

            } catch (Exception e) {
                LOG.error("Unable to initialise FunctionPublisher", e);
            }
        } else {
            LOG.error("AmpsJetFuelExecute is already initalised");
        }
    }

    @Override
    public void shutDown() {
        LOG.error("Starting to shutdown safely ");
        try {
            Set<JetFuelFunction> functionsToProcess = new HashSet<>(functionsPublishedToAmps.values());
            functionsToProcess.forEach(function -> {
                unPublishFunction(function);
            });
            if (ampsClient != null) {
                Set<CommandId> copyOfSubscriptions = new HashSet<>(jetFuelActiveSubscriptions);
                copyOfSubscriptions.forEach(command -> {
                    try {
                        ampsClient.unsubscribe(command);
                    } catch (DisconnectedException e) {
                        LOG.error("Unable to unsubscribe from command " + command, e);
                    }
                });
            }
            LOG.error("JetFuel Shutdown safely completed. We have not closed amps connection.");
        } catch (Exception e) {
            LOG.error("Unable to shutdown safely ", e);
        }
    }

    @Override
    public String getConnectionName() {
        return ampsConnectionName;
    }

    @Override
    public Set<String> getAvailableFunctions() {
        return Collections.unmodifiableSet(functionsReceivedFromAmps.keySet());
    }

    @Override
    public JetFuelFunction getFunction(String functionName) {
        return functionsReceivedFromAmps.get(functionName);
    }

    @Override
    public List<String> findFunction(String functionToLookFor) {
        return functionsReceivedFromAmps.keySet().stream().filter(name -> name.contains("." + functionToLookFor)).collect(Collectors.toList());
    }

    @Override
    public void setOnFunctionAddedListener(Consumer<String> listener) {
        this.onFunctionAddedListener = listener;
    }

    @Override
    public void setOnFunctionRemovedListener(Consumer<String> listener) {
        this.onFunctionRemovedListener = listener;
    }

    @Override
    public boolean publishFunction(JetFuelFunction jetFuelFunction) {
        checkInitialised();
        jetFuelFunction.setTransientFunctionDetatils(ampsConnectionName, hostName, FunctionUtils.getIsoDateTime());
        final String validToPublish = jetFuelFunction.isValidToPublish();
        if (validToPublish != null) {
            LOG.info("Function is not valid to publish due to " + validToPublish);
            return false;
        }
        String fullFunctionName = jetFuelFunction.getFullFunctionName();
        LOG.info("Going to create Function '" + fullFunctionName + "' with parameters " + jetFuelFunction.getFunctionParameters());
        if (checkFunctionExists(jetFuelFunction) &&
                subscribeForCallBacks(jetFuelFunction) &&
                publishFunctionDesc(jetFuelFunction)) {
            LOG.info("Published Function " + fullFunctionName + " with parameters " + jetFuelFunction.getFunctionParameters());
            functionsPublishedToAmps.put(jetFuelFunction.getFullFunctionName(), jetFuelFunction);
            return true;
        } else {
            LOG.error("Could not create Function " + fullFunctionName + " with parameters " + jetFuelFunction.getFunctionParameters());
        }
        return false;
    }

    @Override
    public String executeFunction(String functionName, Object[] functionParameters, FunctionResponseListener response) {
        checkInitialised();
        return checkAndExecuteFunction(functionName, functionParameters, response, FunctionExecutionType.RequestResponse);
    }

    @Override
    public String executeSubscriptionFunction(String functionName, Object[] functionParameters, SubscriptionFunctionResponseListener response) {
        checkInitialised();
        return checkAndExecuteFunction(functionName, functionParameters, response, FunctionExecutionType.Subscription);
    }

    @Override
    public void cancelSubscriptionFunctionRequest(String callId) {
        if (subscriptionRegistry.isActiveClientSubscription(callId)) {
            final Map<String, Object> reply = createDefaultMessageField(ampsConnectionName, CANCEL_REQ_MESSAGE, hostName, callId);
            reply.put(JetFuelExecuteConstants.CURRENT_STATE, FunctionState.RequestCancelSub);
            sendMessageToAmps("cancelSubscriptionFunctionRequest", reply, CANCEL_REQ_MESSAGE);
        } else {
            LOG.error("Got a request to cancel subscription request for id " + callId + " but there was no active subscription.");
        }
    }

    @Override
    public void registerOwnConnectionListener(OwnConnectionListener connectionListener) {
        ownConnectionListeners.add(connectionListener);
    }

    @Override
    public int getUncompletedFunctionCount() {
        return callBackBackLog.size();
    }

    @Override
    public Set<String> getUncompletedFunctionIds() {
        return Collections.unmodifiableSet(callBackBackLog.keySet());
    }

    private void rePublishFunctionDesc(JetFuelFunction jetFuelFunction) {
        checkInitialised();
        try {
            String deleteKey = "/ID='" + jetFuelFunction.getFullFunctionName() + "'";
            ampsClient.sowDelete(getFunctionTopic(), deleteKey, 1000);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Published a delete function for " + deleteKey);
            }
            publishFunctionDesc(jetFuelFunction);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Re published function " + jetFuelFunction.getFullFunctionName());
            }
        } catch (Exception e) {
            LOG.error("Unable to re publish Function " + jetFuelFunction.getFunctionName() + " with ID " + jetFuelFunction.getFullFunctionName(), e);
        }
    }

    @Override
    public void setFunctionIDGenerator(Function<String, String> functionIDGenerator) {
        this.functionIDGenerator = functionIDGenerator;
    }

    private void processReplyMessage(String id, Map<String, Object> map, String functionResponse) {
        FunctionResponseListener result = null;
        try {
            Object state = map.get(JetFuelExecuteConstants.CURRENT_STATE);
            final Object message = map.get(JetFuelExecuteConstants.CURRENT_STATE_MSG);
            final Object returnVal = map.get(JetFuelExecuteConstants.RETURN_VALUE);
            final Object exception = map.get(JetFuelExecuteConstants.EXCEPTION_MESSAGE);
            final Object functionName = map.get(JetFuelExecuteConstants.FUNCTION_TO_CALL);
            String valueMsg;
            if (state.equals(FunctionState.SubUpdate.getText())) {
                valueMsg = "',  'UpdateValue' was '" + map.get(JetFuelExecuteConstants.FUNCTION_UPDATE_MESSAGE);

            } else if (state.equals(FunctionState.Completed.getText())) {
                valueMsg = ",' return value '" + map.get(JetFuelExecuteConstants.RETURN_VALUE);

            } else {
                valueMsg = "',  'UpdateValue' was '" + map.get(JetFuelExecuteConstants.FUNCTION_UPDATE_MESSAGE) +
                        ",' return value '" + map.get(JetFuelExecuteConstants.RETURN_VALUE);
            }
            String errorMessage = "";
            if (state.equals(FunctionState.Error.getText())) {
                errorMessage = ",' exception '" + map.get(JetFuelExecuteConstants.EXCEPTION_MESSAGE) + "' ";
            }
            log("Received JetFuelExecuteFunction execution response for ID " + id + " with state '" + state +
                            "' , message '" + message + valueMsg + errorMessage,
                    " response was " + functionResponse);
            final Map<String, Object> unmodifiableMap = Collections.unmodifiableMap(map);
            result = callBackBackLog.get(id);
            if (result != null) {
                if (state != null) {
                    SubscriptionFunctionResponseListener subscriptionFunctionResponse = null;
                    if (result instanceof SubscriptionFunctionResponseListener) {
                        subscriptionFunctionResponse = (SubscriptionFunctionResponseListener) result;
                    }
                    FunctionState currentState = FunctionState.valueOf(state.toString());
                    switch (currentState) {
                        case Completed:
                            result.onCompleted(id, unmodifiableMap, message, returnVal);
                            break;
                        case Error:
                            result.onError(id, unmodifiableMap, map.get(JetFuelExecuteConstants.CURRENT_STATE_MSG),
                                    map.get(JetFuelExecuteConstants.EXCEPTION_MESSAGE));
                            break;
                        case Timeout:
                            result.onError(id, unmodifiableMap, "Function Timeout",
                                    map.get(JetFuelExecuteConstants.CURRENT_STATE_MSG));
                            break;
                        case SubUpdate:
                            subscriptionFunctionResponse.onSubscriptionUpdate(id, unmodifiableMap,
                                    map.get(JetFuelExecuteConstants.CURRENT_STATE_MSG),
                                    (String) map.get(JetFuelExecuteConstants.FUNCTION_UPDATE_MESSAGE));
                            break;
                        case SubActive:
                            subscriptionFunctionResponse.onSubscriptionStateChanged(id, unmodifiableMap,
                                    map.get(JetFuelExecuteConstants.CURRENT_STATE_MSG), FunctionState.SubActive);
                            subscriptionRegistry.registerActiveClientSubscription(id);
                            break;
                        case SubCancelled:
                            subscriptionFunctionResponse.onSubscriptionStateChanged(id, unmodifiableMap,
                                    map.get(JetFuelExecuteConstants.CURRENT_STATE_MSG), FunctionState.SubCancelled);
                            break;
                        default:
                            result.onError(id, unmodifiableMap, "Unknown state " + currentState + " and message  "
                                    + map.get(JetFuelExecuteConstants.CURRENT_STATE_MSG), null);
                            break;
                    }
                    if (currentState.isFinalState()) {
                        //only cleanup if this was not multiExecute
                        if (!functionName.toString().contains("*")) {
                            subscriptionRegistry.removeActiveClientSubscription(id);
                            callBackBackLog.remove(id);
                            processingThreadFactory.removeCompletedTask(id);
                        }
                    }
                }
            } else {
                LOG.error("Got a response for function " + id + " but we don't have a FunctionResponseListener for it");
            }
            // else wait for functionResponse
        } catch (Exception e) {
            if (result != null) {
                result.onError(id, "Unable to process functionResponse " + functionResponse + " " + e.getMessage(), e);
            }
            LOG.error("Unable to process functionResponse " + functionResponse, e);
        }
    }

    private String checkAndExecuteFunction(String functionName, Object[] functionParameters, FunctionResponseListener response,
                                           FunctionExecutionType functionExecutionType) {
        String callID = functionIDGenerator.apply(ampsConnectionName);
        try {
            JetFuelFunction jetFuelFunction = functionsReceivedFromAmps.get(functionName);
            if (functionName.startsWith("*")) {
                // here a function will not exists so check atleast one function is available as the user is trying
                // to call all functions
                final String lastPartOfFunctionName = functionName.split("\\.")[1];
                final List<String> function = findFunction(lastPartOfFunctionName);
                if (function.size() > 0) {
                    jetFuelFunction = functionsReceivedFromAmps.get(function.get(0));
                }
            }
            if (jetFuelFunction != null) {
                // check valid type of Function
                if (jetFuelFunction.getExecutionType() == functionExecutionType) {
                    Map<String, Object> functionCall = new HashMap<>();
                    functionCall.put(JetFuelExecuteConstants.FUNCTION_CALL_ID, callID);
                    functionCall.put(JetFuelExecuteConstants.FUNCTION_TO_CALL, functionName);
                    functionCall.put(JetFuelExecuteConstants.PARAMETERS, functionParameters);
                    functionCall.put(JetFuelExecuteConstants.FUNCTION_INITIATOR_NAME, ampsConnectionName);
                    functionCall.put(JetFuelExecuteConstants.FUNCTION_CALLER_HOSTNAME, hostName);
                    functionCall.put(JetFuelExecuteConstants.CURRENT_STATE, FunctionState.RequestNew);
                    functionCall.put(JetFuelExecuteConstants.MSG_CREATION_TIME, FunctionUtils.getIsoDateTime());
                    functionCall.put(JetFuelExecuteConstants.MSG_CREATION_NAME, ampsConnectionName);
                    String jsonMsg = jsonMapper.writeValueAsString(functionCall);
                    log("Sending JetFuelExecuteFunction execution request with id " +
                            callID + " to topic " + getFunctionBusTopic() + " function " + jetFuelFunction.getFullFunctionName() +
                            " with parameters " + Arrays.toString(functionParameters), " request " + jsonMsg);
                    ampsClient.publish(getFunctionBusTopic(), jsonMsg);
                    callBackBackLog.put(callID, response);
                    return callID;
                } else {
                    LOG.error("Unable to call function " + functionName + " as the executionType is " + jetFuelFunction.getExecutionType() +
                            ". For FunctionExecutionType.RequestResponse call AmpsJetFuelExecute.executeFunctions() " +
                            ", For FunctionExecutionType.Subscription call AmpsJetFuelExecute.executeSubscriptionFunction()");
                    return null;
                }
            } else {
                final String functionSignature = FunctionUtils.getFunctionSignature(functionName, functionParameters);
                LOG.error("Unable to call function " + functionSignature + " with parameter " + Arrays.toString(functionParameters) + " as it does not exist");
                response.onError(callID, "Function " + functionSignature + " is not available", null);
                return callID;
            }
        } catch (Exception e) {
            final String functionSignature = FunctionUtils.getFunctionSignature(functionName, functionParameters);
            LOG.error("Unable to call function " + functionSignature + " with parameter " + Arrays.toString(functionParameters) + " as it does not exist", e);
            response.onError(callID, "Function " + functionSignature + " is not available", e);
            return callID;
        }
    }

    private void subscribeToFunctionCallbacks() throws Exception {
        String filter = "/" + JetFuelExecuteConstants.FUNCTION_CALLER_HOSTNAME + "= '" + hostName + "' and /" +
                JetFuelExecuteConstants.FUNCTION_INITIATOR_NAME + "= '" + ampsConnectionName + "' and /" +
                JetFuelExecuteConstants.CURRENT_STATE + " NOT IN ('" + FunctionState.RequestNew +
                "', '" + FunctionState.RequestCancelSub + "')";

        final CommandId subscribe = ampsClient.subscribe(m -> {
                    final String functionResponse = m.getData().trim();
                    if (functionResponse.length() > 0) {
                        try {
                            final Map<String, Object> map = jsonMapper.readValue(functionResponse, Map.class);
                            final String id = map.get(JetFuelExecuteConstants.PUBLISH_FUNCTION_ID).toString();
                            processingThreadFactory.processTask(id, () ->
                                    processReplyMessage(id, map, functionResponse));
                        } catch (IOException e) {
                            LOG.error("Unable to parse message  " + functionResponse, e);
                        }
                    }
                },
                getFunctionBusTopic(), filter, AMPS_OPTIONS, 5000);
        jetFuelActiveSubscriptions.add(subscribe);
        LOG.info("Placed a subscription to topic " + getFunctionBusTopic() + " with filter '" + filter + "' for JetFuel function requests");
    }

    private void subscribeToPublishedFunctions() throws Exception {
        CountDownLatch waitForResponse = new CountDownLatch(1);
        final CommandId commandId = ampsClient.sowAndSubscribe(m -> {
                    String data = m.getData().trim();
                    try {
                        if (m.getCommand() == Message.Command.GroupEnd) {
                            waitForResponse.countDown();
                        }
                        if (data.length() > 0) {
                            Map map = jsonMapper.readValue(data, Map.class);
                            final String functionId = (String) map.get(JetFuelExecuteConstants.PUBLISH_FUNCTION_ID);
                            if (m.getCommand() == Message.Command.OOF) {
                                if (LOG.isDebugEnabled()) {
                                    LOG.debug("Received a published function delete with ID " + functionId + " full details " + data);
                                }
                                functionsReceivedFromAmps.remove(functionId);
                                onFunctionRemovedListener.accept(functionId);
                            } else {
                                if (LOG.isDebugEnabled()) {
                                    LOG.debug("Received a published function with ID " + functionId + " full details " + data);
                                }
                                final JetFuelFunction jetFuelFunctionFromMap = FunctionUtils.createJetFuelFunctionFromMap(map);
                                functionsReceivedFromAmps.put(functionId, jetFuelFunctionFromMap);
                                onFunctionAddedListener.accept(functionId);
                            }
                        }
                    } catch (Exception e) {
                        LOG.error("Unable to process published function update from topic " + m.getTopic() + " data " + data, e);
                    }
                }, getFunctionTopic(), "",
                100, AMPS_OPTIONS_WITH_OOF, 5000);
        jetFuelActiveSubscriptions.add(commandId);
        LOG.info("Placed a sowAndSubscribe to topic " + getFunctionTopic() + " with filter '' so we know when new functions are published or removed");
        LOG.info("Waiting to download published Functions");
        boolean success = waitForResponse.await(10, TimeUnit.SECONDS);
        if (success) {
            LOG.info("Received all " + functionsReceivedFromAmps.size() + " available functions");
        } else {
            LOG.info("Received " + functionsReceivedFromAmps.size() + " functions after waiting for 10 seconds");
        }
    }

    private boolean checkFunctionExists(JetFuelFunction jetFuelFunction) {
        final String fullFunctionName = jetFuelFunction.getFullFunctionName();
        final JetFuelFunction previousFunction = functionsReceivedFromAmps.get(fullFunctionName);
        if (previousFunction != null) {
            LOG.error("Function with fullName " + fullFunctionName +
                    " already published by component " + previousFunction.getFunctionPublisherName() +
                    " from host " + previousFunction.getPublisherHostname());
            return false;
        }
        return true;
    }

    private boolean subscribeForCallBacks(final JetFuelFunction jetFuelFunction) {
        String multiExecuteFilter = "";
        if (jetFuelFunction.isAllowMultiExecute()) {
            multiExecuteFilter = "', '*." + jetFuelFunction.getFunctionName();
        }
        String filter = "/" + JetFuelExecuteConstants.FUNCTION_TO_CALL + " IN ('" + jetFuelFunction.getFullFunctionName()
                + multiExecuteFilter + "')"
                + " and /" + JetFuelExecuteConstants.CURRENT_STATE + " IN ('" + FunctionState.RequestNew
                + "', '" + FunctionState.RequestCancelSub + "')";
        try {
            final CommandId subscription = ampsClient.subscribe(m -> {
                String request = m.getData().trim();
                if (request.length() > 0) {
                    try {
                        final Map<String, Object> map = jsonMapper.readValue(request, Map.class);
                        final String id = map.get(JetFuelExecuteConstants.FUNCTION_CALL_ID).toString();
                        processingThreadFactory.processTask(id,
                                () -> processFunctionProcessRequest(m.getUserId(), id, map, request, jetFuelFunction));
                    } catch (IOException e) {
                        LOG.error("Unable to parse message  " + request, e);
                    }
                }
            }, getFunctionBusTopic(), filter, 1000);
            activePublishedFunctions.put(jetFuelFunction.getFullFunctionName(), subscription);
        } catch (Exception e) {
            LOG.error("Unable to subscribe to " + getFunctionBusTopic() + " with filter " + filter, e);
            return false;
        }
        return true;
    }

    private void processFunctionProcessRequest(String ampsFunctionCaller, String id, Map<String, Object> map,
                                               String request, JetFuelFunction jetFuelFunction) {
        try {
            map.put(JetFuelExecuteConstants.FUNCTION_RECEIVED_BY, ampsConnectionName);
            final String caller = map.get(JetFuelExecuteConstants.FUNCTION_INITIATOR_NAME).toString();
            final String currentState = map.get(JetFuelExecuteConstants.CURRENT_STATE).toString();
            final List parameters = (List) map.get(JetFuelExecuteConstants.PARAMETERS);
            log("Received JetFuelExecuteFunction execution request of type '" + currentState
                    + "' with id " + id + " from " + caller + " function " + jetFuelFunction.getFullFunctionName() +
                    " with parameters " + parameters, " request was " + request);
            String callerHostName = map.get(JetFuelExecuteConstants.FUNCTION_CALLER_HOSTNAME).toString();
            if (checkFunctionOwner) {
                if (!caller.equals(ampsFunctionCaller)) {
                    createAndSendError("Spoof Message detected, received message from " + ampsFunctionCaller + " but the message had " + caller,
                            "", id, caller, callerHostName);
                    return;
                }
            }

            if (currentState.equalsIgnoreCase(FunctionState.RequestNew.name())) {
                FunctionProcessor newExecutor = (FunctionProcessor) jetFuelFunction.getExecutor();
                LOG.info("Processing JetFuelExecuteFunction execution request with id " + id + " functionName " + jetFuelFunction.getFullFunctionName() + " with parameter " + parameters + " from caller " + caller);
                if (jetFuelFunction.getExecutionType() == FunctionExecutionType.RequestResponse) {
                    newExecutor.validateAndExecuteFunction(id, jetFuelFunction.getFunctionParameters(),
                            parameters, Collections.unmodifiableMap(map), new FunctionResponseListener() {
                                @Override
                                public void onCompleted(String id, Map<String, Object> map, Object message, Object returnValue) {
                                    createAndSendComplete(caller, message, returnValue, callerHostName, id);
                                }

                                @Override
                                public void onError(String id, Map<String, Object> map, Object message, Object exception) {
                                    createAndSendError(message, exception, id, caller, callerHostName);
                                }
                            });
                } else if (jetFuelFunction.getExecutionType() == FunctionExecutionType.Subscription) {
                    newExecutor.setActiveSubscriptionFactory(subscriptionRegistry);
                    newExecutor.validateAndExecuteFunction(id, jetFuelFunction.getFunctionParameters(),
                            parameters, Collections.unmodifiableMap(map), new SubscriptionFunctionResponseListener() {
                                @Override
                                public void onSubscriptionUpdate(String id, Map<String, Object> map, Object message, String update) {
                                    createAndSendSubscriptionUpdate(caller, id, update, message, callerHostName);
                                }

                                @Override
                                public void onSubscriptionStateChanged(String id, Map<String, Object> map, Object message, FunctionState state) {
                                    createAndSendSubscriptionStateChanged(caller, id, state, message, callerHostName);
                                }

                                @Override
                                public void onCompleted(String id, Map<String, Object> map, Object message, Object returnValue) {
                                    createAndSendComplete(caller, message, returnValue, callerHostName, id);
                                }

                                @Override
                                public void onError(String id, Map<String, Object> map, Object message, Object exception) {
                                    createAndSendError(message, exception, id, caller, callerHostName);
                                }
                            });
                } else {
                    LOG.error("Cant process " + id + " for function name " + jetFuelFunction.getFunctionName() + " as we are not setup for " + jetFuelFunction.getExecutionType());
                }
            } else if (currentState.equalsIgnoreCase(FunctionState.RequestCancelSub.name())) {
                final SubscriptionExecutor subscriptionExecutor = subscriptionRegistry.getAndRemoveActiveServerSubscription(id);
                if (subscriptionExecutor != null) {
                    subscriptionExecutor.stopSubscriptions();
                    subscriptionExecutor.interrupt();
                } else {
                    LOG.error("Received a cancel request for SubscriptionFunction with id '" + id + "' but we dont have an active SubscriptionExecutor for it.");
                }
            } else {
                LOG.error("Unexpected state received for function Id '" + id + "' state was '" + currentState + "' . Full message " + request);
            }
            //Handle onClientDisconnect in the future
        } catch (Exception e) {
            LOG.error("Unable to process JetFuelExecuteFunction execution request " + request, e);
        }
    }

    private Map<String, Object> createDefaultMessageField(String caller, Object message, String callerHostName, String id) {
        Map<String, Object> reply = new HashMap<>();
        reply.put(JetFuelExecuteConstants.FUNCTION_CALL_ID, id);
        reply.put(JetFuelExecuteConstants.MSG_CREATION_TIME, FunctionUtils.getIsoDateTime());
        reply.put(JetFuelExecuteConstants.MSG_CREATION_NAME, ampsConnectionName);
        reply.put(JetFuelExecuteConstants.FUNCTION_INITIATOR_NAME, caller);
        reply.put(JetFuelExecuteConstants.FUNCTION_CALLER_HOSTNAME, callerHostName);
        reply.put(JetFuelExecuteConstants.CURRENT_STATE_MSG, message);
        return reply;
    }

    private void sendMessageToAmps(String methodName, Map<String, Object> reply, Object value) {
        try {
            String json = jsonMapper.writeValueAsString(reply);
            ampsClient.deltaPublish(getFunctionBusTopic(), json);
            final FunctionState currentState = (FunctionState) reply.get(JetFuelExecuteConstants.CURRENT_STATE);
            String valueMsg;
            if (currentState.equals(FunctionState.SubUpdate)) {
                valueMsg = "',  'UpdateValue' was '" + reply.get(JetFuelExecuteConstants.FUNCTION_UPDATE_MESSAGE);

            } else if (currentState.equals(FunctionState.Completed)) {
                valueMsg = ",' return value '" + reply.get(JetFuelExecuteConstants.RETURN_VALUE);

            } else {
                valueMsg = "',  'UpdateValue' was '" + reply.get(JetFuelExecuteConstants.FUNCTION_UPDATE_MESSAGE) +
                        ",' return value '" + reply.get(JetFuelExecuteConstants.RETURN_VALUE);
            }
            String errorMessage = "";
            if (currentState.equals(FunctionState.Error)) {
                errorMessage = ",' exception '" + reply.get(JetFuelExecuteConstants.EXCEPTION_MESSAGE) + "' ";
            }
            log("Sending JetFuelExecuteFunction execution response '" + methodName +
                            "' with id '" + reply.get(JetFuelExecuteConstants.FUNCTION_CALL_ID) +
                            "',  'Message' was '" + reply.get(JetFuelExecuteConstants.CURRENT_STATE_MSG) +
                            valueMsg + errorMessage,
                    "and  json " + json);
        } catch (Exception e) {
            LOG.error("Unable to process JetFuelExecuteFunction execution request with id " +
                    reply.get(JetFuelExecuteConstants.FUNCTION_CALL_ID), e);
        }
    }

    private void createAndSendSubscriptionStateChanged(String caller, String id, FunctionState state, Object message, String callerHostName) {
        final Map<String, Object> reply = createDefaultMessageField(caller, message, callerHostName, id);
        reply.put(JetFuelExecuteConstants.CURRENT_STATE, state);
        sendMessageToAmps("onSubscriptionChanged to '" + state + "'", reply, message);
    }

    private void createAndSendSubscriptionUpdate(String caller, String id, String update, Object message, String callerHostName) {
        final Map<String, Object> reply = createDefaultMessageField(caller, message, callerHostName, id);
        reply.put(JetFuelExecuteConstants.CURRENT_STATE, FunctionState.SubUpdate);
        reply.put(JetFuelExecuteConstants.FUNCTION_UPDATE_MESSAGE, update);
        sendMessageToAmps("onSubscriptionUpdate", reply, message);
    }

    private void createAndSendComplete(String caller, Object message, Object returnValue, String callerHostName, String id) {
        final Map<String, Object> reply = createDefaultMessageField(caller, message, callerHostName, id);
        reply.put(JetFuelExecuteConstants.RETURN_VALUE, returnValue);
        reply.put(JetFuelExecuteConstants.CURRENT_STATE, FunctionState.Completed);
        sendMessageToAmps("onComplete", reply, returnValue);
    }

    private void createAndSendError(Object message, Object exception, String id, String caller, String callerHostName) {
        final Map<String, Object> reply = createDefaultMessageField(caller, message, callerHostName, id);
        reply.put(JetFuelExecuteConstants.EXCEPTION_MESSAGE, exception);
        reply.put(JetFuelExecuteConstants.CURRENT_STATE, FunctionState.Error);
        sendMessageToAmps("onError", reply, exception);
    }

    private void republishFunctions() {
        Set<JetFuelFunction> functionsToProcess = new HashSet<>(functionsPublishedToAmps.values());
        functionsToProcess.forEach(function -> {
            unPublishFunction(function);
            functionsReceivedFromAmps.remove(function.getFullFunctionName());
            publishFunction(function);
        });
    }

    private boolean publishFunctionDesc(JetFuelFunction jetFuelFunction) {
        try {
            final Map<String, Object> publishMap = FunctionUtils.createMapFromJetFuelFunction(jetFuelFunction, ampsConnectionName, hostName);
            String jsonMsg = jsonMapper.writeValueAsString(publishMap);
            ampsClient.publish(getFunctionTopic(), jsonMsg);
        } catch (Exception e) {
            LOG.error("Unable to publish Function " + jetFuelFunction.getFullFunctionName(), e);
            return false;
        }
        return true;
    }

    private void log(String mainMessage, String details) {
        if (LOG.isDebugEnabled()) {
            LOG.debug(mainMessage + details);
        } else {
            LOG.info(mainMessage);
        }
    }

    class AmpsConnectionListener implements ConnectionStateListener {
        // we only want to do this after the first disconnection so start with connected
        AtomicBoolean connected = new AtomicBoolean(true);

        @Override
        public void connectionStateChanged(int state) {
            // only do this if we published functions
            if (functionsPublishedToAmps.size() > 0) {
                if (state == ConnectionStateListener.Connected) {
                    if (!connected.get()) {
                        connected.set(true);
                        LOG.info("Detected a disconnection and reconnection to amps. We will republish all functions again");
                        Runnable republishFunctions = () -> {
                            try {
                                Thread.sleep(50000); // sleep 50 seconds for amps to reconnect fully
                                // clear received functions.
                                functionsReceivedFromAmps.clear();
                                //subscribe to functions
                                subscribeToPublishedFunctions();
                                // republish function descriptions
                                Set<JetFuelFunction> functionsToProcess = new HashSet<>(functionsPublishedToAmps.values());
                                functionsToProcess.forEach(AmpsJetFuelExecute.this::rePublishFunctionDesc);
                                ownConnectionListeners.forEach(listener -> listener.connected());
                            } catch (Exception e) {
                                LOG.error("Unable to republish functions", e);
                            }
                        };
                        jetFuelOwnConnectionProcessorThread.submit(republishFunctions);
                    }
                } else if (state == ConnectionStateListener.Disconnected) {
                    connected.set(false);
                    jetFuelOwnConnectionProcessorThread.submit(() -> {
                        ownConnectionListeners.forEach(listener -> listener.disconnected());
                    });
                }
            }
        }

    }

    public void setNoOfProcessingThreads(int noOfProcessingThreads) {
        this.noOfProcessingThreads = noOfProcessingThreads;
    }

    public void setLoginTopic(String loginTopic) {
        this.loginTopic = loginTopic;
    }

    public void setFunctionTopic(String functionTopic) {
        this.functionTopic = functionTopic;
    }

    public void setFunctionBusTopic(String functionsBusTopic) {
        this.functionBusTopic = functionsBusTopic;
    }

    public String getFunctionTopic() {
        return functionTopic;
    }

    public String getFunctionBusTopic() {
        return functionBusTopic;
    }

    private void checkInitialised() {
        if (!initialised.get()) {
            LOG.warn("AmpsJetFuelExecute was not initialised. Initializing it now.");
            initialise();
        }
    }
}