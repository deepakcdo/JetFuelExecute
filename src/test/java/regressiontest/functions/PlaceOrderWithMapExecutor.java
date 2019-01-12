package regressiontest.functions;

import headfront.jetfuel.execute.functions.AbstractFunctionExecutor;
import headfront.jetfuel.execute.functions.FunctionResponseListener;

import java.util.List;
import java.util.Map;

/**
 * Created by Deepak on 09/05/2017.
 */
public class PlaceOrderWithMapExecutor extends AbstractFunctionExecutor {

    @Override
    public void executeFunction(String id, List<Object> parameters, Map<String, Object> requestParameters,
                                FunctionResponseListener result) {
        Map orderParameters = (Map) parameters.get(0);
        Object side = orderParameters.get("side");
        Object quantity = orderParameters.get("quantity");
        Object price = orderParameters.get("price");

        result.onCompleted(id, "Placed " + side + " order for " + quantity + " at price " + price,
                "Order_ID_Map_" + price);

    }
}
