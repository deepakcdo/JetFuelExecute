package regressiontest.functions;

import com.crankuptheamps.client.HAClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import headfront.jetfuel.execute.functions.AbstractFunctionExecutor;
import headfront.jetfuel.execute.functions.FunctionResponseListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Deepak on 09/05/2017.
 */
public class UpdateBidOfferQuoteExecutor extends AbstractFunctionExecutor {


    private HAClient ampsClient;
    private ObjectMapper jsonMapper;
    private String quoteTopic;

    public UpdateBidOfferQuoteExecutor(HAClient ampsClient, ObjectMapper jsonMapper, String quoteTopic) {
        this.ampsClient = ampsClient;
        this.jsonMapper = jsonMapper;
        this.quoteTopic = quoteTopic;
    }

    @Override
    public void executeFunction(String id, List<Object> parameters,Map<String, Object> requestParameters,  FunctionResponseListener result) {
        try {
            String trader = parameters.get(0).toString();
            String instrument = parameters.get(1).toString();
            Double bidPrice = Double.parseDouble(parameters.get(2).toString());
            Double offerPrice = Double.parseDouble(parameters.get(3).toString());

            Map<String, Object> data = new HashMap<>();
            data.put("ID", instrument);
            data.put("Trader", trader);
            data.put("BidPrice", bidPrice);
            data.put("OfferPrice", offerPrice);
            data.put("FunctionID", id);
            String jsonMsg = jsonMapper.writeValueAsString(data);
            ampsClient.deltaPublish(quoteTopic, jsonMsg);
            result.onCompleted(id, "Quote update was successful", true);

        } catch (Exception e) {
            result.onError(id, "Unable to get update quote " + e.getMessage(), e);
        }
    }
}
