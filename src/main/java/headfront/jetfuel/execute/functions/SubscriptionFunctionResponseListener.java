package headfront.jetfuel.execute.functions;

import headfront.jetfuel.execute.FunctionState;

import java.util.Map;
import java.util.Optional;

/**
 * The same interface is used for the client receiving the callback and the server sending the response.
 * <p>
 * In the server implementation feel free to pass in Optional.empty(). This value is ignored for the server implementation
 * <p>
 * Created by Deepak on 10/05/2017.
 */
public interface SubscriptionFunctionResponseListener extends FunctionResponseListener {

    void onSubscriptionUpdate(String id, Optional<Map<String, Object>> responseMap, Object message, String update);

    void onSubscriptionStateChanged(String id, Optional<Map<String, Object>> responseMap, Object message, FunctionState state);
}
