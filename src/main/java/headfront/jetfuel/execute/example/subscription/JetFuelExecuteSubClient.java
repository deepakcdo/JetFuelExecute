package headfront.jetfuel.execute.example.subscription;

import com.crankuptheamps.client.HAClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import headfront.jetfuel.execute.FunctionState;
import headfront.jetfuel.execute.JetFuelExecute;
import headfront.jetfuel.execute.functions.SubscriptionFunctionResponseListener;
import headfront.jetfuel.execute.impl.AmpsJetFuelExecute;
import headfront.jetfuel.execute.utils.HaClientFactory;

/**
 * Created by Deepak on 01/02/2018.
 */
public class JetFuelExecuteSubClient {

    public static void main(String[] args) {
        new JetFuelExecuteSubClient();
    }

    public JetFuelExecuteSubClient() {
        try {

            // Create amps connection
            final HAClient haClient = new HaClientFactory().createHaClient("SampleJetFuelSubClient",
                    "tcp://192.168.56.101:8001/amps/json", false);
            ObjectMapper jsonMapper = new ObjectMapper();
            System.out.println("Connected to Amps");

            //Create JetFuelExecute
            JetFuelExecute jetFuelExecute = new AmpsJetFuelExecute(haClient, jsonMapper);
            jetFuelExecute.initialise();

            // calling function
            String functionName = "SampleJetFuelSubSever.Get5PricesTicks";
            final String id1 = jetFuelExecute.executeSubscriptionFunction(functionName,
                    new Object[]{"DE0022456"}, new ClientFunctionResponseListener());
            System.out.println("Called " + functionName + " with id  " + id1);

            Thread.sleep(5000);
            jetFuelExecute.cancelSubscriptionFunctionRequest(id1);

        } catch (Exception e) {
            System.out.println("Unable to create JetFuelExecute Client");
            e.printStackTrace();
        }

    }

    static class ClientFunctionResponseListener implements SubscriptionFunctionResponseListener {
        @Override
        public void onCompleted(String id, Object message, Object returnValue) {
            System.out.println("Got onCompleted for id '" + id + "' with message '" + message + "' and returnValue '" + returnValue + "'");
        }

        @Override
        public void onError(String id, Object message, Object exception) {
            System.out.println("Got onError for id '" + id + "' with message '" + message + "' and exception '" + exception + "'");
        }

        @Override
        public void onSubscriptionUpdate(String id, Object message, String update) {
            System.out.println("Got onSubscriptionUpdate called for id '" + id + "' with message '" + message + "' and update '" + update + "'");
        }

        @Override
        public void onSubscriptionStateChanged(String id, Object message, FunctionState state) {
            System.out.println("Got onSubscriptionStateChanged for id '" + id + "' with FunctionState '" + state + "' and message '" + message + "'");
        }
    }
}
