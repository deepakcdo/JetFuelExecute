package headfront.jetfuel.execute.functions;

import headfront.jetfuel.execute.FunctionState;

/**
 * Created by Deepak on 10/05/2017.
 */
public interface FunctionResponse {

    void onCompleted(String id, Object message, Object returnValue);

    void onError(String id, Object message, Object exception);
}
