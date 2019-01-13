package headfront.jetfuel.execute.impl.functions;

import headfront.jetfuel.execute.functions.FunctionParameter;
import headfront.jetfuel.execute.functions.FunctionProcessor;
import headfront.jetfuel.execute.functions.FunctionResponseListener;

import java.util.List;
import java.util.Map;
//bad implementation as this is only a test class

public class AverageCalc<T> implements FunctionProcessor {

    @Override
    public void validateAndExecuteFunction(String s, List<FunctionParameter> var2, List<Object> list, Map<String, Object> request, FunctionResponseListener functionPublisherResult) {
        if (list.size() == 2) {
            Object param = list.get(0);
            if (isInteger(param)) {
                Object param2 = list.get(1);
                if (isInteger(param2)) {
                    functionPublisherResult.onCompleted(s, "Success", "Average is " + ((Integer.parseInt(param.toString()) +
                            Integer.parseInt(param2.toString())) / 2));
                } else {
                    functionPublisherResult.onError(s, "param2 needs to be an integer, we got " + param2.getClass().getSimpleName(), param2);
                }
            } else {
                functionPublisherResult.onError(s, "param1 needs to be an integer, we got " + param.getClass().getSimpleName(), param);
            }
        } else {
            functionPublisherResult.onError(s, "Expected 2 values and got " + list, null);
        }
    }

    public static boolean isInteger(Object s) {
        try {
            Integer.parseInt(s.toString());
        } catch (NumberFormatException e) {
            return false;
        } catch (NullPointerException e) {
            return false;
        }
        // only got here if we didn't return false
        return true;
    }
}
