package headfront.jetfuel.execute.functions;

import headfront.jetfuel.execute.impl.ActiveSubscriptionRegistry;
import headfront.jetfuel.execute.utils.FunctionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.springframework.util.Assert.notNull;

/**
 * Created by Deepak on 28/05/2017.
 */
public abstract class AbstractFunctionExecutor implements FunctionProcessor {

    private static Logger LOG = LoggerFactory.getLogger(AbstractFunctionExecutor.class);

    private List<FunctionParameter> functionParameters;

    public void setFunctionParameters(List<FunctionParameter> functionParameters) {
        notNull(functionParameters);
        this.functionParameters = functionParameters;
    }

    public void validateAndExecuteFunction(String id, List<Object> parameters, FunctionResponse result) {
        String validate = FunctionUtils.validateParameters(parameters, functionParameters);
        if (validate == null) {
            try {
                if (result instanceof SubscriptionFunctionResponse) {
                    final SubscriptionExecutor subscriptionExecutor = executeSubscriptionFunction(id, parameters, (SubscriptionFunctionResponse) result);
                    final String reason = ActiveSubscriptionRegistry.registerActiveSubscription(id, subscriptionExecutor);
                    if (reason != null){
                        LOG.error("Unable to register SubscriptionExecutor for id " + id + " due to " + reason);
                    }
                } else {
                    executeFunction(id, parameters, result);
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
    protected void executeFunction(String id, List<Object> parameters, FunctionResponse result) {
        String mesage = "AbstractFunctionExecutor.executeFunction() has not been extended";
        LOG.error(mesage);
        throw new AbstractMethodError(mesage);
    }

    /**
     * Override this method
     */
    protected SubscriptionExecutor executeSubscriptionFunction(String id, List<Object> parameters, SubscriptionFunctionResponse result) {
        String mesage = "AbstractFunctionExecutor.executeSubscriptionFunction() has not been extended";
        LOG.error(mesage);
        throw new AbstractMethodError(mesage);
    }

}
