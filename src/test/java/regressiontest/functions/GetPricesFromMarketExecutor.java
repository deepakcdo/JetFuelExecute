package regressiontest.functions;

import headfront.jetfuel.execute.FunctionState;
import headfront.jetfuel.execute.functions.AbstractFunctionExecutor;
import headfront.jetfuel.execute.functions.SubscriptionExecutor;
import headfront.jetfuel.execute.functions.SubscriptionFunctionResponse;

import java.util.List;

/**
 * Created by Deepak on 09/05/2017.
 */
public class GetPricesFromMarketExecutor extends AbstractFunctionExecutor {

    @Override
    protected SubscriptionExecutor executeSubscriptionFunction(String id, List<Object> parameters, SubscriptionFunctionResponse result) {
        String inst = parameters.get(0).toString();
        PriceSubscriptionExecutor subExecutor = new PriceSubscriptionExecutor(id, result, Integer.MAX_VALUE);
        subExecutor.start();
        result.onSubscriptionStateChanged(id, "Subscription  for " + inst + " is valid", FunctionState.SubActive);
        return subExecutor;
    }
}
