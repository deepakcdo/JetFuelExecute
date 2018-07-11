package regressiontest.functions;

import headfront.jetfuel.execute.functions.AbstractFunctionExecutor;
import headfront.jetfuel.execute.functions.FunctionResponseListener;

import java.util.List;
import java.util.Map;

/**
 * Created by Deepak on 09/05/2017.
 */
public class UpdateBankStatusExecutor extends AbstractFunctionExecutor {

    @Override
    public void executeFunction(String id, List<Object> parameters, Map<String, Object> requestParameters, FunctionResponseListener result) {
        String name = parameters.get(0).toString();
        if (name.equalsIgnoreCase("Sarah")) {
            // Sarah is a late bloomer she replies after timeout happens
            try {
                Thread.sleep(6000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (name.equalsIgnoreCase("Amanda")) {
            // Amanda throws a null pointer exeception
            String amanda = null;
            final String trimed = amanda.trim();
        } else if (name.equalsIgnoreCase("Lucy")) {
            // We dont know about lucy so we never reply
        } else if (name.equalsIgnoreCase("Jack")) {
            result.onError(id, "Jack always throws error.", "Authorisation exception");
        } else if (name.equalsIgnoreCase("Fred")) {
            result.onCompleted(id, "Fred is not authorised", false);
        } else {
            Boolean value = Boolean.parseBoolean(parameters.get(1).toString());
            String printValue = value ? "ON" : "OFF";
            result.onCompleted(id, name + " is authorised, Bank status is " + printValue, true);
        }
    }
}
