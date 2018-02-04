package headfront.jetfuel.execute.utils;

import com.crankuptheamps.client.ConnectionInfo;
import com.crankuptheamps.client.DefaultServerChooser;
import com.crankuptheamps.client.HAClient;
import com.crankuptheamps.client.exception.ConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Random;

/**
 * Created by Deepak on 09/04/2017.
 */
public class HaClientFactory {

    private static final Logger LOG = LoggerFactory.getLogger(HaClientFactory.class);

    public HAClient createHaClient(String connectionName, String connectionUrl, boolean addSuffixToConnName) throws Exception {
        DefaultServerChooser serverChooser = new ServerChooser();
        final String[] urls = connectionUrl.split(",");
        for (int i = 0; i< urls.length; i++) {
            serverChooser.add(urls[i]);
        }
        if (addSuffixToConnName) {
            Random random = new Random(System.currentTimeMillis());
            int rand = random.nextInt(10000);
            connectionName = connectionName + "_" + rand;
        }

        HAClient haClient = new HAClient(connectionName);
        haClient.setServerChooser(serverChooser);
        haClient.setHeartbeat(5);
//        haClient.setFailedWriteHandler(message -> LOG.error("Message from LastChanceHandler " + message.toString()));
        haClient.setExceptionListener(e -> LOG.error("Exception thrown in the AMPS library.", e));
        haClient.setLastChanceMessageHandler(message -> LOG.error("Message from LastChanceHandler " + message.toString()));
        LOG.info("Connecting to amps");
        haClient.connectAndLogon();
        LOG.info("Connected to amps!!!!!!!!!!!!");
        return haClient;
    }

    static class ServerChooser extends DefaultServerChooser {

        @Override
        public void reportFailure(Exception exception, ConnectionInfo info) throws Exception {
            super.reportFailure(exception, info);
            LOG.error("Disconnected from amps  " + info.toString(), exception);
        }

        @Override
        public void reportSuccess(ConnectionInfo info) {
            super.reportSuccess(info);
            LOG.info("Connected to amps " + info.toString());
        }
    }

}
