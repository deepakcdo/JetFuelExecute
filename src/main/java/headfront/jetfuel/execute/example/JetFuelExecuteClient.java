package headfront.jetfuel.execute.example;

import com.crankuptheamps.client.HAClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import headfront.jetfuel.execute.JetFuelExecute;
import headfront.jetfuel.execute.JetFuelExecuteConstants;
import headfront.jetfuel.execute.functions.FunctionResponseListener;
import headfront.jetfuel.execute.impl.AmpsJetFuelExecute;
import headfront.jetfuel.execute.utils.HaClientFactory;

import java.util.Map;

/**
 * Created by Deepak on 01/02/2018.
 */
public class JetFuelExecuteClient {

    public static void main(String[] args) {
        new JetFuelExecuteClient();
    }

    public JetFuelExecuteClient() {
        try {

            // Create amps connection
            final HAClient haClient = new HaClientFactory().createHaClient("SampleJetFuelClient",
                    "tcp://192.168.56.101:8001/amps/json", false);
            ObjectMapper jsonMapper = new ObjectMapper();
            System.out.println("Connected to Amps");

            //Create JetFuelExecute
            JetFuelExecute jetFuelExecute = new AmpsJetFuelExecute(haClient, jsonMapper);
            jetFuelExecute.initialise();

            // calling function

            final String id1 = jetFuelExecute.executeFunction("SampleJetFuelSever.CheckAbilityToVote",
                    new Object[]{true, 22}, new ClientFunctionResponseListener());
            System.out.println("Called function with id  " + id1);


        } catch (Exception e) {
            System.out.println("Unable to create JetFuelExecute Client");
            e.printStackTrace();
        }

    }

    static class ClientFunctionResponseListener implements FunctionResponseListener {
        @Override
        public void onCompleted(String id, Map<String, Object> map, Object message, Object returnValue) {
            String replyFrom = "";
            if (map.size() > 0) {
                replyFrom = (String) map.get(JetFuelExecuteConstants.MSG_CREATION_NAME);
            }
            System.out.println("Got onCompleted from " + replyFrom + " for id '" +
                    id + "' with message '" + message + "' and returnValue '" + returnValue + "'");
        }

        @Override
        public void onError(String id, Map<String, Object> map, Object message, Object exception) {
            String replyFrom = "";
            if (map.size() > 0) {
                replyFrom = (String) map.get(JetFuelExecuteConstants.MSG_CREATION_NAME);
            }
            System.out.println("Got onCompleted from " + replyFrom + " for id '" + id + "' with message '" + message + "' and exception '" + exception + "'");
        }
    }
}
