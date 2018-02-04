package headfront.jetfuel.execute.functions;

/**
 * Created by Deepak on 10/05/2017.
 */
public interface FunctionResponse {

    void onCompleted(String id, Object message, Object returnValue);

    void onError(String id, Object message, Object exception);
}
