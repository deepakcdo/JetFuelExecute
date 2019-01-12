package regressiontest.functions;

import headfront.jetfuel.execute.functions.AbstractFunctionExecutor;
import headfront.jetfuel.execute.functions.FunctionResponseListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestGetNoOfHolidaysPerMonth extends AbstractFunctionExecutor {

    @Override
    public void executeFunction(String id, List<Object> parameters, Map<String, Object> requestParameters, FunctionResponseListener result) {
        String country = parameters.get(0).toString();
        if (country.equalsIgnoreCase("UK")) {
            Map<String, Integer> holidays = new HashMap<>();
            holidays.put("Dec", 2);
            holidays.put("Jan", 1);
            result.onCompleted(id, "Sending Holidays", holidays);
        } else if (country.equalsIgnoreCase("UAE")) {
            Map<String, Integer> holidays = new HashMap<>();
            holidays.put("Dec", 1);
            holidays.put("Jan", 2);
            holidays.put("Aug", 5);
            result.onCompleted(id, "Sending Holidays", holidays);
        } else {
            result.onError(id, "Unsupported Country - " + country, null);
        }
    }
}