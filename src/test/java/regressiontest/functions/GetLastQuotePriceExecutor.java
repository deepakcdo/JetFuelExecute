package regressiontest.functions;

import com.crankuptheamps.client.HAClient;
import com.crankuptheamps.client.Message;
import com.fasterxml.jackson.databind.ObjectMapper;
import headfront.jetfuel.execute.functions.AbstractFunctionExecutor;
import headfront.jetfuel.execute.functions.FunctionResponseListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import regressiontest.JetFuelExecuteServerTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by Deepak on 09/05/2017.
 */
public class GetLastQuotePriceExecutor extends AbstractFunctionExecutor {

    private static Logger LOG = LoggerFactory.getLogger(JetFuelExecuteServerTest.class);

    private HAClient ampsClient;
    private ObjectMapper jsonMapper;
    private String quoteTopic;

    public GetLastQuotePriceExecutor(HAClient ampsClient, ObjectMapper jsonMapper, String QuoteTopic) {
        this.ampsClient = ampsClient;
        this.jsonMapper = jsonMapper;
        quoteTopic = QuoteTopic;
    }

    @Override
    public void executeFunction(String id, List<Object> parameters, Map<String, Object> requestParameters,
                                FunctionResponseListener result) {
        try {
            String instrument = parameters.get(0).toString();
            CountDownLatch wait = new CountDownLatch(1);
            ArrayList<String> messages = new ArrayList<>();
            ampsClient.sow(m -> {
                        final String trim = m.getData().trim();
                        if (trim.length() > 0) {
                            messages.add(trim);
                        }
                        if (m.getCommand() == Message.Command.GroupEnd) {
                            wait.countDown();
                        }

                    }, quoteTopic
                    , "/ID='" + instrument + "'", 10, 4000);
            wait.await(4, TimeUnit.SECONDS);
            if (messages.size() == 0) {
                result.onError(id, "No Quote found for inst " + instrument, true);
            } else if (messages.size() == 1) {
                result.onCompleted(id, "Quote found for inst " + instrument, messages.get(0));
            } else {
                result.onError(id, "More than one Quote found for inst " + instrument, true);
            }
        } catch (Exception e) {
            LOG.error("Unable to get quote ", e);
            result.onError(id, "Unable to get quote " + e.getMessage(), e);
        }
    }
}
