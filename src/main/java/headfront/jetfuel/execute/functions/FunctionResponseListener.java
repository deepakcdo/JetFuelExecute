package headfront.jetfuel.execute.functions;

import java.util.Map;
import java.util.Optional;

/**
 * The same interface is used for the client receiving the callback and the server sending the response.
 * <p>
 * In the server implementation feel free to pass in Optional.empty(). This value is ignored for the server implementation
 * <p>
 * Created by Deepak on 10/05/2017.
 */
public interface FunctionResponseListener {

    void onCompleted(String id, Optional<Map<String, Object>> responseMap, Object message, Object returnValue);

    void onError(String id, Optional<Map<String, Object>> responseMap, Object message, Object exception);
}
