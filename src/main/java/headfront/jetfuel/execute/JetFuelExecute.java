package headfront.jetfuel.execute;

import headfront.jetfuel.execute.functions.FunctionResponse;
import headfront.jetfuel.execute.functions.JetFuelFunction;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Created by Deepak on 10/01/2018.
 */
public interface JetFuelExecute {

    /**
     * Initialise JetFuelExecute. This will set up internal subscription to amps to get available functions. It will
     * also start listening to messages we are interested..
     * Note JetFuelExecute does not manage the amps connection as this is meant to be manged outside JetFuelExecute.
     * AmpsConnections is passed into the constructor
     */
    void initialise();

    /**
     * Shut down cleanly. This will include unpublishing any functions and close any active subscriptions
     * Note we dont close the amps connections as this is meant to be manged outside JetFuelExecute
     */
    void shutDown();

    /**
     * @return the Amps Connection name
     */
    String getConnectionName();

    /**
     * @return A set of all available functions
     */
    Set<String> getAvailableFunctions();

    /**
     * @param functionName for which you need the JetFuelFunction
     * @return JetFuelFunction registered in JetFuelExecute. This can return null
     */
    JetFuelFunction getFunction(String functionName);

    /**
     * @param name Partial function name for which you need all available functions
     * @return A List of function names that match the give name
     */
    List<String> findFunction(String name);

    /**
     * @return The Function meta data topic in amps. This topic will list all the JetFuel function and its details like
     * descriptions, parameter's, return type etc
     */
    String getFunctionTopic();

    /**
     * @return The Function bus topic in amps. This topic is where all the request and response messages are
     * published to or subscribed from.
     */
    String getFunctionBusTopic();

    /**
     * @param listener That will get notified where a new function becomes available
     */
    void setOnFunctionAddedListener(Consumer<String> listener);

    /**
     * @param listener That will get notified when a functions is removed
     */
    void setOnFunctionRemovedListener(Consumer<String> listener);

    /**
     * @param function to be published on the platform
     * @return true if the publish was successful else false
     */
    boolean publishFunction(JetFuelFunction function);

    /**
     * @param function to be unpublished on the platform
     * @return true if the un publish was successful else false
     */
    boolean unPublishFunction(JetFuelFunction function);

    /**
     * Execute a function in JetFuel Execute
     * @param functionName full name of the function you want to call
     * @param params parameters for the function
     * @param response callback listener for the function call
     * @return The uniqueId for this function call
     */
    String executeFunction(String functionName, Object[] params, FunctionResponse response);

    /**
     * If JetFuelFunction of type FunctionExecutionType#Subscription was executed then you can cancel this subscription
     * when its no longer require updates.
     * Note this is not implemented yet
     * @param callId to be cancelled.
     * @return true if successful else false.
     */
    boolean cancelExecuteFunctionSubscription(String callId);

}