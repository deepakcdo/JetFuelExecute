package headfront.jetfuel.execute.functions;

import java.util.Collections;
import java.util.Map;

/**
 * The same interface is used for the client receiving the callback and the server sending the response.
 * <p>
 * In the server implementation feel free to pass in Optional.empty(). This value is ignored for the server
 * implementation or call the default method
 * <p>
 * Created by Deepak on 10/05/2017.
 */
public interface FunctionResponseListener {

    void onCompleted(String id, Map<String, Object> responseMap, Object message, Object returnValue);

    default void onCompleted(String id, Object message, Object returnValue) {
        onCompleted(id, Collections.EMPTY_MAP, message, returnValue);
    }

    void onError(String id, Map<String, Object> responseMap, Object message, Object exception);

    default void onError(String id, Object message, Object exception) {
        onError(id, Collections.EMPTY_MAP, message, exception);
    }


}
