package regressiontest.functions;

import headfront.jetfuel.execute.functions.AbstractFunctionExecutor;
import headfront.jetfuel.execute.functions.FunctionResponseListener;

import java.util.List;

/**
 * Created by Deepak on 09/05/2017.
 */
public class UpdateBankTraderExecutor extends AbstractFunctionExecutor {

    @Override
    public void executeFunction(String id, List<Object> parameters, FunctionResponseListener result) {
        String name = parameters.get(0).toString();
        Boolean value = Boolean.parseBoolean(parameters.get(1).toString());
        String options = parameters.get(2).toString();

        String printValue = value ? "ON" : "OFF";
        result.onCompleted(id, "Trader  " + name + " is " + printValue + " with option " + options, true);

    }
}
