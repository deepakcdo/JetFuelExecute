package headfront.jetfuel.execute;

import headfront.jetfuel.execute.functions.FunctionResponseListener;
import headfront.jetfuel.execute.functions.JetFuelFunction;
import headfront.jetfuel.execute.functions.SubscriptionFunctionResponseListener;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by Deepak on 10/01/2018.
 */
public interface JetFuelExecute {

    /**
     * Initialise JetFuelExecute. This will set up internal subscription to the configured middleware to get available
     * functions. It will also start listening to messages we are interested..
     * Note JetFuelExecute does not manage the connection ato the middleware s this is meant to be manged outside
     * JetFuelExecute.
     * Usually the connection to the middleware is passed into the constructor of the the class implementing this interface
     */
    void initialise();

    /**
     * Shut down cleanly. This will include unpublishing any functions and close any active subscriptions
     * Note we dont close the middlware connections as this is meant to be manged outside JetFuelExecute
     */
    void shutDown();

    /**
     * @return the Connection name
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
     * @return The Function meta data topic in the configured  middleware. This topic will list all the JetFuel
     * function and its details like descriptions, parameter's, return type etc
     */
    String getFunctionTopic();

    /**
     * @return The Function bus topic in the configured middleware. This topic is where all the request and response
     * messages are published to or subscribed from.
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
     * Execute a function in JetFuel Execute. Call this for Functions that have a FunctionExecutionType of RequestResponse
     *
     * @param functionName       full name of the function you want to call
     * @param functionParameters parameters for the function
     * @param response           callback listener for the function call
     * @return The uniqueId for this function call
     */
    String executeFunction(String functionName, Object[] functionParameters, FunctionResponseListener response);

    /**
     * Execute a Subscription function in JetFuel Execute.  Call this for Functions that have a FunctionExecutionType of Subscription
     *
     * @param functionName       full name of the function you want to call
     * @param functionParameters parameters for the function
     * @param response           callback listener for the function call
     * @return The uniqueId for this function call
     */
    String executeSubscriptionFunction(String functionName, Object[] functionParameters, SubscriptionFunctionResponseListener response);

    /**
     * If JetFuelFunction of type FunctionExecutionType#Subscription was executed then you can cancel this subscription
     * when its no longer require updates. This is an async call
     * Note this is not implemented yet
     *
     * @param callId to be cancelled.
     */
    void cancelSubscriptionFunctionRequest(String callId);

    /**
     * @return number of functions that have not completed. i.e they are active and we may get a response
     */
    int getUncompletedFunctionCount();

    /**
     * @return a set of function id that we are still expecting a response
     */
    Set<String> getUncompletedFunctionIds();


    /**
     * Set how FunctionID is Generated
     *
     * @param functionIDGenerator
     */
    void setFunctionIDGenerator(Function<String, String> functionIDGenerator);

    /**
     * Register for client Disconnections
     * @param connectionName
     * @param listener
     */
    void registerForClientDisconnections(String connectionName, ClientDisconnectionListener listener);

    /**
     * DeRegister for client Disconnections
     * @param connectionName
     */
    void deRegisterForClientDisconnections(String connectionName);

    /**
     * Register a lister for our own connection and disconnection
     * @param connectionListener
     */
    void registerOwnConnectionListener(OwnConnectionListener connectionListener);

}
