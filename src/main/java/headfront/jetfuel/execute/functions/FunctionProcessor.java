package headfront.jetfuel.execute.functions;

import java.util.List;

/**
 * Created by Deepak on 09/05/2017.
 */
public interface FunctionProcessor {

    void validateAndExecuteFunction(String id, List<Object> parameters, FunctionResponse result);

}
