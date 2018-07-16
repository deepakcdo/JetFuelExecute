package headfront.jetfuel.execute.functions;

import headfront.jetfuel.execute.ActiveSubscriptionRegistry;
import headfront.jetfuel.execute.utils.FunctionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Created by Deepak on 28/05/2017.
 */
public abstract class AbstractFunctionExecutor implements FunctionProcessor {

    private static Logger LOG = LoggerFactory.getLogger(AbstractFunctionExecutor.class);
    private ActiveSubscriptionRegistry activeSubscriptionFactory;


    public void validateAndExecuteFunction(String id, List<FunctionParameter> functionParameters,
                                           List<Object> parameters, Map<String, Object> requestParameters, FunctionResponseListener result) {
        String validate = FunctionUtils.validateParameters(parameters, functionParameters);
        if (validate == null) {
            try {
                if (result instanceof SubscriptionFunctionResponseListener) {
                    if (activeSubscriptionFactory == null) {
                        LOG.error("ActiveSubscriptionRegistry is not set for  SubscriptionFunctionResponseListener " +
                                " please set this by calling setActiveSubscriptionFactory(). Function with id " + id +
                                " is not processed and we will send and error");
                        result.onError(id, "Function not set up correctly", "");

                    } else {
                        final SubscriptionExecutor subscriptionExecutor = executeSubscriptionFunction(id, parameters, requestParameters,
                                (SubscriptionFunctionResponseListener) result);
                        final String reason = activeSubscriptionFactory.registerActiveServerSubscription(id, subscriptionExecutor);
                        if (reason != null) {
                            LOG.error("Unable to register SubscriptionExecutor for id " + id + " due to " + reason);
                        }
                    }
                } else {
                    executeFunction(id, parameters, requestParameters, result);
                }
            } catch (Throwable e) {
                result.onError(id, "Unable to process Function call", e.getMessage() + " " + e.toString());
            }
        } else {
            result.onError(id, "Validation failed.", validate);
        }
    }

    /**
     * Override this method
     */
    protected void executeFunction(String id, List<Object> parameters, Map<String, Object> requestParameters, FunctionResponseListener result) {
        String message = "AbstractFunctionExecutor.executeFunction() has not been extended";
        LOG.error(message);
        result.onError(id, "Function has not been setup correctly by the publisher", null);
        throw new AbstractMethodError(message);
    }

    /**
     * Override this method
     */
    protected SubscriptionExecutor executeSubscriptionFunction(String id, List<Object> parameters, Map<String, Object> requestParameters, SubscriptionFunctionResponseListener result) {
        String message = "AbstractFunctionExecutor.executeSubscriptionFunction() has not been extended";
        LOG.error(message);
        result.onError(id, "Function has not been setup correctly by the publisher", null);
        throw new AbstractMethodError(message);
    }

    public void setActiveSubscriptionFactory(ActiveSubscriptionRegistry activeSubscriptionFactory) {
        this.activeSubscriptionFactory = activeSubscriptionFactory;
    }
}
