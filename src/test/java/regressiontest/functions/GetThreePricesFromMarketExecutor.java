package regressiontest.functions;

import headfront.jetfuel.execute.FunctionState;
import headfront.jetfuel.execute.functions.AbstractFunctionExecutor;
import headfront.jetfuel.execute.functions.SubscriptionExecutor;
import headfront.jetfuel.execute.functions.SubscriptionFunctionResponseListener;

import java.util.List;

/**
 * Created by Deepak on 09/05/2017.
 */
public class GetThreePricesFromMarketExecutor extends AbstractFunctionExecutor {

    @Override
    protected SubscriptionExecutor executeSubscriptionFunction(String id, List<Object> parameters, SubscriptionFunctionResponseListener result) {
        String inst = parameters.get(0).toString();
        PriceSubscriptionExecutor subExecutor = new PriceSubscriptionExecutor(id, result, 3);
        subExecutor.start();
        result.onSubscriptionStateChanged(id, "Subscription  for " + inst + " is valid", FunctionState.SubActive);
        return subExecutor;
    }

}
