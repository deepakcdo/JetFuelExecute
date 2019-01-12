package regressiontest.functions;

import headfront.jetfuel.execute.functions.AbstractFunctionExecutor;
import headfront.jetfuel.execute.functions.FunctionResponseListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TestGetWeekends extends AbstractFunctionExecutor {

    @Override
    public void executeFunction(String id, List<Object> parameters, Map<String, Object> requestParameters, FunctionResponseListener result) {
        String country = parameters.get(0).toString();
        if (country.equalsIgnoreCase("UK")) {
            ArrayList arrayList = new ArrayList();
            arrayList.add("Saturday");
            arrayList.add("Sunday");
            result.onCompleted(id, "Sending weekends", arrayList);
        } else if (country.equalsIgnoreCase("UAE")) {
            ArrayList arrayList = new ArrayList();
            arrayList.add("Friday");
            result.onCompleted(id, "Sending weekends", arrayList);
        } else {
            result.onError(id, "Unsupported Country - " + country, null);
        }
    }
}