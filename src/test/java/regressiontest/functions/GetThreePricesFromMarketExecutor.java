package regressiontest.functions;

import com.crankuptheamps.client.HAClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import headfront.jetfuel.execute.FunctionState;
import headfront.jetfuel.execute.functions.AbstractFunctionExecutor;
import headfront.jetfuel.execute.functions.SubscriptionExecutor;
import headfront.jetfuel.execute.functions.SubscriptionFunctionResponse;

import java.util.List;

/**
 * Created by Deepak on 09/05/2017.
 */
public class GetThreePricesFromMarketExecutor extends AbstractFunctionExecutor {


    private HAClient ampsClient;
    private ObjectMapper jsonMapper;
    private String quoteTopic;

    public GetThreePricesFromMarketExecutor(HAClient ampsClient, ObjectMapper jsonMapper, String QuoteTopic) {
        this.ampsClient = ampsClient;
        this.jsonMapper = jsonMapper;
        quoteTopic = QuoteTopic;
    }

    @Override
    protected SubscriptionExecutor executeSubscriptionFunction(String id, List<Object> parameters, SubscriptionFunctionResponse result) {
        String inst = parameters.get(0).toString();
        PriceSubscriptionExecutor subExecutor = new PriceSubscriptionExecutor(id, result, 3);
        subExecutor.start();
        result.onSubscriptionStateChanged(id, "Subscription  for " + inst + " is valid", FunctionState.StateSubActive);
        return subExecutor;
    }

}
