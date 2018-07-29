package headfront.jetfuel.execute.functions;

import headfront.jetfuel.execute.FunctionState;

import java.util.Collections;
import java.util.Map;

/**
 * The same interface is used for the client receiving the callback and the server sending the response.
 * <p>
 * In the server implementation feel free to pass in Optional.empty(). This value is ignored for the server
 * implementation or call the default method
 * Created by Deepak on 10/05/2017.
 */
public interface SubscriptionFunctionResponseListener extends FunctionResponseListener {

    void onSubscriptionUpdate(String id, Map<String, Object> responseMap, Object message, String update);

    default void onSubscriptionUpdate(String id, Object message, String update) {
        onSubscriptionUpdate(id, Collections.EMPTY_MAP, message, update);
    }

    void onSubscriptionStateChanged(String id, Map<String, Object> responseMap, Object message, FunctionState state);

    default void onSubscriptionStateChanged(String id, Object message, FunctionState state) {
        onSubscriptionStateChanged(id, Collections.EMPTY_MAP, message, state);
    }
}
