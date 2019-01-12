package regressiontest.functions;

import headfront.jetfuel.execute.functions.AbstractFunctionExecutor;
import headfront.jetfuel.execute.functions.FunctionResponseListener;

import java.util.List;
import java.util.Map;

/**
 * Created by Deepak on 09/05/2017.
 */
public class PlaceOrderWithListAndStringExecutor extends AbstractFunctionExecutor {

    @Override
    public void executeFunction(String id, List<Object> parameters, Map<String, Object> requestParameters,
                                FunctionResponseListener result) {
        List orderParameters = (List) parameters.get(0);
        Object side = orderParameters.get(0);
        Object quantity = orderParameters.get(1);
        Object price = orderParameters.get(2);

        Object prefix = parameters.get(1);
        result.onCompleted(id, "Placed " + side + " order for " + quantity + " at price " + price,
                prefix + "_Order_ID_List_" + price);

    }
}
