package headfront.jetfuel.execute.functions;

import headfront.jetfuel.execute.FunctionState;

/**
 * Created by Deepak on 10/05/2017.
 */
public interface SubscriptionFunctionResponse extends FunctionResponse {

    void onSubscriptionUpdate(String id, Object message, String update);

    void onSubscriptionStateChanged(String id, Object message, FunctionState state);
}
