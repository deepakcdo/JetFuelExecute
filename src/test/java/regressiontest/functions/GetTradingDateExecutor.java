package regressiontest.functions;

import headfront.jetfuel.execute.functions.AbstractFunctionExecutor;
import headfront.jetfuel.execute.functions.FunctionResponse;

import java.util.List;

/**
 * Created by Deepak on 09/05/2017.
 */
public class GetTradingDateExecutor extends AbstractFunctionExecutor {

    @Override
    public void executeFunction(String id, List<Object> parameters, FunctionResponse result) {
        result.onCompleted(id, "Sending date", "20180225");
    }
}
