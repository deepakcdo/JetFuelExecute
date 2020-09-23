package headfront.jetfuel.execute.impl;

import headfront.jetfuel.execute.ActiveSubscriptionRegistry;
import headfront.jetfuel.execute.JetFuelExecute;
import headfront.jetfuel.execute.OwnConnectionListener;
import headfront.jetfuel.execute.functions.FunctionResponseListener;
import headfront.jetfuel.execute.functions.JetFuelFunction;
import headfront.jetfuel.execute.functions.SubscriptionExecutor;
import headfront.jetfuel.execute.functions.SubscriptionFunctionResponseListener;
import headfront.jetfuel.execute.utils.FunctionUtils;
import headfront.jetfuel.execute.utils.SameThreadExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This class is very useful when you want to test your Function executors locally. This implementation does not connect
 * to any middleware so is very useful. This is non final class to it can be extended as required.
 * Created by Deepak on 21/05/2017.
 */
public class DefaultJetFuelExecuteService implements JetFuelExecute {
    private ActiveSubscriptionRegistry subscriptionRegistry = new AmpsActiveSubscriptionRegistry();

    private static final Logger LOG = LoggerFactory.getLogger(DefaultJetFuelExecuteService.class);
    private String functionTopic = "JETFUEL_EXECUTE";
    private String functionBusTopic = "JETFUEL_EXECUTE_BUS";
    private String connectionName = "DefaultJetFuelExecuteService";
    private Function<String, String> functionIDGenerator = FunctionUtils::getNextID;
    private Consumer<String> functionRemovedListener = newFunction -> {
    };
    private Consumer<String> functionAddedListener = newFunction -> {
    };
    private Map<String, JetFuelFunction> availableFunctions = new HashMap<>();
    private SameThreadExecutor processingThreadFactory = new SameThreadExecutor(1);
    private boolean executeOnDifferentThread = false;

    @Override
    public void initialise() {
    }

    @Override
    public void shutDown() {
        availableFunctions.clear();
    }

    @Override
    public String getConnectionName() {
        return connectionName;
    }

    @Override
    public Set<String> getAvailableFunctions() {
        return Collections.unmodifiableSet(availableFunctions.keySet());
    }

    @Override
    public JetFuelFunction getFunction(String s) {
        return availableFunctions.get(s);
    }

    @Override
    public List<String> findFunction(String s) {
        return availableFunctions.keySet().stream().filter(functionName -> functionName.contains(s)).collect(Collectors.toList());
    }

    @Override
    public void setOnFunctionAddedListener(Consumer<String> functionAddedListener) {
        this.functionAddedListener = functionAddedListener;
    }

    @Override
    public void setOnFunctionRemovedListener(Consumer<String> functionRemovedListener) {
        this.functionRemovedListener = functionRemovedListener;
    }

    @Override
    public boolean publishFunction(JetFuelFunction jetFuelFunction) {
        jetFuelFunction.setTransientFunctionDetatils(getConnectionName(), "DeepakHomeMac", new Date().toString());
        availableFunctions.put(jetFuelFunction.getFullFunctionName(), jetFuelFunction);
        functionAddedListener.accept(jetFuelFunction.getFullFunctionName());
        return true;
    }

    @Override
    public boolean unPublishFunction(JetFuelFunction jetFuelFunction) {
        availableFunctions.remove(jetFuelFunction.getFullFunctionName());
        functionRemovedListener.accept(jetFuelFunction.getFullFunctionName());
        return true;
    }

    @Override
    public String executeFunction(String functionName, Object[] functionParameters,
                                  FunctionResponseListener functionResponse) {
        final String functionID = functionIDGenerator.apply("MOCK");
        try {
            final JetFuelFunction jetFuelFunction = availableFunctions.get(functionName);
            if (jetFuelFunction != null) {
                List<Object> parameters = Arrays.asList(functionParameters);
                if (executeOnDifferentThread) {
                    processingThreadFactory.processTask(functionID, () ->
                            jetFuelFunction.getExecutor().validateAndExecuteFunction(functionID, jetFuelFunction.getFunctionParameters(),
                                    parameters, new HashMap<>(), functionResponse));
                } else {
                    jetFuelFunction.getExecutor().validateAndExecuteFunction(functionID, jetFuelFunction.getFunctionParameters(),
                            parameters, new HashMap<>(), functionResponse);
                }
            } else {
                LOG.error("Unable to processFunction " + functionName + " with parameter " + Arrays.toString(functionParameters)
                        + " as the function was not found");
            }
        } catch (Exception e1) {
            LOG.error("Unable to processFunction " + functionName + " with parameter " + Arrays.toString(functionParameters), e1);
        }
        return functionID;
    }

    @Override
    public String executeSubscriptionFunction(String functionName, Object[] functionParameters,
                                              SubscriptionFunctionResponseListener subscriptionFunctionResponse) {
        return executeFunction(functionName, functionParameters, subscriptionFunctionResponse);
    }

    @Override
    public String getFunctionTopic() {
        return functionTopic;
    }

    @Override
    public String getFunctionBusTopic() {
        return functionBusTopic;
    }

    @Override
    public void setFunctionIDGenerator(Function<String, String> functionIDGenerator) {
        this.functionIDGenerator = functionIDGenerator;
    }

    @Override
    public void cancelSubscriptionFunctionRequest(String s) {
        final SubscriptionExecutor activeSubscription = subscriptionRegistry.getAndRemoveActiveServerSubscription(s);
        if (activeSubscription != null) {
            activeSubscription.stopSubscriptions();
            activeSubscription.interrupt();
        }
    }

    @Override
    public int getUncompletedFunctionCount() {
        return 0;
    }

    @Override
    public Set<String> getUncompletedFunctionIds() {
        return new HashSet<>();
    }

    // public methods that can be used to set up your local environment

    public ActiveSubscriptionRegistry getSubscriptionRegistry() {
        return subscriptionRegistry;
    }

    public void setFunctionTopic(String functionTopic) {
        this.functionTopic = functionTopic;
    }

    public void setFunctionBusTopic(String functionBusTopic) {
        this.functionBusTopic = functionBusTopic;
    }

    public void setConnectionName(String connectionName) {
        this.connectionName = connectionName;
    }

    public void setExecuteOnDifferentThread(boolean executeOnDifferentThread) {
        this.executeOnDifferentThread = executeOnDifferentThread;
    }

    @Override
    public void registerOwnConnectionListener(OwnConnectionListener connectionListener){}
}

