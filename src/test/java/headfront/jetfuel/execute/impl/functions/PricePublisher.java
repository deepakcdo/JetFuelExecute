package headfront.jetfuel.execute.impl.functions;

import headfront.jetfuel.execute.FunctionState;
import headfront.jetfuel.execute.functions.AbstractFunctionExecutor;
import headfront.jetfuel.execute.functions.SubscriptionExecutor;
import headfront.jetfuel.execute.functions.SubscriptionFunctionResponseListener;

import java.util.List;
import java.util.Map;
//bad implementation as this is only a test class

public class PricePublisher<T> extends AbstractFunctionExecutor {

    @Override
    protected SubscriptionExecutor executeSubscriptionFunction(String id, List<Object> parameters, Map<String, Object> request, SubscriptionFunctionResponseListener result) {
        String inst = parameters.get(0).toString();
        TestPriceSubscriptionExecutor subExecutor = new TestPriceSubscriptionExecutor(id, result);
        subExecutor.start();
        result.onSubscriptionStateChanged(id, "Subscription  for " + inst + " is valid", FunctionState.SubActive);
        return subExecutor;
    }
}
