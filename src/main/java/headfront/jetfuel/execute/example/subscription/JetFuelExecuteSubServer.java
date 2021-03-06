package headfront.jetfuel.execute.example.subscription;

import com.crankuptheamps.client.HAClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import headfront.jetfuel.execute.FunctionAccessType;
import headfront.jetfuel.execute.FunctionExecutionType;
import headfront.jetfuel.execute.FunctionState;
import headfront.jetfuel.execute.JetFuelExecute;
import headfront.jetfuel.execute.functions.*;
import headfront.jetfuel.execute.impl.AmpsJetFuelExecute;
import headfront.jetfuel.execute.utils.HaClientFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Deepak on 01/02/2018.
 */
public class JetFuelExecuteSubServer {

    public static void main(String[] args) {
        new JetFuelExecuteSubServer();
    }

    public JetFuelExecuteSubServer() {
        try {

            // Create amps connection
            final HAClient haClient = new HaClientFactory().createHaClient("SampleJetFuelSubSever",
                    "tcp://192.168.56.101:8001/amps/json", false);
            ObjectMapper jsonMapper = new ObjectMapper();
            System.out.println("Connected to Amps");

            //Create JetFuelExecute
            JetFuelExecute jetFuelExecute = new AmpsJetFuelExecute(haClient, jsonMapper);
            jetFuelExecute.initialise();

            //Create JetFuelExecute Function

            // First Create Parameters
            FunctionParameter instrumentParameter = new FunctionParameter("instrument", String.class,
                    "The instrument you want prices for");
            List<FunctionParameter> functionParameters = new ArrayList<>();
            functionParameters.add(instrumentParameter);

            // Then create the function
            JetFuelFunction get5PricesTicks = new JetFuelFunction("Get5PricesTicks",
                    "This function will give you the next 5 price ticks for the given Instrument. The update type will be a string of json",
                    functionParameters,
                    Double.class, "Return The current price of the instrument",
                    new PriceCreatorExecutor(), FunctionAccessType.Read, FunctionExecutionType.Subscription);

            System.out.println("Publishing Function");
            // publish JetFuel Function on the bus
            jetFuelExecute.publishFunction(get5PricesTicks);

            System.out.println("Published Function " + get5PricesTicks.getFullFunctionName() + " Now waiting for client calls");
            //now wait for Clients to call

        } catch (Exception e) {
            System.out.println("Unable to create JetFuelServer");
            e.printStackTrace();
        }
    }

    static class PriceCreatorExecutor extends AbstractFunctionExecutor {

        @Override
        protected SubscriptionExecutor executeSubscriptionFunction(String id, List<Object> parameters,
                                                                   Map<String, Object> requestParameters,
                                                                   SubscriptionFunctionResponseListener result) {
            String inst = parameters.get(0).toString();
            PriceSubscriptionExecutor subExecutor = new PriceSubscriptionExecutor(id, result);
            subExecutor.start();
            result.onSubscriptionStateChanged(id, "Subscription  for " + inst + " is valid", FunctionState.SubActive);
            return subExecutor;
        }
    }
}
