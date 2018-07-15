package regressiontest.functions;

import headfront.jetfuel.execute.FunctionState;
import headfront.jetfuel.execute.functions.AbstractFunctionExecutor;
import headfront.jetfuel.execute.functions.SubscriptionExecutor;
import headfront.jetfuel.execute.functions.SubscriptionFunctionResponseListener;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created by Deepak on 09/05/2017.
 */
public class GetPricesFromMarketExecutor extends AbstractFunctionExecutor {

    @Override
    protected SubscriptionExecutor executeSubscriptionFunction(String id, List<Object> parameters,
                                                               Map<String, Object> requestParameters,
                                                               SubscriptionFunctionResponseListener result) {
        String inst = parameters.get(0).toString();
        PriceSubscriptionExecutor subExecutor = new PriceSubscriptionExecutor(id, result, Integer.MAX_VALUE);
        subExecutor.start();
        result.onSubscriptionStateChanged(id, Optional.empty(), "Subscription  for " + inst + " is valid", FunctionState.SubActive);
        return subExecutor;
    }
}
