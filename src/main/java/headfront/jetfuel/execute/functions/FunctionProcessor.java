package headfront.jetfuel.execute.functions;

import java.util.List;
import java.util.Map;

/**
 * Created by Deepak on 09/05/2017.
 */
public interface FunctionProcessor {

    void validateAndExecuteFunction(String id, List<FunctionParameter> functionParameters,
                                    List<Object> parameters, Map<String, Object> requestParameters,
                                    FunctionResponseListener result);

}
